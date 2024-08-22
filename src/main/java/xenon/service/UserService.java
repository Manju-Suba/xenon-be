package xenon.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import xenon.model.ExcelHistory;
import xenon.model.User;
import xenon.request.LoginRequest;
import xenon.response.ApiResponse;

@Service
public interface UserService {
    ResponseEntity<ApiResponse> signIn(LoginRequest user);

    ResponseEntity<?> logout();

    List<String> fetchCluster();

    List<String> fetchStateByCluster(String name);
    
    User getUserByMail(String email) throws Exception;

    User changePassword(String password, String confirmPassword);

}
