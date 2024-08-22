package xenon.model;

import java.sql.Date;
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
@Table(name = "rs_target")
public class RSTarget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rs_code")
    private String rsCode;

    @Column(name = "rs_name")
    private String rsName;

    @Column(name = "distributor_against_salesman_budget_count")
    private Integer distributorAgainstSalesmanBudgetCount;

    @Column(name = "mkt_type")
    private String mktType;

    @Column(name = "rbm_name")
    private String rbmName;

    @Column(name = "asm_name")
    private String asmName;

    @Column(name = "ase_name")
    private String aseName;

    @Column(name = "asm_emp_id")
    private String asmEmpId;

    @Column(name = "sde_name")
    private String sdeName;

    @Column(name = "sde_emp_id")
    private String sdeEmpId;

    @Column(name = "tsi_name")
    private String tsiName;

    @Column(name = "tsi_emp_id")
    private String tsiEmpId;

    @Column(name = "secondary_tgt_fmcg")
    private Double secondaryTgtFMCG;

    @Column(name = "secondary_tgt_pc")
    private Double secondaryTgtPC;

    @Column(name = "secondary_tgt_ab")
    private Double secondaryTgtAB;

    @Column(name = "secondary_tgt_fd_sn")
    private Double secondaryTgtFDSN;

    @Column(name = "ubo_fmcg")
    private Double uboFMCG;

    @Column(name = "ubo_pc")
    private Double uboPC;

    @Column(name = "ubo_ab")
    private Double uboAB;

    @Column(name = "ubo_fd_sn")
    private Double uboFDSN;

    @Column(name = "focus_brand_name")
    private String focusBrandName;

    @Column(name = "focus_brand_ubo_tgt")
    private Double focusBrandUBOTgt;

    @Column(length = 50)
    private String createdBy;

    @Column(length = 10)
    private Boolean rsStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
