package com.booking.service;

import com.booking.model.Hotel;
import com.booking.repository.HotelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {

    private final HotelRepository repository;

    public HotelService(HotelRepository repository) {
        this.repository = repository;
    }

    public List<Hotel> getAll() {
        return repository.findAll();
    }

    public Hotel create(Hotel hotel) {
        return repository.save(hotel);
    }
}
