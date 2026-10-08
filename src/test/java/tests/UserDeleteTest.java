package tests;

import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserDeleteTest extends BaseTestCase {

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();
    private final String REGISTER_URL = "https://playground.learnqa.ru/api/user";
    private final String LOGIN_URL = "https://playground.learnqa.ru/api/user/login";
    private final String USER_URL = "https://playground.learnqa.ru/api/user/";

    // Registers a fresh user and returns its registration data with the created "id" added.
    private Map<String, String> registerUser() {
        Map<String, String> userData = DataGenerator.getRegistrationData();
        Response responseCreate = apiCoreRequests.makePostRequest(REGISTER_URL, userData);
        userData.put("id", responseCreate.jsonPath().getString("id"));
        return userData;
    }

    private Map<String, String> loginAs(String email, String password) {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", email);
        authData.put("password", password);
        Response responseGetAuth = apiCoreRequests.makePostRequest(LOGIN_URL, authData);

        Map<String, String> auth = new HashMap<>();
        auth.put("header", this.getHeader(responseGetAuth, "x-csrf-token"));
        auth.put("cookie", this.getCookie(responseGetAuth, "auth_sid"));
        return auth;
    }

    // Ex18 #1: the system must not let you delete the protected test user with ID 2.
    @Test
    public void testDeleteProtectedUser() {
        Map<String, String> auth = loginAs("vinkotov@example.com", "1234");

        Response responseDelete = apiCoreRequests.makeDeleteRequest(USER_URL + "2", auth.get("header"), auth.get("cookie"));

        Assertions.assertResponseCodeEquals(responseDelete, 400);
        Assertions.assertJsonByName(responseDelete, "error", "Please, do not delete test users with ID 1, 2, 3, 4 or 5.");
    }

    // Ex18 #2: positive - create a user, authorize as them, delete, then confirm the user is gone.
    @Test
    public void testDeleteJustCreatedUser() {
        Map<String, String> userData = registerUser();
        Map<String, String> auth = loginAs(userData.get("email"), userData.get("password"));

        Response responseDelete = apiCoreRequests.makeDeleteRequest(USER_URL + userData.get("id"), auth.get("header"), auth.get("cookie"));
        Assertions.assertResponseCodeEquals(responseDelete, 200);

        Response responseUserData = apiCoreRequests.makeGetRequest(USER_URL + userData.get("id"), auth.get("header"), auth.get("cookie"));
        Assertions.assertResponseCodeEquals(responseUserData, 404);
        Assertions.assertResponseTextEquals(responseUserData, "User not found");
    }

    // Ex18 #3: negative - try to delete a user while authorized as a DIFFERENT user.
    @Test
    public void testDeleteUserAuthAsAnotherUser() {
        Map<String, String> targetUser = registerUser();
        Map<String, String> anotherUser = registerUser();
        Map<String, String> auth = loginAs(anotherUser.get("email"), anotherUser.get("password"));

        Response responseDelete = apiCoreRequests.makeDeleteRequest(USER_URL + targetUser.get("id"), auth.get("header"), auth.get("cookie"));

        Assertions.assertResponseCodeEquals(responseDelete, 400);
        Assertions.assertJsonByName(responseDelete, "error", "This user can only delete their own account.");
    }
}
