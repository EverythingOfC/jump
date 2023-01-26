package com.example.jump.controller;

import com.example.jump.domain.MetaApi;
import com.example.jump.service.MetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.json.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class MainController {

    @Autowired  // 자동으로 의존 객체를 찾아서 주입함
    private MetaService metaService;

    @GetMapping("/jump/api")
    public String api(@RequestParam(value = "serviceKey") String serviceKey,
                      @RequestParam(value = "startdate", defaultValue = "20211201") String startDate, @RequestParam(value = "enddate") String endDate, Model model) {

        JSONArray jArray = new JSONArray(); // Json객체 배열 생성
        char quotes = '"'; // 매핑시 ""안에 "을 넣기 위해 선언

        // column에 저장될 값
        String title = null;
        String subject = null;
        String description = null;
        String publisher = null;
        String contributors = null;
        String date = null;
        String language = null;
        String identifier = null;
        String format = null;
        String relation = null;
        String coverage = null;
        String right = null;

        // column에 따른 오류 내용을 표시하기 위한 배열 ( 밑에 catch문 조건 참고 )
        String[] menu = {"Title", "Subject", "Description", "Publisher", "Contributors", "Date", "Language", "Identifier", "Format", "Relation", "Coverage", "right"};

        // XML형식의 API데이터 항목들 ( 보도자료 원본의 key값 )
        String[] mappinglist = {"Title", "SubTitle1", "SubTitle2", "SubTitle3", "", "DataContents"  // 6개
                , "", "MinisterCode", "", "", "ModifyDate", "ApproveDate"     // 6개
                , "NewsItemId", "OriginalUrl", "", "", "FileName", "FileUrl", "", ""};    // 8개

        // 보도자료 원본의 key 리스트
        List<String> pitches = new ArrayList<>(Arrays.asList(mappinglist));

        // 보도자료 원본의 value 리스트
        List<JSONObject> values = new ArrayList<>();
        
        try {
            // url객체 생성
            URL url = new URL("http://apis.data.go.kr/1371000/pressReleaseService/pressReleaseList"
                    + "?serviceKey=" + serviceKey + "&startDate=" + startDate + "&endDate=" + endDate);

            // Http연결을 위한 객체 생성
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-type", "application/json");

            // url에서 불러온 데이터를 InputStream -> InputStreamReader -> BufferedReader -> readLine()로 받아옴.
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            StringBuffer result = new StringBuffer();
            String re = null;
            while ( (re = br.readLine() ) != null)    // 받아올 값이 있으면
                result.append(re);  //  StringBuffer 객체에 데이터 추가

            JSONObject jsonObject = XML.toJSONObject(result.toString());    // StringBuffer -> String으로 형 변환 후 XML데이터를 Json객체로 생성
            JSONObject jsonObject2 = jsonObject.getJSONObject("response");  //  key값이 response인 jsonObject를 찾음
            JSONObject jsonObject3 = jsonObject2.getJSONObject("body"); //  key값이 body인 jsonObject를 찾음
            jArray = (JSONArray) jsonObject3.get("NewsItem");  //  key값이 NewsItem인 객체들을 JSON 배열로 만듬

            for (int i = 0; i < jArray.length(); i++) {  // key값이 NewsItem인 객체들의 갯수만큼 반복

                JSONObject item = (JSONObject) jArray.get(i);    // JsonArray의 i+1번째 객체를 얻어옴.
                values.add(item);   // list에 JsonObject객체의 값을 하나씩 저장

                int count = 0;    // 오류 없이 지날 때마다 count가 증가함. ( 다음에 오류가 날 항목을 표시하기 위함 )
                try {
                        // 받아온 데이터에 {와 "를 붙이기 위한 로직들
                    title = ("{" + quotes + "org" + quotes + ":" + quotes + item.get("Title").toString() + quotes + "}");
                    count++;
                    if (!item.get("SubTitle1").equals("")) {
                        subject = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("SubTitle1") + quotes + "}]"));
                    } else if (!item.get("SubTitle2").equals("")) {
                        subject = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("SubTitle2") + quotes + "}]"));
                    } else if (!item.get("SubTitle3").equals("")) {
                        subject = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("SubTitle3") + quotes + "}]"));
                    } else {
                        subject = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("Title") + quotes + "}]"));
                    }
                    count++;
                    description = (("{" + quotes + "summary" + quotes + ":{" + quotes + "org" + quotes + ":" + quotes + item.get("DataContents") + quotes + "}"));
                    count++;
                    publisher = ("{" + quotes + "org" + quotes + ":" + quotes + item.get("MinisterCode").toString() + quotes + "}");
                    count++;
                    contributors = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("MinisterCode") + quotes + "," + quotes + "role" + quotes + ":" + quotes + "author" + quotes + "}]"));
                    count++;

                    // 날짜 변환 로직
                    SimpleDateFormat dfFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");    // 파싱 전 형식
                    SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); // 파싱 후 형식
                    String strDate = item.get("ModifyDate").toString(); // jsonObject의 get메소드로 ModifyDate를 String으로 변환
                    String strDate2 = item.get("ApproveDate").toString(); // jsonObject의 get메소드로 ApproveDate을 String으로 변환
                    String dateTemp = null;
                    String dateTemp2 = null;
                    if(!strDate.equals("")){    // ModifyDate가 값이 있으면 날짜 변환
                        Date formatDate = dfFormat.parse(strDate);  // 기존의 날짜 형식으로 Date객체 생성
                        dateTemp = newDtFormat.format(formatDate);  // 기존의 날짜 형식을 새로운 날짜 형식으로 변환
                    }
                    if(!strDate2.equals("")){   // ApproveDate가 값이 있으면 날짜 변환
                        Date formatDate2 = dfFormat.parse(strDate2);    // 기존의 날짜 형식으로 Date객체 생성
                        dateTemp2 = newDtFormat.format(formatDate2);    // 기존의 날짜 형식을 새로운 날짜 형식으로 변환
                    }
                    count++;
                    date = ("{" + quotes + "modified" + quotes + ":" + quotes + dateTemp + "," + quotes + "available" + quotes + ":" + quotes + dateTemp2 + quotes + "}");
                    language = ("{" + quotes + "org" + quotes + ":" + quotes + "ko" + quotes + "}");
                    count++;
                    identifier = ("{" + quotes + "site" + quotes + ":" + quotes + item.get("NewsItemId").toString() + "," + "view:" + item.get("OriginalUrl").toString() + quotes + "}");
                    count++;
                    format = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));
                    count++;
                    relation = ("{" + quotes + "related" + quotes + ":[" + quotes + item.get("FileName").toString() + quotes + "," + quotes + item.get("FileUrl").toString() + quotes + "]}");
                    count++;
                    coverage = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));
                    count++;
                    right = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));

                }catch(ParseException e){       // 날짜 파싱 오류
                    model.addAttribute("error_column", "날짜 파싱에 실패하였습니다.");
                    return "api";
                }
                catch (JSONException e) {       // 수집 실패한 항목들에 대한 처리
                    model.addAttribute("error_name", "ERROR : 증분 데이터 ERROR~!!");
                    model.addAttribute("error_code", "CODE :  EF_R_001");
                    model.addAttribute("error_column", "수집 실패한 데이터 항목: " + menu[count]);
                    return "api";
                }
                // Entity 객체 생성 후 데이터 저장(DB에 저장) ***** 저장 기능 구현 시 사용 예정 *****
              /*  MetaApi meta = new MetaApi(i + (long) 1, "", "",
                        (title.toString()),
                        (subject.toString()),
                        (description.toString()),
                        (publisher.toString()),
                        (contributors.toString()),
                        (date.toString()),   // LocalDate로 parsing 오류 해결
                        (language.toString()),
                        (identifier.toString()),
                        (format.toString()),
                        (relation.toString()),
                        (coverage.toString()),
                        (right.toString()));
                */
            }
        }catch (ConnectException e){
            model.addAttribute("error_column", "연결시간이 초과되었습니다.");
            return "api";
        }
        catch (Exception e) {
            model.addAttribute("error_name", "ERROR : 데이터 수집 ERROR~!!");
            model.addAttribute("error_code", "CODE : EF_R_003");
            model.addAttribute("error_reason", "사유: 정확한 인증키를 입력해주시기 바랍니다.");
            model.addAttribute("error_key", "요청하신 Key: " + serviceKey);
            model.addAttribute("error_page", "요청하신 Page: ");
            e.printStackTrace();
            return "api";
        }

        model.addAttribute("mapping_1", "[ 매핑전 데이터 예시 ]");

        Map<String, String> map2 = new HashMap<>(); // 매핑 전 칼럼 쌍
        Map<String, Object> map = new HashMap<>();  // 매핑 후 칼럼 쌍

        // 매핑 전 칼럼 이름들
        map2.put("rawdata2_title", pitches.get(0)); //  칼럼의 이름을 속성으로 저장
        map2.put("rawdata2_subject1", pitches.get(1));
        map2.put("rawdata2_subject2", pitches.get(2));
        map2.put("rawdata2_subject3", pitches.get(3));
        map2.put("rawdata2_subject4", pitches.get(4));
        map2.put("rawdata2_description1", pitches.get(5));
        map2.put("rawdata2_description2", pitches.get(6));
        map2.put("rawdata2_publisher", pitches.get(7));
        map2.put("rawdata2_contributors1", pitches.get(8));
        map2.put("rawdata2_contributors2", pitches.get(9));
        map2.put("rawdata2_date1", pitches.get(10));
        map2.put("rawdata2_date2", pitches.get(11));
        map2.put("rawdata2_identifier1", pitches.get(12));
        map2.put("rawdata2_identifier2", pitches.get(13));
        map2.put("rawdata2_identifier3", pitches.get(14));
        map2.put("rawdata2_format", pitches.get(15));
        map2.put("rawdata2_relation1", pitches.get(16));
        map2.put("rawdata2_relation2", pitches.get(17));
        map2.put("rawdata2_coverage", pitches.get(18));
        map2.put("rawdata2_right", pitches.get(19));

        for (Map.Entry<String, String> entry : map2.entrySet()) { // 매핑 전 칼럼이름들을 순환
            String key = entry.getKey();    // 키를 얻어옴.
            String value = entry.getValue();    // 값을 얻어옴.
            model.addAttribute(key, value); // 속성 이름: key,  속성 값: value
        }

        String[] rawData = {"rawdata_title","rawdata_subject1","rawdata_subject2","rawdata_subject3","rawdata_subject4",
                "rawdata_description1","rawdata_description2","rawdata_publisher","rawdata_contributors1","rawdata_contributors2",
                "rawdata_date1","rawdata_date2","rawdata_identifier1","rawdata_identifier2","rawdata_identifier3","rawdata_format",
                "rawdata_relation1","rawdate_relation2","rawdata_coverage","rawdate_right"};    // map에 저장할 key들

        for(int i=0;i<20;i++){
            try{
                map.put(rawData[i], values.get(jArray.length()-1).get(mappinglist[i])); // i번째 json객체의 i번째 column의 값을 저장
            }catch(JSONException e){    // 만약 없는 키 값이면
                map.put(rawData[i],""); // 빈 값 저장
            }
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {  // 매핑 전 칼럼의 값들을 순환
            String key = entry.getKey();        // 키를 얻어옴.
            Object value = entry.getValue();    // 값을 얻어옴.
            model.addAttribute(key, value);     // 속성 이름: key,  속성 값: value
        }

        model.addAttribute("mapping_2", "[ 매핑후 데이터 예시 ]");

        // 매핑 후 데이터 값들
        model.addAttribute("title", title);     // 매핑된 이후에 저장된 값들을 속성으로 저장
        model.addAttribute("subject", subject);
        model.addAttribute("description", description);
        model.addAttribute("publisher", publisher);
        model.addAttribute("contributors", contributors);
        model.addAttribute("date", date);
        model.addAttribute("language", language);
        model.addAttribute("identifier", identifier);
        model.addAttribute("format", format);
        model.addAttribute("relation", relation);
        model.addAttribute("coverage", coverage);
        model.addAttribute("right", right);

        return "api";   // api.html로 이동

    }
}
