package com.upwork;

import com.jayway.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebCalculatorApplicationTests {

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

}
