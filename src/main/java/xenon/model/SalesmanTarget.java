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
@Table(name = "salesman_target")
public class SalesmanTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 200)
    private String cluster;

    @Column(length = 150)
    private String zmName;

    @Column(length = 50)
    private String zmEmpId;

    @Column(length = 150)
    private String amName;

    @Column(length = 50)
    private String asmEmpId;

    @Column(length = 150)
    private String aseName;

    @Column(length = 50)
    private String aseEmpId;

    @Column(length = 150)
    private String tsoName;

    @Column(length = 50)
    private String tsoEmpId;

    @Column(length = 150)
    private String tsiName;

    @Column(length = 50)
    private String tsiEmpId;

    @Column(length = 15)
    private String tsiMobileNumber;

    @Column(length = 150)
    private String regCode;

    @Column(length = 30)
    private String region;

    @Column(length = 150)
    private String zme;

    @Column(length = 150)
    private String smName;

    @Column(length = 50)
    private String empId;

    @Column(length = 60)
    private String smType;

    @Column(length = 255)
    private String smMobile;

    @Column(length = 50)
    private String newRtrTgt;

    @Column(length = 50)
    private String uboTgt;

    @Column(length = 150)
    private String pcUbo;

    @Column(length = 150)
    private String abUbo;

    @Column(length = 150)
    private String fsUbo;

    @Column(length = 50)
    private String billCutsTgtJc;

    @Column(length = 50)
    private String tlsdTgtJc;

    @Column(length = 50)
    private String pcValue;

    @Column(length = 50)
    private String abValue;

    @Column(length = 50)
    private String snValue;

    @Column(length = 50)
    private String wk01;

    @Column(length = 50)
    private String wk02;

    @Column(length = 50)
    private String wk03;

    @Column(length = 50)
    private String wk04;

    @Column(length = 150)
    private String fmcg;

    @Column(length = 150)
    private String bcDaily;

    @Column(length = 150)
    private String tlsdDaily;

    @Column(length = 50)
    private String npdEcoTgt;

    @Column(length = 100)
    private String focusBrandName;

    @Column(length = 50)
    private String focusBrandUboTgt;

    @Column(length = 20)
    private String status;

    @Column(length = 15)
    private String jc;

    @Column(length = 10)
    private String jcNum;

    @Column(length = 30)
    private String finYr;

    @Column(length = 50)
    private String createdBy;

    @Column(length = 10)
    private Boolean smStatus;

    private LocalDateTime createdat;
    private LocalDateTime updatedat;

}
