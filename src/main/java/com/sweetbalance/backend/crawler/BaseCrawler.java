package com.sweetbalance.backend.crawler;

import com.sweetbalance.backend.entity.Beverage;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class BaseCrawler {

    protected WebDriver driver;

    @Autowired
    public BaseCrawler(WebDriver driver) {
        this.driver = driver;
    }

    public abstract List<Beverage> crawlBeverageList();

    // 페이지 이동 공통 메서드
    protected void navigateTo(String url) {
        driver.get(url);
    }

    // WebDriver 종료
    public void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}