package com.sweetbalance.backend.dto.identity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User, UserIdHolder {

    private final AuthUserDTO authUserDTO;
    private boolean isNew;

    public CustomOAuth2User(AuthUserDTO authUserDTO) {
        this.authUserDTO = authUserDTO;
        this.isNew = false;
    }

    public CustomOAuth2User(AuthUserDTO authUserDTO, boolean isNew) {
        this.authUserDTO = authUserDTO;
        this.isNew = isNew;
    }

    // 서드파티별로 Response 구조가 달라 사용 X
    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                return authUserDTO.getRole();
            }
        });

        return collection;
    }

    @Override
    public String getName() {
        return authUserDTO.getEmail();
    }

    @Override
    public Long getUserId() {
        return authUserDTO.getUserId();
    }

    public String getEmail() {
        return authUserDTO.getEmail();
    }

    public boolean isNewUser(){
        return isNew;
    }
}