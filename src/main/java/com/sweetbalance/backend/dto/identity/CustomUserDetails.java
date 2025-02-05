package com.sweetbalance.backend.dto.identity;

import com.sweetbalance.backend.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails, UserIdHolder {

    private Long userId;
    private String email;
    private String password;
    private String role;

    public CustomUserDetails(AuthUserDTO authUserDTO) {
        this.userId = authUserDTO.getUserId();
        this.email = authUserDTO.getEmail();
        this.password = "TMP_PASSWORD";
        this.role = authUserDTO.getRole();
    }

    public CustomUserDetails(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole().getValue();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                return role;
            }
        });

        return collection;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() { // 이메일이 UserDetails 에서 username 역할로 변경
        return email;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Long getUserId(){
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}