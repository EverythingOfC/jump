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
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
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

            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            StringBuffer result = new StringBuffer();
            String re = null;
            while((re = br.readLine()) != null)
                result.append(re);

            jsonObject = XML.toJSONObject(result.toString());
            jsonObject2 = jsonObject.getJSONObject("response");
            jsonObject3 = jsonObject2.getJSONObject("body");
            jsonArray = (JSONArray) jsonObject3.get("NewsItem");    // JsonObject -> JsonArray
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = (JSONObject) jsonArray.get(i);
                int count = 0;    // 오류 없이 지날 때마다 count증가
                try {
                    System.out.println(item);
                    title = ("{"+quotes+"org"+quotes+":"+quotes+item.get("Title").toString()+quotes+"}"); count++;

                    if(!item.get("SubTitle1").equals("")) {
                        subject = (("[{"+quotes+"org"+quotes+":"+quotes+item.get("SubTitle1")+quotes+"}]"));
                    }else if(!item.get("SubTitle2").equals("")) {
                        subject = (("[{"+quotes+"org"+quotes+":"+quotes+item.get("SubTitle2")+quotes+"}]"));
                    }else if(!item.get("SubTitle3").equals("")) {
                        subject = (("[{"+quotes+"org"+quotes+":"+quotes+item.get("SubTitle3")+quotes+"}]"));
                    }else {
                        subject = (("[{"+quotes+"org"+quotes+":"+quotes+item.get("Title")+quotes+"}]"));
                    }	count++;

                    description = (("{"+quotes+"summary"+quotes+":{"+quotes+"org"+quotes+":"+quotes+item.get("DataContents")+quotes+"}")); count++;
                    publisher = ("{"+quotes+"org"+quotes+":"+quotes+item.get("MinisterCode").toString()+quotes+"}"); count++;
                    contributors = (("[{"+quotes+"org"+quotes+":"+quotes+item.get("MinisterCode")+quotes+","+quotes+"role"+quotes+":"+quotes+"author"+quotes+"}]")); count++;
                    date = ("{"+quotes+"modified"+quotes+":"+quotes+item.get("ModifyDate").toString()+","+"available:"+item.get("ApproveDate").toString()+quotes+"}"); count++;
                    language = ("{"+quotes+"org"+quotes+":"+quotes+"ko"+quotes+"}"); count++;
                    identifier = ("{"+quotes+"site"+quotes+":"+quotes+item.get("NewsItemId").toString()+","+"view:"+item.get("OriginalUrl").toString()+quotes+"}"); count++;
                    format = (("{"+quotes+"org"+quotes+":"+quotes+quotes+"}")); count++;
                    relation = ("{"+quotes+"related"+quotes+":["+quotes+item.get("FileName").toString()+quotes+","+quotes+item.get("FileUrl").toString()+quotes+"]}"); count++;
                    coverage= (("{"+quotes+"org"+quotes+":"+quotes+quotes+"}")); count++;
                    right= (("{"+quotes+"org"+quotes+":"+quotes+quotes+"}"));

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
                        (date.toString()),   // LocalDate로 parsing 오류 해결
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

        for(Map.Entry<String, String> entry : map.entrySet()){  // rawdata1
            String key = entry.getKey();    // 키를 얻어옴.
            String value = entry.getValue();    // 값을 얻어옴.
            model.addAttribute(key,value);
        }

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

        for(Map.Entry<String, String> entry : map2.entrySet()){ // rawdata2
            String key = entry.getKey();    // 키를 얻어옴.
            String value = entry.getValue();    // 값을 얻어옴.
            model.addAttribute(key,value);
        }

        Map<String,String> colon = new HashMap<>();
        for(int i=0;i<20;i++){	// 20번 반복해서 들어감
            colon.put("rawdata_Colon"+i,"");
            if(!(pitches.get(i).equals(""))){
                colon.put("rawdata_Colon"+i,":");
            }
            model.addAttribute("rawdata_Colon"+i,colon.get("rawdata_Colon"+i));	// key: rawdata_Colon+ i,   value: "" or ":"
        }

        model.addAttribute("mapping_1", "[ 매핑전 데이터 예시 ]");
        model.addAttribute("mapping_2", "[ 매핑후 데이터 예시 ]");

        return "api";   // api.html 출력

    }

}
