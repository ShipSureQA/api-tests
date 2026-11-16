package qa.shipsure.api.tests;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import qa.shipsure.api.client.BookingClient;
import qa.shipsure.api.data.BookingFactory;
import qa.shipsure.api.models.Booking;
import qa.shipsure.api.models.CreatedBooking;

import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertEquals;

/** Full lifecycle: create → read → update → partial update → delete → confirm gone. */
public class BookingCrudTests {
    private String token;
    private int bookingId;
    private Booking created;

    @BeforeClass(alwaysRun = true)
    public void authenticate() {
        token = BookingClient.token();
    }

    @Test(groups = "smoke", description = "POST /booking creates a booking and echoes it back")
    public void createBooking() {
        created = BookingFactory.random();
        CreatedBooking res = BookingClient.createBooking(created)
                .then()
                .statusCode(200)
                .body("bookingid", greaterThan(0))
                .extract().as(CreatedBooking.class);

        bookingId = res.bookingid();
        assertEquals(res.booking(), created, "Response booking should match the request");
    }

    @Test(groups = "smoke", dependsOnMethods = "createBooking",
          description = "GET /booking/{id} returns the booking and matches the JSON schema")
    public void getBookingById() {
        Booking fetched = BookingClient.getBooking(bookingId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"))
                .extract().as(Booking.class);
        assertEquals(fetched, created);
    }

    @Test(dependsOnMethods = "getBookingById", description = "Booking can be found by name filter")
    public void filterBookingsByName() {
        BookingClient.getBookingIds(Map.of("firstname", created.firstname(), "lastname", created.lastname()))
                .then()
                .statusCode(200)
                .body("bookingid", hasItem(bookingId));
    }

    @Test(dependsOnMethods = "filterBookingsByName", description = "PUT /booking/{id} replaces the booking")
    public void updateBooking() {
        Booking updated = BookingFactory.with(created.firstname(), created.lastname(), 999, true, "Late checkout");
        Booking res = BookingClient.updateBooking(bookingId, updated, token)
                .then()
                .statusCode(200)
                .extract().as(Booking.class);
        assertEquals(res, updated);
        created = updated;
    }

    @Test(dependsOnMethods = "updateBooking", description = "PATCH /booking/{id} changes only the given fields")
    public void partialUpdateBooking() {
        BookingClient.patchBooking(bookingId, Map.of("additionalneeds", "Airport pickup"), token)
                .then()
                .statusCode(200)
                .body("additionalneeds", equalTo("Airport pickup"))
                .body("totalprice", equalTo(created.totalprice()))
                .body("firstname", equalTo(created.firstname()));
    }

    @Test(dependsOnMethods = "partialUpdateBooking", description = "DELETE /booking/{id} removes the booking")
    public void deleteBooking() {
        BookingClient.deleteBooking(bookingId, token).then().statusCode(201);
        BookingClient.getBooking(bookingId).then().statusCode(404);
    }

    @DataProvider(name = "bookingVariants")
    public Object[][] bookingVariants() {
        return new Object[][]{
                {BookingFactory.with("Anna", "Smith", 1, false, "None")},
                {BookingFactory.with("José", "García-López", 5000, true, "Cot for baby")},
                {BookingFactory.with("O'Brien", "Lee", 120, true, "Late arrival")},
        };
    }

    @Test(dataProvider = "bookingVariants", description = "Data-driven: varied valid bookings are accepted unchanged")
    public void createVariousBookings(Booking booking) {
        CreatedBooking res = BookingClient.createBooking(booking)
                .then().statusCode(200)
                .extract().as(CreatedBooking.class);
        assertEquals(res.booking(), booking);
        BookingClient.deleteBooking(res.bookingid(), token); // clean up
    }
}
