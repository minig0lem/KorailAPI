package com.minig0lem.neilro.service;

import com.minig0lem.neilro.dto.TrainDto.TrainResponse;
import com.minig0lem.neilro.dto.TrainDto.CrawlingDto;
import com.minig0lem.neilro.dto.TrainDto.OpenApiDto;
import com.minig0lem.neilro.dto.TrainDto.CrawlingResponse;
import com.minig0lem.neilro.dto.TrainDto.OpenApiResponse;
import com.minig0lem.neilro.dto.TrainDto.TrainListResponse;
import com.minig0lem.neilro.dto.TrainDto.TrainRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class TrainService {

    private final OpenApiManager openApiManager;
    private final CrawlingManager crawlingManager;
    public TrainListResponse getTrainInfo(TrainRequest trainRequest) {
        OpenApiResponse openApiResponse = openApiManager.fetch(trainRequest);
        CrawlingResponse crawlingResponse = crawlingManager.crawlSeatsInfo(trainRequest);
        List<TrainResponse> list = new ArrayList<>();
        List<Long> trainNumList = crawlingResponse.getAvailableTrainNumList();

        int size = openApiResponse.getDtoList().size();
        int idx = 0;
        for(int i = 0; i < size; i++) {
            OpenApiDto openApiDto = openApiResponse.getDtoList().get(i);
            String trainNum = trainNumToString(openApiDto.getTrainNum());
            String depTime = convertDate(openApiDto.getDepTime());
            String arrTime = convertDate(openApiDto.getArrTime());

            if(trainNumList.contains(openApiDto.getTrainNum())) {
                CrawlingDto crawlingDto = crawlingResponse.getTrainSeatsList().get(idx++);
                list.add(TrainResponse.builder()
                        .dep(openApiDto.getDep())
                        .depTime(depTime)
                        .arr(openApiDto.getArr())
                        .arrTime(arrTime)
                        .trainType(openApiDto.getTrainType())
                        .trainNum(trainNum)
                        .forward(crawlingDto.getForward())
                        .backward(crawlingDto.getBackward())
                        .available(true)
                        .build());
            } else {
                list.add(TrainResponse.builder()
                        .dep(openApiDto.getDep())
                        .depTime(depTime)
                        .arr(openApiDto.getArr())
                        .arrTime(arrTime)
                        .trainType(openApiDto.getTrainType())
                        .trainNum(trainNum)
                        .available(false)
                        .build());
            }
        }

        return TrainListResponse.builder()
                .trainList(list)
                .build();
    }
    private String trainNumToString(Long trainNum) {
        String num = String.valueOf(trainNum);
        if (num.length() == 1) {
            return "00" + num;
        } else if (num.length() == 2) {
            return "0" + num;
        } else {
            return num;
        }
    }
    private String convertDate(Long dateTime) {
        String date = String.valueOf(dateTime);
        return date.substring(0,4) + "/" + date.substring(4,6) + "/" + date.substring(6,8) + " " + date.substring(8,10) + ":" + date.substring(10,12);
    }
}
