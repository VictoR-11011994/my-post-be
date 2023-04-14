package com.victorcarablut.code.repository;

import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.victorcarablut.code.entity.post.Post;
import com.victorcarablut.code.entity.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	@Query(value = "SELECT u.email FROM users u WHERE u.email = :email", nativeQuery = true)
    Map<String, Object> findByEmailAndReturnOnlyEmail(String email);
	
	User findByEmail(String email);
	//Optional<User> findByEmail(String email);
	
	User findUserById(Long id);
	
	Boolean existsUserByEmail(String email);
	
	Boolean existsUserByUsername(String username);
	
	Optional<User> findByUsername(String username);
	
	Optional<User> findUsernameByEmail(String email);
	
	// User findByIdAndEmail(Long id, String email);
	
	Optional<User> findOneByEmail(String email);
	
	Boolean existsUserByEmailAndVerificationCode(String email, Integer code);

	User findByPassword(String password);

	User findUserByUsername(String username);

	

}
