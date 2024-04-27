package com.minig0lem.neilro.train;

import com.google.gson.Gson;
import com.minig0lem.neilro.controller.TrainController;
import com.minig0lem.neilro.dto.TrainDto.TrainRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TrainControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Test
    @DisplayName("열차 좌석 조회 성공 테스트")
    void findTrainListTest() throws Exception {
        // Given
        TrainRequest request = TrainRequest.builder()
                .dep("서울")
                .arr("부산")
                .depTime("2024043018")
                .build();

        // When
        ResultActions result = mockMvc.perform(
                post("/api/train")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TrainController.class))
                .andExpect(handler().methodName("findTrainList"))
                .andExpect(jsonPath("$.trainList[0].dep", is(request.getDep())))
                .andExpect(jsonPath("$.trainList[0].arr", is(request.getArr())))
                .andExpect(jsonPath("$.trainList[0].depTime").value(startsWith(request.getDepTime().substring(0, 4))))
                .andExpect(jsonPath("$.trainList[0].forward").exists())
                .andExpect(jsonPath("$.trainList[0].backward").exists())
        ;
    }

    @Test
    @DisplayName("열차 좌석 조회 실패 테스트 (입력 값 누락)")
    void findTrainListFailureTest1() throws Exception {
        //Given
        TrainRequest request = TrainRequest.builder()
                .dep("서울")
                .build();

        //When
        ResultActions result = mockMvc.perform(
                post("/api/train")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        //Then
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(TrainController.class))
                .andExpect(handler().methodName("findTrainList"))
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.messages.length()", is(2)))
                .andExpect(jsonPath("$.messages", hasItem("arr must not be null")))
                .andExpect(jsonPath("$.messages", hasItem("depTime must not be null")))
        ;
    }
    @Test
    @DisplayName("열차 좌석 조회 실패 테스트 (출발 시각 입력 값 형태 오류 -> YYYYMMDDHH 형태가 아닌 경우)")
    void findTrainListFailureTest2() throws Exception {
        //Given
        TrainRequest request = TrainRequest.builder()
                .dep("서울")
                .arr("부산")
                .depTime("20240501")
                .build();

        //When
        ResultActions result = mockMvc.perform(
                post("/api/train")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        //Then
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(TrainController.class))
                .andExpect(handler().methodName("findTrainList"))
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.messages.length()", is(1)))
                .andExpect(jsonPath("$.messages[0]", is("depTime must be in the format of YYYYMMDDHH")))
        ;
    }

    @Test
    @DisplayName("열차 좌석 조회 실패 테스트 (출발 시각 입력 값 형태 오류 -> 없는 날짜 or 없는 시간 ex) 2024138130)")
    void findTrainListFailureTest4() throws Exception {
        //Given
        TrainRequest request = TrainRequest.builder()
                .dep("서울")
                .arr("부산")
                .depTime("2024138130")
                .build();

        //When
        ResultActions result = mockMvc.perform(
                post("/api/train")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        //Then
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(TrainController.class))
                .andExpect(handler().methodName("findTrainList"))
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", is("Open API 호출 오류")))
        ;
    }

    @Test
    @DisplayName("열차 좌석 조회 실패 테스트 (출발 시각 입력 값 이전 날짜 오류)")
    void findTrainListFailureTest3() throws Exception {
        //Given
        TrainRequest request = TrainRequest.builder()
                .dep("서울")
                .arr("부산")
                .depTime("2024032508")
                .build();

        //When
        ResultActions result = mockMvc.perform(
                post("/api/train")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        //Then
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(TrainController.class))
                .andExpect(handler().methodName("findTrainList"))
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message", is("현재 일자보다 이전 일자를 선택하셨습니다.")))
        ;
    }
}
