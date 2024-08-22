package xenon.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;//cluster
	@Column(length = 50)
	private String email;
	private String password;
	private String role;
	@Column(length = 50)
	private String rbm;
	private String misName;
	private String misEmail;
	private String region;
	private String state;
	@Column(length = 50)
	private boolean status;
	private LocalDateTime createddate;

}
