package com.victorcarablut.code.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


// Not used yet

@Service
public class EmailService {
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	//@Value("${spring.mail.username}")
	private String sender;
	
	// Simple (No HTML template)
	public String sendEmail(String receiver, String title, String message) {
		
		try {
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
			simpleMailMessage.setFrom(sender); 
			simpleMailMessage.setTo(receiver);
			simpleMailMessage.setSubject(title);
			simpleMailMessage.setText(message);
						
			javaMailSender.send(simpleMailMessage);
			System.out.println("Email sended");
			return "Email sended!";
		} catch (Exception e) {
			System.out.println("Error sending Email");
			System.out.println(e);
			return "Error sending Email!";
		}
		
	}

}
