package io.github.rothschil.web;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Scope("prototype")
public class IndexController {

    private Integer i = 0;

    public IndexController() {
        System.out.println("构造函数");
    }

    @GetMapping("/test1")
    public Integer test1(){
        System.out.println(++i);
        return i;
    }

    @GetMapping("/test2")
    public Integer test2(){
        System.out.println(++i);
        return i;
    }


}
