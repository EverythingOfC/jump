package com.example.jump.controller;

import com.example.jump.domain.ClientSupportApi;
import com.example.jump.domain.SearchApi;
import com.example.jump.service.MetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
public class MainController {   // api출력 및 저장하는 핵심 로직

    @Autowired  // 자동으로 의존 객체를 찾아서 주입함
    private MetaService metaService;

    @GetMapping("/jump/api")    // Api 저장 또는 출력
    public String api(@RequestParam(value = "serviceKey") String serviceKey,
                      @RequestParam(value = "startdate", defaultValue = "20211201") String startDate,
                      @RequestParam(value = "enddate") String endDate,
                      @RequestParam(value = "submit") String submit,
                      Model model) {

        metaService.getApi(serviceKey,startDate,endDate,submit,model);     // api출력을 위한 서비스 메소드 실행

        return submit.equals("save") ? "redirect:/" : "api";    // 저장 버튼을 누른 경우: 완전히 새로운 요청으로 변환
            // 출력 버튼을 누른 경우: 기존의 요청을 유지
    }


    @GetMapping("/jump/save")   // csv파일로 저장
    public ResponseEntity<byte[]> saveCsv(@RequestParam(value="type")String type){

        return metaService.saveCsv(type);
    }

    @GetMapping("/jump/search") // 검색
    public String search(@RequestParam(value="search_kw",defaultValue = "")String search,Model model){

        List<SearchApi> searchApis = this.metaService.searchApi(search);

        model.addAttribute("search",searchApis);    // 검색 리스트를 세션으로 저장

        return "api";
    }

    @GetMapping("/jump/support")        // 고객지원 폼
    public String support(){
        return "support";
    }


    @GetMapping("/jump/supportHandle")  // 고객지원 처리
    public String supportHandle(@RequestParam(value="sup_category")String category,
                                @RequestParam(value="sup_title")String title,
                                @RequestParam(value="sup_name",defaultValue = "")String name,
                                @RequestParam(value="sup_content")String content,
                                @RequestParam(value="sup_email")String email,Model model){

        ClientSupportApi api = this.metaService.supportSave(category, title, name, content, email);    // 고객지원 데이터 저장

        return "redirect:/jump/search";  // api목록으로 이동
    }
}
