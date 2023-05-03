package com.victorcarablut.code.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentDto {

	private Long commentId;
	private Long postId;
	private Long userId;
	private String userFullName;
	private String username;
	private byte[] userProfileImg;

	private String comment;
	
	private LocalDateTime createdDate;
	private LocalDateTime updatedDate;
}
