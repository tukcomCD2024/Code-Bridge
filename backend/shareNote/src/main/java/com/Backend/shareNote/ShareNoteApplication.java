package com.Backend.shareNote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class ShareNoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShareNoteApplication.class, args);
	}

}
