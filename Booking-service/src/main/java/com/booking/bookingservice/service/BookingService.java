package com.booking.bookingservice.service;

import com.booking.bookingservice.dto.BookingRequest;
import com.booking.bookingservice.dto.BookingResponse;
import com.booking.bookingservice.dto.HotelReference;
import com.booking.bookingservice.model.Booking;
import com.booking.bookingservice.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelValidationClient hotelValidationClient;

    public BookingService(
            BookingRepository bookingRepository,
            HotelValidationClient hotelValidationClient
    ) {
        this.bookingRepository = bookingRepository;
        this.hotelValidationClient = hotelValidationClient;
    }

    public List<BookingResponse> getAll() {
        return bookingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BookingResponse create(BookingRequest request) {
        validateRequest(request);

        Booking booking = new Booking();
        booking.setCustomerName(request.customerName());
        booking.setRoomType(request.roomType());
        booking.setDateFrom(request.dateFrom());
        booking.setDateTo(request.dateTo());
        booking.setHotelId(request.hotel().id());

        return toResponse(bookingRepository.save(booking));
    }

    private void validateRequest(BookingRequest request) {
        if (request.hotel() == null || request.hotel().id() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hotel id is required"
            );
        }

        if (request.dateFrom() == null || request.dateTo() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking dates are required"
            );
        }

        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid date range"
            );
        }

        if (!hotelValidationClient.hotelExists(request.hotel().id())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hotel not found"
            );
        }
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getCustomerName(),
                booking.getRoomType(),
                booking.getDateFrom(),
                booking.getDateTo(),
                new HotelReference(booking.getHotelId())
        );
    }
}
