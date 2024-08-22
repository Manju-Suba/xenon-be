package xenon.service.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import xenon.model.User;

public class UserDetailsImpl implements UserDetails {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String email;
	private String name;
	private String region;
	private String state;
	private String rbm;
	@JsonIgnore
	private String role;
	private String password;
	private Collection<? extends GrantedAuthority> authorities;

	public UserDetailsImpl(Integer id, String email, String name, String region, String state, String rbm, String role,
			String password, Collection<? extends GrantedAuthority> authorities) {
		

		this.id = id;
		this.email = email;
		this.name = name;
		this.region = region;
		this.state = state;
		this.rbm = rbm;
		this.role = role;
		this.password = password;
		this.authorities = authorities;
	}

	public static UserDetails build(User user) {
		List<GrantedAuthority> authorities = Arrays.stream(user.getRole().split(","))
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
		return new UserDetailsImpl(
				user.getId(),
				user.getEmail(),
				user.getName(),
				user.getRegion(),
				user.getState(),
				user.getRbm(),
				user.getRole(),
				user.getPassword(), authorities);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public Integer getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	
	public String getName() {
		return name;
	}

	public String getRegion() {
		return region;
	}

	public String getRbm() {
		return rbm;
	}

	public String getState() {
		return state;
	}

	public String getRole() {
		return role;
	}


	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	

	// public void setId(Integer id) {
	// 	this.id = id;
	// }

	

	// public void setEmail(String email) {
	// 	this.email = email;
	// }


	// public void setName(String name) {
	// 	this.name = name;
	// }

	
	// public void setRegion(String region) {
	// 	this.region = region;
	// }

	

	// public void setRbm(String rbm) {
	// 	this.rbm = rbm;
	// }

	

	// public void setState(String state) {
	// 	this.state = state;
	// }

	
	// public void setRole(String role) {
	// 	this.role = role;
	// }

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserDetailsImpl user = (UserDetailsImpl) o;
		return Objects.equals(id, user.id);
	}

}