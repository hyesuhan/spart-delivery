package com.sparta.spartadelivery.global.infrastructure.config.security;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.exception.ErrorCode;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserEntity user = (UserEntity) userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserPrincipal.from(user);
    }
}
