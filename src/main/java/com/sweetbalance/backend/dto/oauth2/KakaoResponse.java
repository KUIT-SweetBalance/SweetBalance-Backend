package com.sweetbalance.backend.dto.oauth2;

import java.util.Map;

public class KakaoResponse implements OAuth2Response{

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;

    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
            return kakaoAccount.get("email").toString();
        }
        return null; // 이메일이 없는 경우 null 반환
    }

    @Override
    public String getName() {
        if (kakaoAccount != null && kakaoAccount.containsKey("profile")) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null && profile.containsKey("nickname")) {
                return profile.get("nickname").toString();
            }
        }
        return null; // 이름이 없는 경우 null 반환
    }
}
