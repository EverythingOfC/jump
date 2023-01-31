package com.example.jump;

import com.example.jump.domain.MetaApi;
import com.example.jump.repository.MetaApiRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootTest // 해당 클래스가 스프링부트 테스트 클래스임을 알려줌.
class JumpApplicationTests {

    @Autowired  // 해당 클래스에 MetaRepository객체 주입
    private MetaApiRepository metaApiRepository;

    @Test
    void testJpa() {

        JSONArray jArray = null;    // Json배열형 변수 선언

        // column에 따른 오류 내용을 표시하기 위한 배열 ( 밑에 catch문 조건 참고 )
        String[] menu = {"Title", "Subject", "Description", "Publisher", "Contributors", "Date", "Language", "Identifier", "Format", "Relation", "Coverage", "Right"};

        // XML형식의 API데이터 항목들 ( 보도자료 원본의 key값 )
        String[] originItem = {"Title", "SubTitle1", "SubTitle2", "SubTitle3", "", "DataContents"  // 6개
                , "", "MinisterCode", "", "", "ModifyDate", "ApproveDate"                          // 6개
                , "NewsItemId", "OriginalUrl", "", "", "FileName", "FileUrl", "", ""};             // 8개

        // 보도자료 원본의 key 리스트
        List<String> pitches = new ArrayList<>(Arrays.asList(originItem));
        // 보도자료 원본의 value 리스트
        List<JSONObject> values = new ArrayList<>();
        // 보도자료 매핑 후 value 리스트 ( Title ~ right 칼럼들의 값 )
        String[] mappingValue = new String[12];

        try {
            // 보도자료 url객체 생성
            URL url = new URL("http://apis.data.go.kr/1371000/pressReleaseService/pressReleaseList?serviceKey=OyfKMEU9NFp%2FBjVq6X4XzOKgG0iCkwCWtmQNFtDKPlfCOoqhQBo6DhgyLTsJxe5JNjyRns4f2IZ0DmneSFw0Xw%3D%3D&startDate=20211201&endDate=20211203");
            // Http연결을 위한 객체 생성
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-type", "application/json");

            // url에서 불러온 데이터를 InputStream -> InputStreamReader -> BufferedReader -> readLine()로 받아옴.
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            StringBuffer result = new StringBuffer();
            String re = null;
            while ((re = br.readLine()) != null)    // 받아올 값이 있으면
                result.append(re);  //  StringBuffer 객체에 데이터 추가

            JSONObject jsonObject = XML.toJSONObject(result.toString());        //  StringBuffer -> String으로 형 변환 후 XML데이터를 Json객체로 생성
            JSONObject jsonObject2 = jsonObject.getJSONObject("response");  //  key값이 response인 jsonObject를 찾음
            JSONObject jsonObject3 = jsonObject2.getJSONObject("body");     //  key값이 body인 jsonObject를 찾음
            jArray = (JSONArray) jsonObject3.get("NewsItem");       //  key값이 NewsItem인 객체들을 JSON 배열로 만듬

            int length = jArray.length();
            int apiLength = this.metaApiRepository.findAll().size();
            for (int i = 0; i < length; i++) {  // key값이 NewsItem인 객체들의 갯수만큼 반복

                JSONObject item = (JSONObject) jArray.get(i);    // JsonArray의 i+1번째 객체를 얻어옴.
                values.add(item);           // list에 JsonObject객체의 값을 하나씩 저장

                int count = 0;              // 오류 없이 지날 때마다 count가 증가함. ( 다음에 오류가 날 항목을 표시하기 위함 )
                char quotes = '"';          // 매핑시 ""안에 "을 넣기 위해 선언
                try {
                    // 받아온 데이터에 {와 "를 붙이기 위한 로직들
                    mappingValue[0] = ("{" + quotes + "org" + quotes + ":" + quotes + item.get("Title").toString() + quotes + "}");
                    count++;
                    if (!item.get("SubTitle1").equals("")) {
                        mappingValue[1] = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("SubTitle1") + quotes + "}]"));
                    } else if (!item.get("SubTitle2").equals("")) {
                        mappingValue[1] = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("SubTitle2") + quotes + "}]"));
                    } else if (!item.get("SubTitle3").equals("")) {
                        mappingValue[1] = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("SubTitle3") + quotes + "}]"));
                    } else {
                        mappingValue[1] = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("Title") + quotes + "}]"));
                    }
                    count++;
                    mappingValue[2] = (("{" + quotes + "summary" + quotes + ":{" + quotes + "org" + quotes + ":" + quotes + item.get("DataContents") + quotes + "}"));
                    count++;
                    mappingValue[3] = ("{" + quotes + "org" + quotes + ":" + quotes + item.get("MinisterCode").toString() + quotes + "}");
                    count++;
                    mappingValue[4] = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("MinisterCode") + quotes + "," + quotes + "role" + quotes + ":" + quotes + "author" + quotes + "}]"));
                    count++;

                    // 날짜 변환 로직
                    SimpleDateFormat dfFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");    // 파싱 전 형식
                    SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd");          // 파싱 후 형식
                    String strDate = item.get("ModifyDate").toString();   // jsonObject의 get메소드로 ModifyDate를 String으로 변환
                    String strDate2 = item.get("ApproveDate").toString(); // jsonObject의 get메소드로 ApproveDate을 String으로 변환
                    String dateTemp = "";
                    String dateTemp2 = "";
                    if (!strDate.equals("")) {    // ModifyDate가 값이 있으면 날짜 변환
                        Date formatDate = dfFormat.parse(strDate);  // 기존의 날짜 형식으로 Date객체 생성
                        dateTemp = newDtFormat.format(formatDate);  // 기존의 날짜 형식을 새로운 날짜 형식으로 변환
                    }
                    if (!strDate2.equals("")) {   // ApproveDate가 값이 있으면 날짜 변환
                        Date formatDate2 = dfFormat.parse(strDate2);    // 기존의 날짜 형식으로 Date객체 생성
                        dateTemp2 = newDtFormat.format(formatDate2);    // 기존의 날짜 형식을 새로운 날짜 형식으로 변환
                    }

                    mappingValue[5] = ("{" + quotes + "modified" + quotes + ":" + quotes + dateTemp + "," + quotes + "available" + quotes + ":" + quotes + dateTemp2 + quotes + "}");
                    count++;
                    mappingValue[6] = ("{" + quotes + "org" + quotes + ":" + quotes + "ko" + quotes + "}");
                    count++;
                    mappingValue[7] = ("{" + quotes + "site" + quotes + ":" + quotes + item.get("NewsItemId").toString() + "," + "view:" + item.get("OriginalUrl").toString() + quotes + "}");
                    count++;
                    mappingValue[8] = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));
                    count++;
                    mappingValue[9] = ("{" + quotes + "related" + quotes + ":[" + quotes + item.get("FileName").toString() + quotes + "," + quotes + item.get("FileUrl").toString() + quotes + "]}");
                    count++;
                    mappingValue[10] = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));
                    count++;
                    mappingValue[11] = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));

                    // 정규 표현식으로 태그 및 특수문자들 제거
                    for(int j=0;j<12;j++){
                        mappingValue[j]=mappingValue[j].replaceAll("<[^>]*>","");   // HTML 태그 형식 삭제
                        mappingValue[j]=mappingValue[j].replaceAll("&[^;]*;","");   // HTML 특수문자들 제거 ( ex: &nbsp; &middot )
                    }

                } catch (ParseException e) {       // 날짜 파싱 오류

                } catch (JSONException e) {       // 수집 실패한 항목들에 대한 처리

                }
                MetaApi meta = new MetaApi(apiLength + (long) i+1, "", "보도자료",    // 생성자를 통한 객체 초기화
                        (mappingValue[0]),
                        (mappingValue[1]),
                        (mappingValue[2]),
                        (mappingValue[3]),
                        (mappingValue[4]),
                        (mappingValue[5]),
                        (mappingValue[6]),
                        (mappingValue[7]),
                        (mappingValue[8]),
                        (mappingValue[9]),
                        (mappingValue[10]),
                        (mappingValue[11]));
                metaApiRepository.save(meta);  // Entity에 Meta데이터를 저장한다.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}