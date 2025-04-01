package com.example.security_token;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * APP: Demo for testing Spring security.
 *  - Create User - Roles - Permission : schema  database for managing security User level.
 *  - Check security in Controller level (end-point) with @PreAuthorize
 *  - Configuration and Filters for Spring security implementation.
 *  	- JWT. Json Web Token.
 *  
 *  it needs a database running. ie:
 *  docker run --name postgres-security -e POSTGRES_PASSWORD=tu_password_postgres -e POSTGRES_DB=securityDemo
 *  -p 5432:5432 -d postgres:latest
 *  
 */
@SpringBootApplication
public class SecurityTokenApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityTokenApplication.class, args);
	}

}
