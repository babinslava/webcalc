package com.upwork.web;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalcController {

    @RequestMapping(value = "/add/{a}/{b}/{c}")
    public Integer add(@PathVariable("a") Integer operandA, @PathVariable("b") Integer operandB, @PathVariable("c") Integer operandC){
        return operandA+operandB+operandC;
    }
}
