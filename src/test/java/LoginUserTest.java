import client.UserClient;
import generator.UserGenerator;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pojo.LoginUser;
import pojo.RegisterUser;

import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

public class LoginUserTest {
    private final RegisterUser registerData = UserGenerator.getDefaultRegistrationData();
    private LoginUser loginData;
    private String token = "";
    private int statusCode;
    private boolean isLoggedIn;

    @Before
    public void setUp(){
        ValidatableResponse responseRegister = UserClient.registerUser(registerData);
        token = responseRegister.extract().path("accessToken");
    }

    @After
    public void tearDown(){
        UserClient.deleteUser(token);
    }

    @Test
    @DisplayName("Логин пользователя с валидными данными, позитивный тест")
    public void loginUserWithValidData() {
        loginData = UserGenerator.getDefaultLoginData();
        ValidatableResponse responseLogin = UserClient.loginUser(loginData);

        statusCode = responseLogin.extract().statusCode();
        isLoggedIn = responseLogin.extract().path("success");

        Assert.assertEquals("Ошибка в коде или теле ответа", List.of(SC_OK, true),
                List.of(statusCode, isLoggedIn));
    }

    @Test
    @DisplayName("Логин пользователя с неверным логином и паролем, негативный тест")
    public void loginFakeUser() {
        loginData = UserGenerator.getFakeLoginData();
        ValidatableResponse responseLogin = UserClient.loginUser(loginData);

        statusCode = responseLogin.extract().statusCode();
        isLoggedIn = responseLogin.extract().path("success");

        Assert.assertEquals("Ошибка в коде или теле ответа", List.of(SC_UNAUTHORIZED, false),
                List.of(statusCode, isLoggedIn));
    }
}
