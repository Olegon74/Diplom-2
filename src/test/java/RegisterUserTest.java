import client.UserClient;
import generator.UserGenerator;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import pojo.RegisterUser;

import java.util.List;

import static generator.UserGenerator.UserField.*;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;

@RunWith(JUnitParamsRunner.class)
public class RegisterUserTest {
    private RegisterUser registerData;
    private String token = "";
    private int statusCode;
    private boolean isRegistered;

    @Test
    @DisplayName("Создание пользователя с валидными данными, позитивный тест")
    public void registerUserWithValidData() {
        registerData = UserGenerator.getDefaultRegistrationData();
        ValidatableResponse responseRegister = UserClient.registerUser(registerData);

        token = responseRegister.extract().path("accessToken");
        statusCode = responseRegister.extract().statusCode();
        isRegistered = responseRegister.extract().path("success");

        UserClient.deleteUser(token);

        Assert.assertEquals("Ошибка в коде или теле ответа", List.of(SC_OK, true),
                List.of(statusCode, isRegistered));
    }

    @Test
    @DisplayName("Создание пользователя с занятым email, негативный тест")
    public void registerDuplicateUser() {
        registerData = UserGenerator.getDefaultRegistrationData();
        ValidatableResponse responseRegister1 = UserClient.registerUser(registerData);
        ValidatableResponse responseRegister2 = UserClient.registerUser(registerData);

        token = responseRegister1.extract().path("accessToken");
        statusCode = responseRegister2.extract().statusCode();
        isRegistered = responseRegister2.extract().path("success");

        UserClient.deleteUser(token);

        Assert.assertEquals("Ошибка в коде или теле ответа", List.of(SC_FORBIDDEN, false),
                List.of(statusCode, isRegistered));
    }

    @Test
    @Parameters(method = "registerUserWithOneEmptyFieldParameters")
    @TestCaseName("Создание пользователя поочередно без email, password, name, негативный тест")
    public void registerUserWithOneEmptyField(UserGenerator.UserField emptyField) {
        registerData = UserGenerator.getRegistrationDataWithOneEmptyField(emptyField);
        ValidatableResponse responseRegister = UserClient.registerUser(registerData);

        statusCode = responseRegister.extract().statusCode();
        isRegistered = responseRegister.extract().path("success");

        Assert.assertEquals("Ошибка 403 Forbidden, в коде или теле ответа", List.of(SC_FORBIDDEN, false),
                List.of(statusCode, isRegistered));
    }
    private Object[][] registerUserWithOneEmptyFieldParameters() {
        return new Object[][]{
                {EMAIL}, {PASSWORD}, {NAME},
        };
    }

}
