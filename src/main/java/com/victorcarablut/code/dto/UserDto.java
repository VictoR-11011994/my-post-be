package com.victorcarablut.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto {

	private String fullName;
	private String username;
	private String email;
	private String password;
		
}
