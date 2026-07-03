package com.version1.backend;

import com.version1.backend.enums.Role;
import com.version1.backend.pojo.User;
import com.version1.backend.enums.UserStatus;
import com.version1.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	@Profile({ "local", "cloud-run" })
	public CommandLineRunner seedDatabase(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.seed.provider.email}") String email,
			@Value("${app.seed.provider.password}") String password) {
		return args -> {
			if (!userRepository.existsByEmail(email)) {
				User provider = User.builder()
						.email(email)
						.passwordHash(passwordEncoder.encode(password))
						.role(Role.PROVIDER)
						.status(UserStatus.ACTIVE)
						.build();
				User savedProvider = userRepository.save(provider);
				System.out.println("SEED: Created default provider user with ID: " + savedProvider.getId());
			} else {
				userRepository.findByEmail(email).ifPresent(provider -> {
					System.out.println("SEED: Found existing provider user with ID: " + provider.getId());
				});
			}
		};
	}

}
