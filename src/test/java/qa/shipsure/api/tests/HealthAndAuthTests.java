package qa.shipsure.api.tests;

import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import qa.shipsure.api.client.BookingClient;

import static org.hamcrest.Matchers.*;

public class HealthAndAuthTests {

    @Test(groups = "smoke", description = "Health check endpoint is up")
    public void pingReturns201() {
        BookingClient.ping().then().statusCode(201);
    }

    @Test(groups = "smoke", description = "Valid credentials return a non-empty token")
    public void validCredentialsReturnToken() {
        BookingClient.auth(BookingClient.ADMIN_USER, BookingClient.ADMIN_PASSWORD)
                .then()
                .statusCode(200)
                .body("token", not(emptyOrNullString()))
                .time(lessThan(10000L)); // generous: free host may cold-start
    }

    @DataProvider(name = "badCredentials")
    public Object[][] badCredentials() {
        return new Object[][]{
                {"admin", "wrong-password"},
                {"unknown-user", "password123"},
                {"", ""},
        };
    }

    // Restful-booker returns 200 + {"reason":"Bad credentials"} rather than 401.
    // Documented here as observed behaviour; in a client project this would be raised as a finding.
    @Test(dataProvider = "badCredentials", description = "Invalid credentials never return a token")
    public void invalidCredentialsAreRejected(String user, String password) {
        Response res = BookingClient.auth(user, password);
        res.then()
                .body("token", nullValue())
                .body("reason", equalTo("Bad credentials"));
    }
}
