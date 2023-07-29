package dev.rdcl.auth.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {

    @GetMapping
    public String index() {
        return "demo/index";
    }

    @GetMapping("/register")
    public String register() {
        return "demo/register";
    }

}
