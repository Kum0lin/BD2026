package com.booking.service;

import com.booking.model.Booking;
import com.booking.model.Hotel;
import com.booking.repository.BookingRepository;
import com.booking.repository.HotelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;

    public BookingService(BookingRepository bookingRepository
            , HotelRepository hotelRepository) {

        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
    }

    public List<Booking> getAll() {
        return bookingRepository.findAll();
    }

    public Booking create(Booking booking) {

        //Check hotelID in DB
        Long hotelId = booking.getHotel().getId();

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Hotel not found"
                ));

        //Validate date range
        if (booking.getDateFrom().isAfter(booking.getDateTo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid date range"
            );
        }

        booking.setHotel(hotel);
        return bookingRepository.save(booking);
    }
}
