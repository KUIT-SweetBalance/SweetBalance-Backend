package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.user.LoginType;

import java.util.Optional;

public interface UserService {

    // user date
    void join(SignUpRequestDTO signUpRequestDTO);
    void updateMetaData(User user, MetadataRequestDTO metaDataRequestDTO);
    boolean resetPassword(String email, String newPassword);
    void softDeleteUser(User user);

    Optional<User> findUserByUserId(Long userId);
    Optional<User> findUserByEmailAndLoginTypeAndDeletedAtIsNull(String email, LoginType loginType);
    
    // email
    boolean sendEmailVerificationCode(String email);
    boolean checkEmailVerificationCode(String email, String code);
}
