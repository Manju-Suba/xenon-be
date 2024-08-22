package xenon.serviceImplementation.excelMasterServiceImpl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import xenon.model.ExcelHistory;
import xenon.model.SalesHierarchy;
import xenon.repository.ExcelHistoryRepository;
import xenon.repository.SalesHierarchyRepository;
import xenon.repository.UserRepository;
import xenon.response.ApiResponse;
import xenon.service.excelMasterService.SalesHierarchyService;
import xenon.util.AuthUser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class SalesHierarchyServiceImplementation implements SalesHierarchyService {

    private static final Logger logger = LoggerFactory.getLogger(SalesHierarchyServiceImplementation.class);
    private final UserRepository userRepository;
    @Value("${fileBasePath}")
    private String fileBasePath;

    LocalDateTime currentTime = LocalDateTime.now();

    @Autowired
    private SalesHierarchyRepository salesHierarchyRepository;

    @Autowired
    private Validator validator;

    @Autowired
    private ExcelHistoryRepository excelHistoryRepository;

    // common function for excel upload
    private String getColumnName(Cell cell) {
        int columnIndex = cell.getColumnIndex();
        Row headerRow = cell.getSheet().getRow(0);
        return headerRow.getCell(columnIndex).getStringCellValue();
    }

    private boolean isEmptyRow(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ResponseEntity<ApiResponse> importShExcel(String cluster, MultipartFile excelFile) throws IOException {

        try {
            List<SalesHierarchy> salesHierarchies = parseExcel(excelFile); // Implement Excel to Entity conversion logic
            if (salesHierarchies.isEmpty()) {
                ApiResponse response = new ApiResponse(false, "Some fields are missing, Please check!",
                        Collections.emptyList());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            } else {

                if (excelFile != null && !excelFile.isEmpty()) {
                    Path directoryPath = Paths.get(fileBasePath);
                    if (!Files.exists(directoryPath)) {
                        Files.createDirectories(directoryPath);
                    }

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    String formattedTime = currentTime.format(formatter);
                    String fileName = formattedTime + "_" + excelFile.getOriginalFilename();
                    Path path = Paths.get(fileBasePath + fileName);
                    Files.copy(excelFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                    if((AuthUser.getRole()).equals("CTMO")){

                        Integer id = excelHistoryRepository.findLastRecordIdBasedOnCluster(cluster, "sales_hierarchy");

                        ExcelHistory excelHistory = new ExcelHistory();
                        excelHistory.setExcelName(fileName);
                        excelHistory.setExcelType("sales_hierarchy");
                        excelHistory.setUploaderName(AuthUser.getRole());
                        excelHistory.setExcelHistoryId(id);
                        excelHistory.setCreatedat(LocalDateTime.now());
                        excelHistory.setUpdatedat(LocalDateTime.now());
                        // excelHistoryRepository.save(excelHistory);
                        ExcelHistory savedExcelHistory = excelHistoryRepository.save(excelHistory);
                        Integer lastInsertedId = savedExcelHistory.getId();

                        excelHistoryRepository.updateCTMORecordId(id ,lastInsertedId );

                    }else{
                        ExcelHistory excelHistory = new ExcelHistory();
                        excelHistory.setExcelName(fileName);
                        excelHistory.setExcelType("sales_hierarchy");
                        excelHistory.setUploaderName(AuthUser.getRole());
                        excelHistory.setCreatedat(LocalDateTime.now());
                        excelHistory.setUpdatedat(LocalDateTime.now());
                        excelHistoryRepository.save(excelHistory);
                    }

                }

                salesHierarchyRepository.saveAll(salesHierarchies);
            }
            ApiResponse response = new ApiResponse(true, "Excel Uploaded Successfully", Collections.emptyList());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (DuplicateRecordsException e) {
            ApiResponse response = new ApiResponse(false, "Duplicate records, Please check!", Collections.emptyList());
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        } catch (IOException e) {
            logger.error("Failed to import data from Excel", e);
            ApiResponse response = new ApiResponse(false, "Failed to import data from Excel", Collections.emptyList());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<SalesHierarchy> parseExcel(MultipartFile excelFile) throws IOException, DuplicateRecordsException {
        List<SalesHierarchy> shData = new ArrayList<>();
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Set<String> uniqueRows = new HashSet<>();
        List<String> duplicateRecords = new ArrayList<>();

        try (InputStream inputStream = excelFile.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                // Skip header row
                if (row.getRowNum() == 0 || isEmptyRow(row)) {
                    continue;
                }

                boolean isDuplicateRow = false;

                SalesHierarchy salesHierarchy = new SalesHierarchy();
                Map<String, Consumer<String>> columnSetterMap = new HashMap<>();
                columnSetterMap.put("ConcatCode", salesHierarchy::setConcatCode);
                columnSetterMap.put("AppType", salesHierarchy::setAppType);
                columnSetterMap.put("SMMob", salesHierarchy::setSmMob);

                columnSetterMap.put("SMEmpCd", salesHierarchy::setSmEmpCd);
                columnSetterMap.put("SMCode", salesHierarchy::setSmCode);
                columnSetterMap.put("SMSysName", salesHierarchy::setSmSysName);
                columnSetterMap.put("SMActName", salesHierarchy::setSmActName);
                columnSetterMap.put("SMTyp", salesHierarchy::setSmType);
                columnSetterMap.put("Market", salesHierarchy::setMarket);
                columnSetterMap.put("SMMob1", salesHierarchy::setSmMob1);
                columnSetterMap.put("RSCode", salesHierarchy::setRsCode);
                columnSetterMap.put("RSName", salesHierarchy::setRsName);
                columnSetterMap.put("RSTyp", salesHierarchy::setRsType);
                columnSetterMap.put("Channel", salesHierarchy::setChannel);
                columnSetterMap.put("NM", salesHierarchy::setNm);
                columnSetterMap.put("GM", salesHierarchy::setGm);
                columnSetterMap.put("DGM", salesHierarchy::setDgm);
                columnSetterMap.put("ZM", salesHierarchy::setZm);
                columnSetterMap.put("ZM ID", salesHierarchy::setZmId);
                columnSetterMap.put("AMName", salesHierarchy::setAmName);
                columnSetterMap.put("AM ID", salesHierarchy::setAmId);
                columnSetterMap.put("TSOName", salesHierarchy::setTsoName);
                columnSetterMap.put("TSO ID", salesHierarchy::setTsoId);
                columnSetterMap.put("RegCode", salesHierarchy::setRegCode);
                columnSetterMap.put("Region", salesHierarchy::setRegion);
                columnSetterMap.put("RegDef", salesHierarchy::setRegDef);
                columnSetterMap.put("StSAPd", salesHierarchy::setStSapd);
                columnSetterMap.put("State", salesHierarchy::setState);
                columnSetterMap.put("CCode", salesHierarchy::setCCode);
                columnSetterMap.put("CName", salesHierarchy::setCName);
                columnSetterMap.put("DCode", salesHierarchy::setDCode);
                columnSetterMap.put("DName", salesHierarchy::setDName);
                columnSetterMap.put("JC", salesHierarchy::setJc);
                columnSetterMap.put("JCNum", salesHierarchy::setJcNum);
                columnSetterMap.put("FinYr", salesHierarchy::setFinYr);
                columnSetterMap.put("ZSM_Mob_Num", salesHierarchy::setZsmMobNum);
                columnSetterMap.put("ASM_Mob_num", salesHierarchy::setAsmMobNum);
                columnSetterMap.put("TSO_Mob_Num", salesHierarchy::setTsoMobNum);
                columnSetterMap.put("MIS_Mail_ID", salesHierarchy::setMisMailId);
                columnSetterMap.put("RTMM_Mail_ID", salesHierarchy::setRtmmMailId);
                columnSetterMap.put("ZM_Mail_ID", salesHierarchy::setZmMailId);
                columnSetterMap.put("AM_Mail_ID", salesHierarchy::setAmMailId);
                columnSetterMap.put("TSO_Mail_ID", salesHierarchy::setTsoMailId);
                columnSetterMap.put("Status - Active / Inactive", salesHierarchy::setStatus);

                for (Cell cell : row) {
                    if (cell == null || cell.getCellType() == CellType.BLANK) {
                        continue;
                    }
                    String columnName = getColumnName(cell);
                    if (columnName != null && columnSetterMap.containsKey(columnName)) {
                        // if ("jc_period".equals(columnName) || "employee_code".equals(columnName)
                        // || "target_amount".equals(columnName)) {
                        switch (cell.getCellType()) {
                            case STRING:
                                columnSetterMap.get(columnName).accept(cell.getStringCellValue());
                                break;
                            case NUMERIC:
                                if (columnName.equals("SMMob") || columnName.equals("SMMob1")
                                        || columnName.equals("TSO_Mob_Num") || columnName.equals("ASM_Mob_num")
                                        || columnName.equals("ZSM_Mob_Num")) {
                                    long numericValue = (long) cell.getNumericCellValue();
                                    columnSetterMap.get(columnName).accept(String.valueOf(numericValue));
                                } else {
                                    double numericValue = cell.getNumericCellValue();
                                    int intValue2 = (int) numericValue;
                                    columnSetterMap.get(columnName).accept(String.valueOf(intValue2));
                                }

                                break;
                            default:
                                logger.warn("Unsupported cell type for column: {}", columnName);
                        }
                        // }

                    }
                }

                // Check if row is duplicate in the Excel file
                String uniqueKey = salesHierarchy.getRsCode();
                if (uniqueRows.contains(uniqueKey) ) {
                    isDuplicateRow = true;
                    duplicateRecords.add("Duplicate row found with key: " + uniqueKey + " at row " + row.getRowNum());
                } else {
                    uniqueRows.add(uniqueKey);
                }

                if (!isDuplicateRow) {
                    salesHierarchy.setShStatus(true);
                    salesHierarchy.setCreatedBy(AuthUser.getRole());
                    salesHierarchy.setCreatedAt(currentTime); // Set current time
                    salesHierarchy.setUpdatedAt(currentTime); // Set current time
    
                    // Validate the entity before adding to the list
                    if (isValidSalesHierarchy(salesHierarchy)) {
    
                        Long maxId = salesHierarchyRepository.findMaxId();
                        if (maxId > 0) {
                            salesHierarchyRepository.updateShStatusForExistingRecords(maxId);
                        }
    
                        shData.add(salesHierarchy);
                    }
                }
            }

            if (!duplicateRecords.isEmpty()) {
                throw new DuplicateRecordsException(String.join("\n", duplicateRecords));
            }

        } catch (IOException e) {
            throw new IOException("Error processing the Excel file", e);
        }

        return shData;
    }

    private boolean isValidSalesHierarchy(SalesHierarchy salesHierarchy) {
        // Perform your validation logic here
        return salesHierarchy.getConcatCode() != null
                && salesHierarchy.getSmMob() != null
                && salesHierarchy.getSmSysName() != null
                && salesHierarchy.getSmType() != null
                && salesHierarchy.getRsCode() != null;
    }

    public class DuplicateRecordsException extends Exception {
        public DuplicateRecordsException(String message) {
            super(message);
        }
    }

    @Override
    public List<SalesHierarchy> fetchAll(int page, int size) {
        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);

        String stateString = AuthUser.getState();
        List<String> states = Arrays.asList(stateString.split(","));
        Page<SalesHierarchy> records = salesHierarchyRepository.findAllByState(states, pageable);
        List<SalesHierarchy> content = records.getContent();
        return content;
    }

    @Override
    public List<SalesHierarchy> ctmoFetchAll(int page, int size, String cluster, String state) {
        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);

        if (cluster != null && !cluster.equals("undefined")) {

            if (state != null && !state.equals("undefined")) {
                Page<SalesHierarchy> records = salesHierarchyRepository.ctmoFetchAllByClusterAndState(state, pageable);
                List<SalesHierarchy> content = records.getContent();
                return content;

            } else {
                String stateString = userRepository.findByRole(cluster);
                List<String> states = Arrays.asList(stateString.split(","));
                Page<SalesHierarchy> records = salesHierarchyRepository.ctmoFetchAllByCluster(states, pageable);
                List<SalesHierarchy> content = records.getContent();
                return content;

            }
        }

        Page<SalesHierarchy> records = salesHierarchyRepository.ctmoFetchAll(pageable);
        List<SalesHierarchy> content = records.getContent();  // Extracts the content from the Page object

        // Convert the List to JSON (assuming you use Jackson for JSON serialization)
        // ObjectMapper objectMapper = new ObjectMapper();
        // String jsonContent = objectMapper.writeValueAsString(content);

        return content;
    }

  
}
