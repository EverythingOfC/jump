package com.example.jump.controller;

import com.example.jump.domain.SearchApi;
import com.example.jump.service.MetaSearch;
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

    @Autowired  // 자동으로 의존 객체를 찾아서 주입함
    private MetaSearch metaSearch;

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
    public ResponseEntity<byte[]> saveCsv(){

        return metaService.saveCsv();
    }

    @GetMapping("/jump/search") // 검색 시 이동
    public String search(@RequestParam(value="search_kw",defaultValue = "")String search,Model model){

        List<SearchApi> searchApis = this.metaSearch.searchApi(search);

        model.addAttribute("search",searchApis);    // 검색 리스트를 세션으로 저장

        return "api";
    }


}
