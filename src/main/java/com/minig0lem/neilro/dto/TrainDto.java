package com.minig0lem.neilro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

public class TrainDto {
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrainRequest {
        @NotNull(message = "dep must not be null")
        private String dep;

        @NotNull(message = "arr must not be null")
        private String arr;

        @NotNull(message = "depTime must not be null")
        @Pattern(regexp = "^\\d{10}$", message = "depTime must be in the format of YYYYMMDDHH")
        private String depTime;

    }
    @Builder
    @Getter
    public static class TrainListResponse {
        private List<TrainResponse> trainList;
    }
    @Builder
    @Getter
    public static class TrainResponse {
        private String dep;
        private String depTime;
        private String arr;
        private String arrTime;
        private String trainType;
        private String  trainNum;
        private int forward;
        private int backward;
        private boolean available;
    }
    @Builder
    @Getter
    public static class OpenApiResponse {
        private List<OpenApiDto> dtoList;
    }
    @Builder
    @Getter
    public static class OpenApiDto {
        private String dep;
        private Long depTime;
        private String arr;
        private Long arrTime;
        private String trainType;
        private Long trainNum;
    }
    @Builder
    @Getter
    public static class CrawlingResponse {
        private List<Long> availableTrainNumList;
        private List<CrawlingDto> trainSeatsList;
    }
    @Builder
    @Getter
    public static class CrawlingDto {
        private int forward;
        private int backward;
        private boolean available;
    }
}
