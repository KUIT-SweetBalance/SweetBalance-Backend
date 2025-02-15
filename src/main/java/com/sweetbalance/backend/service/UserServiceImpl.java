package com.sweetbalance.backend.service;

import com.nimbusds.jose.util.Pair;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.common.Status;
import com.sweetbalance.backend.enums.user.LoginType;
import com.sweetbalance.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final Map<String, Pair<String, LocalDateTime>> emailVerificationCodeStore = new ConcurrentHashMap<>();

    private final Map<String, LocalDateTime> verifiedEmailStore = new ConcurrentHashMap<>();

    @Override
    public void join(SignUpRequestDTO signUpRequestDTO){
        User bCryptPasswordEncodedUser = makeBCryptPasswordEncodedUser(signUpRequestDTO);
        userRepository.save(bCryptPasswordEncodedUser);
    }

    private User makeBCryptPasswordEncodedUser(SignUpRequestDTO signUpRequestDTO){
        User user = signUpRequestDTO.toActiveUser();
        String rawPassword = user.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        return user;
    }

    @Override
    public Optional<User> findUserByUserId(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> findUserByEmailAndLoginTypeAndDeletedAtIsNull(String email, LoginType loginType) {
        return userRepository.findByEmailAndLoginTypeAndDeletedAtIsNull(email, loginType);
    }

    @Override
    public void softDeleteUser(User user) {
        user.setStatus(Status.DELETED);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void updateMetaData(User user, MetadataRequestDTO metaDataRequestDTO) {
        user.setGender(metaDataRequestDTO.getGender());
        user.setNickname(metaDataRequestDTO.getNickname());
        userRepository.save(user);
    }

    @Override
    public boolean sendEmailVerificationCode(String email) {
        String verificationCode = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(3);
        emailVerificationCodeStore.put(email, Pair.of(verificationCode, expirationTime));
        try {
            emailService.sendEmailVerificationCodeMail(email, verificationCode);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean checkEmailVerificationCode(String email, String code) {
        Pair<String, LocalDateTime> storedInfo = emailVerificationCodeStore.get(email);
        if (storedInfo == null) {
            return false;
        }
        String storedCode = storedInfo.getLeft();
        LocalDateTime expirationTime = storedInfo.getRight();
        if (LocalDateTime.now().isAfter(expirationTime)) {
            emailVerificationCodeStore.remove(email);
            return false;
        }
        if (storedCode.equals(code)) {
            emailVerificationCodeStore.remove(email);
            verifiedEmailStore.put(email, LocalDateTime.now().plusMinutes(5));
            return true;
        }
        return false;
    }

    @Override
    public boolean resetPassword(String email, String newPassword) {
        LocalDateTime verifiedExpiration = verifiedEmailStore.get(email);
        if (verifiedExpiration == null || LocalDateTime.now().isAfter(verifiedExpiration)) {
            return false;
        }
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();
        String encodedNewPassword = bCryptPasswordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        userRepository.save(user);
        verifiedEmailStore.remove(email);
        return true;
    }
}