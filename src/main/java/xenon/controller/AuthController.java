package xenon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import xenon.exception.ValidationException;
import xenon.model.User;
import xenon.request.LoginRequest;
import xenon.response.ApiResponse;
import xenon.service.UserService;


@RestController
@CrossOrigin("*")
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private UserService userService;

	@GetMapping("/get-user")
	public String getUser() {
		return "Hi";
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse> loginUser(@RequestBody LoginRequest user) {
		if (user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
			throw new ValidationException("User Email and Password should not be empty");
		}
		return userService.signIn(user);
	}
	
	@PostMapping("/logout")
	public ResponseEntity<?> logoutUser() {
		return userService.logout();
	}

	
    @GetMapping("/request-send-for-forget-password")
    public ResponseEntity<User> requestSendForForgetPassword(@RequestParam String email) {
        try {
            User users = userService.getUserByMail(email);
            if (users != null) {
                return ResponseEntity.ok(users);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
