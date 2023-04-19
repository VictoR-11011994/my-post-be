package com.victorcarablut.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostStatusDto {
	
	private Long userId;
	private Long postId;
	private String status;

}
