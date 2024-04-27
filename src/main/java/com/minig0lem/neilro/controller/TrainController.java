package com.minig0lem.neilro.controller;

import com.minig0lem.neilro.dto.TrainDto.TrainRequest;
import com.minig0lem.neilro.dto.TrainDto.TrainListResponse;
import com.minig0lem.neilro.service.TrainService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TrainController {
    private final TrainService trainService;
    @PostMapping("/train")
    public ResponseEntity<TrainListResponse> findTrainList(@Valid @RequestBody TrainRequest trainRequest) {
        return ResponseEntity.ok(trainService.getTrainInfo(trainRequest));
    }
}
