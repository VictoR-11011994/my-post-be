package com.victorcarablut.code.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.victorcarablut.code.entity.post.Like;
import com.victorcarablut.code.entity.user.User;

public interface LikeRepository extends JpaRepository<Like, Long> {

	List<Like> findAllByPostId(Long postId);

	Long countByPostId(Long postId);

	Boolean existsPostByPostIdAndUserId(Long postId, Long userId);

	Like findByPostIdAndUserId(Long postId, Long userId);
}
