package com.upwork.web;

import com.upwork.dto.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalcController {

    @RequestMapping(value = "/add/{a}/{b}/{c:.+}")
    public Result add(@PathVariable("a") double operandA, @PathVariable("b") double operandB, @PathVariable("c") double operandC){
        return new Result(operandA+operandB+operandC);
    }
}
