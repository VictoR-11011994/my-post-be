package com.victorcarablut.code.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.victorcarablut.code.entity.post.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	Post findPostById(Long id);

	@Query(value = "SELECT posts.* FROM posts WHERE posts.user_id = :userId ORDER BY posts.id DESC", nativeQuery = true)
	List<Post> findAllByOrderByUserIdDesc(Long userId);
	

}
