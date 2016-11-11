package com.upwork;

import com.jayway.restassured.RestAssured;
import com.upwork.dto.Result;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.hamcrest.Matchers;
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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebCalculatorApplicationTests {

	private static final Logger logger =
			LoggerFactory.getLogger(WebCalculatorApplicationTests.class);

	@Autowired
	CacheManager cacheManager;

	@LocalServerPort
	int port;

	@Before
	public void init() {
		RestAssured.port = port;
	}

	@Test
	public void addMethodShouldAddValues() {
		float a,b,c;
		a=1.2f;
		b=2.3f;
		c=3.1f;

		callCalcMethodAndCheckResult("add",a,b,c,a+b+c);
	}

	@Test
	public void subtractMethodShouldSubtractValues() {
		float a,b,c;
		a=1.2f;
		b=2.3f;
		c=3.1f;

		callCalcMethodAndCheckResult("subtract",a,b,c,a-b-c);
	}

	@Test
	public void addMethodShouldUseCache() {
		float a,b,c;
		a=0f;
		b=0.2f;
		c=-3.1f;

		String methodName="add";
		float result = a+b+c;
		callCalcMethod(methodName,a,b,c);
		checkResultInCache(new SimpleKey(methodName,a,b,c),result);
	}

	@Test
	public void subtractMethodShouldUseCache() {
		float a,b,c;
		a=2f;
		b=0.4f;
		c=-1.6f;

		String methodName="subtract";
		float result = a-b-c;
		callCalcMethod(methodName,a,b,c);
		checkResultInCache(new SimpleKey(methodName,a,b,c),result);
	}

	private void checkResultInCache(SimpleKey cacheKey, float result){
		Ehcache cache = (Ehcache) cacheManager.getCache("calc").getNativeCache();
		final Element cachedResult = cache.getQuiet(cacheKey);
		assertThat("Should find element in cache",cachedResult,notNullValue());
		assertThat("Should be the right result",((Result) cachedResult.getObjectValue()).getResult(),Matchers.equalTo(result));
	}

	private void callCalcMethod(String methodName, float operandA, float operandB, float operandC){
		given().
			pathParam("a",operandA).
			pathParam("b",operandB).
			pathParam("c",operandC).
			contentType(JSON).
		when().
			get("/"+methodName+"/{a}/{b}/{c}").
		then().
			assertThat().statusCode(200);
	}
	private void callCalcMethodAndCheckResult(String methodName, float operandA, float operandB, float operandC, float result){
		given().
			pathParam("a",operandA).
			pathParam("b",operandB).
			pathParam("c",operandC).
			contentType(JSON).
		when().
			get("/"+methodName+"/{a}/{b}/{c}").
		then().
			assertThat().statusCode(200).
			body("result",Matchers.equalTo(result));
	}

}