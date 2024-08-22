package xenon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xenon.model.ResetPassword;

public interface ResetPasswordRepository extends JpaRepository<ResetPassword, Long> {
}