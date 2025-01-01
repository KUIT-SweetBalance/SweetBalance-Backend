package com.sweetbalance.backend.service;

import com.sweetbalance.backend.crawler.StarbucksCrawler;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.repository.BeverageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CrawlingService {

    private final StarbucksCrawler starbucksCrawler;
    private final BeverageRepository beverageRepository;

    @Autowired
    public CrawlingService(StarbucksCrawler starbucksCrawler, BeverageRepository beverageRepository) {
        this.starbucksCrawler = starbucksCrawler;
        this.beverageRepository = beverageRepository;
    }

    public void executeCrawling() {

        // 스타벅스 데이터 크롤링 - 크롤링 전용 DTO나 데이터 무결성 검사 등은 구현되지 않음..
        List<Beverage> starbucksData = starbucksCrawler.crawlBeverageList();
        for (Beverage newBeverage : starbucksData) {

            Optional<Beverage> existingBeverage = beverageRepository.findByName(newBeverage.getName());

            if (existingBeverage.isPresent()) {
                Beverage beverage = existingBeverage.get();

                // 기존 데이터 업데이트
                updateExistingBeverage(beverage, newBeverage);
                beverageRepository.save(beverage);
            } else {

                // 새 데이터 삽입
                beverageRepository.save(newBeverage);
            }
        }

    }

    private void updateExistingBeverage(Beverage existingBeverage, Beverage newBeverage) {
        // 이름과 브랜드는 변경되지 않는다고 가정
        // existingBeverage.setName(newBeverage.getName());
        // existingBeverage.setBrand(newBeverage.getBrand());

        existingBeverage.setCategory(newBeverage.getCategory());
        existingBeverage.setSize(newBeverage.getSize());
        existingBeverage.setSugar(newBeverage.getSugar());
        existingBeverage.setCalories(newBeverage.getCalories());
        existingBeverage.setCaffeine(newBeverage.getCaffeine());
        existingBeverage.setVolume(newBeverage.getVolume());
        existingBeverage.setStatus(newBeverage.getStatus());
    }
}

