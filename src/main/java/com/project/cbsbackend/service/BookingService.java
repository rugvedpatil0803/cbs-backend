package com.project.cbsbackend.service;

import com.project.cbsbackend.dto.CreateBookingRequest;
import com.project.cbsbackend.dto.CreateBookingResponse;
import com.project.cbsbackend.dto.UserBookingResponse;

import java.util.List;

public interface BookingService {
    CreateBookingResponse createBooking(Long requestingUserId, CreateBookingRequest request);
    void unenrollBooking(Long requestingUserId, Long sessionId, Long targetParticipantId);
    List<UserBookingResponse> getUserBookings(Long requestingUserId, Long targetParticipantId);
}