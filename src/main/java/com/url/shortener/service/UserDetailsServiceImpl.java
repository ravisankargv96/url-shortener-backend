package com.url.shortener.service;

import com.url.shortener.models.User;
import com.url.shortener.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service implementation specifying how to retrieve user-specific data 
 * during the Spring Security authentication process.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    /**
     * Locates the user based on the username and maps their specific authorities.
     * Required internally by Spring Security's AuthenticationManager during login attempts.
     *
     * @param username the username identifying the user whose data is required.
     * @return a fully populated UserDetails object
     * @throws UsernameNotFoundException if the user cannot be found in the system
     */
    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with username: "+username));
        return UserDetailsImpl.build(user);
    }
}
