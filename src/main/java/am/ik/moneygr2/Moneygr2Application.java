package am.ik.moneygr2;

import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication
@EnableJpaAuditing
public class Moneygr2Application {

	@Bean
	AuditorAware<String> auditorProvider() {
		return () -> {
			Authentication authentication = SecurityContextHolder.getContext()
					.getAuthentication();
			if (authentication == null) {
				return Optional.empty();
			}
			return Optional.of(authentication.getName());
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(Moneygr2Application.class, args);
	}
}
