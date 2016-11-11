package com.upwork.web;

import com.upwork.dto.Result;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalcController {

    @Cacheable("calc")
    @RequestMapping(value = "/add/{a}/{b}/{c:.+}")
    public Result add(@PathVariable("a") float operandA, @PathVariable("b") float operandB, @PathVariable("c") float operandC){
        return new Result(operandA+operandB+operandC);
    }
}
