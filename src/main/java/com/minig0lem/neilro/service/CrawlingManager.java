package com.minig0lem.neilro.service;

import com.minig0lem.neilro.dto.NeilroDto.NeilroRequest;
import com.minig0lem.neilro.dto.NeilroDto.CrawlingResponse;
import com.minig0lem.neilro.exceptions.CrawlingException;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class CrawlingManager {
    @Value("${crawling.url}")
    private String url;

    @Value("${crawling.driver-path}")
    private String path;
    public CrawlingResponse crawlNeilroTrain(NeilroRequest neilroRequest) {
        //크롬드라이버 연결
        WebDriver driver = getWebDriver();
        //일반승차권 예매 페이지 연결
        driver.get(url);

        //출발역 설정
        setInput(driver, "start", neilroRequest.getDep());
        //도착역 설정
        setInput(driver, "get", neilroRequest.getArr());

        String depTime = neilroRequest.getDepTime();
        //출발일 설정
        selectInput(driver, "s_year", depTime.substring(0, 4));
        selectInput(driver, "s_month", depTime.substring(4, 6));
        selectInput(driver, "s_day", depTime.substring(6, 8));
        //조회하기 버튼 클릭
        click(driver, ".btn_inq > a");
        //다음 페이지로 넘어갈 때까지 대기
        wait(driver, "직통");

        List<WebElement> elements = driver.findElements(By.xpath("//a[@title='SeatMap예약']"));

        for(WebElement element: elements) {
            log.info("{}", element.getAttribute("outerHTML"));
        }

        driver.quit();

        return null;
    }


    private void wait(WebDriver driver, String title) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.titleContains(title));
    }

    private void click(WebDriver driver, String name) {
        WebElement button = driver.findElement(By.cssSelector(name));
        button.click();
    }
    private void setInput(WebDriver driver, String id, String value) {
        WebElement inputElement = driver.findElement(By.id(id));
        inputElement.clear();
        inputElement.sendKeys(value);
    }

    private void selectInput(WebDriver driver, String id, String value) {
        try {
            WebElement selectElement = driver.findElement(By.id(id));
            Select select = new Select(selectElement);
            select.selectByValue(value);
        } catch(Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new CrawlingException("입력 날짜 오류");
        }
    }

    private WebDriver getWebDriver() {
        System.setProperty("webdriver.chrome.driver", path);
        ChromeOptions options = new ChromeOptions();

        //크롬 브라우저가 뜨는 것을 방지(헤드리스 모드)
        options.addArguments("--headless");
        return new ChromeDriver(options);
    }
}
