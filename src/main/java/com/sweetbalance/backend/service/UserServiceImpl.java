package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.repository.BeverageLogRepository;
import com.sweetbalance.backend.repository.BeverageRepository;
import com.sweetbalance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final BeverageLogRepository beverageLogRepository;


    public void join(SignUpRequestDTO signUpRequestDTO){
        // 정규식 처리 등 가입 불가 문자에 대한 처리도 진행해주어야 한다.
        boolean userExists = userRepository.existsByUsername(signUpRequestDTO.getUsername());
        if(userExists) throw new RuntimeException("The username '" + signUpRequestDTO.getUsername() + "' is already taken.");

        User bCryptPasswordEncodedUser = makeBCryptPasswordEncodedUser(signUpRequestDTO);
        userRepository.save(bCryptPasswordEncodedUser);
    }

    public Optional<User> findUserByUserId(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 특정 사용자 ID로 음료 목록 조회
    public List<Beverage> findBeveragesByUserId(Long userId) {
        return userRepository.findBeveragesByUserId(userId);
    }


    private User makeBCryptPasswordEncodedUser(SignUpRequestDTO signUpRequestDTO){
        User user = signUpRequestDTO.toActiveUser();
        String rawPassword = user.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        return user;
    }

    @Override
    public void updateMetaData(User user, MetadataRequestDTO metaDataRequestDTO) {
        user.setGender(metaDataRequestDTO.getGender());
        user.setNickname(metaDataRequestDTO.getNickname());
        // user에 one_liner 추가하면 주석 해제
        // user.setOne_liner(metaDataRequestDTO.getOne_liner());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

    }

    @Override
    public void addBeverageRecord(User user, Beverage beverage, AddBeverageRecordRequestDTO addBeverageRecordRequestDTO) {
        BeverageLog beverageLog = new BeverageLog();
        beverageLog.setUser(user);
        beverageLog.setBeverage(beverage);
        beverageLog.setCreatedAt(LocalDateTime.now());
        beverageLog.setUpdatedAt(LocalDateTime.now());
//        beverageLog.setCount(addBeverageRecordRequestDTO.getCount());

        beverageLogRepository.save(beverageLog);
    }
}