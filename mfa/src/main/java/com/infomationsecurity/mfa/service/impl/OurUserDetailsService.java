package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OurUserDetailsService implements UserDetailsService {
    private final String LOG_PREFIX = "[OurUserDetailsService]: ";
    private final AccountRepository accountRepository;

    /**
     * Loads the user details based on the provided username for authentication purposes.
     * This method is used by Spring Security during the authentication process.
     *
     * @param username the username identifying the user whose data is required
     * @return a {@link UserDetails} object containing user's authentication and authorization information
     * @throws UsernameNotFoundException if no user is found with the given username
     */
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("{} Loading user by username: {}", LOG_PREFIX, username);

        return accountRepository.findByAccountUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("Account not found with username: " + username));
    }
}
