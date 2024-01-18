package com.victorcarablut.code.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.victorcarablut.code.dto.CommentDto;
import com.victorcarablut.code.dto.LikeDto;
import com.victorcarablut.code.dto.PostStatusDto;
import com.victorcarablut.code.entity.post.Comment;
import com.victorcarablut.code.entity.post.Like;
import com.victorcarablut.code.entity.post.Post;
import com.victorcarablut.code.entity.user.User;
import com.victorcarablut.code.exceptions.ErrorSaveDataToDatabaseException;
import com.victorcarablut.code.exceptions.GenericException;
import com.victorcarablut.code.exceptions.PostMaxLimitException;
import com.victorcarablut.code.service.PostService;

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


//	@GetMapping("/all/{filterBy}")
//	private List<Post> getAllPosts(Authentication authentication, @PathVariable("filterBy") String filter) {
//		
//		final String userRole = authentication.getAuthorities().toString();
//		
//		if(filter.equals("admin") && userRole.contains("ADMIN")) {
//			// by all active and non active (used in Admin Dashboard)
//			return postService.findAllPosts(true, null);
//		} else if(filter.equals("all")) {
//			// by all active post only (used in Home Page)
//			return postService.findAllPosts(false, null);
//		} else {
//			// by username only (used is User Profile)
//			// filter --> username
//			return postService.findAllPosts(false, filter);
//		}
//		
//	}
	
	@PostMapping("/find")
	private List<Post> getAllPosts(Authentication authentication, @RequestBody LinkedHashMap<String, String> data) {
		
		//final Long actualUserId = Long.valueOf(data.get("actualUserId"));
		
		// filter = active | admin | username (victor.carablut)
		
		final String filter = data.get("filter");
		final String currentUsernameFromToken = authentication.getName();
		
		//System.out.println(actualUsernameToken);	
		
		final String userRoleToken = authentication.getAuthorities().toString();
		
		if(filter.equals("admin") && userRoleToken.contains("ADMIN")) {
			// by all active and non active, admin can see everything (used in Admin Dashboard)
			return postService.findAllPosts(true, currentUsernameFromToken, null);
		} else if(filter.equals("active")) {
			// by all active post only (used in Home Page)
			return postService.findAllPosts(false, currentUsernameFromToken, null);
		} else {
			// by username only (used is User Profile)
			// filter can contain also --> username
			// if current username = username of profile visit page show all (active and non active)
			return postService.findAllPosts(false, currentUsernameFromToken, filter);
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
	
	@PostMapping("/like/all")
	public ArrayList<LikeDto> getAllPostLikes(@RequestBody LikeDto likeDto) {
      return postService.findAllPostLikes(likeDto.getPostId());
	}
	
	@PostMapping("/like")
	public ResponseEntity<String> postLike(@RequestBody Like like) {
		postService.postLike(like);
		return new ResponseEntity<String>(HttpStatus.OK);
	}
	
	// ---------- Comments ----------
	
	@PostMapping("/comment/all")
	public ArrayList<CommentDto> getAllPostComments(@RequestBody CommentDto commentDto) {
      return postService.findAllPostComments(commentDto.getPostId());
	}
	
	@PostMapping("/comment/add")
	public ResponseEntity<String> postAddComment(@RequestBody Comment comment) {
		postService.addComment(comment);
		return new ResponseEntity<String>("Comment Added!", HttpStatus.OK);
	}
	
	@PutMapping("/comment/update")
	public ResponseEntity<String> postUpdateComment(@RequestBody CommentDto commentDto) {
		postService.updateComment(commentDto);
		return new ResponseEntity<String>("Comment Updated!", HttpStatus.OK);
	}
	
	@PostMapping("/comment/delete")
	public ResponseEntity<String> postCommentDelete(@RequestBody CommentDto commentDto) {
		postService.deleteComment(commentDto);
		return new ResponseEntity<String>("Comment Deleted!", HttpStatus.OK);
	}
	
	


}
