import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LongtimeJobTest {

    private static final String URL = "https://playground.learnqa.ru/ajax/api/longtime_job";

    @Test
    public void testLongtimeJob() throws InterruptedException {
        // 1) Create a job
        JsonPath createResponse = RestAssured
                .get(URL)
                .jsonPath();

        String token = createResponse.getString("token");
        int seconds = createResponse.getInt("seconds");
        System.out.println("token: " + token + ", seconds: " + seconds);

        // 2) Check the job before it is ready
        JsonPath notReadyResponse = RestAssured
                .given()
                .queryParam("token", token)
                .get(URL)
                .jsonPath();

        assertEquals("Job is NOT ready", notReadyResponse.getString("status"));
        assertNull(notReadyResponse.getString("result"), "result should be absent before the job is ready");

        // 3) Wait until the job is done
        Thread.sleep(seconds * 1000L);

        // 4) Check the job after it is ready
        JsonPath readyResponse = RestAssured
                .given()
                .queryParam("token", token)
                .get(URL)
                .jsonPath();

        assertEquals("Job is ready", readyResponse.getString("status"));
        assertNotNull(readyResponse.getString("result"), "result should be present after the job is ready");
        System.out.println("result: " + readyResponse.getString("result"));
    }
}
