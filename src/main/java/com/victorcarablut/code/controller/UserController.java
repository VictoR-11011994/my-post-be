package com.victorcarablut.code.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.victorcarablut.code.dto.UserDto;
import com.victorcarablut.code.entity.user.User;
import com.victorcarablut.code.entity.user.UserBlocked;
import com.victorcarablut.code.exceptions.EmailAlreadyExistsException;
import com.victorcarablut.code.exceptions.EmailNotExistsException;
import com.victorcarablut.code.exceptions.EmailNotVerifiedException;
import com.victorcarablut.code.exceptions.EmailWrongCodeException;
import com.victorcarablut.code.exceptions.ErrorSaveDataToDatabaseException;
import com.victorcarablut.code.exceptions.ErrorSendEmailException;
import com.victorcarablut.code.exceptions.GenericException;
import com.victorcarablut.code.exceptions.InvalidEmailException;
import com.victorcarablut.code.exceptions.PasswordNotMatchException;
import com.victorcarablut.code.exceptions.UsernameAlreadyExistsException;
import com.victorcarablut.code.exceptions.WrongEmailOrPasswordException;
import com.victorcarablut.code.service.UserService;

// private access

@CrossOrigin(origins = "${url.fe.cross.origin}")
@RestController
@RequestMapping("/api/user")
public class UserController {

	@Autowired
	private UserService userService;

	// Custom exceptions response

	@ExceptionHandler({ GenericException.class })
	public ResponseEntity<String> handleGenericError() {
		final String message = "Error";
		return new ResponseEntity<String>(message, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ ErrorSaveDataToDatabaseException.class })
	public Map<String, Object> handleErrorSaveDataToDatabase() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 1);
		responseJSON.put("status_message", "Error save data to DB");
		return responseJSON;
	}

	@ExceptionHandler({ InvalidEmailException.class })
	public Map<String, Object> handleInvalidEmail() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 2);
		responseJSON.put("status_message", "Invalid email format.");
		return responseJSON;
	}

	@ExceptionHandler({ EmailAlreadyExistsException.class })
	public Map<String, Object> handleEmailAlreadyExists() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 3);
		responseJSON.put("status_message", "Account with that email already exists.");
		return responseJSON;
	}

	@ExceptionHandler({ EmailNotExistsException.class })
	public Map<String, Object> handleEmailNotExists() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 4);
		responseJSON.put("status_message", "Account with that email doesn't exist.");
		return responseJSON;
	}
	

	@ExceptionHandler({ EmailWrongCodeException.class })
	public Map<String, Object> handleWrongEmailCode() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 5);
		responseJSON.put("status_message", "Wrong verification code.");
		return responseJSON;
	}

	@ExceptionHandler({ EmailNotVerifiedException.class })
	public Map<String, Object> handleEmailNotVerified() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 6);
		responseJSON.put("status_message", "Email not verified yet!");
		return responseJSON;
	}

	@ExceptionHandler({ ErrorSendEmailException.class })
	public Map<String, Object> handleErrorSendEmail() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 7);
		responseJSON.put("status_message", "Error while sending email, try again!");
		return responseJSON;
	}
	
	@ExceptionHandler({ UsernameAlreadyExistsException.class })
	public Map<String, Object> handleUsernameAlreadyExists() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 10);
		responseJSON.put("status_message", "Account with that username already exists.");
		return responseJSON;
	}

	// used when updating password (old to new)
	@ExceptionHandler({ PasswordNotMatchException.class })
	public Map<String, Object> handlePasswordNotMatch() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 8);
		responseJSON.put("status_message", "Passwords not match.");
		return responseJSON;
	}

	// used in auth check
	@ExceptionHandler({ WrongEmailOrPasswordException.class })
	public Map<String, Object> handleWrongEmailOrPassword() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 9);
		responseJSON.put("status_message", "Wrong email or password.");
		return responseJSON;
	}

	@GetMapping("/details")
	public Optional<User> getUserDetails(Authentication authentication) {

		return userService.findUserDetails(authentication.getName());
	}
	
	@GetMapping("/all")
	public List<User> getAllUsers(Authentication authentication) {
		final String userRole = authentication.getAuthorities().toString();
		if(userRole.contains("ADMIN")) {
			return userService.findAllUsers();
		} else {
			return null;
		}
	}
	
	@GetMapping("/all/blocked")
	public List<UserBlocked> getAllBlockedUsers(Authentication authentication) {
		final String userRole = authentication.getAuthorities().toString();
		
		System.out.println(authentication.getAuthorities());
		
		if(userRole.contains("ADMIN")) {
			return userService.findAllBlockedUsers();
		} else {
			return null;
		}
	}
	
	@GetMapping("/{username}")
	public Optional<User> getUserProfile(@PathVariable("username") String username) {
		return userService.findUserDetails(username);
	}

	@PutMapping("/details/update")
	public ResponseEntity<String> updateUserDetails(@RequestBody UserDto userDto) {
		userService.updateUserDetails(userDto);
		return new ResponseEntity<String>("User Updated!", HttpStatus.OK);
	}

	// 1) generate & send code on email
	// 2) enter code & new email
	@PutMapping("/email/update")
	public ResponseEntity<String> updateUserEmail(@RequestBody LinkedHashMap<String, String> data) {
		userService.updateUserEmail(data.get("old_email"), data.get("password"), data.get("new_email"), data.get("code"));
		return new ResponseEntity<String>("Email Updated!", HttpStatus.OK);
	}
	
	@PutMapping("/username/update")
	public ResponseEntity<String> upadateUserUsername(@RequestBody LinkedHashMap<String, String> data) {
		userService.updateUserUsername(data.get("email"), data.get("password"), data.get("old_username"), data.get("new_username").toLowerCase());
		return new ResponseEntity<String>("Username Updated!", HttpStatus.OK);
	}

	@PutMapping("/password/update")
	public ResponseEntity<String> upadateUserPassword(@RequestBody LinkedHashMap<String, String> data) {
		userService.updateUserPassword(data.get("email"), data.get("old_password"), data.get("new_password"));
		return new ResponseEntity<String>("Password Updated!", HttpStatus.OK);
	}
	

	@PutMapping("/image/update")
	public ResponseEntity<String> updateUserImg(@RequestParam String filter, @RequestParam String email, @RequestParam("userImg") MultipartFile file) {
		
			try {
				userService.updateUserImg(filter, email, file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		return new ResponseEntity<String>("Image Updated!", HttpStatus.OK);
	}
	
	@PostMapping("/image/delete")
	public ResponseEntity<String> updateUserImg(@RequestBody LinkedHashMap<String, String> data) {
		userService.deleteUserImg(data.get("filter"), data.get("email"));
		return new ResponseEntity<String>("Image Deleted!", HttpStatus.OK);
	}
	
	@PostMapping("/status")
	public ResponseEntity<String> statusUser(Authentication authentication, @RequestBody UserDto userDto) {
		userService.statusUser(authentication.getName(), userDto.getUsername(), userDto.getUserId(), userDto.getStatus());
		return new ResponseEntity<String>("User status updated!", HttpStatus.OK);
	}
	
	@PostMapping("/role")
	public ResponseEntity<String> updateUserRole(Authentication authentication, @RequestBody UserDto userDto) {
		userService.updateUserRole(authentication.getName(), userDto.getUsername(), userDto.getPassword(), userDto.getUserId());
		return new ResponseEntity<String>("User role updated!", HttpStatus.OK);
	}
	
	
}
