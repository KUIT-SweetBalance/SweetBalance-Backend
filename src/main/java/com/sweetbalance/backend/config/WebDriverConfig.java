package com.sweetbalance.backend.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class WebDriverConfig {

    @Bean
    public WebDriver webDriver() {
        // WebDriver Manager 자동 설정
        WebDriverManager.chromedriver().setup();

        // Chrome 옵션 설정
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless=new");           // GUI 없이 헤드리스 모드로 Chrome 실행
        chromeOptions.addArguments("--lang=ko");                // 브라우저 언어를 한국어(ko)로 설정
        chromeOptions.addArguments("--no-sandbox");             // 샌드박스 모드 비활성화
        chromeOptions.addArguments("--disable-dev-shm-usage");  // /dev/shm(공유 메모리 디렉토리) 사용 비활성화
        chromeOptions.addArguments("--disable-gpu");            // GPU 가속 비활성화

        // WebDriver 인스턴스 생성
        WebDriver driver = new ChromeDriver(chromeOptions);
        // 페이지 로딩 시간 설정
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        return driver;
    }
}