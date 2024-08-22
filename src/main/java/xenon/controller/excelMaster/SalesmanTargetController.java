package xenon.controller.excelMaster;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import xenon.model.RSTarget;
import xenon.model.SalesHierarchy;
import xenon.model.SalesmanTarget;
import xenon.response.ApiResponse;
import xenon.service.excelMasterService.SalesmanTargetService;
import xenon.util.AuthUser;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@CrossOrigin("*")
@RestController
@RequestMapping("/salesman-target")
public class SalesmanTargetController {

    @Autowired
    private SalesmanTargetService salesmanTargetService;
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";
    private static final String DATA_FETCHED_SUCCESSFULLY = "Data Fetched Successfully";
    private static final String CLUSTER_NOT_NULL_OR_EMPTY_MESSAGE = "Cluster must not be null or empty";
    private static final String NO_DATA_FOUND_MESSAGE = "No Data Found";


    @PostMapping("/excel-upload")
    public ResponseEntity<ApiResponse> salesmanTargetImport(@RequestParam(required = false) String cluster, @RequestParam("file") MultipartFile file) throws IOException {
        if ((AuthUser.getRole()).equals("CTMO") && (cluster == null || cluster.isEmpty())) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, CLUSTER_NOT_NULL_OR_EMPTY_MESSAGE, Collections.emptyList()));
        }
        return salesmanTargetService.importSalesmanTarget(cluster, file);
    }


    @GetMapping("/fetch-all")
    public ResponseEntity<ApiResponse> fetchAll(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        try {
            List<SalesmanTarget> salesmanTarget = salesmanTargetService.fetchAll(page, size);
            if (salesmanTarget == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, NO_DATA_FOUND_MESSAGE, Collections.emptyList()));
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, DATA_FETCHED_SUCCESSFULLY, salesmanTarget));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }
    @GetMapping("/ctmo-fetch-all")
    public ResponseEntity<ApiResponse> ctmoFetchAll(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size,@RequestParam(required = false) String cluster,
            @RequestParam(required = false) String state) {
        try {
            List<SalesmanTarget> salesmanTarget = salesmanTargetService.ctmoFetchAll(page, size, cluster, state);
            if (salesmanTarget == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, NO_DATA_FOUND_MESSAGE, Collections.emptyList()));
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, DATA_FETCHED_SUCCESSFULLY, salesmanTarget));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, INTERNAL_SERVER_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/download/sales-target")
    public void downloadSalesTarget(HttpServletResponse response) throws IOException {
        salesmanTargetService.SalesTargetSampleDownload(response);
    }
    
}
