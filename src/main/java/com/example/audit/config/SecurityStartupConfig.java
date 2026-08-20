package com.example.audit.config;
import com.example.audit.model.UserAccount;
import com.example.audit.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityStartupConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityStartupConfig.class);

    @Bean
    public CommandLineRunner seedUsers(UserAccountRepository repo, PasswordEncoder encoder) {
        return args -> {
            String adminPass = System.getenv("ADMIN_PASSWORD");
            String userPass = System.getenv("USER_PASSWORD");
            
            if (adminPass == null || adminPass.isBlank() || userPass == null || userPass.isBlank()) {
                throw new IllegalStateException("CRITICAL SECURITY FAILURE: Missing required environment secrets.");
            }

            if (!repo.existsById("admin")) {
                UserAccount admin = new UserAccount(); admin.setUsername("admin"); admin.setPassword(encoder.encode(adminPass)); admin.setRole("ADMIN");
                repo.save(admin);
                UserAccount user = new UserAccount(); user.setUsername("user-1"); user.setPassword(encoder.encode(userPass)); user.setRole("USER");
                repo.save(user);
            }
        };
    }
}