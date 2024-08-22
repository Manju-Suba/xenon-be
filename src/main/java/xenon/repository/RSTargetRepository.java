package xenon.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;
import xenon.model.RSTarget;

public interface RSTargetRepository extends JpaRepository<RSTarget, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE RSTarget SET rsStatus = false WHERE id <= :maxId")
    void updateRsStatusForExistingRecords(Long maxId);

    @Query("SELECT COALESCE(MAX(r.id), 0) FROM RSTarget r")
    Long findMaxId();
    
//     @Query("SELECT e FROM RSTarget e LEFT JOIN SalesHierarchy sh ON e.rsCode=sh.rsCode WHERE e.rsStatus = true AND sh.state IN (:state)")
        @Query(value = "SELECT e.* " +
        "FROM rs_target e " +
        "LEFT JOIN sales_hierarchy sh ON  sh.rs_code = e.rs_code " +
        "WHERE e.rs_status = true AND sh.state IN (:state) GROUP BY e.rs_code",
        countQuery = "SELECT COUNT(*) " +
        "FROM rs_target e " +
        "LEFT JOIN sales_hierarchy sh ON e.rs_code = sh.rs_code " +
        "WHERE e.rs_status = true AND sh.state IN (:state)", 
        nativeQuery = true)
        Page<RSTarget> findbyState(List<String> state, Pageable pageable);


    // @Query("SELECT e FROM RSTarget e LEFT JOIN SalesHierarchy sh ON e.rsCode=sh.rsCode WHERE e.rsStatus = true AND sh.state = :state")
    // List<RSTarget> ctmoFetchAllByClusterAndState(String state);

    // @Query("SELECT e FROM RSTarget e LEFT JOIN SalesHierarchy sh ON e.rsCode=sh.rsCode WHERE e.rsStatus = true")
    // List<RSTarget> ctmoFetchAll();

    @Query(value = "SELECT e.* " +
               "FROM rs_target e " +
               " JOIN sales_hierarchy sh ON  sh.rs_code = e.rs_code " +
               "WHERE e.rs_status = true GROUP BY e.rs_code",
       countQuery = "SELECT COUNT(*) " +
                    "FROM rs_target e " +
                    "LEFT JOIN sales_hierarchy sh ON e.rs_code = sh.rs_code " +
                    "WHERE e.rs_status = true", 
       nativeQuery = true)
    Page<RSTarget> ctmoFetchAll(Pageable pageable);


    // @Query("SELECT e FROM RSTarget e LEFT JOIN SalesHierarchy sh ON e.rsCode=sh.rsCode WHERE e.rsStatus = true AND sh.state IN (:state)")
    // List<RSTarget> ctmoFetchAllByCluster(List<String> state);

    @Query(value = "SELECT e.* " +
               "FROM rs_target e " +
               " JOIN sales_hierarchy sh ON  sh.rs_code = e.rs_code " +
               "WHERE e.rs_status = true AND sh.state IN (:state) GROUP BY e.rs_code",
       countQuery = "SELECT COUNT(*) " +
                    "FROM rs_target e " +
                    "LEFT JOIN sales_hierarchy sh ON e.rs_code = sh.rs_code " +
                    "WHERE e.rs_status = true AND sh.state IN (:state)",
       nativeQuery = true)
    Page<RSTarget> ctmoFetchAllByCluster(List<String> state, Pageable pageable);

    @Query(value = "SELECT e.* " +
               "FROM rs_target e " +
               " JOIN sales_hierarchy sh ON  sh.rs_code = e.rs_code " +
               "WHERE e.rs_status = true AND sh.state =:state GROUP BY e.rs_code",
       countQuery = "SELECT COUNT(*) " +
                    "FROM rs_target e " +
                    "LEFT JOIN sales_hierarchy sh ON e.rs_code = sh.rs_code " +
                    "WHERE e.rs_status = true AND sh.state =:state", 
       nativeQuery = true)
    Page<RSTarget> ctmoFetchAllByClusterAndState(String state, Pageable pageable);

}
