package com.example.jump.service;

import com.example.jump.domain.MetaApi;
import com.example.jump.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service    // 해당 클래스를 스프링의 서비스로 인식
public class MetaServiceImpl implements MetaService {

    private final MetaRepository metaRepository;    // 레퍼지토리 객체 생성

    public Page<MetaApi> getList(int page) {   // 전체 조회

        Pageable pageable = null;    //  조회할 페이지 번호와 한 페이지에 보여줄 데이터의 개수를 객체로 저장
        try {
            pageable = PageRequest.of(page,10);
        } catch (IllegalArgumentException e) {      // api 리스트가 비어있을 시에, 이전, 다음 버튼을 누르면 오류가 나므로 예외처리
            this.metaRepository.findAll(pageable);
        }
        return this.metaRepository.findAll(pageable);
    }

    public MetaApi getView(Long id) {  // 상세
        Optional<MetaApi> ID = this.metaRepository.findById(id);
        return ID.isPresent() ? ID.get() : null;       // id에 해당하는 데이터가 있으면 불러옴
    }

    public void delete(Long[] id) {      // 삭제
        int length = id.length;

        for(int i=0;i<length;i++){      // 삭제할 id값들을 반복함.
            Optional<MetaApi> ID = this.metaRepository.findById(id[i]);
            if (ID.isPresent())  // 값이 있다면
                this.metaRepository.delete(ID.get());   //  해당 객체 삭제
        }
    }

    public void save(MetaApi meta) {   // 수정
        this.metaRepository.save(meta);
    }

    public void getApi(String serviceKey, String startDate, String endDate,String submit, Model model){    // Api 출력만

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
            while ((re = br.readLine()) != null)    // 받아올 값이 있으면
                result.append(re);  //  StringBuffer 객체에 데이터 추가

            JSONObject jsonObject = XML.toJSONObject(result.toString());    // StringBuffer -> String으로 형 변환 후 XML데이터를 Json객체로 생성
            JSONObject jsonObject2 = jsonObject.getJSONObject("response");  //  key값이 response인 jsonObject를 찾음
            JSONObject jsonObject3 = jsonObject2.getJSONObject("body"); //  key값이 body인 jsonObject를 찾음
            jArray = (JSONArray) jsonObject3.get("NewsItem");  //  key값이 NewsItem인 객체들을 JSON 배열로 만듬

            int length = jArray.length();
            for (int i = 0; i < length; i++) {  // key값이 NewsItem인 객체들의 갯수만큼 반복

                JSONObject item = (JSONObject) jArray.get(i);    // JsonArray의 i+1번째 객체를 얻어옴.
                values.add(item);   // list에 JsonObject객체의 값을 하나씩 저장

                int count = 0;    // 오류 없이 지날 때마다 count가 증가함. ( 다음에 오류가 날 항목을 표시하기 위함 )
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
                    SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd"); // 파싱 후 형식
                    String strDate = item.get("ModifyDate").toString(); // jsonObject의 get메소드로 ModifyDate를 String으로 변환
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
                    model.addAttribute("error_column", "날짜 파싱에 실패하였습니다.");
                } catch (JSONException e) {       // 수집 실패한 항목들에 대한 처리
                    model.addAttribute("error_name", "ERROR : 증분 데이터 ERROR~!!");
                    model.addAttribute("error_code", "CODE :  EF_R_001");
                    model.addAttribute("error_column", "수집 실패한 데이터 항목: " + menu[count]);
                }
                    MetaApi meta = new MetaApi(i + (long) 1, "", "",    // 생성자를 통한 객체 초기화
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
                    metaRepository.save(meta);  // Entity에 Meta데이터를 저장한다.
            }
        } catch (ConnectException e) {
            model.addAttribute("error_column", "연결시간이 초과되었습니다.");
        } catch (Exception e) {
            model.addAttribute("error_name", "ERROR : 데이터 수집 ERROR~!!");
            model.addAttribute("error_code", "CODE : EF_R_003");
            model.addAttribute("error_reason", "사유: 정확한 인증키를 입력해주시기 바랍니다.");
            model.addAttribute("error_key", "요청하신 Key: " + serviceKey);
            model.addAttribute("error_page", "요청하신 Page: ");
            e.printStackTrace();
        }

        model.addAttribute("mapping_1", "[ 매핑전 데이터 예시 ]");
        model.addAttribute("mapping_2", "[ 매핑후 데이터 예시 ]");

        // 매핑 전, 칼럼의 이름을 참조하기 위한 key들
        String[] rawData2 = {"rawdata2_title", "rawdata2_subject1", "rawdata2_subject2", "rawdata2_subject3", "rawdata2_subject4",
                "rawdata2_description1", "rawdata2_description2", "rawdata2_publisher", "rawdata2_contributors1", "rawdata2_contributors2",
                "rawdata2_date1", "rawdata2_date2", "rawdata2_identifier1", "rawdata2_identifier2", "rawdata2_identifier3", "rawdata2_format",
                "rawdata2_relation1", "rawdata2_relation2", "rawdata2_coverage", "rawdata2_right"};

        // 매핑 전, 칼럼의 값을 참조하기 위한 key들
        String[] rawData = {"rawdata_title", "rawdata_subject1", "rawdata_subject2", "rawdata_subject3", "rawdata_subject4",
                "rawdata_description1", "rawdata_description2", "rawdata_publisher", "rawdata_contributors1", "rawdata_contributors2",
                "rawdata_date1", "rawdata_date2", "rawdata_identifier1", "rawdata_identifier2", "rawdata_identifier3", "rawdata_format",
                "rawdata_relation1", "rawdata_relation2", "rawdata_coverage", "rawdata_right"};

        // 매핑 전, 칼럼들의 이름과 값을 model에 추가
        for (int i = 0; i < 20; i++) {
            model.addAttribute(rawData2[i], pitches.get(i));     // 속성 이름: i번째 key,  속성 값: i번째 데이터 항목
            try {
                model.addAttribute(rawData[i], values.get(jArray.length() - 1).get(originItem[i]));  // 속성 이름: i번째 key,  속성 값: 마지막 json객체의 key가 mappingList[i]인 value
            } catch (JSONException e) {    // 만약 없는 키 값이면
                model.addAttribute(rawData[i], "");
            }
        }

        // 매핑 후, 칼럼들의 값을 model에 추가
        for (int i = 0; i < menu.length; i++) {
            model.addAttribute(menu[i], mappingValue[i]);   // 속성 이름: 항목명,  속성 값: 매핑 후 데이터 값
        }

        // 기존의 요청 인자 값을 model에 추가
        model.addAttribute("serviceKey",serviceKey);
        model.addAttribute("startDate",startDate);
        model.addAttribute("endDate",endDate);
    }

    public ResponseEntity<byte[]> saveCsv(){

        List<MetaApi> meta = metaRepository.findAll(); // 전체 데이터를 받아옴.
        String[] menu = {"Title", "Subject", "Description", "Publisher", "Contributors", "Date",
                "Language", "Identifier", "Format", "Relation", "Coverage", "Right"};   // CSV의 Header로 사용할 column들

        byte[] csvFile = null;          // csv데이터를 담을 배열
        CSVPrinter csvPrinter = null;   // csv형식의 값을 출력
        StringWriter sw = new StringWriter();   // 문자열 writer객체 생성

        try{
            csvPrinter = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader(menu));        // csv의 헤더 생성
            for(MetaApi m : meta){
                List<String> data = Arrays.asList(m.getMetaTitle(),m.getMetaSubjects(), // csv파일에 추가하고 싶은 데이터를 임의로 대입
                        m.getMetaDescription(),m.getMetaPublisher(),
                        m.getMetaContributor(),m.getMetaDate(),
                        m.getMetaLanguage(),m.getMetaIdentifier(),
                        m.getMetaFormat(),m.getMetaRelation(),
                        m.getMetaCoverage(),m.getMetaRight()
                );
                csvPrinter.printRecord(data);   // 실제 데이터 넣기
            }
            sw.flush();
            csvFile = sw.toString().getBytes("MS949");  // MS949로 인코딩한다.

            // csv파일 return
            HttpHeaders header = new HttpHeaders();
            header.add("Content-Type", "text/csv;charset=MS949");   // 인코딩 방식 지정
            header.setContentType(MediaType.valueOf("plain/text"));
            header.set(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=meta.csv");
            header.setContentLength(csvFile.length);

            return new ResponseEntity<byte[]>(csvFile, header, HttpStatus.OK);  // HttpRequest에 대한 응답 데이터를 포함함.
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            try {
                csvPrinter.close(); // 닫아줌.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;    // 없으면 null
    }
}
