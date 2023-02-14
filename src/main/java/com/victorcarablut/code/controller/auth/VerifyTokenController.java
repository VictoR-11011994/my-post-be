package com.victorcarablut.code.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "${url.fe.cross.origin}", maxAge = 3600)
@RestController
@RequestMapping("/api/token")
public class VerifyTokenController {
	
	// private api access by default
	// if the request to this api response is OK the token is valid
	
	@GetMapping("/verify")
	public ResponseEntity<?> privateAccess() {
		return ResponseEntity.ok(HttpStatus.OK);
	}

}
