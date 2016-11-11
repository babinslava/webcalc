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

		given().
			pathParam("a",a).
			pathParam("b",b).
			pathParam("c",c).
			contentType(JSON).
		when().
			get("/add/{a}/{b}/{c}").
		then().
			assertThat().statusCode(200).
			body("result",Matchers.equalTo(a+b+c));
	}

	@Test
	public void addMethodShouldUseCache() {
		float a,b,c;
		a=0f;
		b=0.2f;
		c=-3.1f;

		given().
				pathParam("a",a).
				pathParam("b",b).
				pathParam("c",c).
				contentType(JSON).
				when().
				get("/add/{a}/{b}/{c}").
				then().
				assertThat().statusCode(200);

		Ehcache cache = (Ehcache) cacheManager.getCache("calc").getNativeCache();
		SimpleKey key = new SimpleKey(a,b,c);
		final Element cachedResult = cache.getQuiet(key);
		assertThat("Should find element in cache",cachedResult,notNullValue());
		assertThat("Should be the right result",((Result) cachedResult.getObjectValue()).getResult(),Matchers.equalTo(a+b+c));
	}
}