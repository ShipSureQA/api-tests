package qa.shipsure.api.tests;

import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import qa.shipsure.api.client.BookingClient;
import qa.shipsure.api.data.BookingFactory;
import qa.shipsure.api.models.Booking;
import qa.shipsure.api.models.CreatedBooking;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Hostile input must be treated as plain data: no server error, no execution,
 * stored and returned exactly as sent (output encoding is the consumer's job).
 */
public class BookingSecurityTests {
    private String token;

    @BeforeClass(alwaysRun = true)
    public void authenticate() {
        token = BookingClient.token();
    }

    @DataProvider(name = "hostilePayloads")
    public Object[][] hostilePayloads() {
        return new Object[][]{
                {"SQL injection", "Robert'); DROP TABLE bookings;--"},
                {"SQL tautology", "' OR '1'='1"},
                {"NoSQL operator", "{\"$gt\": \"\"}"},
                {"Stored XSS", "<script>alert('xss')</script>"},
                {"HTML injection", "<img src=x onerror=alert(1)>"},
                {"Unicode/emoji", "Zoë 😀 测试"},
        };
    }

    @Test(dataProvider = "hostilePayloads", description = "Hostile strings are stored literally and never cause a 5xx")
    public void hostileInputIsStoredAsPlainData(String attack, String payload) {
        Booking booking = BookingFactory.with(payload, "Security", 100, true, payload);
        Response res = BookingClient.createBooking(booking);

        assertTrue(res.statusCode() < 500, attack + " caused a server error: " + res.statusCode());
        assertEquals(res.statusCode(), 200, attack + " should be accepted as plain text");

        CreatedBooking created = res.as(CreatedBooking.class);
        Booking fetched = BookingClient.getBooking(created.bookingid()).then().statusCode(200).extract().as(Booking.class);
        assertEquals(fetched.firstname(), payload, attack + ": value was altered or executed");
        assertEquals(fetched.additionalneeds(), payload);

        BookingClient.deleteBooking(created.bookingid(), token); // clean up
    }

    @Test(description = "Token from a deleted/expired session cannot modify data")
    public void forgedTokenCannotDelete() {
        int id = BookingClient.createBooking(BookingFactory.random()).then().statusCode(200).extract().path("bookingid");
        BookingClient.deleteBooking(id, "abc123forged").then().statusCode(403);
        BookingClient.getBooking(id).then().statusCode(200); // still there
        BookingClient.deleteBooking(id, token);
    }
}
