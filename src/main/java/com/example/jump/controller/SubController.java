package com.example.jump.controller;

import com.example.jump.domain.MetaApi;
import com.example.jump.service.MetaService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/*
    스프링 MVC구조

	- 사용자 <--> Controller <--> Service <--> Repository <--> Domain

    1.  사용자가 URL로 요청
    2.  Controller가 GetMapping으로 요청을 받음. resources/templates에서 "api"라는 이름을 가진 html 파일을 찾음.
    3.  Controller에서 Service 메소드를 호출 -> Service메소드에서 Repository 메소드를 호출 -> Domain 접근
    4.  역순으로 와서 사용자에게 돌아옴.

 */
@RequiredArgsConstructor    // final이 붙은 속성을 초기화하는 생성자를 만들어줌.
@Controller // 해당 클래스를 스프링의 컨트롤러로 인식
public class SubController {

    private final MetaService metaService;  // 서비스 객체를 생성자 주입

    @GetMapping({"/index","/"})    // 검색 페이지
    public String index(){
        return "index";
    }

    @GetMapping("/jump/login")       // 로그인 페이지
    public String login(){
        return "login";
    }

    @GetMapping("/jump/delete")      // 삭제
    public String delete(@RequestParam(value="delete",defaultValue = "0") Long[] metaId,@RequestParam(value="listPage")int listPage){  // form의 checkbox에 있는 metaId값을 배열로 받아와서 여러 개의 데이터 삭제
        metaService.delete(metaId);     // 해당 id값들로 데이터를 삭제

        return "redirect:/jump/list?listPage=" +listPage;   // 삭제해도 페이지 값은 유지
    }

    @GetMapping("/jump/detail")     // 상세
    public String detail(Model model, Long metaId, int listPage){   // model클래스를 통해 데이터를 템플릿에 전달함.
        MetaApi meta = metaService.getView(metaId);
        model.addAttribute("metaView",meta);            // MetaApi객체를 모델의 속성으로 저장
        model.addAttribute("listPage",listPage);        // 현재 페이지 번호를 모델의 속성으로 저장
        return "detail";
    }

    @GetMapping("/jump/list")       // 전체보기                // 사용자가 url로 jump/list를 요청하면 list()를 실행하고, 해당 이름의 html파일 호출
    public String list(Model model, @RequestParam(value="listPage",defaultValue = "1") int page){                        // model클래스를 통해 데이터를 템플릿에 전달함.
        Page<MetaApi> meta = metaService.getList(page-1);   // Page객체는 실제 페이지를 0부터 계산

        model.addAttribute("paging",meta);    // Page<MetaApi>객체를 모델의 속성으로 저장
        model.addAttribute("listPage",page);  // 현재 페이지 정보를 모델의 속성으로 저장
        return "list";                                    // resources/templates밑에 있는 list.html을 보여줌.
    }


}
