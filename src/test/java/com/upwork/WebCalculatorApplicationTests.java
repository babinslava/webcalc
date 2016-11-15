package com.upwork;

import com.upwork.dto.Result;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebCalculatorApplicationTests {

	private static final Logger logger =
			LoggerFactory.getLogger(WebCalculatorApplicationTests.class);

	@Autowired
	CacheManager cacheManager;

	@LocalServerPort
	private int port;

	@Before
	public void init() {
		RestAssured.port = port;
	}

	@Test
	public void addMethodShouldAddValues() {
		String methodName="add";
		for (int i = 0; i < 20; i++) {
			double [] operands = getRandomOperands();
			double result = (operands[0]+operands[1]+operands[2]);
			callCalcMethodAndCheckResult(methodName,operands,result);
		}
	}

	@Test
	public void subtractMethodShouldSubtractValues() {
		String methodName="subtract";
		for (int i = 0; i < 20; i++) {
			double[] operands = getRandomOperands();
			double result = (operands[0] - operands[1] - operands[2]);
			callCalcMethodAndCheckResult(methodName, operands, result);
		}
	}

	@Test
	public void multiplyMethodShouldMultiplyValues() {
		String methodName="multiply";
		for (int i = 0; i < 20; i++) {
			double [] operands = getRandomOperands();
			double result = (operands[0] * operands[1] * operands[2]);
			callCalcMethodAndCheckResult(methodName, operands, result);
		}
	}

	@Test
	public void multiplyMethodTestMinusZeroResult() {
		String methodName="multiply";
		double [] operands = {1,-2.5,0};
		double result = (operands[0] * operands[1] * operands[2]);
		callCalcMethodAndCheckResult(methodName, operands, result);
	}

	@Test
	public void divideMethodShouldDivideValues() {
		String methodName="divide";
		for (int i = 0; i < 20; i++) {
			double [] operands = getRandomOperands();
			if(operands[1]==0) operands[1]+=0.1;
			double result = operands[0]/operands[1];
			Response response = callDivideMethod(methodName,operands);
			response.then().body("result",closeTo(BigDecimal.valueOf(result),new BigDecimal("1E-20")));
		}
	}

	@Test
	public void divideMethodOnZeroShouldReturnInfinity() {
		double operand =  ThreadLocalRandom.current().nextDouble(0.1, 100);
		testDivideResult(new double [] {operand,0});
	}

	@Test
	public void divideMethodOnZeroShouldReturnNegativeInfinity1() {
		double operand = ThreadLocalRandom.current().nextDouble(-100, -0.1);
		testDivideResult(new double [] {operand,0});
	}

	@Test
	public void divideMethodOnZeroShouldReturnNegativeInfinity2() {
		double operand = ThreadLocalRandom.current().nextDouble(0.1, 100);
		testDivideResult(new double [] {operand,-0});
	}

	@Test
	public void divideMethodZeroOnZeroShouldReturnNaN() {
		testDivideResult(new double [] {0f,0f});
	}

	@Test
	public void addMethodShouldUseCache() {
		String methodName="add";
		double [] operands = getRandomOperands();
		double result = operands[0]+operands[1]+operands[2];
		callCalcMethod(methodName,operands);
		checkResultInCache(new SimpleKey(methodName,operands[0],operands[1],operands[2]),result);
	}

	@Test
	public void subtractMethodShouldUseCache() {
		String methodName="subtract";
		double [] operands = getRandomOperands();
		double result = (operands[0]-operands[1]-operands[2]);
		callCalcMethod(methodName,operands);
		checkResultInCache(new SimpleKey(methodName,operands[0],operands[1],operands[2]),result);
	}

	@Test
	public void multiplyMethodShouldUseCache() {
		String methodName="multiply";
		double [] operands = getRandomOperands();
		double result = (operands[0]*operands[1]*operands[2]);
		callCalcMethod(methodName,operands);
		checkResultInCache(new SimpleKey(methodName,operands[0],operands[1],operands[2]),result);
	}

	@Test
	public void divideMethodShouldUseCache() {
		String methodName="divide";
		double [] operands = getRandomOperands();
		if(operands[1]==0) operands[1]+=0.1f;
		double result = operands[0]/operands[1];
		callDivideMethod(methodName,operands);
		checkResultInCache(new SimpleKey(operands[0],operands[1]),result);
	}

	@Test
	public void shouldUseDifferentCacheKeysOnSameParams() {
		double [] operands = {2,0.4,-1.6};
		String methodName="add";
		double result = operands[0]+operands[1]+operands[2];
		callCalcMethodAndCheckResult(methodName,operands,result);
		methodName="subtract";
		result = operands[0]-operands[1]-operands[2];
		callCalcMethodAndCheckResult(methodName,operands,result);
		methodName="multiply";
		result = operands[0] * operands[1] * operands[2];
		callCalcMethodAndCheckResult(methodName, operands, result);
	}

	@Test
	public void divideMethodShouldGiveErrorWhenInvalidArguments() {
		Object [] operands = {2,"sdfg"};
		String methodName = "divide";
		given().
				pathParam("a",operands[0]).
				pathParam("b",operands[1]).
				contentType(JSON).
				log().ifValidationFails().
			when().
				get("/"+methodName+"/{a}/{b}").
			then().
				log().ifValidationFails().
				assertThat().statusCode(400).
				body("exception",equalTo(MethodArgumentTypeMismatchException.class.getName()));
	}

	@Test
	public void shouldGiveErrorWhenInvalidArguments() {
		Object [] operands = {2,"sdfg",-1.6};
		callCalcMethodWithInvalidArgumentAndCheckResult("add",operands);
		callCalcMethodWithInvalidArgumentAndCheckResult("subtract",operands);
		callCalcMethodWithInvalidArgumentAndCheckResult("multiply",operands);
	}

	public void callCalcMethodWithInvalidArgumentAndCheckResult(String methodName, Object [] operands) {
		given().
				pathParam("a",operands[0]).
				pathParam("b",operands[1]).
				pathParam("c",operands[2]).
				contentType(JSON).
				log().ifValidationFails().
			when().
				get("/"+methodName+"/{a}/{b}/{c}").
			then().
				log().ifValidationFails().
				assertThat().statusCode(400).
				body("exception",equalTo(MethodArgumentTypeMismatchException.class.getName()));
	}

	private void checkResultInCache(SimpleKey cacheKey, double result){
		Ehcache cache = (Ehcache) cacheManager.getCache("calc").getNativeCache();
		final Element cachedResult = cache.getQuiet(cacheKey);
		assertThat("Should find element in cache",cachedResult,notNullValue());
		assertThat("Should be the right result",((Result) cachedResult.getObjectValue()).getResult(),equalTo(result));
	}

	private Response callCalcMethod(String methodName, double [] operands){
		Response response = given().
				config(RestAssured.config().jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL))).
				pathParam("a",operands[0]).
				pathParam("b",operands[1]).
				pathParam("c",operands[2]).
				contentType(JSON).
				log().ifValidationFails().
			when().
				get("/"+methodName+"/{a}/{b}/{c}").
			then().
				assertThat().statusCode(200).
				log().ifValidationFails().
			extract().
				response();
		return response;
	}

	public void testDivideResult(double [] operands) {
		String methodName="divide";
		double result = operands[0]/operands[1];
		Response response = callDivideMethod(methodName,operands);
		response.then().body("result",equalTo(Double.toString(result)));
		logger.info(Double.toString(result));
	}

	private Response callDivideMethod(String methodName, double [] operands){
		Response response = given().
				config(RestAssured.config().jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL))).
				pathParam("a",operands[0]).
				pathParam("b",operands[1]).
				contentType(JSON).
				log().ifValidationFails().
			when().
				get("/"+methodName+"/{a}/{b}").
			then().
				log().ifValidationFails().
				assertThat().statusCode(200).
			extract().
				response();
		return response;
	}

	private void callCalcMethodAndCheckResult(String methodName, double [] operands, double result){
		Response response =callCalcMethod(methodName, operands);
		response.then().body("result",closeTo(BigDecimal.valueOf(result),new BigDecimal("1E-20")));
	}

	private double[] getRandomOperands(){
		double a = ThreadLocalRandom.current().nextDouble(-100, 100);
		double b = ThreadLocalRandom.current().nextDouble(-100, 100);
		double c = ThreadLocalRandom.current().nextDouble(-100, 100);
		return new double[] {a,b,c};
	}

}