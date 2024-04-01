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

import static com.minig0lem.neilro.dto.NeilroDto.OpenApiResponse;
import static com.minig0lem.neilro.dto.NeilroDto.NeilroRequest;
import static com.minig0lem.neilro.dto.NeilroDto.OpenApiVo;

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


    public OpenApiResponse fetch(NeilroRequest neilroRequest) {
        String url = makeRequestUrl(1, 500, neilroRequest.getDep()
                                    , neilroRequest.getDepTime(), neilroRequest.getArr());

        List<OpenApiVo> voList = new ArrayList<>();

        try {
            RestTemplate restTemplate = new RestTemplate();
            JSONParser jsonParser = new JSONParser();

            String jsonString = restTemplate.getForObject(url, String.class);

            JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonString);

            JSONObject jsonResponse = (JSONObject) jsonObject.get("response");
            JSONObject jsonBody = (JSONObject) jsonResponse.get("body");
            JSONObject jsonItems = (JSONObject) jsonBody.get("items");
            JSONArray jsonItemList = (JSONArray) jsonItems.get("item");

            for (Object o : jsonItemList) {
                JSONObject item = (JSONObject) o;
                OpenApiVo vo = toVo(item);
                voList.add(vo);
            }

            Long totalCount = (Long) jsonBody.get("totalCount");

            return OpenApiResponse.builder()
                    .totalCount(totalCount)
                    .voList(voList)
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new OpenApiException("Open API 호출 오류");
        }
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


    private OpenApiVo toVo(JSONObject item) {
        return OpenApiVo.builder()
                .dep((String) item.get("depplacename"))
                .depTime((Long) item.get("arrplandtime"))
                .arr((String) item.get("arrplacename"))
                .arrTime((Long) item.get("arrplandtime"))
                .trainType((String) item.get("traingradename"))
                .trainNum((Long) item.get("trainno"))
                .build();
    }
}
