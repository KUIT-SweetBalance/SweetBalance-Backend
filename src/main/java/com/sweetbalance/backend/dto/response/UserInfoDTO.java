package com.sweetbalance.backend.dto.response;

import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.user.Gender;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class UserInfoDTO {
    private String email;
    private String nickname;
    private Gender gender;

    public UserInfoDTO(User user) {
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.gender = user.getGender();
    }
}
