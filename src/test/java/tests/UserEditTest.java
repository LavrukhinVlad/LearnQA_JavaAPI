package tests;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserEditTest  extends BaseTestCase {
    @Test
    public void testEditJustCreatedTest() {
        //GENERATE USER
        Map<String,String> userData = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post("https://playground.learnqa.ru/api/user/")
                .jsonPath();

                String userId = responseCreateAuth.getString("id");

                //LOGIN
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response reponseGetAuth = RestAssured
                .given()
                .body(authData)
                .post("https://playground.learnqa.ru/api/user/login")
                .andReturn();

        //EDIT
        String newName = "Changed Name";
                Map<String, String> editData = new HashMap<>();
                authData.put("firstName", newName);

                Response reponseEditUser = RestAssured
                        .given()
                        .header("x-csrf-token", this.getHeader(reponseGetAuth, "x-csrf-token"))
                        .cookie("auth-sid", this.getCookie(reponseGetAuth, "auth-sid"))
                        .body(editData)
                        .post("https://playground.learnqa.ru/api/user/" + userId)
                        .andReturn();

                //GET
        Response reponseUserData = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(reponseGetAuth, "x-csrf-token"))
                .cookie("auth-sid", this.getCookie(reponseGetAuth, "auth-sid"))
                .get("https://playground.learnqa.ru/api/user/" + userId)
                .andReturn();

        System.out.println(reponseUserData.asString());
    }
}
