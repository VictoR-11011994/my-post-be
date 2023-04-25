package com.victorcarablut.code.controller.auth;

import java.util.LinkedHashMap;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.victorcarablut.code.exceptions.GenericException;
import com.victorcarablut.code.exceptions.InvalidEmailException;
import com.victorcarablut.code.exceptions.PasswordNotMatchException;
import com.victorcarablut.code.exceptions.UserBlockedException;
import com.victorcarablut.code.exceptions.WrongEmailOrPasswordException;
import com.victorcarablut.code.service.UserService;
import com.victorcarablut.code.exceptions.EmailWrongCodeException;
import com.victorcarablut.code.exceptions.EmailAlreadyExistsException;

import com.victorcarablut.code.exceptions.EmailNotExistsException;
import com.victorcarablut.code.exceptions.EmailNotVerifiedException;
import com.victorcarablut.code.exceptions.ErrorSendEmailException;

import com.victorcarablut.code.exceptions.ErrorSaveDataToDatabaseException;

import com.victorcarablut.code.dto.UserDto;

//public access

@CrossOrigin(origins = "${url.fe.cross.origin}")
@RestController
@RequestMapping("/api/account")
public class AuthenticationController {

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
	
	@ExceptionHandler({ UserBlockedException.class })
	public Map<String, Object> handleUserBlocked() {
		Map<String, Object> responseJSON = new LinkedHashMap<>();
		responseJSON.put("status_code", 12);
		responseJSON.put("status_message", "Account is blocked because rules were violated.");
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

	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@RequestBody UserDto userDto) {
		userService.registerUser(userDto);
		return new ResponseEntity<String>("User Registered!", HttpStatus.OK);
	}

	@PostMapping("/email/code/send")
	public ResponseEntity<String> sendEmailCodeNoReply(@RequestBody LinkedHashMap<String, String> data) {
		final String email = data.get("email");
		userService.sendEmailCodeNoReply(email);
		return new ResponseEntity<String>("An email with a verification code was sent to: " + email.substring(0, 5)
				+ "**********" + " | (no-reply)", HttpStatus.OK);
	}

	@PostMapping("/new-email/code/send")
	public ResponseEntity<String> sendEmailCodeNoReplyNewEmail(@RequestBody LinkedHashMap<String, String> data) {
		final String oldEmail = data.get("old_email");
		final String newEmail = data.get("new_email");
		userService.sendEmailCodeNoReplyNewEmail(oldEmail, newEmail);
		return new ResponseEntity<String>("An email with a verification code was sent to: " + newEmail.substring(0, 5)
				+ "**********" + " | (no-reply)", HttpStatus.OK);
	}

	@Autowired
	@Qualifier("javaMailSenderPrimary")
	private JavaMailSender javaMailSender;

	// test sending email with another host email (works OK)
	@PostMapping("/email/code/primary")
	public ResponseEntity<String> sendEmailCodePrimary(@RequestBody LinkedHashMap<String, String> data) {

		try {
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
			simpleMailMessage.setFrom("my-post@code.victorcarablut.com");
			simpleMailMessage.setTo("mail@mail.com");
			simpleMailMessage.setSubject("My Post - primary");
			simpleMailMessage.setText("000");

			javaMailSender.send(simpleMailMessage);
			// System.out.println("Email sended (primary)");

		} catch (Exception e) {
			// System.out.println("Error sending Email (primary)");
			throw new GenericException();

		}

		return new ResponseEntity<String>("Code sended on email (primary)", HttpStatus.OK);
	}

	// Account created successfully
	@PostMapping("/created/email/info")
	public ResponseEntity<String> sendEmailAccountCreated(@RequestBody LinkedHashMap<String, String> data) {
		userService.sendEmailAccountCreated(data.get("email"));
		return new ResponseEntity<String>("Account successfully created! (primary)", HttpStatus.OK);
	}

	@PostMapping("/email/code/verify")
	public ResponseEntity<String> verifyEmailCode(@RequestBody LinkedHashMap<String, String> data) {
		userService.verifyEmailCode(data.get("email"), data.get("code"));
		return new ResponseEntity<String>("Code verified!", HttpStatus.OK);
	}


	// 1) generate & send code on email
	// 2) enter code & new password
	@PostMapping("/password/recover")
	public ResponseEntity<String> recoverUserPassword(@RequestBody LinkedHashMap<String, String> data) {
		userService.recoverUserPassword(data.get("email"), data.get("code"), data.get("password"));
		return new ResponseEntity<String>(HttpStatus.OK);
	}

	// auth
	@PostMapping("/login")
	public ResponseEntity<Map<Object, String>> authenticate(@RequestBody UserDto userDto) {
		return ResponseEntity.ok(userService.loginUser(userDto));
	}

}
