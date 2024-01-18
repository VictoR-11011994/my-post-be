package com.victorcarablut.code.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.victorcarablut.code.security.jwt.JwtAuthenticationFilter;

@Configuration
public class SecurityConfiguration {
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthFilter;
	
	@Autowired
	private AuthenticationProvider authenticationProvider;
	
	private static final String[] LIST_URLS_FREE_ACCESS = { 
			"/api/account/**",
			"/api/public/**"
	};
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		return http
				.cors().and().csrf().disable() // prevent message error in browser: No 'Access-Control-Allow-Origin' - (disable if using different authentication mechanism... like jwt)
				.authorizeHttpRequests()
				.requestMatchers(LIST_URLS_FREE_ACCESS).permitAll()
				.anyRequest().authenticated()
				.and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.authenticationProvider(authenticationProvider)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

}
