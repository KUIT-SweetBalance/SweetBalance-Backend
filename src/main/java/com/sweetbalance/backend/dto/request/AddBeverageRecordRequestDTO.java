package com.sweetbalance.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


public class AddBeverageRecordRequestDTO {

    private Long beverageSizeId;
    private String syrupName;
    private int syrupCount;

}
