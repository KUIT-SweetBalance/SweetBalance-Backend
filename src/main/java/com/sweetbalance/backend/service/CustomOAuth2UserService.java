package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.identity.AuthUserDTO;
import com.sweetbalance.backend.dto.identity.CustomOAuth2User;
import com.sweetbalance.backend.dto.oauth2.GoogleResponse;
import com.sweetbalance.backend.dto.oauth2.KakaoResponse;
import com.sweetbalance.backend.dto.oauth2.NaverResponse;
import com.sweetbalance.backend.dto.oauth2.OAuth2Response;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.user.LoginType;
import com.sweetbalance.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.sweetbalance.backend.enums.common.Status.ACTIVE;
import static com.sweetbalance.backend.enums.user.LoginType.*;
import static com.sweetbalance.backend.enums.user.Role.USER;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Autowired
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        LoginType currentLoginType = null;
        OAuth2Response oAuth2Response = null;

        if (registrationId.equals("naver")) {
            currentLoginType = NAVER;
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("kakao")) {
            currentLoginType = KAKAO;
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("google")) {
            currentLoginType = GOOGLE;
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else {

            return null;
        }

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만들어 존재하는 유저인지 확인
        String providerID = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        Optional<User> existData = userRepository.findByProviderIdAndDeletedAtIsNull(providerID);

        // DB에 존재하지 않는 신규 유저일 때
        if (existData.isEmpty()) {
            User newUser = new User();
            newUser.setRole(USER);
            newUser.setEmail(oAuth2Response.getEmail());
            newUser.setNickname(oAuth2Response.getName());
            newUser.setProviderId(providerID);
            newUser.setLoginType(currentLoginType);
            newUser.setStatus(ACTIVE);

            User createdUser = userRepository.save(newUser);

            AuthUserDTO authUserDTO = new AuthUserDTO();
            authUserDTO.setUserId(createdUser.getUserId());
            authUserDTO.setEmail(createdUser.getEmail());
            authUserDTO.setRole(createdUser.getRole().getValue());

            return new CustomOAuth2User(authUserDTO, true);
        }

        // DB에 이미 존재하는 유저일 때
        else {
            User existUser = existData.get();
            existUser.setEmail(oAuth2Response.getEmail()); // 계정에 연동된 이메일 변동시에만 업데이트

            userRepository.save(existUser);

            AuthUserDTO authUserDTO = new AuthUserDTO();
            authUserDTO.setUserId(existUser.getUserId());
            authUserDTO.setEmail(existUser.getEmail());
            authUserDTO.setRole(existUser.getRole().getValue());

            return new CustomOAuth2User(authUserDTO, false);
        }
    }
}