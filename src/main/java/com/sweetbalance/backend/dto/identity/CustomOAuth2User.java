package com.sweetbalance.backend.dto.identity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User, UserIdHolder {

    private final AuthUserDTO authUserDTO;

    public CustomOAuth2User(AuthUserDTO authUserDTO) {
        this.authUserDTO = authUserDTO;
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

    // SecurityContextHolder의 name 값은 username을 뜻함
    @Override
    public String getName() {
        return authUserDTO.getUsername();
    }

    @Override
    public Long getUserId() {
        return authUserDTO.getUserId();
    }

    public String getUsername() {
        return authUserDTO.getUsername();
    }
}