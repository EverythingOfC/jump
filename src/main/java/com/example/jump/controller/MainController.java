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
                      @RequestParam(value = "startdate", defaultValue = "20211201") String startDate,
                      @RequestParam(value = "enddate") String endDate,
                      @RequestParam(value = "submit") String submit,
                      Model model) {

        metaService.getApi(serviceKey,startDate,endDate,submit,model);     // api출력을 위한 서비스 메소드 실행

        return submit.equals("save") ? "redirect:/" : "api";    // 저장 버튼을 누른 경우: 완전히 새로운 요청으로 변환
                                                                // 출력 버튼을 누른 경우: 기존의 요청을 유지
    }
}
