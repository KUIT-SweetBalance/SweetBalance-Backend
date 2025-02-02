package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.entity.*;
import com.sweetbalance.backend.enums.common.Status;
import com.sweetbalance.backend.repository.*;
import com.sweetbalance.backend.util.syrup.SyrupManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final BeverageLogRepository beverageLogRepository;
    private final FavoriteRepository favoriteRepository;
    private final BeverageSizeRepository beverageSizeRepository;
    private final BeverageRepository beverageRepository;


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

    @Override
    public Optional<BeverageLog> findBeverageLogByBeverageLogId(Long beverageLogId) {
        return beverageLogRepository.findById(beverageLogId);
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
        userRepository.save(user);

    }

    @Override
    public void addBeverageRecord(User user, BeverageSize beverageSize, AddBeverageRecordRequestDTO dto) {
        BeverageLog beverageLog = new BeverageLog();
        beverageLog.setUser(user);
        beverageLog.setBeverageSize(beverageSize);
        beverageLog.setSyrupName(dto.getSyrupName());
        beverageLog.setSyrupCount(dto.getSyrupCount());
        beverageLog.setStatus(Status.ACTIVE);

        String brandName;
        Optional<BeverageSize> optionalBeverageSize = beverageSizeRepository.findById(beverageSize.getId());
        Beverage beverage = optionalBeverageSize.get().getBeverage();
        brandName = beverage.getBrand();

        double additionalSugar = (dto.getSyrupName() == null) ? 
                0D : dto.getSyrupCount() * SyrupManager.getAmountOfSugar(brandName, dto.getSyrupName());
        beverageLog.setAdditionalSugar(additionalSugar);

        beverageLogRepository.save(beverageLog);
        beverage.setConsumeCount(beverage.getConsumeCount() + 1);
        beverageRepository.save(beverage);
    }

    // 로그 Id로 음료 기록 찾고, 음료 사이즈 정보, 시럽 이름, 시럽 개수, 추가 당 함량 다시 설정.
    @Override
    public void editBeverageRecord(Long beverageLogId, BeverageSize beverageSize, AddBeverageRecordRequestDTO dto) {
        Optional<BeverageLog> log = beverageLogRepository.findById(beverageLogId);
        BeverageLog beverageLog = log.get();
        boolean isBeverageChanged = false;
        Beverage originalBeverage = beverageLog.getBeverageSize().getBeverage();
        if(originalBeverage.getBeverageId() != beverageSize.getBeverage().getBeverageId()){
            isBeverageChanged = true;
        }
        beverageLog.setBeverageSize(beverageSize);
        beverageLog.setSyrupName(dto.getSyrupName());
        beverageLog.setSyrupCount(dto.getSyrupCount());

        String brandName;
        Optional<BeverageSize> optionalBeverageSize = beverageSizeRepository.findById(beverageSize.getId());
        Beverage newBeverage = optionalBeverageSize.get().getBeverage();
        brandName = newBeverage.getBrand();

        double additionalSugar = (dto.getSyrupName() == null) ?
                0D : dto.getSyrupCount() * SyrupManager.getAmountOfSugar(brandName, dto.getSyrupName());
        beverageLog.setAdditionalSugar(additionalSugar);
        beverageLogRepository.save(beverageLog);

        if(isBeverageChanged){
            originalBeverage.setConsumeCount(originalBeverage.getConsumeCount() - 1);
            newBeverage.setConsumeCount(newBeverage.getConsumeCount() + 1);
            beverageRepository.save(originalBeverage);
            beverageRepository.save(newBeverage);
        }

    }

    @Override
    public void deleteBeverageRecord(BeverageLog beverageLog) {
        beverageLog.setStatus(Status.DELETED);
        beverageLogRepository.save(beverageLog);
    }

    @Override
    public void addFavoriteRecord(User user, Beverage beverage) {
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setBeverage(beverage);

        favoriteRepository.save(favorite);
    }

    @Override
    public void deleteFavoriteRecord(User user, Beverage beverage) {
        Optional<Favorite> favoriteOptional = favoriteRepository.findByUserAndBeverage(user, beverage);

        favoriteOptional.ifPresent(favoriteRepository::delete);
    }


}