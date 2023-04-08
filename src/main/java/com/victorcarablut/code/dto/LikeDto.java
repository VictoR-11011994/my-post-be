package com.victorcarablut.code.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LikeDto {

	//@JsonIgnore
	private Long postId;
	
	private Long userId;
	private String userFullName;


}
