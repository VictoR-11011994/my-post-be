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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.victorcarablut.code.dto.LikeDto;
import com.victorcarablut.code.dto.UserDto;
import com.victorcarablut.code.entity.post.Like;
import com.victorcarablut.code.entity.post.Post;
import com.victorcarablut.code.entity.user.User;
import com.victorcarablut.code.exceptions.ErrorSaveDataToDatabaseException;
import com.victorcarablut.code.exceptions.GenericException;
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

	@GetMapping("/all")
	private List<Post> getAllPosts() {
		return postService.findAllPosts();
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
		//Long postId = 69L;
		//System.out.println(postId);
		
		final long postIdExtractId = Long.parseLong(postId.replaceAll("[^0-9]", ""));  
		postService.updatePost(postIdExtractId, post, image, imageStatus);
		return new ResponseEntity<String>("Post Updated!", HttpStatus.OK);
	}
	
	@PostMapping("/delete")
	public ResponseEntity<String> deletePost(@RequestBody LinkedHashMap<String, Long> data) {
		//System.out.println("user id: " + data.get("user_id"));
		//System.out.println("post id: " + data.get("post_id"));
		
		postService.deletePost(data.get("user_id"), data.get("post_id"));
		return new ResponseEntity<String>("Post Deleted!", HttpStatus.OK);
	}
	
	// ---------- Likes ----------

	// NOT USED
	@PutMapping("/add/like0")
	public ResponseEntity<String> addLike0(@RequestBody LinkedHashMap<String, Long> data) {
		//postService.createPost(post, image);
		
		//likeRepository.save(like);
		
		//postService.addLike(data.get("user_id"));
		
		
		return new ResponseEntity<String>("Liked!", HttpStatus.OK);
	}
	
	@PostMapping("/add/like")
	public ResponseEntity<String> addLike(@RequestBody Like like) {
		//postService.createPost(post, image);
		
		//likeRepository.save(like);
		
		postService.addLike(like);
		
		
		return new ResponseEntity<String>("Liked!", HttpStatus.OK);
	}
	
	@PostMapping("/remove/like")
	public ResponseEntity<String> removeLike(@RequestBody Like like) {
		//postService.createPost(post, image);
		
		//likeRepository.save(like);
		
		postService.removeLike(like);
		
		
		return new ResponseEntity<String>("Like Removed!", HttpStatus.OK);
	}
	

	
	// NOT USED
	@PostMapping("/find/likes")
	private List<LikeDto> getAllPostLikes(@RequestBody LikeDto likeDto) {
		return postService.findAllPostLikes(likeDto);
	}
	
	@GetMapping("/find/likes/test")
	public List<Post> findTest() {
		return postService.findTest();
	}

//	@PostMapping("/add")
//	public ResponseEntity<String> addPost(@RequestParam("user_id") Long userId, @RequestParam("post_title") String postTitle, @RequestParam("post_description") String postDescription, @RequestParam("post_image") MultipartFile file) {
//		
//		User user = new User();
//		user.setId(userId);
//		
//		Post post = new Post();
//		//post.setUser();
//		post.setTitle(postTitle);
//		post.setDescription(postDescription);
//		post.setCreatedDate(LocalDateTime.now());
//		
//		postService.createPost(post);
//		
//		try {
//			if(file.getBytes() != null) {
//				
//				//postService.uploadImg(userId, post.getId(), file);
//				
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		return new ResponseEntity<String>("Post Created!", HttpStatus.OK);
//	}

	/*
	 * @PostMapping("/upload/image") public ResponseEntity<String>
	 * uploadImage(@RequestParam("user_id") Long userId,
	 * 
	 * @RequestParam("post_id") Long postId, @RequestParam("image") MultipartFile
	 * file) { try { postService.uploadImg(userId, postId, file); } catch
	 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
	 * return new ResponseEntity<String>("Image Uploaded!", HttpStatus.OK); }
	 */

}
