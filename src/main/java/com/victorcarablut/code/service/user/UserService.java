package com.victorcarablut.code.service.user;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.victorcarablut.code.dto.TokenDto;
import com.victorcarablut.code.dto.UserDto;
import com.victorcarablut.code.entity.user.Role;
import com.victorcarablut.code.entity.user.User;
import com.victorcarablut.code.exceptions.EmailAlreadyExistsException;
import com.victorcarablut.code.exceptions.EmptyInputException;
import com.victorcarablut.code.exceptions.GenericException;
import com.victorcarablut.code.exceptions.WrongEmailCodeException;
import com.victorcarablut.code.repository.user.UserRepository;
import com.victorcarablut.code.security.jwt.JwtService;
import com.victorcarablut.code.service.email.EmailService;

@Service
public class UserService {
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	//@Autowired
	//private EmailService emailService;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JwtService jwtService;

	@Autowired
	@Qualifier("javaMailSenderPrimary")
	private JavaMailSender javaMailSenderPrimary;
	
	@Autowired
	@Qualifier("javaMailSenderNoReply")
	private JavaMailSender javaMailSenderNoReply;

	@Value("${mail.username.no-reply}")
	private String senderEmailNoReply;

	// find and get only user email
	public Map<String, Object> findUserByEmail(String email) {
		return userRepository.findByEmailAndReturnOnlyEmail(email);
	}
	
	// find user details
	public Optional<User> findUserDetails(String username) {
		return userRepository.findByUsername(username);
	}

	// save new user
	public void registerUser(UserDto userDto) {

		String userEmail = null;
		try {
			userEmail = userDto.getEmail().trim();
		} catch (Exception e) {
			System.out.println(e);
			throw new GenericException();
		}

		User user = new User();
		user.setFullName(userDto.getFullName());
		user.setEmail(userEmail);
		
		//final String encodedPassword = passwordEncoder.encode(userDto.getPassword());
		user.setPassword(passwordEncoder.encode(userDto.getPassword()));
		
		user.setRegisteredDate(LocalDateTime.now());

		// UUID uuid = UUID.randomUUID();
		DateFormat dateFormat = new SimpleDateFormat("ddMMHHmmssmm");
		try {
			user.setUsername(user.getFullName().toLowerCase() + "-" + dateFormat.format(new Date()));
		} catch (Exception e) {
			System.out.println(e);
			throw new GenericException();
		}

		user.setRole(Role.USER);
		user.setEnabled(false);

		// Map<String, Object> email = userRepository.findByEmail(user.getEmail());
		// email.forEach((key, value) -> System.out.println(key + ":" + value));

		// System.out.println("User Email: " + user.getEmail());
		// System.out.println("User Email exists in DB?: " + email.isEmpty());

		// email input control
		Boolean emailMatchControl = userEmail.contains("@") && userEmail.contains(".");

		if (userEmail == null || userEmail.isEmpty() || !emailMatchControl) {
			throw new EmptyInputException();

		} else if (findUserByEmail(userEmail).isEmpty()) {
			// User with that email does not exists yet...
			try {
				// try to save the user...
				userRepository.save(user);

				try {
					// ...then try to send generated code and send it on email...
					//generateEmailCode(user.getId().toString(), user.getEmail());
					generateEmailCode(user.getEmail());
				} catch (Exception e) {
					System.out.println(e);
					throw new GenericException();
				}

			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(e);
				throw new GenericException();
			}

		} else {
			// User with that email already exists...
			throw new EmailAlreadyExistsException();
		}

	}

	// generate code and save to db
	public void generateEmailCode(String email) {

		String userEmail = null;
		try {
			userEmail = email.trim();
		} catch (Exception e) {
			throw new GenericException();
		}

		// email input control
		Boolean emailMatchControl = userEmail.contains("@") && userEmail.contains(".");
		Boolean emailFromFindUserByEmail = findUserByEmail(userEmail).isEmpty();

		if (userEmail == null || userEmail.isEmpty() || !emailMatchControl) {
			throw new EmptyInputException();

		} else if (emailFromFindUserByEmail == true) {
			// User with that email does not exists...
			throw new GenericException();

		} else {

			try {
				User user = userRepository.findByEmail(email);
			
				//Long userId = Long.valueOf(id);
					

				// generate 6 random numbers
				SecureRandom secureRandomNumbers = SecureRandom.getInstance("SHA1PRNG");
				int randomNumbers = secureRandomNumbers.nextInt(900000) + 100000;

				user.setVerificationCode(randomNumbers);

				userRepository.save(user);

				// after code is saved to db try to send it on email
				try {
					sendEmailCode(userEmail);
				} catch (Exception e) {
					// TODO: handle exception
					throw new GenericException();
				}

			} catch (Exception e) {
				// TODO: handle exception
				throw new GenericException();
			}
		}

	}

	// find email and send code on email
	public void sendEmailCode(String email) {

		try {
			User user = userRepository.findByEmail(email);
			// Integer code = user.getVerificationCode();

			// send email
			// System.out.println("Email sended" + code);
			// emailService.sendEmail(user.getEmail(), "Password Recover Code", "code: " + user.getVerificationCode());

			try {
				SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
				simpleMailMessage.setFrom(senderEmailNoReply);
				simpleMailMessage.setTo(user.getEmail());
				simpleMailMessage.setSubject("Verify Email Code (no-reply)");
				simpleMailMessage.setText(user.getVerificationCode().toString());

				javaMailSenderNoReply.send(simpleMailMessage);
				System.out.println("Email sended (no-reply)");

			} catch (Exception e) {
				System.out.println("Error sending Email (no-reply)");
				throw new GenericException();

			}

		} catch (Exception e) {
			// TODO: handle exception
			throw new GenericException();
		}

	}

	// verify code received on email
	public boolean verifyEmailCode(String email, String code) {
		
		boolean statusVerifyEmailCode = false;

		String userEmail = null;
		try {
			userEmail = email.trim();
		} catch (Exception e) {
			throw new GenericException();
		}

		// email input control
		Boolean emailMatchControl = userEmail.contains("@") && userEmail.contains(".");
		Boolean emailFromFindUserByEmail = findUserByEmail(userEmail).isEmpty();

		if (userEmail == null || userEmail.isEmpty() || !emailMatchControl) {
			throw new EmptyInputException();

		} else if (emailFromFindUserByEmail == true) {
			// User with that email does not exists...
			throw new GenericException();

		} else {

			try {
				Integer emailCode = Integer.valueOf(code);
				User user = userRepository.findByEmailAndVerificationCode(email, emailCode);

				// clear code and enable user account
				user.setVerificationCode(null);
				user.setEnabled(true);
		
				userRepository.save(user);
				
				statusVerifyEmailCode = true;


			} catch (Exception e) {
				// TODO: handle exception
				statusVerifyEmailCode = false;
				throw new WrongEmailCodeException();
			}
		}
		return statusVerifyEmailCode;

	}

	// recover password if forget
	public void recoverUserPassword(String email, String code, String password) {
		// TODO Auto-generated method stub
		
		// 1) generate & send code on email
		// 2) verify code
		if(verifyEmailCode(email, code) == true) {
			
			try {
				User user = userRepository.findByEmail(email);
				user.setPassword(passwordEncoder.encode(password));
				userRepository.save(user);
				
			} catch (Exception e) {
				throw new GenericException();
			}
			
		} else {
			throw new GenericException();
		}
		
		
	}
	
	
	// auth and get token
	public Map<Object, String> authenticate(UserDto userDto) {

		// get email from input and find related username
		User user = userRepository.findByEmail(userDto.getEmail());
		
		//System.out.println("usernameFromEmail: " + user.getUsername());
		
		userDto.setUsername(user.getUsername());
		
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword()));
	
		final String token = jwtService.generateToken(userRepository.findByUsername(userDto.getUsername()).orElseThrow());
		TokenDto jwtToken = new TokenDto("token", token);


		Map<Object, String> tokenJSON = new LinkedHashMap<>();

	    tokenJSON.put(jwtToken.getNameVar(), jwtToken.getToken());

		return tokenJSON;
	}

}
