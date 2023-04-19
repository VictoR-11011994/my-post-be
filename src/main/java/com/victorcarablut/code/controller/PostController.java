package com.victorcarablut.code.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.victorcarablut.code.dto.LikeDto;
import com.victorcarablut.code.dto.PostStatusDto;
import com.victorcarablut.code.dto.UserDto;
import com.victorcarablut.code.entity.post.Like;
import com.victorcarablut.code.entity.post.Post;
import com.victorcarablut.code.entity.user.User;
import com.victorcarablut.code.exceptions.ErrorSaveDataToDatabaseException;
import com.victorcarablut.code.exceptions.GenericException;
import com.victorcarablut.code.exceptions.PostMaxLimitException;
import com.victorcarablut.code.service.PostService;
import com.victorcarablut.code.service.UserService;

//private access

@CrossOrigin(origins = "${url.fe.cross.origin}")
@RestController
@RequestMapping("/api/post")
public class PostController {

	@Autowired
	private PostService postService;


	// Custom exceptions response

	@ExceptionHandler({ GenericException.class })
	public ResponseEntity<String> handleGenericError() {
		final String message = "Error";
		return new ResponseEntity<String>(message, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ ErrorSaveDataToDatabaseException.class })
	public Map<String, Object> handleErrorSaveDataToDatabase() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 1);
		responseJSON.put("status_message", "Error save data to DB");
		return responseJSON;
	}
	
	@ExceptionHandler({ PostMaxLimitException.class })
	public Map<String, Object> handleErrorPostMaxLimit() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 11);
		responseJSON.put("status_message", "Reached maximum Posts limit per User!");
		return responseJSON;
	}


	@GetMapping("/all/{filterBy}")
	private List<Post> getAllPosts(Authentication authentication, @PathVariable("filterBy") String filter) {
		// System.out.println(filter);
		// System.out.println("role: " + authentication.getAuthorities());
		
		final String userRole = authentication.getAuthorities().toString();
		
		if(filter.equals("admin") && userRole.contains("ADMIN")) {
			// by all active and non active
			return postService.findAllPosts(true);
		} else if(filter.equals("all")) {
			// by all active post only
			return postService.findAllPosts(false);
		} else {
			// by username only
			return postService.findAllPostsOwner(filter);
		}
	
		
	}

	@PostMapping("/add")
	public ResponseEntity<String> addPost(@RequestPart(name = "data") Post post,
			@RequestPart(name = "image", required = false) MultipartFile image) {
		postService.createPost(post, image);
		return new ResponseEntity<String>("Post Created!", HttpStatus.OK);
	}

	@PutMapping("/update")
	public ResponseEntity<String> updatePost(@RequestPart(name = "post_id") String postId, @RequestPart(name = "data") Post post,
			@RequestPart(name = "image", required = false) MultipartFile image, @RequestPart(name = "image_status") String imageStatus) {
		final long postIdExtractId = Long.parseLong(postId.replaceAll("[^0-9]", ""));  
		postService.updatePost(postIdExtractId, post, image, imageStatus);
		return new ResponseEntity<String>("Post Updated!", HttpStatus.OK);
	}
	
	@PostMapping("/delete")
	public ResponseEntity<String> deletePost(@RequestBody LinkedHashMap<String, Long> data) {
		postService.deletePost(data.get("user_id"), data.get("post_id"));
		return new ResponseEntity<String>("Post Deleted!", HttpStatus.OK);
	}
	
	@PostMapping("/status")
	public ResponseEntity<String> statusPost(Authentication authentication, @RequestBody PostStatusDto postDto) {
		postService.statusPost(authentication.getName(), postDto.getUserId(), postDto.getPostId(), postDto.getStatus());
		return new ResponseEntity<String>("Post status updated!", HttpStatus.OK);
	}
	
	
	// ---------- Likes ----------
	
	@PostMapping("/like")
	public ResponseEntity<String> userLike(@RequestBody Like like) {
		postService.userLike(like);
		return new ResponseEntity<String>(HttpStatus.OK);
	}
	
	


}
