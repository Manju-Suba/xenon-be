package xenon.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import xenon.dto.ExcelHistoryDTO;
import xenon.exception.ValidationException;
import xenon.model.ExcelHistory;
import xenon.model.User;
import xenon.request.LoginRequest;
import xenon.response.ApiResponse;
import xenon.service.CommonService;
import xenon.service.UserService;
import xenon.service.excelMasterService.RSTargetService;
import xenon.service.excelMasterService.SalesHierarchyService;

@CrossOrigin("*")
@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class CommonController {

    private final UserService userService;

    @Autowired
    private final CommonService commonService;

    @Autowired
    private SalesHierarchyService salesHierarchyService;

    @Autowired
    private RSTargetService rsTargetService;

    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";
    private static final String DATA_FETCHED_SUCCESSFULLY = "Data Fetched Successfully";
    private static final String ID_NOT_NULL_OR_EMPTY_MESSAGE = "Id must not be null or empty";
    private static final String PASSWORD_CHANGED_SUCCESSFULLY = "Password Changed Successfully";
    private static final String NO_DATA_FOUND_MESSAGE = "No Data Found";

    @GetMapping("/fetch-cluster")
    public ResponseEntity<ApiResponse> fetchCluster() {
        try {
            List<String> users = userService.fetchCluster();
            if (users == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, NO_DATA_FOUND_MESSAGE, Collections.emptyList()));
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, DATA_FETCHED_SUCCESSFULLY, users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/fetch-state-by-cluster")
    public ResponseEntity<ApiResponse> fetchStateByCluster(@RequestParam(required = false) String cluster) {
        try {
            List<String> users = userService.fetchStateByCluster(cluster);
            if (users == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, NO_DATA_FOUND_MESSAGE, Collections.emptyList()));
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, DATA_FETCHED_SUCCESSFULLY, users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/fetchall-excel-history")
    public ResponseEntity<ApiResponse> fetchAll(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        try {
            List<ExcelHistoryDTO> excelHistory = commonService.fetchAll(page,size);
            // List<ExcelHistory> excelHistory = commonService.fetchAll();
            
            if (excelHistory == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, NO_DATA_FOUND_MESSAGE, Collections.emptyList()));
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, DATA_FETCHED_SUCCESSFULLY, excelHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    
    @GetMapping("/ctmo-cluster-wise-download")
    public ResponseEntity<ApiResponse> ctmoClusterWiseDownload(@RequestParam(required = true) String cluster,@RequestParam(required = true) String excelType) {
        try {
            List<ExcelHistory> excelHistory = commonService.ctmoClusterWiseDownload(cluster,excelType);
            if (excelHistory == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, NO_DATA_FOUND_MESSAGE, Collections.emptyList()));
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, DATA_FETCHED_SUCCESSFULLY, excelHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        String confirmPassword = request.get("confirmPassword");

        try {
            User user = userService.changePassword(password, confirmPassword);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, NO_DATA_FOUND_MESSAGE, Collections.emptyList()));
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, PASSWORD_CHANGED_SUCCESSFULLY, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }


}
