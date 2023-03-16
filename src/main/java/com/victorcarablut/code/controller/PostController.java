package com.victorcarablut.code.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.victorcarablut.code.entity.post.Post;
import com.victorcarablut.code.service.PostService;

//private access

@CrossOrigin(origins = "${url.fe.cross.origin}")
@RestController
@RequestMapping("/api/post")
public class PostController {
	
	@Autowired
	private PostService postService;
	
	@GetMapping("/all")
	private List<Post> getAllPosts() {
		return postService.findAllPosts();
	}
	
	@PostMapping("/add")
	public ResponseEntity<String> addPost(@RequestBody Post post) {
		postService.createPost(post);
		return new ResponseEntity<String>("Post Created!", HttpStatus.OK);
	}

}
