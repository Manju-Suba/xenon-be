package xenon.service.excelMasterService;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import xenon.model.SalesmanTarget;
import xenon.response.ApiResponse;

@Service
public interface SalesmanTargetService {
    
    public ResponseEntity<ApiResponse> importSalesmanTarget(String Cluster, MultipartFile file) throws IOException;

    public List<SalesmanTarget> fetchAll(int page, int size);

    List<SalesmanTarget> ctmoFetchAll(int page, int size, String cluster, String state);
    
    public void SalesTargetSampleDownload(HttpServletResponse response) throws IOException;
}
