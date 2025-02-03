package com.sweetbalance.backend.util.syrup;

import org.springframework.stereotype.Component;

@Component
public class SugarCalculator {

    public double calculate(String brand, String syrupName, int syrupCount) {
        if(syrupName == null || syrupName.trim().isEmpty()){
            return 0D;
        }
        return syrupCount * SyrupManager.getAmountOfSugar(brand,syrupName);
    }
}
