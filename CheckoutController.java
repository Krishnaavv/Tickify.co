package com.ticketbooking.booking;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final BookingService bookingService;

    public CheckoutController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponse checkout(@RequestBody ReservationRequest request) {
        return bookingService.reserve(request);
    }
}
