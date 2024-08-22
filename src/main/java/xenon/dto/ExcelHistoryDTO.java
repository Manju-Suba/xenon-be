package xenon.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelHistoryDTO {

    private Integer id;
    private String excelType;
    private String clusterName;
    private String clusterExcel;
    private String ctmoExcel;
    private LocalDateTime currentExcelDate;
}
