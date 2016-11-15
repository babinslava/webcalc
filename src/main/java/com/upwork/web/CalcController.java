package com.upwork.web;

import com.upwork.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CalcController {

    private static final Logger logger =
            LoggerFactory.getLogger(CalcController.class);

    @Cacheable(value="calc",
            key="new org.springframework.cache.interceptor.SimpleKey( #root.methodName, #operandA, #operandB, #operandC  )")
    @RequestMapping(value = "/add/{a}/{b}/{c:.+}")
    public Result add(@PathVariable("a") double operandA, @PathVariable("b") double operandB, @PathVariable("c") double operandC){
        return new Result(operandA+operandB+operandC);
    }

    @Cacheable(value="calc",
            key="new org.springframework.cache.interceptor.SimpleKey( #root.methodName, #operandA, #operandB, #operandC  )")
    @RequestMapping(value = "/subtract/{a}/{b}/{c:.+}")
    public Result subtract(@PathVariable("a") double operandA, @PathVariable("b") double operandB, @PathVariable("c") double operandC){
        return new Result(operandA-operandB-operandC);
    }

    @Cacheable(value="calc",
            key="new org.springframework.cache.interceptor.SimpleKey( #root.methodName, #operandA, #operandB, #operandC  )")
    @RequestMapping(value = "/multiply/{a}/{b}/{c:.+}")
    public Result multiply(@PathVariable("a") double operandA, @PathVariable("b") double operandB, @PathVariable("c") double operandC){
        return new Result(operandA*operandB*operandC);
    }

    @Cacheable(value="calc")
    @RequestMapping(value = "/divide/{a}/{b:.+}")
    public Result divide(@PathVariable("a") double operandA, @PathVariable("b") double operandB){
        return new Result(operandA/operandB);
    }
}
