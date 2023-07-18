package com.admin.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author wang_dgang
 * @since 2018-10-22 15:50:48
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		new SpringApplication(Application.class).run(args);
	}

	@Override
	public SpringApplicationBuilder createSpringApplicationBuilder() {
		return new SpringApplicationBuilder(Application.class);
	}
}
