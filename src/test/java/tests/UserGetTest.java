package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
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
@Feature("Получение данных пользователя (GET /user/{id})")
@Owner("Vladislav Lavrukhin")
public class UserGetTest extends BaseTestCase {

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();
    private final String USER_URL = "https://playground.learnqa.ru/api/user/";
    private final String LOGIN_URL = "https://playground.learnqa.ru/api/user/login";
    private final String REGISTER_URL = "https://playground.learnqa.ru/api/user";

    @Test
    @Story("Просмотр данных без авторизации")
    @DisplayName("Неавторизованный запрос возвращает только username")
    @Description("Без авторизации доступно только публичное поле username.")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("user-get")
    public void testGetUserDataNotAuth() {
        Response responseUserData = apiCoreRequests.makeGetRequestWithoutToken(USER_URL + "2");

        Assertions.asserJsonHasKey(responseUserData, "username");
        Assertions.asserJsonHasNotKey(responseUserData, "firstName");
        Assertions.asserJsonHasNotKey(responseUserData, "lastName");
        Assertions.asserJsonHasNotKey(responseUserData, "email");
    }

    @Test
    @Story("Просмотр своих данных")
    @DisplayName("Авторизованный пользователь видит все свои поля")
    @Description("При запросе своих данных возвращаются username, firstName, lastName, email.")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("positive")
    @Tag("user-get")
    public void testGetUserDetailsAuthAsSameUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(LOGIN_URL, authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseUserData = apiCoreRequests.makeGetRequest(USER_URL + "2", header, cookie);

        String[] expectedFields = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData, expectedFields);
    }

    // Ex16: authorize as one user but request another user's data -> only username must be visible
    @Test
    @Story("Просмотр чужих данных")
    @DisplayName("Чужие данные ограничены полем username")
    @Description("Авторизованный пользователь не должен видеть приватные поля другого пользователя.")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("security")
    @Tag("user-get")
    public void testGetUserDetailsAuthAsOtherUser() {
        // Register another user to request data of
        Map<String, String> otherUserData = DataGenerator.getRegistrationData();
        Response responseCreateOtherUser = apiCoreRequests.makePostRequest(REGISTER_URL, otherUserData);
        String otherUserId = responseCreateOtherUser.jsonPath().getString("id");

        // Authorize as the base user (vinkotov, id 2)
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(LOGIN_URL, authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        // Request the OTHER user's data while authorized as the base user
        Response responseUserData = apiCoreRequests.makeGetRequest(USER_URL + otherUserId, header, cookie);

        Assertions.asserJsonHasKey(responseUserData, "username");
        Assertions.asserJsonHasNotKey(responseUserData, "firstName");
        Assertions.asserJsonHasNotKey(responseUserData, "lastName");
        Assertions.asserJsonHasNotKey(responseUserData, "email");
    }
}
