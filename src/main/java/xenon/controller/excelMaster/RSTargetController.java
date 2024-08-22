package xenon.controller.excelMaster;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import xenon.model.RSTarget;
import xenon.response.ApiResponse;
import xenon.service.excelMasterService.RSTargetService;
import xenon.util.AuthUser;

@CrossOrigin("*")
@RestController
@RequestMapping("/rs-target")
@RequiredArgsConstructor
public class RSTargetController {

    @Autowired
    private RSTargetService rsTargetService;
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";
    private static final String DATA_FETCHED_SUCCESSFULLY = "Data Fetched Successfully";
    private static final String CLUSTER_NOT_NULL_OR_EMPTY_MESSAGE = "Cluster must not be null or empty";
    private static final String NO_DATA_FOUND_MESSAGE = "No Data Found";

    @PostMapping("/excel-upload")
    public ResponseEntity<ApiResponse> RsTargetImport(@RequestParam(required = false) String cluster, @RequestParam("file") MultipartFile file) throws IOException {
        if ((AuthUser.getRole()).equals("CTMO") && (cluster == null || cluster.isEmpty())) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, CLUSTER_NOT_NULL_OR_EMPTY_MESSAGE, Collections.emptyList()));
        }
        return rsTargetService.importRsTarget(cluster, file);
    }

    @GetMapping("/fetch-all")
    public ResponseEntity<ApiResponse> fetchAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            List<RSTarget> rsTarget = rsTargetService.fetchAll(page, size);
            if (rsTarget == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, NO_DATA_FOUND_MESSAGE, Collections.emptyList()));
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, DATA_FETCHED_SUCCESSFULLY, rsTarget));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/ctmo-fetch-all")
    public ResponseEntity<ApiResponse> ctmoFetchAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String cluster,@RequestParam(required = false) String state) {
        try {
            List<RSTarget> rsTarget = rsTargetService.ctmoFetchAll(page, size, cluster,state);
            if (rsTarget == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, NO_DATA_FOUND_MESSAGE, Collections.emptyList()));
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, DATA_FETCHED_SUCCESSFULLY, rsTarget));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

}
