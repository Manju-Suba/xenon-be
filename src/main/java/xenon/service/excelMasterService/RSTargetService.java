package xenon.service.excelMasterService;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import xenon.model.RSTarget;
import xenon.response.ApiResponse;

public interface RSTargetService {

    public ResponseEntity<ApiResponse> importRsTarget(String cluster, MultipartFile file) throws IOException;

    public List<RSTarget> fetchAll(int page, int size);

    public List<RSTarget> ctmoFetchAll(int page, int size, String cluster, String state);
}
