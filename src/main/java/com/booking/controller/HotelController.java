package com.booking.controller;

import com.booking.model.Hotel;
import com.booking.service.HotelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public List<Hotel> getAll() {
        return hotelService.getAll();
    }

    @PostMapping
    public Hotel create(@RequestBody Hotel hotel) {
        return hotelService.create(hotel);
    }
}
