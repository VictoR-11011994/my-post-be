package com.victorcarablut.code.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.victorcarablut.code.entity.post.Like;
import com.victorcarablut.code.entity.user.User;

public interface LikeRepository extends JpaRepository<Like, Long> {

	List<Like> findAllByPostId(Long postId);

	long countByPostId(Long postId);

	//long findPostIdByUserId(Long userId);

	//User findUserById(Long id);

	//Boolean existsByUserId(Long userId);

	//Like findByUserId(Long userId);
	
	Boolean existsPostByPostIdAndUserId(Long postId, Long userId);

	//void deleteByPostIdAndUserId(Long postId, Long userId);

	Like findByPostIdAndUserId(Long postId, Long userId);

}
