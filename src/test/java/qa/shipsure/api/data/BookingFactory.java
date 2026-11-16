package qa.shipsure.api.data;

import qa.shipsure.api.models.Booking;
import qa.shipsure.api.models.BookingDates;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

/** Builds valid, unique test bookings so tests never depend on shared data. */
public final class BookingFactory {
    private BookingFactory() {}

    public static Booking random() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        LocalDate checkin = LocalDate.now().plusDays(r.nextInt(10, 60));
        return new Booking(
                "Test" + r.nextInt(1000, 9999),
                "Guest" + r.nextInt(1000, 9999),
                r.nextInt(50, 1000),
                r.nextBoolean(),
                new BookingDates(checkin.toString(), checkin.plusDays(r.nextInt(1, 14)).toString()),
                "Breakfast");
    }

    public static Booking with(String firstname, String lastname, int price, boolean deposit, String needs) {
        LocalDate checkin = LocalDate.now().plusDays(30);
        return new Booking(firstname, lastname, price, deposit,
                new BookingDates(checkin.toString(), checkin.plusDays(3).toString()), needs);
    }
}
