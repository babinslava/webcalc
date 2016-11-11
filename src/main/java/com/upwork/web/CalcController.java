package com.upwork.web;

import com.upwork.dto.Result;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalcController {

    @Cacheable(value="calc",
            key="new org.springframework.cache.interceptor.SimpleKey( #root.methodName, #operandA, #operandB, #operandC  )")
    @RequestMapping(value = "/add/{a}/{b}/{c:.+}")
    public Result add(@PathVariable("a") float operandA, @PathVariable("b") float operandB, @PathVariable("c") float operandC){
        return new Result(operandA+operandB+operandC);
    }

    @Cacheable(value="calc",
            key="new org.springframework.cache.interceptor.SimpleKey( #root.methodName, #operandA, #operandB, #operandC  )")
    @RequestMapping(value = "/subtract/{a}/{b}/{c:.+}")
    public Result subtract(@PathVariable("a") float operandA, @PathVariable("b") float operandB, @PathVariable("c") float operandC){
        return new Result(operandA-operandB-operandC);
    }

    @Cacheable(value="calc",
            key="new org.springframework.cache.interceptor.SimpleKey( #root.methodName, #operandA, #operandB, #operandC  )")
    @RequestMapping(value = "/multiply/{a}/{b}/{c:.+}")
    public Result multiply(@PathVariable("a") float operandA, @PathVariable("b") float operandB, @PathVariable("c") float operandC){
        return new Result(operandA*operandB*operandC);
    }
}
