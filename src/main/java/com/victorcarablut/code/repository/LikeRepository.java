package com.victorcarablut.code.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.victorcarablut.code.entity.post.Like;

public interface LikeRepository extends JpaRepository<Like, Long> {

	List<Like> findAllByPostId(Long postId);

}
