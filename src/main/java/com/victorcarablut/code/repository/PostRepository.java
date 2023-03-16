package com.victorcarablut.code.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.victorcarablut.code.entity.post.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}
