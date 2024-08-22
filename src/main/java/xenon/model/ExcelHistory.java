package xenon.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "excel_history")
public class ExcelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 200)
    private String excelName;

    @Column(length = 70)
    private String excelType;

    @Column(length = 70)
    private String uploaderName;

    @Column(length = 10)
    private Integer excelHistoryId;

    private LocalDateTime createdat;
    private LocalDateTime updatedat;

}
