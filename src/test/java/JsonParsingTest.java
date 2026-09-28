import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

public class JsonParsingTest {

    @Test
    public void testGetSecondMessage() {
        JsonPath response = RestAssured
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .jsonPath();

        response.prettyPrint();

        String secondMessage = response.get("messages[1].message");
        System.out.println(secondMessage);
    }
}