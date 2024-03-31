package com.minig0lem.neilro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.minig0lem.neilro.dto.NeilroDto.NeilroListResponse;
import static com.minig0lem.neilro.dto.NeilroDto.NeilroRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class NeilroService{

    private final OpenApiManager openApiManager;

//    public NeilroListResponse findNeilroTrain(NeilroRequest neilroRequest) {
//
//    }
}
