package com.booking.bookingservice.dto;

import java.time.LocalDate;

public record BookingRequest(
        String customerName,
        String roomType,
        LocalDate dateFrom,
        LocalDate dateTo,
        HotelReference hotel
) {
}
