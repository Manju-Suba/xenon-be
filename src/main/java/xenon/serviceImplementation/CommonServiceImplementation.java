package xenon.serviceImplementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
// import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import xenon.dto.ExcelHistoryDTO;
import xenon.model.ExcelHistory;
import xenon.repository.ExcelHistoryRepository;
import xenon.service.CommonService;
import java.util.*;


@Service
public class CommonServiceImplementation implements CommonService{

    @Autowired
    private ExcelHistoryRepository excelHistoryRepository;

        // return excelHistoryRepository.findAll();

    @Override
    public List<ExcelHistoryDTO> fetchAll(int page, int size) {
        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);

        Page<Object[]> records = excelHistoryRepository.findAllWithCTMO(pageable);
        
        List<ExcelHistoryDTO> result = new ArrayList<>();
        for (Object[] record : records) {
            ExcelHistory eh = (ExcelHistory) record[0];
            String cehExcelName = (String) record[1];
    
            ExcelHistoryDTO dto = new ExcelHistoryDTO();
            dto.setId(eh.getId());
            dto.setExcelType(eh.getExcelType());
            dto.setClusterName(eh.getUploaderName());
            dto.setClusterExcel(eh.getExcelName());
            dto.setCtmoExcel(cehExcelName); // This is the value from the second element in the Object array
    
            dto.setCurrentExcelDate(eh.getCreatedat());
            result.add(dto);
        }
        return result;
    }
    


    // @Override
    // public List<ExcelHistoryDTO> fetchAll() {
    //     List<ExcelHistory> records = excelHistoryRepository.findLastTwoRecordsForEachUploaderAndType();
    //     Map<String, List<ExcelHistory>> groupedRecords = new HashMap<>();

    //     for (ExcelHistory record : records) {
    //         String key = record.getUploaderName() + "_" + record.getExcelType();
    //         groupedRecords.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
    //     }

    //     List<ExcelHistoryDTO> result = new ArrayList<>();
    //     for (List<ExcelHistory> recordList : groupedRecords.values()) {
    //         if (recordList.size() == 2) {
    //             ExcelHistoryDTO dto = new ExcelHistoryDTO();
    //             dto.setId(recordList.get(0).getId());
    //             dto.setCurrentExcel(recordList.get(0).getExcelName());
    //             dto.setExistsExcel(recordList.get(1).getExcelName());
    //             dto.setExcelType(recordList.get(0).getExcelType());
    //             dto.setUploaderName(recordList.get(0).getUploaderName());
    //             dto.setCurrentExcelDate(recordList.get(0).getCreatedat());
    //             dto.setExistsExcelDate(recordList.get(1).getCreatedat());
    //             result.add(dto);
    //         }
    //     }
    //     return result;
    // }

    @Override
    public List<ExcelHistory> ctmoClusterWiseDownload(String cluster, String excelType) {
        return excelHistoryRepository.findLatestClusterAndExcelTypeRecord(cluster, excelType);
    }


}
