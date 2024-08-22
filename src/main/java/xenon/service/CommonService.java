package xenon.service;

import java.util.List;
import org.springframework.stereotype.Service;

import xenon.dto.ExcelHistoryDTO;
import xenon.model.ExcelHistory;

@Service
public interface CommonService {

    public List<ExcelHistoryDTO> fetchAll(int page, int size);
    // public List<ExcelHistory> fetchAll();

    public List<ExcelHistory> ctmoClusterWiseDownload(String cluster, String excelType);

}
