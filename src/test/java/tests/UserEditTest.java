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
@Feature("Редактирование пользователя (PUT /user/{id})")
@Owner("Vladislav Lavrukhin")
public class UserEditTest extends BaseTestCase {

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

    private Map<String, String> loginAs(Map<String, String> userData) {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));
        Response responseGetAuth = apiCoreRequests.makePostRequest(LOGIN_URL, authData);

        Map<String, String> auth = new HashMap<>();
        auth.put("header", this.getHeader(responseGetAuth, "x-csrf-token"));
        auth.put("cookie", this.getCookie(responseGetAuth, "auth_sid"));
        return auth;
    }

    @Test
    @Story("Позитивное редактирование")
    @DisplayName("Пользователь редактирует своё имя")
    @Description("Авторизованный пользователь может изменить собственный firstName.")
    @Severity(SeverityLevel.BLOCKER)
    @Tag("positive")
    @Tag("user-edit")
    public void testEditJustCreatedUser() {
        Map<String, String> userData = registerUser();
        Map<String, String> auth = loginAs(userData);

        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);
        apiCoreRequests.makePutRequest(USER_URL + userData.get("id"), auth.get("header"), auth.get("cookie"), editData);

        Response responseUserData = apiCoreRequests.makeGetRequest(USER_URL + userData.get("id"), auth.get("header"), auth.get("cookie"));
        Assertions.assertJsonByName(responseUserData, "firstName", newName);
    }

    @Test
    @Story("Негативное редактирование")
    @DisplayName("Нельзя редактировать пользователя без авторизации")
    @Severity(SeverityLevel.CRITICAL)
    @Tag("negative")
    @Tag("security")
    public void testEditUserNotAuth() {
        Map<String, String> userData = registerUser();

        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", "ChangedName");
        Response responseEditUser = apiCoreRequests.makePutRequestWithoutToken(USER_URL + userData.get("id"), editData);

        Assertions.assertResponseCodeEquals(responseEditUser, 400);
        Assertions.assertJsonByName(responseEditUser, "error", "Auth token not supplied");
    }

    @Test
    @Story("Негативное редактирование")
    @DisplayName("Нельзя редактировать данные другого пользователя")
    @Description("Под авторизацией одного пользователя запрещено менять данные другого.")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("DEV-EDIT-OWNERSHIP")
    @Tag("negative")
    @Tag("security")
    public void testEditUserAuthAsAnotherUser() {
        Map<String, String> targetUser = registerUser();
        Map<String, String> anotherUser = registerUser();
        Map<String, String> auth = loginAs(anotherUser);

        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", "ChangedName");
        Response responseEditUser = apiCoreRequests.makePutRequest(USER_URL + targetUser.get("id"), auth.get("header"), auth.get("cookie"), editData);

        Assertions.assertResponseCodeEquals(responseEditUser, 400);
        Assertions.assertJsonByName(responseEditUser, "error", "This user can only edit their own data.");
    }

    @Test
    @Story("Негативное редактирование")
    @DisplayName("Нельзя сменить email на значение без @")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("validation")
    public void testEditUserWithIncorrectEmail() {
        Map<String, String> userData = registerUser();
        Map<String, String> auth = loginAs(userData);

        Map<String, String> editData = new HashMap<>();
        editData.put("email", "learnqaexample.com");
        Response responseEditUser = apiCoreRequests.makePutRequest(USER_URL + userData.get("id"), auth.get("header"), auth.get("cookie"), editData);

        Assertions.assertResponseCodeEquals(responseEditUser, 400);
        Assertions.assertJsonByName(responseEditUser, "error", "Invalid email format");
    }

    @Test
    @Story("Негативное редактирование")
    @DisplayName("Нельзя сменить firstName на значение в один символ")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("validation")
    public void testEditUserWithShortFirstName() {
        Map<String, String> userData = registerUser();
        Map<String, String> auth = loginAs(userData);

        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", "a");
        Response responseEditUser = apiCoreRequests.makePutRequest(USER_URL + userData.get("id"), auth.get("header"), auth.get("cookie"), editData);

        Assertions.assertResponseCodeEquals(responseEditUser, 400);
        Assertions.assertJsonByName(responseEditUser, "error", "The value for field `firstName` is too short");
    }
}
