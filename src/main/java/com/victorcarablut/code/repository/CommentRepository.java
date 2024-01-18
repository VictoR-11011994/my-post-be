package com.victorcarablut.code.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.victorcarablut.code.entity.post.Comment;
import com.victorcarablut.code.entity.post.Like;
import com.victorcarablut.code.entity.post.Post;
import com.victorcarablut.code.entity.user.User;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findAllByPostId(Long postId);
	
	@Query(value = "SELECT comments.* FROM comments WHERE comments.post_id = :postId ORDER BY comments.id DESC", nativeQuery = true)
	List<Comment> findAllByOrderByPostIdDesc(Long postId);

	Comment findCommentById(Long id);

}
