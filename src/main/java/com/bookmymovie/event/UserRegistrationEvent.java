package com.bookmymovie.event;

import org.springframework.context.ApplicationEvent;

public class UserRegistrationEvent extends ApplicationEvent {

    private final Long userId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final String ipAddress;

    public UserRegistrationEvent(Object source, Long userId, String email,
                                 String firstName, String lastName, String phoneNumber, String ipAddress) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.ipAddress = ipAddress;
    }

    // Getters
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getIpAddress() { return ipAddress; }

    @Override
    public String toString() {
        return "UserRegistrationEvent{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}