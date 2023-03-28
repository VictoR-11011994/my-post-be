package com.victorcarablut.code.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.victorcarablut.code.dto.UserDto;
import com.victorcarablut.code.entity.post.Post;
import com.victorcarablut.code.entity.user.User;
import com.victorcarablut.code.service.PostService;
import com.victorcarablut.code.service.UserService;

//private access

@CrossOrigin(origins = "${url.fe.cross.origin}")
@RestController
@RequestMapping("/api/post")
public class PostController {
	
	@Autowired
	private PostService postService;
	
	@Autowired
	private UserService userService;
	
	@GetMapping("/all")
	private List<Post> getAllPosts() {
		return postService.findAllPosts();
	}
	
	@PostMapping("/add")
	public ResponseEntity<String> addPost(@RequestPart(name = "data") Post post, @RequestPart(name = "image", required = false) MultipartFile image) {
		postService.createPost(post, image);
		return new ResponseEntity<String>("Post Created!", HttpStatus.OK);
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
	
	@PostMapping("/upload/image")
	public ResponseEntity<String> uploadImage(@RequestParam("user_id") Long userId, @RequestParam("post_id") Long postId, @RequestParam("image") MultipartFile file) {
		try {
			postService.uploadImg(userId, postId, file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity<String>("Image Uploaded!", HttpStatus.OK);
	}

}
