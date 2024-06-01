package com.Backend.shareNote.domain.User.service;

import com.Backend.shareNote.domain.User.dto.CustomUserDetails;
import com.Backend.shareNote.domain.User.entity.Users;
import com.Backend.shareNote.domain.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {

        // DB에서 조회
        Optional<Users> userData = userRepository.findByEmail(userEmail);

        if (!userData.isEmpty()) {

            // UserDetails에 담아서 반환하면 AuthenticationManager가 검증 함
            // UserDetails는 DTO 느낌이네
            return new CustomUserDetails(userData.get());
        }

        return null;
    }
}
