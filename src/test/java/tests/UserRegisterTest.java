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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

@Epic("Автоматизация тестирования REST API на Java")
@Feature("Создание пользователя (POST /user)")
@Owner("Vladislav Lavrukhin")
public class UserRegisterTest extends BaseTestCase {

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();
    private final String URL = "https://playground.learnqa.ru/api/user";

    @Test
    @Story("Негативная регистрация")
    @DisplayName("Нельзя создать пользователя с уже существующим email")
    @Description("Повторная регистрация на занятый email должна отклоняться с кодом 400.")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("registration")
    public void testCreateUserWithExistingEmail() {
        String email = "vinkotov@example.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(URL, userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Users with email '" + email + "' already exists");
    }

    @Test
    @Story("Позитивная регистрация")
    @DisplayName("Успешное создание пользователя")
    @Description("Регистрация с корректными уникальными данными возвращает 200 и поле id.")
    @Severity(SeverityLevel.BLOCKER)
    @Tag("positive")
    @Tag("registration")
    public void testCreateUserSuccessfully() {
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(URL, userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");
    }

    @Test
    @Story("Негативная регистрация")
    @DisplayName("Нельзя создать пользователя с email без @")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("validation")
    public void testCreateUserWithIncorrectEmail() {
        Map<String, String> userData = new HashMap<>();
        userData.put("email", "vinkotovexample.com");
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(URL, userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Invalid email format");
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "password", "username", "firstName", "lastName"})
    @Story("Негативная регистрация")
    @DisplayName("Нельзя создать пользователя без одного из обязательных полей")
    @Description("Отсутствие любого из обязательных полей должно блокировать регистрацию.")
    @Severity(SeverityLevel.NORMAL)
    @Tag("negative")
    @Tag("validation")
    public void testCreateUserWithoutOneField(String fieldName) {
        Map<String, String> userData = DataGenerator.getRegistrationData();
        userData.remove(fieldName);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(URL, userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The following required params are missed: " + fieldName);
    }

    @Test
    @Story("Негативная регистрация")
    @DisplayName("Нельзя создать пользователя с именем в один символ")
    @Severity(SeverityLevel.MINOR)
    @Tag("negative")
    @Tag("validation")
    public void testCreateUserWithShortName() {
        Map<String, String> userData = new HashMap<>();
        userData.put("username", "a");
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(URL, userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'username' field is too short");
    }

    @Test
    @Story("Негативная регистрация")
    @DisplayName("Нельзя создать пользователя с именем длиннее 250 символов")
    @Severity(SeverityLevel.MINOR)
    @Tag("negative")
    @Tag("validation")
    public void testCreateUserWithLongName() {
        StringBuilder longNameBuilder = new StringBuilder();
        for (int i = 0; i < 251; i++) {
            longNameBuilder.append("a");
        }
        String longName = longNameBuilder.toString();

        Map<String, String> userData = new HashMap<>();
        userData.put("username", longName);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(URL, userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'username' field is too long");
    }
}
