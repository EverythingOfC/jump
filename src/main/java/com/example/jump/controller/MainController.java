package com.example.jump.controller;

import com.example.jump.domain.MetaApi;
import com.example.jump.repository.MetaRepository;
import com.example.jump.service.MetaService;
import com.example.jump.service.MetaServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor    // final이 붙은 속성을 초기화하는 생성자를 만들어줌.
@Controller // 해당 클래스를 스프링의 컨트롤러로 인식
public class MainController {

    private final MetaService metaService;  // 서비스 객체를 생성자 주입

    @GetMapping({"/jump","/"})  //   /jump나 /로 요청이 들어오면
    public String jump(){
        return "redirect:/jump/list";   // 완전히 새로운 URL로 요청함.
    }


    @GetMapping("/jump/detail") // 상세보기
    public String detail(Model model, MetaApi metaApi){

        return "detail";
    }

    @GetMapping("/jump/list")   // 전체보기                // 사용자가 url로 jump/list를 요청하면 list()를 실행하고, 해당 이름의 html파일 호출
    public String list(Model model){                        // model클래스를 통해 데이터를 템플릿에 전달함.
        List<MetaApi> meta = metaService.getList();
        model.addAttribute("metaList",meta);    // List<MetaApi>객체를 모델의 속성으로 저장
        return "list";                                      // resources/templates밑에 있는 list.html을 보여줌.
    }
}
