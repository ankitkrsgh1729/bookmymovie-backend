package com.bookmymovie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BookMyMovieBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookMyMovieBackendApplication.class, args);
	}

}
