package com.nimbleways.springboilerplate.services.handlers;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.INotificationService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@UnitTest
class SeasonalProductHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private SeasonalProductHandler handler;

    @Test
    void whenInSeasonAndInStock_thenDecrementsAndSaves() {
        LocalDate seasonStart = LocalDate.now().minusDays(5);
        LocalDate seasonEnd = LocalDate.now().plusDays(30);
        Product product = new Product(1L, 10, 20, ProductType.SEASONAL, "Watermelon", null, seasonStart, seasonEnd);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        assertEquals(19, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void whenInSeasonOutOfStockAndLeadTimeWithinSeason_thenNotifiesDelay() {
        LocalDate seasonStart = LocalDate.now().minusDays(5);
        LocalDate seasonEnd = LocalDate.now().plusDays(30);
        Product product = new Product(1L, 10, 0, ProductType.SEASONAL, "Watermelon", null, seasonStart, seasonEnd);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendDelayNotification(10, "Watermelon");
    }

    @Test
    void whenInSeasonOutOfStockAndLeadTimeExceedsSeason_thenNotifiesOutOfStock() {
        LocalDate seasonStart = LocalDate.now().minusDays(5);
        LocalDate seasonEnd = LocalDate.now().plusDays(5);
        Product product = new Product(1L, 10, 0, ProductType.SEASONAL, "Watermelon", null, seasonStart, seasonEnd);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService, times(1)).sendOutOfStockNotification("Watermelon");
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void whenSeasonNotStartedYet_thenNotifiesOutOfStock() {
        LocalDate seasonStart = LocalDate.now().plusDays(30);
        LocalDate seasonEnd = LocalDate.now().plusDays(90);
        Product product = new Product(1L, 5, 10, ProductType.SEASONAL, "Grapes", null, seasonStart, seasonEnd);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        verify(notificationService, times(1)).sendOutOfStockNotification("Grapes");
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void whenSeasonAlreadyEnded_thenNotifiesOutOfStockAndSetsAvailableToZero() {
        LocalDate seasonStart = LocalDate.now().minusDays(60);
        LocalDate seasonEnd = LocalDate.now().minusDays(5);
        Product product = new Product(1L, 10, 5, ProductType.SEASONAL, "Strawberry", null, seasonStart, seasonEnd);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService, times(1)).sendOutOfStockNotification("Strawberry");
        verify(productRepository, times(1)).save(product);
    }
}
