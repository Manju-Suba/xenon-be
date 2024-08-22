package xenon.repository;

import xenon.model.ExcelHistory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface ExcelHistoryRepository extends JpaRepository<ExcelHistory, Long> {

    @Query(value = "WITH RankedRecords AS (" +
            "   SELECT *, " +
            "   ROW_NUMBER() OVER (PARTITION BY uploader_name, excel_type ORDER BY createdat DESC) AS rn " +
            "   FROM excel_history" +
            ") " +
            "SELECT * FROM RankedRecords WHERE rn <= 2",
    nativeQuery = true)
    List<ExcelHistory> findLastTwoRecordsForEachUploaderAndType();


    @Query("SELECT eh FROM ExcelHistory eh WHERE eh.uploaderName = :cluster AND eh.excelType = :excelType ORDER BY eh.id DESC LIMIT 1")
    List<ExcelHistory> findLatestClusterAndExcelTypeRecord(String cluster, String excelType);

    @Query("SELECT eh.id FROM ExcelHistory eh WHERE eh.uploaderName = :cluster AND eh.excelType = :excelType ORDER BY eh.id DESC LIMIT 1")
    Integer findLastRecordIdBasedOnCluster(String cluster, String excelType);

    @Modifying
    @Transactional
    @Query("UPDATE ExcelHistory SET excelHistoryId =:ctmoId WHERE id =:id")
    void updateCTMORecordId(Integer id , Integer ctmoId);

    @Query("SELECT eh, ceh.excelName FROM ExcelHistory eh LEFT JOIN ExcelHistory ceh ON eh.excelHistoryId = ceh.id WHERE eh.uploaderName != 'CTMO'")
    Page<Object[]> findAllWithCTMO( Pageable pageable);
    


}
