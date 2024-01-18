package com.victorcarablut.code.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.victorcarablut.code.entity.user.UserBlocked;

public interface UserBlockedRepository extends JpaRepository<UserBlocked, Long> {

	Boolean existsUserBlockedByEmail(String email);

}
