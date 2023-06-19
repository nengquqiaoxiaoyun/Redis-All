package cn.huakai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: huakaimay
 * @since: 2023-06-19
 */
@RestController
@RequestMapping("/hello")
public class HelloController {


    @GetMapping
    public String hello(String name) {
        return "hello" + name;
    }
}
