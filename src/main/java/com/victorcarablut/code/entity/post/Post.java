package com.victorcarablut.code.entity.post;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.victorcarablut.code.dto.LikeDto;
import com.victorcarablut.code.entity.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Post {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(length = 100, nullable = false)
	@NotBlank
	@Size(max = 100)
	private String title;
	
	@Column(length = 500)
	@Size(max = 500)
	private String description;
	
	@Column(columnDefinition = "MEDIUMBLOB") // (max: 16 mb)
	private byte[] image;
	
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime updatedDate;
	
	@ManyToOne()
	@JoinColumn(name = "user_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;
	
	@JsonInclude()
	@Transient
	private List<LikeDto> likes = new ArrayList<>();
	
	@JsonInclude()
	@Transient
	private Integer maxPostsLimit = 3; // max. posts creation per User
	
	
	private String status;

}
