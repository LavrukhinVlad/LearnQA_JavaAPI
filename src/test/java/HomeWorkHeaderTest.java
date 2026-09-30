import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomeWorkHeaderTest {

    @Test
    public void testHomeWorkHeader() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/homework_header")
                .andReturn();

        assertTrue(
                response.getHeaders().hasHeaderWithName("x-secret-homework-header"),
                "Response does not have the 'x-secret-homework-header' header"
        );
        assertEquals(
                "Some secret value",
                response.getHeader("x-secret-homework-header"),
                "Unexpected value of the 'x-secret-homework-header' header"
        );
    }
}
