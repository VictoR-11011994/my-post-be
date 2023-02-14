package com.victorcarablut.code.entity.user;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

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
	// @JsonIgnore: prevent to return the result visible.

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100, nullable = false)
	@NotBlank @Size(max = 100)
	private String fullName;

	@Column(unique = true, length = 100, nullable = false)
	@Email 
	@NotBlank @Size(max = 100)
	private String email;

	@Column(unique = true, length = 20)
	@Size(max = 20)
	private String username;

	@JsonIgnore
	@JsonProperty(access = Access.WRITE_ONLY)
	@Column(length = 100, nullable = false)
	@NotBlank @Size(max = 100)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Role role;
	
	private boolean enabled;

	@JsonIgnore
	private Integer verificationCode;

	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime registeredDate;
	
	
	@JsonIgnore
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

	@JsonIgnore
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@JsonIgnore
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@JsonIgnore
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
