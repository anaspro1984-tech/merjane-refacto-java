package com.nimbleways.springboilerplate.services.handlers;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.INotificationService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SeasonalProductHandler implements ProductHandler {

    private final ProductRepository productRepository;
    private final INotificationService notificationService;

    public SeasonalProductHandler(ProductRepository productRepository, INotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void handle(Product product) {
        LocalDate now = LocalDate.now();
        boolean inSeason = now.isAfter(product.getSeasonStartDate()) && now.isBefore(product.getSeasonEndDate());

        if (inSeason && product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
        } else {
            handleOutOfSeasonOrStock(product, now);
        }
    }

    private void handleOutOfSeasonOrStock(Product product, LocalDate now) {
        if (now.plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(product.getName());
            product.setAvailable(0);
            productRepository.save(product);
        } else if (product.getSeasonStartDate().isAfter(now)) {
            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);
        } else {
            productRepository.save(product);
            notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
        }
    }
}
