package com.victorcarablut.code.controller.auth;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.victorcarablut.code.exceptions.GenericException;
import com.victorcarablut.code.exceptions.EmailAlreadyExistsException;
import com.victorcarablut.code.exceptions.EmptyInputException;
import com.victorcarablut.code.config.email.EmailConfig;
import com.victorcarablut.code.dto.UserDto;
import com.victorcarablut.code.entity.user.User;
import com.victorcarablut.code.service.user.UserService;


@CrossOrigin(origins = "${url.fe.cross.origin}")
@RestController
@RequestMapping("/api/account")
public class AuthenticationController {
	
	@Autowired
	private UserService userService;
	
	@ExceptionHandler({GenericException.class})
	public ResponseEntity<String> handleGenericError() {
		String message = "Error";
		return new ResponseEntity<String>(message, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler({EmptyInputException.class})
	public ResponseEntity<String> handleEmptyInput() {
		String message = "Fill the required fields.";
		return new ResponseEntity<String>(message, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler({EmailAlreadyExistsException.class})
	public ResponseEntity<String> handleEmailAlreadyExists() {
		String message = "Email already exists.";
		return new ResponseEntity<String>(message, HttpStatus.CONFLICT);
	}
	

//	@PostMapping("/register")
//	public void user(@RequestBody UserDto userDto) {
//		
//		return userService.registerUser(userDto);
//	}
	
	@PostMapping("/user/register")
	public ResponseEntity<String> registerUser(@RequestBody UserDto userDto) {
		 userService.registerUser(userDto);
		 return new ResponseEntity<String>("User registered", HttpStatus.OK);
	}
	
//	@PostMapping("/email/code")
//	public ResponseEntity<String> sendEmailCode(@RequestBody LinkedHashMap<String, String> data) {
//		userService.generateEmailCode(null, data.get("email"));
//		return new ResponseEntity<String>("Code sended on email", HttpStatus.OK);
//	}
	
	@PostMapping("/email/code")
	public ResponseEntity<String> sendEmailCode(@RequestBody LinkedHashMap<String, String> data) {
		userService.generateEmailCode(data.get("email"));
		return new ResponseEntity<String>("Code sended on email (no-reply)", HttpStatus.OK);
	}
	
	
	@Autowired
	@Qualifier("javaMailSenderPrimary")
	private JavaMailSender javaMailSender2;
	
	
	// test
	@PostMapping("/email/code/primary")
	public ResponseEntity<String> sendEmailCodePrimary(@RequestBody LinkedHashMap<String, String> data) {
		//userService.generateEmailCode(data.get("email"));
		try {
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
			simpleMailMessage.setFrom("my-post@code.victorcarablut.com");
			simpleMailMessage.setTo("dibakos701@mirtox.com");
			simpleMailMessage.setSubject("My Post - primary");
			simpleMailMessage.setText("000");

			javaMailSender2.send(simpleMailMessage);
			System.out.println("Email sended (primary)");

		} catch (Exception e) {
			System.out.println("Error sending Email (primary)");
			throw new GenericException();

		}
     
		return new ResponseEntity<String>("Code sended on email (primary)", HttpStatus.OK);
	}
	
	
	@PostMapping("/email/code/verify")
	public ResponseEntity<String> verifyEmailCode(@RequestBody LinkedHashMap<String, String> data) {
		userService.verifyEmailCode(data.get("email"), data.get("code"));
		return new ResponseEntity<String>("Code verified!", HttpStatus.OK);
	}
	
//	@PostMapping("/password/update")
//	public ResponseEntity<String> updateUserPassword(@RequestBody UserDto userDto) {
//		 userService.resetUserPassword(userDto);
//		 return new ResponseEntity<String>(HttpStatus.OK);
//	}
	
	// 1) generate & send code on email
	// 2) enter a new password
	@PostMapping("/user/password/recover")
	public ResponseEntity<String> recoverUserPassword(@RequestBody LinkedHashMap<String, String> data) {
		 userService.recoverUserPassword(data.get("email"), data.get("code"), data.get("password"));
		 return new ResponseEntity<String>(HttpStatus.OK);
	}
	
	// auth
	@PostMapping("/user/login")
	public ResponseEntity<Map<Object, String>> authenticate(@RequestBody UserDto userDto) {
		return ResponseEntity.ok(userService.authenticate(userDto));
	}
	
	@GetMapping("/user/details")
	public Optional<User> getUsername(Authentication authentication) {

		//System.out.println(authentication.getName());
		//System.out.println(authentication.getAuthorities());
		
		//final Optional<User> fullName = userService.findUserDetails(user.getFullName().toString());
		//final String username = authentication.getName();
		//final String email = "";
		//final String role = authentication.getAuthorities();
		
		//TokenDto jwtToken = new TokenDto("token", token);


		//Map<Object, String> tokenJSON = new LinkedHashMap<>();

	    //tokenJSON.put(jwtToken.getNameVar(), jwtToken.getToken());
		
		return userService.findUserDetails(authentication.getName());
	}
	
}
