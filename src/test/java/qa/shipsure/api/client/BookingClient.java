package qa.shipsure.api.client;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import qa.shipsure.api.models.Booking;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * One place for every endpoint. Tests read like specs; if the API changes,
 * only this class changes.
 */
public final class BookingClient {
    private static final String BASE_URL =
            System.getProperty("baseUrl", "https://restful-booker.herokuapp.com");

    // Public demo credentials documented by restful-booker
    public static final String ADMIN_USER = "admin";
    public static final String ADMIN_PASSWORD = "password123";

    private static final RequestSpecification SPEC = new RequestSpecBuilder()
            .setBaseUri(BASE_URL)
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .log(LogDetail.URI)
            .build();

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private BookingClient() {}

    public static Response ping() {
        return given(SPEC).get("/ping");
    }

    public static Response auth(String username, String password) {
        return given(SPEC).body(Map.of("username", username, "password", password)).post("/auth");
    }

    /** Returns a valid token or fails fast. */
    public static String token() {
        String token = auth(ADMIN_USER, ADMIN_PASSWORD).then().statusCode(200).extract().path("token");
        if (token == null) throw new IllegalStateException("Auth did not return a token");
        return token;
    }

    public static Response getBookingIds(Map<String, ?> filters) {
        return given(SPEC).queryParams(filters).get("/booking");
    }

    public static Response getBooking(int id) {
        return given(SPEC).get("/booking/{id}", id);
    }

    public static Response createBooking(Object body) {
        return given(SPEC).body(body).post("/booking");
    }

    public static Response updateBooking(int id, Booking body, String token) {
        return withToken(token).body(body).put("/booking/{id}", id);
    }

    public static Response patchBooking(int id, Map<String, ?> fields, String token) {
        return withToken(token).body(fields).patch("/booking/{id}", id);
    }

    public static Response deleteBooking(int id, String token) {
        return withToken(token).delete("/booking/{id}", id);
    }

    private static RequestSpecification withToken(String token) {
        RequestSpecification req = given(SPEC);
        return token == null ? req : req.cookie("token", token);
    }
}
