package com.victorcarablut.code.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.victorcarablut.code.entity.post.Post;
import com.victorcarablut.code.entity.user.User;
import com.victorcarablut.code.repository.PostRepository;

@Service
public class PostService {
	
	@Autowired
	private PostRepository postRepository;
	
	public List<Post> findAllPosts() {
		return postRepository.findAll();
	}
	
	public void createPost(Post post) {
		post.setCreatedDate(LocalDateTime.now());
		postRepository.save(post);
	}

}
