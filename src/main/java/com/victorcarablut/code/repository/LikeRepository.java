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

	//SELECT u.email FROM users u WHERE u.email = :email"
	
	//@Query(value = "SELECT * FROM likes l INNER JOIN users u WHERE u.id = l.user_id AND l.post_id = :postId", nativeQuery = true)
	//List<Like> findAllByPostId2(Long postId);
	
	//SELECT users.full_name  FROM my_post.likes INNER JOIN my_post.users WHERE users.id = likes.user_id AND post_id = 33;

}
