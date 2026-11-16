package qa.shipsure.api.models;

/** Dates in yyyy-MM-dd format, as the API expects. */
public record BookingDates(String checkin, String checkout) {}
