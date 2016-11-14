package com.upwork;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import com.upwork.dto.Result;
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

import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

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
			float [] operands = getRandomOperands();
			float result = operands[0]+operands[1]+operands[2];
			callCalcMethodAndCheckResult(methodName,operands,result);
		}
	}

	@Test
	public void subtractMethodShouldSubtractValues() {
		String methodName="subtract";
		for (int i = 0; i < 20; i++) {
			float[] operands = getRandomOperands();
			float result = operands[0] - operands[1] - operands[2];
			callCalcMethodAndCheckResult(methodName, operands, result);
		}
	}

	@Test
	public void multiplyMethodShouldMultiplyValues() {
		String methodName="multiply";
		for (int i = 0; i < 20; i++) {
			float[] operands = getRandomOperands();
			float result = operands[0] * operands[1] * operands[2];
			callCalcMethodAndCheckResult(methodName, operands, result);
		}
	}

	@Test
	public void multiplyMethodTestMinusZeroResult() {
		String methodName="multiply";
		float[] operands = {1f,-2.5f,0};
		float result = operands[0] * operands[1] * operands[2];
		callCalcMethodAndCheckResult(methodName, operands, result);
	}

	@Test
	public void divideMethodShouldDivideValues() {
		String methodName="divide";
		for (int i = 0; i < 20; i++) {
			float [] operands = getRandomOperands();
			if(operands[1]==0) operands[1]+=0.1f;
			float result = operands[0]/operands[1];
			Response response = callDivideMethod(methodName,operands);
			response.then().body("result",equalTo(result));
		}
	}

	@Test
	public void divideMethodOnZeroShouldReturnInfinity() {
		float operand = (float) ThreadLocalRandom.current().nextDouble(0.1, 100);
		testDivideResult(new float[] {operand,0f});
	}

	@Test
	public void divideMethodOnZeroShouldReturnNegativeInfinity1() {
		float operand = (float) ThreadLocalRandom.current().nextDouble(-100, -0.1);
		testDivideResult(new float[] {operand,0f});
	}

	@Test
	public void divideMethodOnZeroShouldReturnNegativeInfinity2() {
		float operand = (float) ThreadLocalRandom.current().nextDouble(0.1, 100);
		testDivideResult(new float[] {operand,-0f});
	}

	@Test
	public void divideMethodZeroOnZeroShouldReturnNaN() {
		testDivideResult(new float[] {0f,0f});
	}

	@Test
	public void addMethodShouldUseCache() {
		String methodName="add";
		float [] operands = getRandomOperands();
		float result = operands[0]+operands[1]+operands[2];
		callCalcMethod(methodName,operands);
		checkResultInCache(new SimpleKey(methodName,operands[0],operands[1],operands[2]),result);
	}

	@Test
	public void subtractMethodShouldUseCache() {
		String methodName="subtract";
		float [] operands = getRandomOperands();
		float result = operands[0]-operands[1]-operands[2];
		callCalcMethod(methodName,operands);
		checkResultInCache(new SimpleKey(methodName,operands[0],operands[1],operands[2]),result);
	}

	@Test
	public void multiplyMethodShouldUseCache() {
		String methodName="multiply";
		float [] operands = getRandomOperands();
		float result = operands[0]*operands[1]*operands[2];
		callCalcMethod(methodName,operands);
		checkResultInCache(new SimpleKey(methodName,operands[0],operands[1],operands[2]),result);
	}

	@Test
	public void divideMethodShouldUseCache() {
		String methodName="divide";
		float [] operands = getRandomOperands();
		if(operands[1]==0) operands[1]+=0.1f;
		float result = operands[0]/operands[1];
		callDivideMethod(methodName,operands);
		checkResultInCache(new SimpleKey(operands[0],operands[1]),result);
	}

	@Test
	public void shouldUseDifferentCacheKeysOnSameParams() {
		float [] operands = {2f,0.4f,-1.6f};
		String methodName="add";
		float result = operands[0]+operands[1]+operands[2];
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
		Object [] operands = {2f,"sdfg"};
		String methodName = "divide";
		given().
				pathParam("a",operands[0]).
				pathParam("b",operands[1]).
				contentType(JSON).
				log().ifValidationFails().
			when().
				get("/"+methodName+"/{a}/{b}").
			then().
				assertThat().statusCode(400).
				body("exception",equalTo(MethodArgumentTypeMismatchException.class.getName()));
	}

	@Test
	public void shouldGiveErrorWhenInvalidArguments() {
		Object [] operands = {2f,"sdfg",-1.6f};
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
				assertThat().statusCode(400).
				body("exception",equalTo(MethodArgumentTypeMismatchException.class.getName()));
	}

	private void checkResultInCache(SimpleKey cacheKey, float result){
		Ehcache cache = (Ehcache) cacheManager.getCache("calc").getNativeCache();
		final Element cachedResult = cache.getQuiet(cacheKey);
		assertThat("Should find element in cache",cachedResult,notNullValue());
		assertThat("Should be the right result",((Result) cachedResult.getObjectValue()).getResult(),equalTo(result));
	}

	private Response callCalcMethod(String methodName, float [] operands){
		Response response = given().
				pathParam("a",operands[0]).
				pathParam("b",operands[1]).
				pathParam("c",operands[2]).
				contentType(JSON).
				log().ifValidationFails().
			when().
				get("/"+methodName+"/{a}/{b}/{c}").
			then().
				assertThat().statusCode(200).
			extract().
				response();
		return response;
	}

	public void testDivideResult(float [] operands) {
		String methodName="divide";
		float result = operands[0]/operands[1];
		Response response = callDivideMethod(methodName,operands);
		response.then().body("result",equalTo(Float.toString(result)));
	}

	private Response callDivideMethod(String methodName, float [] operands){
		Response response = given().
				pathParam("a",operands[0]).
				pathParam("b",operands[1]).
				contentType(JSON).
				log().ifValidationFails().
			when().
				get("/"+methodName+"/{a}/{b}").
			then().
				assertThat().statusCode(200).
			extract().
				response();
		return response;
	}

	private void callCalcMethodAndCheckResult(String methodName, float [] operands, float result){
		Response response =callCalcMethod(methodName, operands);
		//rest assured's json path convert string '-0' to float 0f
		if (result == -0f) result=0f;
		response.then().body("result",equalTo(result));
	}

	private float[] getRandomOperands(){
		float a = (float) ThreadLocalRandom.current().nextDouble(-100, 100);
		float b = (float) ThreadLocalRandom.current().nextDouble(-100, 100);
		float c = (float) ThreadLocalRandom.current().nextDouble(-100, 100);
		return new float[] {a,b,c};
	}

}