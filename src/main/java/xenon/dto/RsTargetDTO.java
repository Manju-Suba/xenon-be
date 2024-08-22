package xenon.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xenon.model.RSTarget;
import xenon.model.SalesHierarchy;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RsTargetDTO {
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String state;

    public RsTargetDTO(RSTarget rsTarget, String state) {
        this.id = rsTarget.getId();
        this.rsCode = rsTarget.getRsCode();
        this.rsName = rsTarget.getRsName();
        this.distributorAgainstSalesmanBudgetCount = rsTarget.getDistributorAgainstSalesmanBudgetCount();
        this.mktType = rsTarget.getMktType();
        this.rbmName = rsTarget.getRbmName();
        this.asmName = rsTarget.getAsmName();
        this.aseName = rsTarget.getAseName();
        this.asmEmpId = rsTarget.getAsmEmpId();
        this.sdeName = rsTarget.getSdeName();
        this.sdeEmpId = rsTarget.getSdeEmpId();
        this.tsiName = rsTarget.getTsiName();
        this.tsiEmpId = rsTarget.getTsiEmpId();
        this.secondaryTgtFMCG = rsTarget.getSecondaryTgtFMCG();
        this.secondaryTgtPC = rsTarget.getSecondaryTgtPC();
        this.secondaryTgtAB = rsTarget.getSecondaryTgtAB();
        this.secondaryTgtFDSN = rsTarget.getSecondaryTgtFDSN();
        this.uboFMCG = rsTarget.getUboFMCG();
        this.uboPC = rsTarget.getUboPC();
        this.uboAB = rsTarget.getUboAB();
        this.uboFDSN = rsTarget.getUboFDSN();
        this.focusBrandName = rsTarget.getFocusBrandName();
        this.focusBrandUBOTgt = rsTarget.getFocusBrandUBOTgt();
        this.createdAt = rsTarget.getCreatedAt();
        this.updatedAt = rsTarget.getUpdatedAt();
        this.state = state; // Initialize state from SalesHierarchy
    }
}
