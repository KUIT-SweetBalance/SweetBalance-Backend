package com.sweetbalance.backend.util.syrup;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Syrup {

    @NonNull
    private final String syrupName;

    @NonNull
    private final Double sugarPerPump;

    static Syrup of(String syrupName, Double sugarPerPump){
        return new Syrup(syrupName,sugarPerPump);
    }
}
