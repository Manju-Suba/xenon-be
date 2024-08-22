package xenon.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sales_hierarchy")
public class SalesHierarchy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 200)
    private String concatCode;

    @Column(length = 20)
    private String appType;

    @Column(length = 255)
    private String smMob;

    @Column(length = 20)
    private String smEmpCd;

    @Column(length = 100)
    private String smCode;

    @Column(length = 100)
    private String smSysName;

    @Column(length = 100)
    private String smActName;

    @Column(length = 20)
    private String smType;

    @Column(length = 20)
    private String market;

    @Column(length = 15)
    private String smMob1;

    @Column(length = 50)
    private String rsCode;

    @Column(length = 150)
    private String rsName;

    @Column(length = 20)
    private String rsType;

    @Column(length = 20)
    private String channel;

    @Column(length = 50)
    private String nm;

    @Column(length = 50)
    private String gm;

    @Column(length = 50)
    private String dgm;

    @Column(length = 150)
    private String zm;

    @Column(length = 20)
    private String zmId;

    @Column(length = 150)
    private String amName;

    @Column(length = 20)
    private String amId;

    @Column(length = 150)
    private String tsoName;

    @Column(length = 20)
    private String tsoId;

    @Column(length = 20)
    private String regCode;

    @Column(length = 30)
    private String region;

    @Column(length = 50)
    private String regDef;

    @Column(length = 10)
    private String stSapd;

    @Column(length = 80)
    private String state;

    @Column(length = 30)
    private String cCode;

    @Column(length = 80)
    private String cName;

    @Column(length = 30)
    private String dCode;

    @Column(length = 150)
    private String dName;

    @Column(length = 20)
    private String jc;

    @Column(length = 10)
    private String jcNum;

    @Column(length = 25)
    private String finYr;

    @Column(length = 15)
    private String zsmMobNum;

    @Column(length = 15)
    private String asmMobNum;

    @Column(length = 15)
    private String tsoMobNum;

    @Column(length = 150)
    private String misMailId;

    @Column(length = 150)
    private String rtmmMailId;

    @Column(length = 150)
    private String zmMailId;

    @Column(length = 150)
    private String amMailId;

    @Column(length = 150)
    private String tsoMailId;

    @Column(length = 10)
    private String status;

    @Column(length = 50)
    private String createdBy;

    @Column(length = 10)
    private Boolean shStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
