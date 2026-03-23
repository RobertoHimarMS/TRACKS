package es.rhms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages="es.rhms")
public class TracksApplication {

	public static void main(String[] args) {
		SpringApplication.run(TracksApplication.class, args);
	}

}