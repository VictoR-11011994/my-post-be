package com.victorcarablut.code.service.user;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.victorcarablut.code.dto.TokenDto;
import com.victorcarablut.code.dto.UserDto;
import com.victorcarablut.code.entity.user.Role;
import com.victorcarablut.code.entity.user.User;

import com.victorcarablut.code.exceptions.EmailAlreadyExistsException;
import com.victorcarablut.code.exceptions.EmailNotExistsException;
import com.victorcarablut.code.exceptions.EmailNotVerifiedException;
import com.victorcarablut.code.exceptions.ErrorSendEmailException;
import com.victorcarablut.code.exceptions.ErrorSaveDataToDatabaseException;
import com.victorcarablut.code.exceptions.GenericException;
import com.victorcarablut.code.exceptions.InvalidEmailException;
import com.victorcarablut.code.exceptions.PasswordNotMatchException;
import com.victorcarablut.code.exceptions.WrongEmailOrPasswordException;
import com.victorcarablut.code.exceptions.EmailWrongCodeException;

import com.victorcarablut.code.repository.user.UserRepository;
import com.victorcarablut.code.security.jwt.JwtService;

@Service
public class UserService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	// @Autowired
	// private EmailService emailService;

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

	// find only email true/false
	public boolean existsUserByEmail(String email) {
		return userRepository.existsUserByEmail(email);
	}

	// find user details
	public Optional<User> findUserDetails(String username) {
		return userRepository.findByUsername(username);
	}

	// check email input validity
	public boolean emailInputIsValid(String email) {

		final Boolean emailFormatControl = email.contains("@") && email.contains(".");

		if (email == null || email.contains(" ") || email.length() == 0 || email.length() > 100 || email.isEmpty()
				|| email.isBlank() || !emailFormatControl) {
			return false;
		} else {
			return true;
		}
	}

	// check username input validity
	public boolean usernameInputIsValid(String username) {

		if (username == null || username.contains(" ") || username.length() == 0 || username.length() > 20
				|| username.isEmpty() || username.isBlank()) {
			return false;
		} else {
			return true;
		}
	}

	// register new user
	public void registerUser(UserDto userDto) {

		if (emailInputIsValid(userDto.getEmail())) {

			if (existsUserByEmail(userDto.getEmail())) {
				throw new EmailAlreadyExistsException();
			} else {

				User user = new User();
				user.setFullName(userDto.getFullName());
				user.setEmail(userDto.getEmail());

				// first time: generate automatic username (max: 20 characters)
				try {
					String uuid = UUID.randomUUID().toString().replace("-", "");
					DateFormat dateFormat = new SimpleDateFormat("ddMMHHmmssmm");
					user.setUsername(uuid.substring(0, 7).toLowerCase() + "-" + dateFormat.format(new Date())); // max
																												// // 20
																												// //
																												// characters
				} catch (Exception e) {
					throw new GenericException();
				}

				user.setPassword(passwordEncoder.encode(userDto.getPassword()));
				user.setRegisteredDate(LocalDateTime.now());
				user.setRole(Role.USER); // first time: default role is USER
				user.setEnabled(false); // first time: account is not enabled (necessary verification code from email)

				try {
					userRepository.save(user);
				} catch (Exception e) {
					throw new ErrorSaveDataToDatabaseException();
				}

				// ... after (registerUser) on front-end call a separate API to generate code
				// and send it on email (necessary to verify and enable account)

			}

		} else {
			throw new InvalidEmailException();
		}

	}

	// login/auth user and generate token
	public Map<Object, String> loginUser(UserDto userDto) {

		Map<Object, String> tokenJSON = new LinkedHashMap<>();

		if (emailInputIsValid(userDto.getEmail())) {

			if (existsUserByEmail(userDto.getEmail())) {

				User user = userRepository.findByEmail(userDto.getEmail());

				if (user.isEnabled()) {

					if (verifyAuth(user.getUsername(), userDto.getPassword())) {

						final String token = jwtService
								.generateToken(userRepository.findByUsername(user.getUsername()).orElseThrow());
						TokenDto jwtToken = new TokenDto("token", token);

						tokenJSON.put(jwtToken.getNameVar(), jwtToken.getToken());

					} else {
						throw new WrongEmailOrPasswordException();
					}

				} else {
					throw new EmailNotVerifiedException();
				}

			} else {
				throw new EmailNotExistsException();
			}

		} else {
			throw new InvalidEmailException();
		}

		return tokenJSON;
	}

	public void generateCode(String email) {

		try {

			User user = userRepository.findByEmail(email);

			// generate 6 random numbers
			SecureRandom secureRandomNumbers = SecureRandom.getInstance("SHA1PRNG");
			final int randomNumbers = secureRandomNumbers.nextInt(900000) + 100000;
			user.setVerificationCode(randomNumbers);

			userRepository.save(user);

		} catch (Exception e) {
			throw new ErrorSaveDataToDatabaseException();
		}
	}

	public void sendEmailCodeNoReply(String email) {

		if (emailInputIsValid(email)) {

			if (existsUserByEmail(email)) {

				// execute external method
				generateCode(email);

				User user = userRepository.findByEmail(email);

				// extra control of generated code available on db
				if (user.getVerificationCode() == 0) {
					throw new GenericException();
				} else {
					try {
						SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
						simpleMailMessage.setFrom(senderEmailNoReply);
						simpleMailMessage.setTo(email);
						simpleMailMessage.setSubject("Verification Code (no-reply)");
						simpleMailMessage.setText(user.getVerificationCode().toString());

						javaMailSenderNoReply.send(simpleMailMessage);

					} catch (Exception e) {
						throw new ErrorSendEmailException();
					}
				}

			} else {
				throw new EmailNotExistsException();
			}

		} else {
			throw new InvalidEmailException();
		}
	}

	// used when update email
	public void sendEmailCodeNoReplyNewEmail(String oldEmail, String newEmail) {

		if (emailInputIsValid(oldEmail) && emailInputIsValid(newEmail)) {

			if (existsUserByEmail(oldEmail)) {

				// execute external method
				generateCode(oldEmail);

				User user = userRepository.findByEmail(oldEmail);

				// extra control of generated code available on db
				if (user.getVerificationCode() == 0) {
					throw new GenericException();
				} else {
					try {
						SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
						simpleMailMessage.setFrom(senderEmailNoReply);
						simpleMailMessage.setTo(newEmail);
						simpleMailMessage.setSubject("Update Email - Verification Code (no-reply)");
						simpleMailMessage.setText(user.getVerificationCode().toString());

						javaMailSenderNoReply.send(simpleMailMessage);

					} catch (Exception e) {
						throw new ErrorSendEmailException();
					}
				}

			} else {
				throw new EmailNotExistsException();
			}

		} else {
			throw new InvalidEmailException();
		}
	}

//	public void enableUserAccount(UserDto userDto) {
//		try {
//			User user = userRepository.findByEmail(userDto.getEmail());
//			user.setEnabled(true);
//			userRepository.save(user);
//		} catch (Exception e) {
//			throw new ErrorSaveDataToDatabaseException();
//		}
//	}

	// verify code received on email
	public boolean verifyEmailCode(String email, String code) {

		boolean returnStatus = false;

		if (emailInputIsValid(email)) {

			if (existsUserByEmail(email)) {

				final Integer emailCode = Integer.valueOf(code);

				final boolean existsUserAndCode = userRepository.existsUserByEmailAndVerificationCode(email, emailCode);

				if (existsUserAndCode) {

					User user = userRepository.findByEmail(email);
					// clear code
					user.setVerificationCode(null);
					user.setEnabled(true);

					try {
						userRepository.save(user);
						returnStatus = true;
					} catch (Exception e) {
						returnStatus = false;
						throw new ErrorSaveDataToDatabaseException();
					}

				} else {
					returnStatus = false;
					throw new EmailWrongCodeException();
				}

			} else {
				returnStatus = false;
				throw new EmailNotExistsException();
			}

		} else {
			returnStatus = false;
			throw new InvalidEmailException();
		}

		return returnStatus;

	}

	public boolean verifyAuth(String username, String password) {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// update User (exists another separate update for: email, username, password)
	public void updateUserDetails(UserDto userDto) {

		if (emailInputIsValid(userDto.getEmail())) {

			if (existsUserByEmail(userDto.getEmail())) {

				User user = userRepository.findByEmail(userDto.getEmail());

				user.setFullName(userDto.getFullName());

				try {
					userRepository.save(user);
				} catch (Exception e) {
					throw new ErrorSaveDataToDatabaseException();
				}

			} else {
				throw new EmailNotExistsException();
			}

		} else {
			throw new InvalidEmailException();
		}

	}

	// update User (only email)
	public void updateUserEmail(String oldEmail, String password, String newEmail, String newEmailCode) {

		if (emailInputIsValid(oldEmail)) {

			if (existsUserByEmail(oldEmail)) {

				// first: enter password
				// second: verify password (auth)

				User user = userRepository.findByEmail(oldEmail);

				if (verifyAuth(user.getUsername(), password)) {

					if (emailInputIsValid(newEmail)) {

						if (existsUserByEmail(newEmail)) {

							throw new EmailAlreadyExistsException();

						} else {

							// generateCode(oldEmail);

							if (verifyEmailCode(oldEmail, newEmailCode)) {

								user.setEmail(newEmail);

								try {
									userRepository.save(user);
								} catch (Exception e) {
									throw new ErrorSaveDataToDatabaseException();
								}

							} else {
								throw new EmailWrongCodeException();
							}
						}

					} else {
						throw new InvalidEmailException();
					}

				} else {
					throw new WrongEmailOrPasswordException();
				}

			} else {
				throw new EmailNotExistsException();
			}

		} else {
			throw new InvalidEmailException();
		}
	}

	// // update User (only username)
	public void updateUserUsername(String email, String password, String oldUsername, String newUsername) {

		if (emailInputIsValid(email)) {

			if (existsUserByEmail(email)) {

				if (usernameInputIsValid(newUsername)) {

					if (verifyAuth(oldUsername, password)) {

						User user = userRepository.findByEmail(email);
						user.setUsername(newUsername);

						try {
							userRepository.save(user);
						} catch (Exception e) {
							throw new ErrorSaveDataToDatabaseException();
						}

					} else {
						// TODO: create exception username
						throw new WrongEmailOrPasswordException();
					}

				} else {
					throw new GenericException();
				}

			} else {
				throw new EmailNotExistsException();
			}

		} else {
			throw new InvalidEmailException();
		}
	}

	// update existing password
	public void updateUserPassword(String email, String oldPassword, String newPassword) {

		if (emailInputIsValid(email)) {

			if (existsUserByEmail(email)) {

				User user = userRepository.findByEmail(email);

				if (verifyAuth(user.getUsername(), oldPassword)) {

					user.setPassword(passwordEncoder.encode(newPassword));

					try {
						userRepository.save(user);
					} catch (Exception e) {
						throw new ErrorSaveDataToDatabaseException();
					}

				} else {
					throw new PasswordNotMatchException();
				}

			} else {
				throw new EmailNotExistsException();
			}

		} else {
			throw new InvalidEmailException();
		}
	}

	// recover password if forget
	public void recoverUserPassword(String email, String code, String password) {

		if (emailInputIsValid(email)) {

			if (existsUserByEmail(email)) {

				// first send email code no-reply

				if (verifyEmailCode(email, code)) {

					User user = userRepository.findByEmail(email);
					user.setPassword(passwordEncoder.encode(password));

					try {
						userRepository.save(user);
					} catch (Exception e) {
						throw new ErrorSaveDataToDatabaseException();
					}

				} else {
					throw new EmailWrongCodeException();
				}

			} else {
				throw new EmailNotExistsException();
			}

		} else {
			throw new InvalidEmailException();
		}

	}

}
