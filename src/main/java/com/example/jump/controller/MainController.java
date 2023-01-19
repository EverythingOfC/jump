package com.example.jump.controller;

import com.example.jump.domain.MetaApi;
import com.example.jump.service.MetaService;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.json.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class MainController {

    @Autowired  // 자동으로 의존 객체를 찾아서 주입함
    private MetaService metaService;

    @GetMapping("/jump/api")
    public String api(@RequestParam(value = "serviceKey") String serviceKey,
                      @RequestParam(value = "startdate", defaultValue = "20211201") String startDate, @RequestParam(value = "enddate") String endDate, Model model) {
        JSONArray jsonArray = new JSONArray();
        char quotes = '"'; // 매핑시 ""안에 "을 넣기 위해 선언
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
        String[] menu = {"Title", "Subject", "Description", "Publisher", "Contributors", "Date", "Language", "Identifier", "Format", "Relation", "Coverage", "right"};
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObject2 = new JSONObject();
        JSONObject jsonObject3 = new JSONObject();

        String[] mappinglist = {"Title", "SubTitle1", "SubTitle2", "SubTitle3", "", "DataContents"  // 6개
                , "", "MinisterCode", "", "", "ModifyDate", "ApproveDate"     // 6개
                , "NewsItemId", "OriginalUrl", "", "", "FileName", "FileUrl", "", ""};    // 8개

        List<String> pitches = new ArrayList<>(Arrays.asList(mappinglist));

        try {

            URL url = new URL("http://apis.data.go.kr/1371000/pressReleaseService/pressReleaseList"
                    + "?serviceKey=" + serviceKey + "&startDate=" + startDate + "&endDate=" + endDate);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-type", "application/json");

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

            String result = br.readLine();

            JSONParser jsonParser = new JSONParser(result);   // String -> Object 변환
            jsonObject = (JSONObject) jsonParser.parse();   // Object -> JsonObject로 변환
            jsonObject2 = jsonObject.getJSONObject("response");
            jsonObject3 = jsonObject2.getJSONObject("body");
            jsonArray = (JSONArray) jsonObject3.get("NewsItem");    // JsonObject -> JsonArray
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = (JSONObject) jsonArray.get(i);
                int count = 0;    // 오류 없이 지날 때마다 count증가
                try {
                    title = "{" + quotes + "org" + quotes + ":" + quotes + item.get("법령명한글").toString() + quotes + "}";
                    count++;
                    String temp = !item.get("법령약칭명").equals("") ? "법령약칭명" : "법령명한글";    // 있으면 약칭명으로, 없으면 한글로
                    subject = "{" + quotes + "org" + quotes + ":" + quotes + item.get(temp).toString() + quotes + "}";
                    count++;
                    description = "{" + quotes + "summary" + quotes + ":{" + quotes + "org" + quotes + ":" + quotes + item.get("법령명한글") + quotes + "}}";
                    count++;
                    publisher = "{" + quotes + "org" + quotes + ":" + quotes + item.get("소관부처명").toString() + quotes + "}";
                    count++;
                    contributors = "[{" + quotes + "org" + quotes + ":" + quotes + item.get("소관부처명") + quotes + "," + quotes + "role" + quotes + ":" + quotes + "author" + quotes + "}]";
                    count++;
                    date = "{" + quotes + "issued" + quotes + ":" + quotes + item.get("시행일자").toString() + quotes + "," + quotes + "created" + quotes + ":" + quotes + item.get("공포일자").toString() + quotes + "}";
                    count++;
                    language = "{" + quotes + "org" + quotes + ":" + quotes + "ko" + quotes + "}";
                    count++;
                    identifier = "{" + quotes + "site" + quotes + ":" + quotes + item.get("법령일련번호").toString() + quotes + "," + quotes + "url" + quotes + ":" + quotes + item.get("법령상세링크").toString() + quotes + "}";
                    count++;
                    format = "{" + quotes + "org" + quotes + ":" + quotes + quotes + "}";
                    count++;
                    relation = "{" + quotes + "isPartOF" + quotes + ":" + quotes + item.get("제개정구분명").toString() + quotes + "}";
                    count++;
                    coverage = "{" + quotes + "org" + quotes + ":" + quotes + quotes + "}";
                    count++;
                    right = "{" + quotes + "org" + quotes + ":" + quotes + quotes + "}";
                } catch (Exception e) {
                    model.addAttribute("error_name", "ERROR : 증분 데이터 ERROR~!!");
                    model.addAttribute("error_code", "CODE :  EF_R_001");
                    model.addAttribute("error_column", "수집 실패한 데이터항목: " + menu[count]);
                    return "api";
                }

                DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                MetaApi meta = new MetaApi(i + (long) 1, "", "",
                        (title.toString()),
                        (subject.toString()),
                        (description.toString()),
                        (publisher.toString()),
                        (contributors.toString()),
                        (LocalDateTime.parse(date, format1)),
                        (language.toString()),
                        (identifier.toString()),
                        (format.toString()),
                        (relation.toString()),
                        (coverage.toString()),
                        (right.toString()));
                metaService.save(meta);
            }
        } catch (Exception e) {
            model.addAttribute("error_name", "ERROR : 데이터 수집 ERROR~!!");
            model.addAttribute("error_code", "CODE : EF_R_003");
            model.addAttribute("error_reason", "사유: 정확한 인증키를 입력해주시기 바랍니다.");
            model.addAttribute("error_key", "요청하신 Key: "+serviceKey);
            model.addAttribute("error_page", "요청하신 Page: ");
            e.printStackTrace();
            return "api";
        }

        model.addAttribute("title", title);
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

        // 매핑된 데이터 제목
        model.addAttribute("title2", "title : ");
        model.addAttribute("subject2", "subject : ");
        model.addAttribute("description2", "description : ");
        model.addAttribute("publisher2", "publisher : ");
        model.addAttribute("contributors2", "contributors : ");
        model.addAttribute("date2", "date : ");
        model.addAttribute("language2", "language : ");
        model.addAttribute("identifier2", "identifier :");
        model.addAttribute("format2", "format : ");
        model.addAttribute("relation2", "relation : ");
        model.addAttribute("coverage2", "coverage : ");
        model.addAttribute("right2", "right : " );

        Map<String,String> map = new HashMap<>();
        Map<String,String> map2 = new HashMap<>();

        map.put("rawdata_title",pitches.get(0));
        map.put("rawdata_subject1",pitches.get(1));
        map.put("rawdata_subject2",pitches.get(2));
        map.put("rawdata_subject3",pitches.get(3));
        map.put("rawdata_subject4",pitches.get(4));
        map.put("rawdata_description1",pitches.get(5));
        map.put("rawdata_description2",pitches.get(6));
        map.put("rawdata_publisher",pitches.get(7));
        map.put("rawdata_contributors1",pitches.get(8));
        map.put("rawdata_contributors2",pitches.get(9));
        map.put("rawdata_date1",pitches.get(10));
        map.put("rawdata_date2",pitches.get(11));
        map.put("rawdata_identifier1",pitches.get(12));
        map.put("rawdata_identifier2",pitches.get(13));
        map.put("rawdata_identifier3",pitches.get(14));
        map.put("rawdata_format",pitches.get(15));
        map.put("rawdata_relation1",pitches.get(16));
        map.put("rawdata_relation2",pitches.get(17));
        map.put("rawdata_coverage",pitches.get(18));
        map.put("rawdata_right",pitches.get(19));

        map2.put("rawdata2_title",pitches.get(0));
        map2.put("rawdata2_subject1",pitches.get(1));
        map2.put("rawdata2_subject2",pitches.get(2));
        map2.put("rawdata2_subject3",pitches.get(3));
        map2.put("rawdata2_subject4",pitches.get(4));
        map2.put("rawdata2_description1",pitches.get(5));
        map2.put("rawdata2_description2",pitches.get(6));
        map2.put("rawdata2_publisher",pitches.get(7));
        map2.put("rawdata2_contributors1",pitches.get(8));
        map2.put("rawdata2_contributors2",pitches.get(9));
        map2.put("rawdata2_date1",pitches.get(10));
        map2.put("rawdata2_date2",pitches.get(11));
        map2.put("rawdata2_identifier1",pitches.get(12));
        map2.put("rawdata2_identifier2",pitches.get(13));
        map2.put("rawdata2_identifier3",pitches.get(14));
        map2.put("rawdata2_format",pitches.get(15));
        map2.put("rawdata2_relation1",pitches.get(16));
        map2.put("rawdata2_relation2",pitches.get(17));
        map2.put("rawdata2_coverage",pitches.get(18));
        map2.put("rawdata2_right",pitches.get(19));

        int i = 0;
        for (String key : map.keySet()){	 // map의 key값을 모두 얻어와서 key값에 해당 값들을 순차적으로 저장
            String value= String.valueOf(pitches.get(i));	// rawdata 칼럼의 값들을 모두 저장
            if(!pitches.get(i++).equals(""))
                map.put(key,value);	// rawdata의 칼럼 데이터들을 순차적으로 저장
            model.addAttribute(key,value);
        }

        i = 0;
        for(String key2 : map2.keySet()){	// map2의 key값을 모두 얻어와서 key값에 해당 값들을 순차적으로 저장
            model.addAttribute(key2,pitches.get(i++));
        }

        Map<String,String> colon = new HashMap<>();
        for(i=0;i<20;i++){	// 20번 반복해서 들어감
            colon.put("rawdata_Colon"+i,"");
            if(!pitches.get(i).equals("")){
                colon.put("rawdata_Colon"+i,":");
            }
            model.addAttribute("rawdata_Colon"+i,colon.get(i));	// key: rawdata_Colon+ i,   value: "" or ":"
        }

        model.addAttribute("mapping_1", "[ 매핑전 데이터 예시 ]");
        model.addAttribute("mapping_2", "[ 매핑후 데이터 예시 ]");

        return "api";   // api.html 출력

    }

}
