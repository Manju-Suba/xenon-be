package xenon.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "reset_password")
public class ResetPassword {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String email;

    private LocalDateTime time;

    @Column(length = 200)
    private String url;

    @Column(length = 20)
    private String activeStatus;

    @Column(length = 20)
    private String urlStatus;

    private Date createdAt;
    private Date updatedAt;
}
