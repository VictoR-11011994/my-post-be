package com.victorcarablut.code.entity.user;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users_blocked")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserBlocked {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;
	private String fullName;
	private String username;
	private String email;
	
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime registeredDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime blockedDate;

}
