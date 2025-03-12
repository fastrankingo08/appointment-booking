package com.fastranking.appointment_booking.exception;

public class ResourceNotFoundException extends RuntimeException  {
    public ResourceNotFoundException(String message){
        super(message);
    }
}
