package com.victorcarablut.code.config.email;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

// Email Configuration (multiple hosts)

@Configuration
public class EmailConfig {
	
	@Value("${mail.host}")
	private String host;
	
	@Value("${mail.port}")
	private int port;
	
	// primary
	@Value("${mail.username.primary}")
	private String usernamePrimary;
	
	@Value("${mail.password.primary}")
	private String passwordPrimary;
	
	// no-reply
	@Value("${mail.username.no-reply}")
	private String usernameNoReply;
	
	@Value("${mail.password.no-reply}")
	private String passwordNoReply;
	
	
	// properties
	@Value("${mail.smtp.auth}")
	private boolean smtpAuth;
	
	@Value("${mail.smtp.starttls.enable}")
	private boolean smtpStartTlsEnable;
	
	@Value("${mail.smtp.connectiontimeout}")
	private int smtpConnectionTimeout;
	
	@Value("${mail.smtp.writetimeout}")
	private int smtpWriteTimeout;
	
	@Value("${mail.smtp.timeout}")
	private int smtpTimeout;


	// primary
	@Bean
	@Qualifier("javaMailSenderPrimary")
	@Primary
	JavaMailSender javaMailSenderPrimary() {
	    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	    mailSender.setHost(host);
	    mailSender.setPort(port);

	    mailSender.setUsername(usernamePrimary);
	    mailSender.setPassword(passwordPrimary);

	    Properties properties = mailSender.getJavaMailProperties();
	    properties.put("mail.smtp.auth", smtpAuth);
	    properties.put("mail.smtp.starttls.enable", smtpStartTlsEnable);
	    properties.put("mail.smtp.connectiontimeout", smtpConnectionTimeout);
	    properties.put("mail.smtp.writetimeout", smtpWriteTimeout);
	    properties.put("mail.smtp.timeout", smtpTimeout);
	    
	    return mailSender;
	}
	

	// no-reply
	@Bean
	@Qualifier("javaMailSenderNoReply")
	JavaMailSender javaMailSenderNoReply() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	    mailSender.setHost(host);
	    mailSender.setPort(port);

	    mailSender.setUsername(usernameNoReply);
	    mailSender.setPassword(passwordNoReply);

	    Properties properties = mailSender.getJavaMailProperties();
	    properties.put("mail.smtp.auth", smtpAuth);
	    properties.put("mail.smtp.starttls.enable", smtpStartTlsEnable);
	    properties.put("mail.smtp.connectiontimeout", smtpConnectionTimeout);
	    properties.put("mail.smtp.writetimeout", smtpWriteTimeout);
	    properties.put("mail.smtp.timeout", smtpTimeout);

	    return mailSender;
	}

	

}
