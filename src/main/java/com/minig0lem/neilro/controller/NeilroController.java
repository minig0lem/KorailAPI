package com.minig0lem.neilro.controller;

import com.minig0lem.neilro.constants.StationCode;
import com.minig0lem.neilro.service.CrawlingManager;
import com.minig0lem.neilro.service.NeilroService;
import com.minig0lem.neilro.service.OpenApiManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.minig0lem.neilro.dto.NeilroDto.NeilroRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class NeilroController {

    private final NeilroService neilroService;
    private final OpenApiManager openApiManager;
    private final CrawlingManager crawlingManager;

    @PostMapping("/neilro")
    public ResponseEntity<Void> findNeilro(@Valid @RequestBody NeilroRequest neilroRequest) {
        //openApiManager.fetch(neilroRequest);
        crawlingManager.crawlNeilroTrain(neilroRequest);
        return ResponseEntity.ok().build();
    }
}
