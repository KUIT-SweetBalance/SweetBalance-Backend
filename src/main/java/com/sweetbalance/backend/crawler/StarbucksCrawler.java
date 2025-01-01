package com.sweetbalance.backend.crawler;

import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.enums.beverage.BeverageCategory;
import com.sweetbalance.backend.enums.beverage.BeverageSize;
import com.sweetbalance.backend.enums.common.Status;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class StarbucksCrawler extends BaseCrawler {

    @Value("${spring.selenium.base-url.starbucks}")
    private String BASE_URL;

    @Autowired
    public StarbucksCrawler(WebDriver driver) {
        super(driver);
    }

    /*
     * 현재는 음료 사진은 가져오지 않는 상태
     * 만약 음료 사진을 가져와 저장해야 하는 형태라면..
     * 추가적인 로직이 필요함 (가져오는 대상이 되는 element 위치 자체가 다름)
     */

    @Override
    public List<Beverage> crawlBeverageList() {
        List<Beverage> results = new ArrayList<>();

        try {
            // 사이트 이동
            navigateTo(BASE_URL);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 상위 단일 dd 태그 탐색
            WebElement ddSection = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[3]/div[7]/div[2]/div[2]/div/dl/dd[2]"))
            );

            // dd 태그 하위의 반복 구조 처리
            List<WebElement> childElements = ddSection.findElements(By.xpath("./*"));

            BeverageCategory currentCategory = null;

            for (WebElement child : childElements) {
                String tagName = child.getTagName();

                if ("h3".equals(tagName)) {
                    // 카테고리 이름 추출 및 매핑
                    String categoryName = child.getAttribute("textContent").trim();
                    System.out.println("카테고리 이름: " + categoryName);
                    currentCategory = determineBeverageCategory(categoryName);
                }

                else if ("table".equals(tagName)) {
                    // 테이블 처리
                    List<WebElement> beverageRows = child.findElements(By.xpath(".//tbody/tr"));

                    for (WebElement row : beverageRows) {
                        try {
                            // tr 하위 td 요소에서 데이터 추출
                            String name = row.findElement(By.xpath("./td[1]")).getAttribute("textContent").trim();

                            String caloriesText = row.findElement(By.xpath("./td[2]")).getAttribute("textContent").trim();
                            int calories = parseInteger(caloriesText);

                            String sugarText = row.findElement(By.xpath("./td[3]")).getAttribute("textContent").trim();
                            int sugar = parseInteger(sugarText);

                            String caffeineText = row.findElement(By.xpath("./td[5]")).getAttribute("textContent").trim();
                            int caffeine = parseInteger(caffeineText);

                            int volume = estimateVolume(name);

                            // Beverage 객체 생성
                            Beverage beverage = Beverage.builder()
                                    .name(name)
                                    .brand("Starbucks")
                                    .category(currentCategory)
                                    .size(BeverageSize.TALL)
                                    .sugar(sugar)
                                    .calories(calories)
                                    .caffeine(caffeine)
                                    .volume(volume)
                                    .status(Status.ACTIVE)
                                    .build();

                            results.add(beverage);
                        } catch (NumberFormatException e) {
                            // 특정 열이 "-"인 경우 건너뜀
                            System.err.println("Skipping row due to invalid number format: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // 드라이버 종료
        }

        return results;
    }

    // 카테고리 이름을 BeverageCategory Enum으로 매핑
    private BeverageCategory determineBeverageCategory(String name) {
        name = name.toLowerCase();
        if (name.contains("콜드 브루")) {
            return BeverageCategory.콜드브루;
        } else if (name.contains("브루드")) {
            return BeverageCategory.브루드커피;
        } else if (name.contains("아메리카노")) {
            return BeverageCategory.아메리카노;
        } else if (name.contains("에스프레소")) {
            return BeverageCategory.에스프레소;
        } else if (name.contains("티")) {
            return BeverageCategory.티;
        } else if (name.contains("주스") || name.contains("블렌디드") || name.contains("리프레셔") || name.contains("피지오")) {
            return BeverageCategory.주스;
        } else {
            return BeverageCategory.기타;
        }
    }

    // 음료 이름이나 특정 조건에 따라 부피를 계산하는 로직 추가 요함
    private int estimateVolume(String name) {
        return 355; // 기본값으로 Tall 사이즈 355ml 설정
    }

    // 문자열을 정수로 변환, "-"는 예외 처리 -> "에스프레소 플라이트" 때문에
    private int parseInteger(String text) {
        if (text.contains("-")) {
            throw new NumberFormatException("Invalid input: " + text);
        }
        return Integer.parseInt(text);
    }
}
