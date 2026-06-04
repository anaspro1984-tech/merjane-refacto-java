package com.nimbleways.springboilerplate.services;

import java.time.LocalDate;

public interface INotificationService {
    void sendDelayNotification(int leadTime, String productName);
    void sendOutOfStockNotification(String productName);
    void sendExpirationNotification(String productName, LocalDate expiryDate);
}
