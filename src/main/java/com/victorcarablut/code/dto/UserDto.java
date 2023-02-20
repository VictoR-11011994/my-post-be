package com.victorcarablut.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserDto {

	private String fullName;
	private String username;
	
	@Email @NotBlank
	private String email;
	
	@NotBlank
	private String password;
	
	public UserDto() {
		
	}
	
	public UserDto(String fullName, String username, String email, String password) {

		this.fullName = fullName;
		this.username = username;
		this.email = email;
		this.password = password;
	}


	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
	
}
