package com.victorcarablut.code.entity.account;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User implements UserDetails {

	private static final long serialVersionUID = 1L;

	// @NotBlank: must not be null and must contain at least onenon-whitespace character.
	// @JsonIgnore -> prevent to return the result visible.

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id", nullable = false)
	@NotBlank
	private Long id;

	@Column(length = 100, nullable = false)
	@NotBlank @Size(max = 100)
	private String fullName;

	@Column(unique = true, length = 100, nullable = false)
	@Email 
	@NotBlank @Size(max = 100)
	private String email;

	@Column(unique = true, length = 20, nullable = false)
	@NotBlank @Size(max = 20)
	private String username;

	@JsonIgnore
	@Column(length = 100, nullable = false)
	@NotBlank @Size(max = 100)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	@NotBlank @Size(max = 20)
	private Role role;
	
	@Column(nullable = false)
	@NotBlank
	private boolean enabled;

	@JsonIgnore
	@Column(nullable = true)
	@NotBlank @Size(max = 10)
	private Integer verificationCode;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	@NotBlank
	private Date registeredDate;
	
	
	

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role.name()));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
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
		return enabled;
	}

}
