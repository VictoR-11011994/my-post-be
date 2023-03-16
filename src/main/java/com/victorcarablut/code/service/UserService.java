package com.victorcarablut.code.service;

import java.io.IOException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Base64;
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
import org.springframework.web.multipart.MultipartFile;

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
import com.victorcarablut.code.repository.UserRepository;
import com.victorcarablut.code.exceptions.EmailWrongCodeException;
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

	// find only email: true/false
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
					user.setUsername(uuid.substring(0, 7).toLowerCase() + "-" + dateFormat.format(new Date()));

				} catch (Exception e) {
					throw new GenericException();
				}

				user.setPassword(passwordEncoder.encode(userDto.getPassword()));
				user.setRegisteredDate(LocalDateTime.now());
				user.setRole(Role.USER); // first time: default role is USER
				user.setEnabled(false); // first time: account is disabled (necessary verification code from email)

				// upload default user profile image
				final String userImgBase64 = "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";
				final byte[] decodeUserImgBase64 = Base64.getDecoder().decode(userImgBase64);
				user.setUserProfileImg(decodeUserImgBase64);

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

	// update User (exists another separate update for: email, username, password,
	// ...)
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

	public void updateUserProfileImg(String email, MultipartFile file) throws IOException {

		if (emailInputIsValid(email)) {

			if (existsUserByEmail(email)) {

				// TODO: separated method
//				private String bytesIntoHumanReadable(long bytes) {
//				    long kilobyte = 1024;
//				    long megabyte = kilobyte * 1024;
//				    long gigabyte = megabyte * 1024;
//				    long terabyte = gigabyte * 1024;
//
//				    if ((bytes >= 0) && (bytes < kilobyte)) {
//				        return bytes + " B";
//
//				    } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
//				        return (bytes / kilobyte) + " KB";
//
//				    } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
//				        return (bytes / megabyte) + " MB";
//
//				    } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
//				        return (bytes / gigabyte) + " GB";
//
//				    } else if (bytes >= terabyte) {
//				        return (bytes / terabyte) + " TB";
//
//				    } else {
//				        return bytes + " Bytes";
//				    }
//				}
//				
				// final String userImgBase64 = userDto.getUserProfileImg();
				// final byte[] decodeUserImgBase64 = Base64.getDecoder().decode(userImgBase64);
				// final long imgSize = decodeUserImgBase64.length;
				byte[] decodeUserImgBase64 = null;
				try {
					decodeUserImgBase64 = file.getBytes();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				final long imgSize = decodeUserImgBase64.length;
				Long finalImgSize = 0L;
				String imgSizeFile = "NULL";
				String imgType = file.getContentType().toString();

				long kilobyte = 1024;
				long megabyte = kilobyte * 1024;
				long gigabyte = megabyte * 1024;
				long terabyte = gigabyte * 1024;

				if ((imgSize >= 0) && (imgSize < kilobyte)) {
					finalImgSize = imgSize; // B
					imgSizeFile = "B";

				} else if ((imgSize >= kilobyte) && (imgSize < megabyte)) {
					finalImgSize = (imgSize / kilobyte); // KB
					imgSizeFile = "KB";

				} else if ((imgSize >= megabyte) && (imgSize < gigabyte)) {
					finalImgSize = (imgSize / megabyte); // MB
					imgSizeFile = "MB";

				} else if ((imgSize >= gigabyte) && (imgSize < terabyte)) {
					finalImgSize = (imgSize / gigabyte); // GB
					imgSizeFile = "GB";

				} else if (imgSize >= terabyte) {
					finalImgSize = (imgSize / terabyte); // TB
					imgSizeFile = "TB";

				} else {
					finalImgSize = imgSize;
					imgSizeFile = "NULL";
				}

				System.out.println(finalImgSize + imgSizeFile + " " + imgType);

				// max: 10 MB
				if ((finalImgSize > 10 && imgSizeFile == "MB") || imgSizeFile == "GB" || imgSizeFile == "TB"
						|| imgSizeFile == "NULL" || !imgType.equals("image/jpeg")) {
					throw new GenericException();
				} else {
					User user = userRepository.findByEmail(email);

					user.setUserProfileImg(file.getBytes());

					try {
						userRepository.save(user);
					} catch (Exception e) {
						throw new ErrorSaveDataToDatabaseException();
					}
				}

			} else {
				throw new EmailNotExistsException();
			}

		} else {
			throw new InvalidEmailException();
		}
	}

	public void deleteUserProfileImg(UserDto userDto) {
		
		if (emailInputIsValid(userDto.getEmail())) {

			if (existsUserByEmail(userDto.getEmail())) {
				
				User user = userRepository.findByEmail(userDto.getEmail());

				user.setUserProfileImg(null);

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

}
