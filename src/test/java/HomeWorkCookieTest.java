import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomeWorkCookieTest {

    @Test
    public void testHomeWorkCookie() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/homework_cookie")
                .andReturn();

        Map<String, String> cookies = response.getCookies();

        assertTrue(cookies.containsKey("HomeWork"), "Response does not have a cookie named 'HomeWork'");
        assertEquals("hw_value", cookies.get("HomeWork"), "Unexpected value of the 'HomeWork' cookie");
    }
}
