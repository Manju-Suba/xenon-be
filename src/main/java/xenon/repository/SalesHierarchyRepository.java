package xenon.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;
import xenon.model.SalesHierarchy;

public interface SalesHierarchyRepository extends JpaRepository<SalesHierarchy, Long> {

    

    @Modifying
    @Transactional
    @Query("UPDATE SalesHierarchy SET shStatus = false WHERE id <= :maxId")
    void updateShStatusForExistingRecords(Long maxId);
    
    @Query("SELECT COALESCE(MAX(s.id), 0) FROM SalesHierarchy s")
    Long findMaxId();

    // @Query("SELECT sh FROM SalesHierarchy sh  WHERE sh.shStatus = true AND sh.state IN (:state)")
    // List<SalesHierarchy> findAllByState(List<String> state);

    @Query("SELECT sh FROM SalesHierarchy sh  WHERE sh.shStatus = true AND sh.state IN (:state)")
    Page<SalesHierarchy> findAllByState(List<String> state, Pageable pageable);

    @Query("SELECT sh FROM SalesHierarchy sh  WHERE sh.shStatus = true AND sh.state =:state")
    Page<SalesHierarchy> ctmoFetchAllByClusterAndState(String state, Pageable pageable);

    @Query("SELECT sh FROM SalesHierarchy sh  WHERE sh.shStatus = true AND sh.state IN (:states)")
    Page<SalesHierarchy> ctmoFetchAllByCluster(List<String> states, Pageable pageable);

    @Query("SELECT sh FROM SalesHierarchy sh WHERE sh.shStatus = true")
    Page<SalesHierarchy> ctmoFetchAll(Pageable pageable);
}
