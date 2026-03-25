package com.project.cbsbackend.service;

import com.project.cbsbackend.dto.booking.CreateBookingRequest;
import com.project.cbsbackend.dto.booking.CreateBookingResponse;
import com.project.cbsbackend.dto.booking.UserBookingResponse;

import java.util.List;

public interface BookingService {
    CreateBookingResponse createBooking(Long requestingUserId, CreateBookingRequest request);
    void unenrollBooking(Long requestingUserId, Long sessionId, Long targetParticipantId);
    List<UserBookingResponse> getUserBookings(Long requestingUserId, Long targetParticipantId);
}