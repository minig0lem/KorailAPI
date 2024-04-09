package com.minig0lem.neilro.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

public class NeilroDto {

    @Getter
    public static class NeilroRequest {
        @Max(value = 9999, message = "Page number must be less than 10000")
        @Min(value = 1, message = "Page number must be greater than 0")
        private int pageNo;

        @Max(value = 9999, message = "numOfRows must be less than 10000")
        @Min(value = 1, message = "numOfRows must be greater than 0")
        private int numOfRows;

        @NotNull(message = "dep must not be null")
        private String dep;

        @NotNull(message = "arr must not be null")
        private String arr;

        @NotNull(message = "depTime must not be null")
        @Pattern(regexp = "^\\d{10}$", message = "depTime must be in the format of YYYYMMDDHH")
        private String depTime;

        public NeilroRequest(int pageNo, int numOfRows, String dep, String arr, String depTime) {
            this.pageNo = pageNo == 0 ? 1 : pageNo;
            this.numOfRows = numOfRows == 0 ? 10 : numOfRows;
            this.dep = dep;
            this.arr = arr;
            this.depTime = depTime;
        }
    }

    @Builder
    public static class NeilroListResponse {
        private Long totalCount;
        private List<NeilroResponse> neilroList;
    }

    @Builder
    public static class NeilroResponse {
        private String dep;
        private String depTime;
        private String arr;
        private String arrTime;
        private String trainType;
        private Long trainNum;
    }

    @Builder
    public static class OpenApiResponse {
        private Long totalCount;
        private List<OpenApiVo> voList;
    }

    @Data
    @Builder
    public static class OpenApiVo {
        private String dep;
        private Long depTime;
        private String arr;
        private Long arrTime;
        private String trainType;
        private Long trainNum;

        public NeilroResponse toResponse() {
            return NeilroResponse.builder()
                    .dep(dep)
                    .depTime(String.valueOf(depTime))
                    .arr(arr)
                    .arrTime(String.valueOf(arrTime))
                    .trainType(trainType)
                    .trainNum(trainNum)
                    .build();
        }
    }
    @Builder
    public static class CrawlingResponse {
        private List<Integer> NeilroTrainNumList;
    }
}
