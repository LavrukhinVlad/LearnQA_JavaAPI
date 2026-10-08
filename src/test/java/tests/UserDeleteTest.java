package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@Epic("Автоматизация тестирования REST API на Java")
@Feature("Удаление пользователя (DELETE /user/{id})")
@Owner("Vladislav Lavrukhin")
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

    @Test
    @Story("Защита тестовых пользователей")
    @DisplayName("Нельзя удалить защищённого пользователя с ID 2")
    @Description("Удаление тестовых пользователей с ID 1-5 должно быть запрещено.")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("negative")
    @Tag("user-delete")
    public void testDeleteProtectedUser() {
        Map<String, String> auth = loginAs("vinkotov@example.com", "1234");

        Response responseDelete = apiCoreRequests.makeDeleteRequest(USER_URL + "2", auth.get("header"), auth.get("cookie"));

        Assertions.assertResponseCodeEquals(responseDelete, 400);
        Assertions.assertJsonByName(responseDelete, "error", "Please, do not delete test users with ID 1, 2, 3, 4 or 5.");
    }

    @Test
    @Story("Позитивное удаление")
    @DisplayName("Пользователь удаляет сам себя, после чего не находится")
    @Description("После удаления своего аккаунта повторный GET по ID возвращает 404 User not found.")
    @Severity(SeverityLevel.BLOCKER)
    @Tag("positive")
    @Tag("user-delete")
    public void testDeleteJustCreatedUser() {
        Map<String, String> userData = registerUser();
        Map<String, String> auth = loginAs(userData.get("email"), userData.get("password"));

        Response responseDelete = apiCoreRequests.makeDeleteRequest(USER_URL + userData.get("id"), auth.get("header"), auth.get("cookie"));
        Assertions.assertResponseCodeEquals(responseDelete, 200);

        Response responseUserData = apiCoreRequests.makeGetRequest(USER_URL + userData.get("id"), auth.get("header"), auth.get("cookie"));
        Assertions.assertResponseCodeEquals(responseUserData, 404);
        Assertions.assertResponseTextEquals(responseUserData, "User not found");
    }

    @Test
    @Story("Негативное удаление")
    @DisplayName("Нельзя удалить другого пользователя")
    @Description("Под авторизацией одного пользователя запрещено удалять аккаунт другого.")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("DEV-DELETE-OWNERSHIP")
    @Tag("negative")
    @Tag("security")
    public void testDeleteUserAuthAsAnotherUser() {
        Map<String, String> targetUser = registerUser();
        Map<String, String> anotherUser = registerUser();
        Map<String, String> auth = loginAs(anotherUser.get("email"), anotherUser.get("password"));

        Response responseDelete = apiCoreRequests.makeDeleteRequest(USER_URL + targetUser.get("id"), auth.get("header"), auth.get("cookie"));

        Assertions.assertResponseCodeEquals(responseDelete, 400);
        Assertions.assertJsonByName(responseDelete, "error", "This user can only delete their own account.");
    }
}
