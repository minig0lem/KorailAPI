package com.minig0lem.neilro.service;

import com.minig0lem.neilro.constants.StationCode;
import com.minig0lem.neilro.exceptions.OpenApiException;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static com.minig0lem.neilro.dto.TrainDto.OpenApiResponse;
import static com.minig0lem.neilro.dto.TrainDto.TrainRequest;
import static com.minig0lem.neilro.dto.TrainDto.OpenApiDto;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class OpenApiManager {
    @Value("${openApi.serviceKey}")
    private String serviceKey;

    @Value("${openApi.callBackUrl}")
    private String callBackUrl;

    @Value("${openApi.dataType}")
    private String dataType;

    public OpenApiResponse fetch(TrainRequest trainRequest) {
        String url = makeRequestUrl(1, 150, trainRequest.getDep()
                                    , trainRequest.getDepTime().substring(0,8), trainRequest.getArr());

        List<OpenApiDto> dtoList = new ArrayList<>();

        try {
            RestTemplate restTemplate = new RestTemplate();
            JSONParser jsonParser = new JSONParser();

            String jsonString = restTemplate.getForObject(url, String.class);
            JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonString);

            JSONObject jsonResponse = (JSONObject) jsonObject.get("response");
            JSONObject jsonBody = (JSONObject) jsonResponse.get("body");
            JSONObject jsonItems = (JSONObject) jsonBody.get("items");
            JSONArray jsonItemList = (JSONArray) jsonItems.get("item");

            String depHour = trainRequest.getDepTime().substring(8);

            int idx = findFirstIndex(jsonItemList, depHour);

            for (int i = 0; i < 10; i++) {
                if(idx == jsonItemList.size()) break;
                JSONObject item = (JSONObject) jsonItemList.get(idx++);
                OpenApiDto dto = toDto(item);
                dtoList.add(dto);
            }

            return OpenApiResponse.builder()
                    .dtoList(dtoList)
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new OpenApiException("Open API 호출 오류");
        }
    }
    private int findFirstIndex(JSONArray jsonItemList, String depHour) {
        for(int i = 0; i < jsonItemList.size(); i++) {
            JSONObject item = (JSONObject) jsonItemList.get(i);
            String dep = String.valueOf(item.get("depplandtime")).substring(8, 10);
            if(dep.compareTo(depHour) < 0) {
                continue;
            }
            return i;
        }
        return 0;
    }
    private String makeRequestUrl(int pageNo, int numOfRows, String dep, String depTime, String arr) {
        return callBackUrl
                + "?serviceKey=" + serviceKey
                + "&pageNo=" + pageNo
                + "&numOfRows=" + numOfRows
                + "&_type=" + dataType
                + "&depPlaceId=" + StationCode.valueOf(dep).getCode()
                + "&arrPlaceId=" + StationCode.valueOf(arr).getCode()
                + "&depPlandTime=" + depTime;
    }
    private OpenApiDto toDto(JSONObject item) {
        return OpenApiDto.builder()
                .dep((String) item.get("depplacename"))
                .depTime((Long) item.get("depplandtime"))
                .arr((String) item.get("arrplacename"))
                .arrTime((Long) item.get("arrplandtime"))
                .trainType((String) item.get("traingradename"))
                .trainNum((Long) item.get("trainno"))
                .build();
    }
}
