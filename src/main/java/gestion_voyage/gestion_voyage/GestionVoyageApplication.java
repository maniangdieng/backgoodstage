package gestion_voyage.gestion_voyage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "gestion_voyage.gestion_voyage.repository")
@EnableScheduling

public class GestionVoyageApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionVoyageApplication.class, args);
	}
}