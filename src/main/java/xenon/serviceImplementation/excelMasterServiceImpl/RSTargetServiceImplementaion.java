package xenon.serviceImplementation.excelMasterServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
// import java.util.function.Consumer;

import java.util.function.BiConsumer;

import org.springframework.validation.Validator;

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
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import xenon.model.ExcelHistory;
import xenon.model.RSTarget;
import xenon.model.SalesHierarchy;
import xenon.repository.ExcelHistoryRepository;
import xenon.repository.RSTargetRepository;
import xenon.repository.UserRepository;
import xenon.response.ApiResponse;
import xenon.service.excelMasterService.RSTargetService;
import xenon.util.AuthUser;

@Service
@RequiredArgsConstructor
public class RSTargetServiceImplementaion implements RSTargetService {

    private static final Logger logger = LoggerFactory.getLogger(SalesHierarchyServiceImplementation.class);

    @Autowired
    private RSTargetRepository rsTargetRepository;

    @Autowired
    private Validator validator;

    @Value("${fileBasePath}")
    private String fileBasePath;

    @Autowired
    private ExcelHistoryRepository excelHistoryRepository;
    private final UserRepository userRepository;

    LocalDateTime currentTime = LocalDateTime.now();

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
    public ResponseEntity<ApiResponse> importRsTarget(String cluster, MultipartFile excelFile) {
        try {
            List<RSTarget> RSTarget = parseExcel(excelFile); // Implement Excel to Entity conversion logic

            if (RSTarget.isEmpty()) {
                ApiResponse response = new ApiResponse(false,
                        "All fields are mandatory. Please check and fill in all required fields.",
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

                        Integer id = excelHistoryRepository.findLastRecordIdBasedOnCluster(cluster, "rs_target");

                        ExcelHistory excelHistory = new ExcelHistory();
                        excelHistory.setExcelName(fileName);
                        excelHistory.setExcelType("rs_target");
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
                        excelHistory.setExcelType("rs_target");
                        excelHistory.setUploaderName(AuthUser.getRole());
                        excelHistory.setCreatedat(LocalDateTime.now());
                        excelHistory.setUpdatedat(LocalDateTime.now());
                        excelHistoryRepository.save(excelHistory);
                    }

                }

                rsTargetRepository.saveAll(RSTarget);
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

    public List<RSTarget> parseExcel(MultipartFile excelFile) throws IOException, DuplicateRecordsException {
        List<RSTarget> rsData = new ArrayList<>();
        Set<String> uniqueRows = new HashSet<>();
        // Set<String> existingRsCodes = rsTargetRepository.findAll().stream()
        //         .map(RSTarget::getRsCode)
        //         .collect(Collectors.toSet());

        List<String> duplicateRecords = new ArrayList<>();

        try (InputStream inputStream = excelFile.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            Map<String, BiConsumer<RSTarget, String>> columnSetterMap = createColumnSetterMap();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                // Skip header row
                if (row.getRowNum() == 0 || isEmptyRow(row)) {
                    continue;
                }

                RSTarget rsTarget = new RSTarget();
                boolean isDuplicateRow = false;

                for (Cell cell : row) {
                    if (cell == null || cell.getCellType() == CellType.BLANK) {
                        continue;
                    }
                    String columnName = getColumnName(cell);
                    if (columnName != null && columnSetterMap.containsKey(columnName)) {
                        switch (cell.getCellType()) {
                            case STRING:
                                columnSetterMap.get(columnName).accept(rsTarget, cell.getStringCellValue());
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    columnSetterMap.get(columnName).accept(rsTarget,
                                            cell.getLocalDateTimeCellValue().toString());
                                } else {
                                    double numericValue = cell.getNumericCellValue();
                                    if (columnName.contains("Count") || columnName.contains("ID")) {
                                        // Assume fields containing "Count" or "ID" are integers
                                        columnSetterMap.get(columnName).accept(rsTarget,
                                                String.valueOf((int) numericValue));
                                    } else {
                                        // For other numeric values, use Double
                                        columnSetterMap.get(columnName).accept(rsTarget, String.valueOf(numericValue));
                                    }
                                }
                                break;
                            default:
                                // Handle other cell types if needed
                                break;
                        }
                    }
                }

                // Check if row is duplicate in the Excel file
                String uniqueKey = rsTarget.getRsCode();
                if (uniqueRows.contains(uniqueKey) ) {
                    isDuplicateRow = true;
                    duplicateRecords.add("Duplicate row found with key: " + uniqueKey + " at row " + row.getRowNum());
                } else {
                    uniqueRows.add(uniqueKey);
                }

                if (!isDuplicateRow) {
                    rsTarget.setRsStatus(true);
                    rsTarget.setCreatedBy(AuthUser.getRole());
                    rsTarget.setCreatedAt(currentTime);
                    rsTarget.setUpdatedAt(currentTime);

                    if (isValidrsTarget(rsTarget)) {

                        Long maxId = rsTargetRepository.findMaxId();
                        if (maxId > 0) {
                            rsTargetRepository.updateRsStatusForExistingRecords(maxId);
                        }
                        rsData.add(rsTarget);
                    }
                }
            }

            // Save all RSTarget instances to the database
            // rsTargetRepository.saveAll(rsData);

            if (!duplicateRecords.isEmpty()) {
                throw new DuplicateRecordsException(String.join("\n", duplicateRecords));
            }

        } catch (IOException e) {
            throw new IOException("Error processing the Excel file", e);
        }

        return rsData;
    }

    private Map<String, BiConsumer<RSTarget, String>> createColumnSetterMap() {
        Map<String, BiConsumer<RSTarget, String>> columnSetterMap = new HashMap<>();
        columnSetterMap.put("RS Code", (rsTarget, value) -> rsTarget.setRsCode(value));
        columnSetterMap.put("RS Name", (rsTarget, value) -> rsTarget.setRsName(value));
        columnSetterMap.put("Distributor Against Salesman Budget Count",
                (rsTarget, value) -> rsTarget.setDistributorAgainstSalesmanBudgetCount(Integer.valueOf(value)));
        columnSetterMap.put("Mkt Type", (rsTarget, value) -> rsTarget.setMktType(value));
        columnSetterMap.put("RBM Name", (rsTarget, value) -> rsTarget.setRbmName(value));
        columnSetterMap.put("ASM Name", (rsTarget, value) -> rsTarget.setAsmName(value));
        columnSetterMap.put("ASE Name", (rsTarget, value) -> rsTarget.setAseName(value));
        columnSetterMap.put("ASM EMP ID", (rsTarget, value) -> rsTarget.setAsmEmpId(value));
        columnSetterMap.put("SDE Name", (rsTarget, value) -> rsTarget.setSdeName(value));
        columnSetterMap.put("SDE EMP ID", (rsTarget, value) -> rsTarget.setSdeEmpId(value));
        columnSetterMap.put("TSI Name", (rsTarget, value) -> rsTarget.setTsiName(value));
        columnSetterMap.put("TSI EMP ID", (rsTarget, value) -> rsTarget.setTsiEmpId(value));
        columnSetterMap.put("SEC FMCG", (rsTarget, value) -> rsTarget.setSecondaryTgtFMCG(Double.valueOf(value)));
        columnSetterMap.put("SEC PC", (rsTarget, value) -> rsTarget.setSecondaryTgtPC(Double.valueOf(value)));
        columnSetterMap.put("SEC AB", (rsTarget, value) -> rsTarget.setSecondaryTgtAB(Double.valueOf(value)));
        columnSetterMap.put("SEC FD & SN", (rsTarget, value) -> rsTarget.setSecondaryTgtFDSN(Double.valueOf(value)));
        columnSetterMap.put("UBO FMCG", (rsTarget, value) -> rsTarget.setUboFMCG(Double.valueOf(value)));
        columnSetterMap.put("UBO PC", (rsTarget, value) -> rsTarget.setUboPC(Double.valueOf(value)));
        columnSetterMap.put("UBO AB", (rsTarget, value) -> rsTarget.setUboAB(Double.valueOf(value)));
        columnSetterMap.put("UBO FD & SN", (rsTarget, value) -> rsTarget.setUboFDSN(Double.valueOf(value)));
        columnSetterMap.put("Focus Brand Name", (rsTarget, value) -> rsTarget.setFocusBrandName(value));
        columnSetterMap.put("Focus Brand UBO Tgt",
                (rsTarget, value) -> rsTarget.setFocusBrandUBOTgt(Double.valueOf(value)));

        return columnSetterMap;
    }

    private boolean isValidrsTarget(RSTarget rsTarget) {
        // Perform your validation logic here
        return rsTarget.getRsCode() != null
                && rsTarget.getRsName() != null
                && rsTarget.getDistributorAgainstSalesmanBudgetCount() != null
                && rsTarget.getMktType() != null
                && rsTarget.getRbmName() != null
                && rsTarget.getAsmName() != null
                && rsTarget.getAsmEmpId() != null
                && rsTarget.getAseName() != null
                && rsTarget.getSdeName() != null
                && rsTarget.getSdeEmpId() != null
                && rsTarget.getTsiName() != null
                && rsTarget.getTsiEmpId() != null
                && rsTarget.getSecondaryTgtFMCG() != null
                && rsTarget.getSecondaryTgtPC() != null
                && rsTarget.getSecondaryTgtAB() != null
                && rsTarget.getSecondaryTgtFDSN() != null
                && rsTarget.getUboFMCG() != null
                && rsTarget.getUboPC() != null
                && rsTarget.getUboAB() != null
                && rsTarget.getUboFDSN() != null
                && rsTarget.getFocusBrandName() != null
                && rsTarget.getFocusBrandUBOTgt() != null;
    }

    public class DuplicateRecordsException extends Exception {
        public DuplicateRecordsException(String message) {
            super(message);
        }
    }

    @Override
    public List<RSTarget> fetchAll(int page, int size) {
        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);

        String stateString = AuthUser.getState();
        List<String> states = Arrays.asList(stateString.split(","));
        Page<RSTarget> record = rsTargetRepository.findbyState(states, pageable);
        List<RSTarget> content = record.getContent();
        return content;
    }

    @Override
    public List<RSTarget> ctmoFetchAll(int page, int size, String cluster, String state) {
        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);

        if (cluster != null && !cluster.equals("undefined")) {

            if (state != null && !state.equals("undefined")) {
                Page<RSTarget> record = rsTargetRepository.ctmoFetchAllByClusterAndState(state, pageable);
                List<RSTarget> content = record.getContent();
                return content;
            } else {
                String stateString = userRepository.findByRole(cluster);
                List<String> states = Arrays.asList(stateString.split(","));
                Page<RSTarget> record = rsTargetRepository.ctmoFetchAllByCluster(states, pageable);
                List<RSTarget> content = record.getContent();
                return content;
            }
        }
        Page<RSTarget> record =  rsTargetRepository.ctmoFetchAll(pageable);
        List<RSTarget> content = record.getContent();
        return content;
    }
}
