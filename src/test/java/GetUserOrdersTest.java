import client.OrderClient;
import client.UserClient;
import generator.OrderGenerator;
import generator.UserGenerator;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import junitparams.JUnitParamsRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import pojo.CreateOrder;
import pojo.RegisterUser;

import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

@RunWith(JUnitParamsRunner.class)
public class GetUserOrdersTest {
    private final static RegisterUser registerData = UserGenerator.getDefaultRegistrationData();
    private final static CreateOrder createOrderData = OrderGenerator.getDefaultOrder();
    private static String token = "";
    private int statusCode;
    private boolean isGot;

    @BeforeClass
    public static void setUp() {
        ValidatableResponse responseRegister = UserClient.registerUser(registerData);
        token = responseRegister.extract().path("accessToken");
        OrderClient.createOrder(createOrderData, token);
    }

    @AfterClass
    public static void tearDown() {
        UserClient.deleteUser(token);
    }

    @Test
    @DisplayName("Получение заказов конкретного авторизованного пользователя")
    public void getAuthorizedUserOrders() {
        ValidatableResponse responseGetOrders = OrderClient.getUserOrders(token);
        statusCode = responseGetOrders.extract().statusCode();
        isGot = responseGetOrders.extract().path("success");
        List<Object> orders = responseGetOrders.extract().path("orders");

        Assert.assertEquals("Ошибка в коде или теле ответа", List.of(SC_OK, true, false),
                List.of(statusCode, isGot, orders.isEmpty()));
    }

    @Test
    @DisplayName("Получение заказов конкретного неавторизованного пользователя")
    public void getUnauthorizedUserOrders() {
        ValidatableResponse responseGetOrders = OrderClient.getUserOrders("abc");
        statusCode = responseGetOrders.extract().statusCode();
        isGot = responseGetOrders.extract().path("success");

        Assert.assertEquals("Ошибка в коде или теле ответа", List.of(SC_UNAUTHORIZED, false),
                List.of(statusCode, isGot));
    }

}
