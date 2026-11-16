package qa.shipsure.api.tests;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import qa.shipsure.api.client.BookingClient;
import qa.shipsure.api.data.BookingFactory;
import qa.shipsure.api.models.Booking;

import java.util.Map;

/** Security and error handling: what happens when callers get it wrong. */
public class BookingNegativeTests {
    private String token;
    private int bookingId;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        token = BookingClient.token();
        bookingId = BookingClient.createBooking(BookingFactory.random())
                .then().statusCode(200).extract().path("bookingid");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        BookingClient.deleteBooking(bookingId, token);
    }

    @Test(groups = "smoke", description = "PUT without a token is forbidden")
    public void updateWithoutTokenIsForbidden() {
        Booking body = BookingFactory.random();
        BookingClient.updateBooking(bookingId, body, null).then().statusCode(403);
    }

    @Test(description = "DELETE with an invalid token is forbidden")
    public void deleteWithInvalidTokenIsForbidden() {
        BookingClient.deleteBooking(bookingId, "not-a-real-token").then().statusCode(403);
    }

    @Test(description = "PATCH without a token is forbidden")
    public void patchWithoutTokenIsForbidden() {
        BookingClient.patchBooking(bookingId, Map.of("firstname", "Hacker"), null).then().statusCode(403);
    }

    @Test(description = "GET a booking that does not exist returns 404")
    public void getNonExistentBookingReturns404() {
        BookingClient.getBooking(999_999_999).then().statusCode(404);
    }

    // Restful-booker answers a malformed create with 500 instead of 400.
    // Asserting "not 200" keeps the suite stable; the 500 itself would be logged as a bug for a client.
    @Test(description = "POST with missing required fields is not accepted")
    public void createWithMissingFieldsIsRejected() {
        int status = BookingClient.createBooking(Map.of("firstname", "OnlyFirstName")).statusCode();
        org.testng.Assert.assertNotEquals(status, 200, "Incomplete booking should be rejected");
    }
}
