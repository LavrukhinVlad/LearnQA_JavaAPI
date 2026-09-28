import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class LongRedirectTest {

    @Test
    public void testLongRedirectChain() {
        String url = "https://playground.learnqa.ru/api/long_redirect";
        int redirectCount = 0;

        while (true) {
            Response response = RestAssured
                    .given()
                    .redirects()
                    .follow(false)
                    .when()
                    .get(url)
                    .andReturn();

            int statusCode = response.getStatusCode();
            System.out.println(statusCode + " " + url);

            if (statusCode == 200) {
                break;
            }

            url = response.getHeader("Location");
            redirectCount++;
        }

        System.out.println("Redirects: " + redirectCount);
        System.out.println("Final URL: " + url);
    }
}
