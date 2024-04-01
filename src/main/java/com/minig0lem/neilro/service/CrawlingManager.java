package com.minig0lem.neilro.service;

import com.minig0lem.neilro.dto.NeilroDto.NeilroRequest;
import com.minig0lem.neilro.dto.NeilroDto.CrawlingResponse;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CrawlingManager {
    @Value("${crawling.url}")
    private String url;

    @Value("${crawling.driver-path}")
    private String path;
    public CrawlingResponse crawlNeilroTrain(NeilroRequest neilroRequest) {
        System.setProperty("webdriver.chrome.driver", path);

        ChromeOptions options = new ChromeOptions();
        //크롬 브라우저가 뜨는 것을 방지(헤드리스 모드)
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);

        driver.get(url);

        log.info("{}", driver.getTitle());

        driver.quit();

        return null;
    }
}
