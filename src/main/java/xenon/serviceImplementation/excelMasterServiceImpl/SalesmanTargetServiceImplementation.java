package xenon.serviceImplementation.excelMasterServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import xenon.dto.SalesTargetDTO;
import xenon.model.ExcelHistory;
import xenon.model.RSTarget;
import xenon.model.SalesHierarchy;
import xenon.model.SalesmanTarget;
import xenon.repository.ExcelHistoryRepository;
import xenon.repository.SalesmanTargetRepository;
import xenon.repository.UserRepository;
import xenon.response.ApiResponse;
import xenon.service.excelMasterService.SalesmanTargetService;
import xenon.util.AuthUser;

@Service
@RequiredArgsConstructor
public class SalesmanTargetServiceImplementation implements SalesmanTargetService {

    private static final Logger logger = LoggerFactory.getLogger(SalesHierarchyServiceImplementation.class);

    @Autowired
    private SalesmanTargetRepository salesmanTargetRepository;

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
    public ResponseEntity<ApiResponse> importSalesmanTarget(String cluster, MultipartFile excelFile) {
        try {
            List<SalesmanTarget> salesmanTarget = parseExcel(excelFile); // Implement Excel to Entity conversion logic
            if (salesmanTarget.isEmpty()) {
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

                        Integer id = excelHistoryRepository.findLastRecordIdBasedOnCluster(cluster, "salesman_target");

                        ExcelHistory excelHistory = new ExcelHistory();
                        excelHistory.setExcelName(fileName);
                        excelHistory.setExcelType("salesman_target");
                        excelHistory.setUploaderName(AuthUser.getRole());
                        excelHistory.setExcelHistoryId(id);
                        excelHistory.setCreatedat(LocalDateTime.now());
                        excelHistory.setUpdatedat(LocalDateTime.now());
                        ExcelHistory savedExcelHistory = excelHistoryRepository.save(excelHistory);
                        Integer lastInsertedId = savedExcelHistory.getId();

                        excelHistoryRepository.updateCTMORecordId(id ,lastInsertedId );
                    }else{
                        ExcelHistory excelHistory = new ExcelHistory();
                        excelHistory.setExcelName(fileName);
                        excelHistory.setExcelType("salesman_target");
                        excelHistory.setUploaderName(AuthUser.getRole());
                        excelHistory.setCreatedat(LocalDateTime.now());
                        excelHistory.setUpdatedat(LocalDateTime.now());
                        excelHistoryRepository.save(excelHistory);
                    }
        
                }

                salesmanTargetRepository.saveAll(salesmanTarget);
            }
            ApiResponse response = new ApiResponse(true, "Excel Uploaded Successfully", Collections.emptyList());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Failed to import data from Excel", e);
            ApiResponse response = new ApiResponse(false, "Failed to import data from Excel", Collections.emptyList());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<SalesmanTarget> parseExcel(MultipartFile excelFile) throws IOException {
        List<SalesmanTarget> shData = new ArrayList<>();

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
                SalesmanTarget salesmanTarget = new SalesmanTarget();
                Map<String, Consumer<String>> columnSetterMap = new HashMap<>();
                columnSetterMap.put("CLUSTER", salesmanTarget::setCluster);
                columnSetterMap.put("ZM NAME", salesmanTarget::setZmName);
                columnSetterMap.put("ZM Emp ID", salesmanTarget::setZmEmpId);

                columnSetterMap.put("AMName", salesmanTarget::setAmName);
                columnSetterMap.put("ASM Emp ID", salesmanTarget::setAsmEmpId);
                columnSetterMap.put("ASE NAME", salesmanTarget::setAseName);
                columnSetterMap.put("ASE Emp ID", salesmanTarget::setAseEmpId);
                columnSetterMap.put("TSOName", salesmanTarget::setTsoName);
                columnSetterMap.put("TSO Emp ID", salesmanTarget::setTsoEmpId);
                columnSetterMap.put("TSI Name", salesmanTarget::setTsiName);
                columnSetterMap.put("TSI Emp ID", salesmanTarget::setTsiEmpId);
                columnSetterMap.put("TSI Mobil Number", salesmanTarget::setTsiMobileNumber);
                columnSetterMap.put("RegCode", salesmanTarget::setRegCode);
                columnSetterMap.put("Region", salesmanTarget::setRegion);
                columnSetterMap.put("ZME", salesmanTarget::setZme);
                columnSetterMap.put("SM_NAME", salesmanTarget::setSmName);
                columnSetterMap.put("EMP_ID", salesmanTarget::setEmpId);
                columnSetterMap.put("SM_Type", salesmanTarget::setSmType);
                columnSetterMap.put("SM_MOBILE", salesmanTarget::setSmMobile);
                columnSetterMap.put("New_Rtr_Tgt", salesmanTarget::setNewRtrTgt);
                columnSetterMap.put("UBO Tgt", salesmanTarget::setUboTgt);
                columnSetterMap.put("PC UBO", salesmanTarget::setPcUbo);
                columnSetterMap.put("AB UBO", salesmanTarget::setAbUbo);
                columnSetterMap.put("F&S UBO", salesmanTarget::setFsUbo);
                columnSetterMap.put("BILL CUTS_TGT_JC", salesmanTarget::setBillCutsTgtJc);
                columnSetterMap.put("TLSD_TGT_JC", salesmanTarget::setTlsdTgtJc);
                columnSetterMap.put("PC_Value", salesmanTarget::setPcValue);
                columnSetterMap.put("AB_Value", salesmanTarget::setAbValue);
                columnSetterMap.put("SN_Value", salesmanTarget::setSnValue);
                columnSetterMap.put("WK01", salesmanTarget::setWk01);
                columnSetterMap.put("WK02", salesmanTarget::setWk02);
                columnSetterMap.put("WK03", salesmanTarget::setWk03);
                columnSetterMap.put("WK04", salesmanTarget::setWk04);
                columnSetterMap.put("FMCG", salesmanTarget::setFmcg);
                columnSetterMap.put("BC_Daily", salesmanTarget::setBcDaily);
                columnSetterMap.put("TLSD_Daily", salesmanTarget::setTlsdDaily);
                columnSetterMap.put("NPD_ECO_Tgt", salesmanTarget::setNpdEcoTgt);
                columnSetterMap.put("Focus Brand Name", salesmanTarget::setFocusBrandName);
                columnSetterMap.put("Focus Brand UBO Tgt", salesmanTarget::setFocusBrandUboTgt);
                columnSetterMap.put("Status | Active / Inactive", salesmanTarget::setStatus);
                columnSetterMap.put("JC", salesmanTarget::setJc);
                columnSetterMap.put("JCNum", salesmanTarget::setJcNum);
                columnSetterMap.put("FinYr", salesmanTarget::setFinYr);

                for (Cell cell : row) {
                    if (cell == null || cell.getCellType() == CellType.BLANK) {
                        continue;
                    }
                    String columnName = getColumnName(cell);
                    if (columnName != null && columnSetterMap.containsKey(columnName)) {
                        switch (cell.getCellType()) {
                            case STRING:
                                columnSetterMap.get(columnName).accept(cell.getStringCellValue());
                                break;
                            case NUMERIC:
                                if (columnName.equals("SM_MOBILE")) {
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

                    }
                }
                salesmanTarget.setSmStatus(true);
                salesmanTarget.setCreatedBy(AuthUser.getRole());
                salesmanTarget.setCreatedat(currentTime);
                salesmanTarget.setUpdatedat(currentTime);

                // Validate the entity before adding to the list
                if (isValidSalesmanTarget(salesmanTarget)) {

                    Long maxId = salesmanTargetRepository.findMaxId();
                    if (maxId > 0) {
                        salesmanTargetRepository.updateSmStatusForExistingRecords(maxId);
                    }
                    shData.add(salesmanTarget);
                }
            }
        }
        return shData;
    }

    private boolean isValidSalesmanTarget(SalesmanTarget salesmanTarget) {
        // Perform your validation logic here
        return salesmanTarget.getSmMobile() != null
                && salesmanTarget.getSmType() != null
                && salesmanTarget.getSmName() != null;
    }


    // @Override
    // public List<SalesmanTarget> fetchAll() {
    //     System.out.println(AuthUser.getState());
    //     return salesmanTargetRepository.findbyState(AuthUser.getState());
    // }

    @Override
    public List<SalesmanTarget> fetchAll(int page, int size) {
        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);

        String stateString = AuthUser.getState();
        List<String> states = Arrays.asList(stateString.split(","));
        Page<SalesmanTarget> record = salesmanTargetRepository.ctmoFetchAllByCluster(states, pageable);
        List<SalesmanTarget> content = record.getContent();
        return content;
    }

    @Override
    public List<SalesmanTarget> ctmoFetchAll(int page, int size, String cluster, String state) {
        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);

        if (cluster != null && !cluster.equals("undefined")) {

            if (state != null && !state.equals("undefined")) {
                Page<SalesmanTarget> record = salesmanTargetRepository.ctmoFetchAllByClusterAndState(state, pageable);
                List<SalesmanTarget> content = record.getContent();
                return content;
            } else {
                String stateString = userRepository.findByRole(cluster);
                List<String> states = Arrays.asList(stateString.split(","));
                Page<SalesmanTarget> record = salesmanTargetRepository.ctmoFetchAllByCluster(states, pageable);
                List<SalesmanTarget> content = record.getContent();
                return content;
            }
        }
        Page<SalesmanTarget> record = salesmanTargetRepository.ctmoFetchAll(pageable);
        List<SalesmanTarget> content = record.getContent();
        return content;
    }

    @Override
    public void SalesTargetSampleDownload(HttpServletResponse response) throws IOException {
        List<SalesTargetDTO> salesmanTarget = salesmanTargetRepository.JoinFetchAll();

        // Create a new Workbook
        Workbook workbook = new XSSFWorkbook();

        // Create a sheet
        Sheet sheet = workbook.createSheet("Sales Target Data");

        // Create a header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "CLUSTER", "ZM NAME", "ZM Emp ID", "AMName", "ASM Emp ID", "ASE NAME", "ASE Emp ID", "TSOName",
                "TSO Emp ID", "TSI Name", "TSI Emp ID", "TSI Mobil Number", "RegCode", "Region", "ZME", "SM_NAME",
                "EMP_ID", "SM_Type", "SM_MOBILE", "New_Rtr_Tgt", "UBO Tgt", "PC UBO", "AB UBO", "F&S UBO",
                "BILL CUTS_TGT_JC", "TLSD_TGT_JC", "PC_Value", "AB_Value", "SN_Value", "WK01", "WK02", "WK03",
                "WK04", "FMCG", "BC_Daily", "TLSD_Daily", "NPD_ECO_Tgt", "Focus Brand Name", "Focus Brand UBO Tgt",
                "Status | Active / Inactive", "JC", "JCNum", "FinYr"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Populate the sheet with data
        int rowNum = 1;
        for (SalesTargetDTO target : salesmanTarget) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(target.getSaleTarget().getCluster());
            row.createCell(1).setCellValue(target.getSalesHierarchy().getZm());
            row.createCell(2).setCellValue(target.getSalesHierarchy().getZmId());
            row.createCell(3).setCellValue(target.getSalesHierarchy().getAmName());
            row.createCell(4).setCellValue(target.getSaleTarget().getAsmEmpId());
            row.createCell(5).setCellValue(target.getSaleTarget().getAseName());
            row.createCell(6).setCellValue(target.getSaleTarget().getAseEmpId());
            row.createCell(7).setCellValue(target.getSalesHierarchy().getTsoName());
            row.createCell(8).setCellValue(target.getSalesHierarchy().getTsoId());
            row.createCell(9).setCellValue(target.getSaleTarget().getTsiName());
            row.createCell(10).setCellValue(target.getSaleTarget().getTsiEmpId());
            row.createCell(11).setCellValue(target.getSaleTarget().getTsiMobileNumber());
            row.createCell(12).setCellValue(target.getSaleTarget().getRegCode());
            row.createCell(13).setCellValue(target.getSaleTarget().getRegion());
            row.createCell(14).setCellValue(target.getSaleTarget().getZme());
            row.createCell(15).setCellValue(target.getSaleTarget().getSmName());
            row.createCell(16).setCellValue(target.getSaleTarget().getEmpId());
            row.createCell(17).setCellValue(target.getSaleTarget().getSmType());
            row.createCell(18).setCellValue(target.getSaleTarget().getSmMobile());
            row.createCell(19).setCellValue(target.getSaleTarget().getNewRtrTgt());
            row.createCell(20).setCellValue(target.getSaleTarget().getUboTgt());
            row.createCell(21).setCellValue(target.getSaleTarget().getPcUbo());
            row.createCell(22).setCellValue(target.getSaleTarget().getAbUbo());
            row.createCell(23).setCellValue(target.getSaleTarget().getFsUbo());
            row.createCell(24).setCellValue(target.getSaleTarget().getBillCutsTgtJc());
            row.createCell(25).setCellValue(target.getSaleTarget().getTlsdTgtJc());
            row.createCell(26).setCellValue(target.getSaleTarget().getPcValue());
            row.createCell(27).setCellValue(target.getSaleTarget().getAbValue());
            row.createCell(28).setCellValue(target.getSaleTarget().getSnValue());
            row.createCell(29).setCellValue(target.getSaleTarget().getWk01());
            row.createCell(30).setCellValue(target.getSaleTarget().getWk02());
            row.createCell(31).setCellValue(target.getSaleTarget().getWk03());
            row.createCell(32).setCellValue(target.getSaleTarget().getWk04());
            row.createCell(33).setCellValue(target.getSaleTarget().getFmcg());
            row.createCell(34).setCellValue(target.getSaleTarget().getBcDaily());
            row.createCell(35).setCellValue(target.getSaleTarget().getTlsdDaily());
            row.createCell(36).setCellValue(target.getSaleTarget().getNpdEcoTgt());
            row.createCell(37).setCellValue(target.getSaleTarget().getFocusBrandName());
            row.createCell(38).setCellValue(target.getSaleTarget().getFocusBrandUboTgt());
            row.createCell(39).setCellValue(target.getSaleTarget().getStatus());
            row.createCell(40).setCellValue(target.getSaleTarget().getJc());
            row.createCell(41).setCellValue(target.getSaleTarget().getJcNum());
            row.createCell(42).setCellValue(target.getSaleTarget().getFinYr());
        }

        // Set the content type and attachment header
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=sales_target_data.xlsx");

        // Write the data to the output stream
        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
