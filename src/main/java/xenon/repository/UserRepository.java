package xenon.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import xenon.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u.role FROM User u WHERE u.role != 'CTMO'")
    List<String> findAllName();

    @Query("SELECT u.state FROM User u WHERE u.role =:name")
    String findNameByState(String name);

    @Query("SELECT u.state FROM User u WHERE u.role =:cluster")
    String findByRole(String cluster);
}
