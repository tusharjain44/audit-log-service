package com.example.audit.service;
import com.example.audit.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserAccountRepository repository;
    public CustomUserDetailsService(UserAccountRepository repository) { this.repository = repository; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findById(username)
            .map(acc -> User.builder().username(acc.getUsername()).password(acc.getPassword()).roles(acc.getRole()).build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}