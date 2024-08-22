package xenon.service.excelMasterService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import xenon.model.SalesHierarchy;
import xenon.response.ApiResponse;

import java.io.IOException;
import java.util.List;

@Service
public interface SalesHierarchyService {

    public ResponseEntity<ApiResponse> importShExcel(String cluster, MultipartFile file) throws IOException;

    public List<SalesHierarchy> fetchAll(int page, int size);

    List<SalesHierarchy> ctmoFetchAll(int page, int size, String cluster, String state);
}
