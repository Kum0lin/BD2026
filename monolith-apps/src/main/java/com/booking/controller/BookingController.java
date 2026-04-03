package com.booking.controller;

import com.booking.model.Booking;
import com.booking.service.BookingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

//    Get all bookings
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAll();
    }
//    Create booking
    @PostMapping
    public  Booking createBooking(@RequestBody Booking booking) {
        return bookingService.create(booking);
    }
}
