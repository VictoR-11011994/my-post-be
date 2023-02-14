package com.victorcarablut.code.dto;

public class TokenDto {
	
	private String nameVar;
	private String token;


	public TokenDto(String nameVar, String token) {
		this.nameVar = nameVar;
		this.token = token;
	}
	
	public String getNameVar() {
		return nameVar;
	}

	public void setNameVar(String nameVar) {
		this.nameVar = nameVar;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	

	
}
