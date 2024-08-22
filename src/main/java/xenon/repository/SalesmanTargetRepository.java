package xenon.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import xenon.dto.SalesTargetDTO;
import xenon.model.SalesmanTarget;

public interface SalesmanTargetRepository extends JpaRepository<SalesmanTarget, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE SalesmanTarget SET smStatus = false WHERE id <= :maxId")
    void updateSmStatusForExistingRecords(Long maxId);
    
    @Query("SELECT COALESCE(MAX(s.id), 0) FROM SalesmanTarget s")
    Long findMaxId();

    // @Query("SELECT st FROM SalesmanTarget st LEFT JOIN SalesHierarchy sh ON
    // st.smMobile=sh.smMob WHERE sh.state IN (:state)")
    // List<SalesmanTarget> findbyState(String state);

    // @Query("SELECT st FROM SalesmanTarget st LEFT JOIN SalesHierarchy sh ON st.smMobile=sh.smMob WHERE st.smStatus = true AND sh.state IN (:states)")
    // List<SalesmanTarget> findbyState(@Param("states") List<String> states);

    // @Query("SELECT st FROM SalesmanTarget st LEFT JOIN SalesHierarchy sh ON st.smMobile=sh.smMob WHERE st.smStatus = true")
    // List<SalesmanTarget> ctmoFetchAll();

    @Query(value = "SELECT st.* " +
               "FROM salesman_target st " +
               " JOIN sales_hierarchy sh ON  sh.sm_mob = st.sm_mobile " +
               "WHERE st.sm_status = true GROUP BY st.sm_mobile",
       countQuery = "SELECT COUNT(*) " +
                    "FROM salesman_target st " +
                    "LEFT JOIN sales_hierarchy sh ON st.sm_mobile = sh.sm_mob " +
                    "WHERE st.sm_status = true", 
       nativeQuery = true)
    Page<SalesmanTarget> ctmoFetchAll(Pageable pageable);

    @Query(value = "SELECT st.* " +
               "FROM salesman_target st " +
               "LEFT JOIN sales_hierarchy sh ON  sh.sm_mob = st.sm_mobile " +
               "WHERE st.sm_status = true AND sh.state IN (:states) GROUP BY st.sm_mobile",
       countQuery = "SELECT COUNT(*) " +
                    "FROM salesman_target st " +
                    "LEFT JOIN sales_hierarchy sh ON st.sm_mobile = sh.sm_mob " +
                    "WHERE st.sm_status = true AND sh.state IN (:states) ", 
       nativeQuery = true)
    Page<SalesmanTarget> ctmoFetchAllByCluster(List<String> states, Pageable pageable);

    @Query(value = "SELECT st.* " +
    "FROM salesman_target st " +
    "LEFT JOIN sales_hierarchy sh ON  sh.sm_mob = st.sm_mobile " +
    "WHERE st.sm_status = true AND sh.state =:state GROUP BY st.sm_mobile",
    countQuery = "SELECT COUNT(*) " +
         "FROM salesman_target st " +
         "LEFT JOIN sales_hierarchy sh ON st.sm_mobile = sh.sm_mob " +
         "WHERE st.sm_status = true AND sh.state =:state ", nativeQuery = true)
    Page<SalesmanTarget> ctmoFetchAllByClusterAndState(String state, Pageable pageable);


    // @Query("SELECT st FROM SalesmanTarget st LEFT JOIN SalesHierarchy sh ON st.smMobile=sh.smMob WHERE st.smStatus = true AND sh.state IN (:states)")
    // List<SalesmanTarget> ctmoFetchAllByCluster(List<String> states);

    // @Query("SELECT st FROM SalesmanTarget st LEFT JOIN SalesHierarchy sh ON st.smMobile=sh.smMob WHERE st.smStatus = true AND sh.state =:state")
    // List<SalesmanTarget> ctmoFetchAllByClusterAndState(String state);

    @Query("SELECT new xenon.dto.SalesTargetDTO(st, sh) " +
           "FROM SalesmanTarget st INNER JOIN SalesHierarchy sh ON st.smMobile = sh.smMob where st.smStatus = true and sh.shStatus = true")
    List<SalesTargetDTO> JoinFetchAll();

}
