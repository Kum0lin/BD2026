package com.booking.bookingservice.dto;

import java.time.LocalDate;

public record BookingResponse(
        Long id,
        String customerName,
        String roomType,
        LocalDate dateFrom,
        LocalDate dateTo,
        HotelReference hotel
) {
}
