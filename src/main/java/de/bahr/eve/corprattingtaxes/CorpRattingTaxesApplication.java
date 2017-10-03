package de.bahr.eve.corprattingtaxes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CorpRattingTaxesApplication {

	public static void main(String[] args) {
		SpringApplication.run(CorpRattingTaxesApplication.class, args);
	}
}
