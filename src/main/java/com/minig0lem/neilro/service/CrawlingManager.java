package com.minig0lem.neilro.service;

import com.minig0lem.neilro.dto.TrainDto.CrawlingDto;
import com.minig0lem.neilro.dto.TrainDto.TrainRequest;
import com.minig0lem.neilro.dto.TrainDto.CrawlingResponse;
import com.minig0lem.neilro.exceptions.CrawlingException;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
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
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CrawlingManager {
    @Value("${crawling.url}")
    private String url;

    @Value("${crawling.driver-path}")
    private String path;
    public CrawlingResponse crawlSeatsInfo(TrainRequest trainRequest) {

        try {
            //크롬드라이버 연결
            WebDriver driver = getWebDriver();
            //일반승차권 예매 페이지 연결
            driver.get(url);
            //페이지 이동 시 기다릴 wait 객체 생성
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
            //사용자 입력 값을 기반으로 한 열차 시간표 페이지 이동
            viewTrainSchedulePage(trainRequest, driver, wait);
            //열차 좌석 수 가져오기
            CrawlingResponse response = getTrainSeatsList(driver, wait);

            driver.quit();
            return response;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new CrawlingException("웹 페이지 Crawling 오류: " + e.getMessage());
        }



    }
    private CrawlingResponse getTrainSeatsList(WebDriver driver, WebDriverWait wait) {
        List<CrawlingDto> crawlingDtoList = new ArrayList<>();
        List<Long> trainNumList = new ArrayList<>();
        //좌석 선택 가능한 열차 수 가져오기
        int num = getAvailableTrainsNum(driver);
        log.info("{}" , num);
        for(int i = 0; i < num; i++) {
            List<WebElement> links = driver.findElements(By.xpath("//a[contains(@href, 'infochk(6')]"));
            //열차 번호 가져오기
            WebElement curLink = links.get(i);
            String trainNum = curLink.findElement(By.xpath("./parent::td/parent::tr/td[contains(@class, 'bdl_on')]/a/span")).getAttribute("innerHTML").trim();

            //좌석 선택 버튼 클릭
            curLink.click();

            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("korail-modal-seatmap")));
            } catch (TimeoutException e) {
                //KTX-산천 안내메세지 뜨는 경우
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("korail-modal-traininfo")));
                driver.switchTo().frame(driver.findElement(By.id("embeded-modal-traininfo")));
                driver.findElement(By.cssSelector(".btn_c > a")).click();
                driver.switchTo().defaultContent();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("korail-modal-seatmap")));
            }

            //좌석 선택 페이지로 프레임 전환
            driver.switchTo().frame(driver.findElement(By.id("embeded-modal-seatmap")));

            //예매 가능 좌석 수 계산하기
            CrawlingDto crawlingDto = getCrawlingDto(driver, wait);
            if(crawlingDto.isAvailable()) {
                crawlingDtoList.add(crawlingDto);
                trainNumList.add(Long.parseLong(trainNum));
            }

            //열차 시간표 페이지로 프레임 전환
            driver.findElement(By.cssSelector(".pop_close")).click();
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
            driver.navigate().refresh();
        }
        return CrawlingResponse
                .builder()
                .trainSeatsList(crawlingDtoList)
                .availableTrainNumList(trainNumList)
                .build();
    }
    private CrawlingDto getCrawlingDto(WebDriver driver, WebDriverWait wait) {
        //예약 가능한 호차의 수 가져오기
        int roomSize = getAvailableRoomsNum(driver);

        int forward = 0;
        int backward = 0;
        boolean available = false;

        for (int j = 1; j <= roomSize; j++) {
            WebElement cur = driver.findElement(By.cssSelector(".tra_num > p > a:nth-of-type(" + j + ")"));
            String roomNum = cur.getAttribute("innerHTML").split("호차")[0];

            //호차 선택
            if (j == 1) {
                cur.click();
            } else {
                driver.findElement(By.cssSelector(".btn_r > a")).click();
            }
            wait.until(ExpectedConditions.attributeToBe(By.name("txtSrcarNo"), "value", roomNum));

            //예매 가능 좌석 중 순방향, 역방향 좌석 가져오기
            List<WebElement> f = driver.findElements(By.xpath("//div[contains(@class, 'seat_box_in')]//span[contains(@class, 'on') and contains(@onmouseover, '순방')]"));
            List<WebElement> b = driver.findElements(By.xpath("//div[contains(@class, 'seat_box_in')]//span[contains(@class, 'on') and contains(@onmouseover, '역방')]"));
            forward += f.size();
            backward += b.size();
        }

        //예매 가능 좌석이 1개라도 있으면 예매 가능
        if(forward + backward != 0) available = true;

        log.info("forward: {}, backward: {}" , forward, backward);
        return CrawlingDto.builder()
                .available(available)
                .forward(forward)
                .backward(backward)
                .build();
    }
    private int getAvailableRoomsNum(WebDriver driver) {
        return driver.findElements(By.cssSelector(".tra_num > p > a")).size();
    }
    private int getAvailableTrainsNum(WebDriver driver) {
        return driver.findElements(By.xpath("//a[contains(@href, 'infochk(6')]")).size();
    }
    private void viewTrainSchedulePage (TrainRequest trainRequest, WebDriver driver, WebDriverWait wait)  {
        //출발역 설정
        setInput(driver, "start", trainRequest.getDep());
        //도착역 설정
        setInput(driver, "get", trainRequest.getArr());

        String depTime = trainRequest.getDepTime();
        //출발일, 시간 설정
        selectInput(driver, "s_year", depTime.substring(0, 4));
        selectInput(driver, "s_month", depTime.substring(4, 6));
        selectInput(driver, "s_day", depTime.substring(6, 8));
        selectInput(driver, "s_hour", depTime.substring(8, 10));
        //인접역포함 체크 해제
        driver.findElement(By.id("adjcCheckYn")).click();
        //조회하기 버튼 클릭
        WebElement button = driver.findElement(By.cssSelector(".btn_inq > a"));
        button.click();
        //다음 페이지로 넘어갈 때까지 대기 (10초)
        wait.until(ExpectedConditions.titleContains("직통"));
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
