package com.sweetbalance.backend.dto.request;

import com.sweetbalance.backend.enums.user.Gender;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class MetadataRequestDTO {
        private String nickname;
        private Gender gender;
//        private String one_liner;
}
