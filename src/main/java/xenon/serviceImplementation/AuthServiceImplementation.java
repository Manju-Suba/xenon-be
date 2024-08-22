package xenon.serviceImplementation;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import xenon.jwt.JwtUtils;
import xenon.model.ResetPassword;
import xenon.model.User;
import xenon.repository.ResetPasswordRepository;
import xenon.repository.UserRepository;
import xenon.request.LoginRequest;
import xenon.response.ApiResponse;
import xenon.service.MailService;
import xenon.service.UserService;
import xenon.service.security.UserDetailsImpl;
import xenon.util.AuthUser;

@Service
@RequiredArgsConstructor
public class AuthServiceImplementation implements UserService {
        private final AuthenticationManager authenticationManager;
        private final JwtUtils jwtUtils;
        private final UserRepository userRepository;

        public static final String URL_FE = "http://localhost:3000";

        @Autowired
        private PasswordEncoder passwordEncoder;

        private final MailService mailService;

        private final ResetPasswordRepository resetPasswordRepository;

        @Autowired
        public AuthServiceImplementation(
                        AuthenticationManager authenticationManager,
                        UserRepository userRepository,
                        PasswordEncoder encoder,
                        MailService mailService,
                        ResetPasswordRepository resetPasswordRepository,
                        JwtUtils jwtUtils) {
                this.authenticationManager = authenticationManager;
                this.mailService = mailService;
                this.resetPasswordRepository = resetPasswordRepository;
                this.jwtUtils = jwtUtils;
                this.userRepository = userRepository;
        }

        @Override
        public ResponseEntity<ApiResponse> signIn(LoginRequest user) {
                try {
                        Authentication authentication = authenticationManager
                                        .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(),
                                                        user.getPassword()));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        String jwt = jwtUtils.generateTokenFromUsername(userDetails.getEmail());
                        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(jwt);

//                        System.out.println(userDetails);
                        Map<String, Object> userData = Map.of(
                                        "Token", jwt,
                                        "name", userDetails.getName(),
                                        "region", userDetails.getRegion(),
                                        "rbm", userDetails.getRbm(),
                                        "email", userDetails.getEmail(),
                                        "role", userDetails.getRole(),
                                        "state", userDetails.getState());

                        ApiResponse response = new ApiResponse(true, "Login Successfully", userData);

                        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(response);
                } catch (UsernameNotFoundException | BadCredentialsException e) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(new ApiResponse(false, "Credentials Mismatch"));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(new ApiResponse(false, "Internal Server Error", e.getMessage()));
                }
        }

        public ResponseEntity<?> logout() {
                ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
                ApiResponse response = new ApiResponse(true, "User has been logged out!", "");
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body(response);
        }

        @Override
        public List<String> fetchCluster() {
                return userRepository.findAllName();
        }

        @Override
        public List<String> fetchStateByCluster(String name) {
                String stateString = userRepository.findNameByState(name);
                List<String> states = Arrays.asList(stateString.split(","));
                return states;
        }

        @Override
        public User getUserByMail(String email) throws Exception {
                Optional<User> optionalUser = userRepository.findByEmail(email);
                if (optionalUser.isPresent()) {
                        User user = optionalUser.get();
                        Map<String, Object> content = new HashMap<>();
                        String recipientEmail = user.getEmail();
                        String resetLink = generateResetLink(user);
                        String subject = "Password Reset Link";
                        content.put("data", resetLink);
                        content.put("Name", user.getName());
                        String emailSent = mailService.sendRequestMail(content, subject, recipientEmail);
                        if ("success".equals(emailSent)) {
                                saveResetPasswordData(user, resetLink);
                        } else {
                                throw new RuntimeException("Failed to send email to " + recipientEmail);
                        }
                        return user;
                } else {
                throw new RuntimeException("User with email " + email + " not found or not active.");
                }
        }

        private String generateResetLink(User user) throws Exception {
                // SecretKey secretKey = CustomEncryption.generateAESKey(Integer.parseInt(KEY));
                // byte[] encryptedBytes = CustomEncryption.encrypt(user.getEmail(), secretKey);
                String encodedEncryptedBytes = Base64.getEncoder().encodeToString(user.getEmail().getBytes());
                String strongEncodedEncryptedBytes = Base64.getEncoder().encodeToString(encodedEncryptedBytes.getBytes());
                return URL_FE + "/#/reset-password/" + strongEncodedEncryptedBytes;
        }


        public void saveResetPasswordData(User user, String link) {
                ResetPassword resetPassword = new ResetPassword();
                resetPassword.setTime(LocalDateTime.now());
                resetPassword.setUrl(link);
                resetPassword.setEmail(user.getEmail());
                resetPassword.setActiveStatus("Active");
                Date currentdate = new Date();
                resetPassword.setCreatedAt(currentdate);
                resetPassword.setUpdatedAt(currentdate);
                resetPasswordRepository.save(resetPassword);
        }

        public User changePassword(String password, String confirmPassword) {
                String email = AuthUser.getEmail();
                Optional<User> optionalUser = userRepository.findByEmail(email);

                if (optionalUser.isPresent() && password.equals(confirmPassword)) {
                        User user = optionalUser.get();

                        // Hash the password
                        String encodedPassword = passwordEncoder.encode(password);
                        user.setPassword(encodedPassword);  // Update password with hashed password

                        userRepository.save(user);   // Save updated user

                        return user;
                } else {
                        throw new IllegalArgumentException("Password & Confirm Password do not match");
                }
        }

}
