package com.victorcarablut.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LikeDto {

	private Long likeId;
	private Long postId;
	private Long userId;
	private String userFullName;
	private String username;
}
