package com.sparta.spartadelivery.payment.domain.validator;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.payment.exception.PayErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AdminPaymentValidator {

    private final UserRepository userRepository;

    public void isAdmin(Long userId) {
        UserEntity user = findUserById(userId);

        if (user.getRole() == Role.MASTER || user.getRole() == Role.MANAGER) {
            return;
        }

        throw new AppException(PayErrorCode.NO_ACCESS_PERMISSION);
    }

    public String isMaster(Long userId) {
        UserEntity user = findUserById(userId);

        if (user.getRole() == Role.MASTER) {
            return  user.getUsername();
        }

        throw new AppException(PayErrorCode.NO_ACCESS_PERMISSION);
    }

   private UserEntity findUserById(Long userId) {
       return userRepository.findById(userId)
               .orElseThrow(()-> new AppException(AuthErrorCode.USER_NOT_FOUND));
   }

}
