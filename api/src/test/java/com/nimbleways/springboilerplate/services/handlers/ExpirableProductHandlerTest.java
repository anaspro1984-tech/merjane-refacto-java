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
class ExpirableProductHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private ExpirableProductHandler handler;

    @Test
    void whenInStockAndNotExpired_thenDecrementsAndSaves() {
        LocalDate expiryDate = LocalDate.now().plusDays(10);
        Product product = new Product(1L, 15, 30, ProductType.EXPIRABLE, "Butter", expiryDate, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        assertEquals(29, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void whenExpiresTomorrow_thenDecrementsNormally() {
        LocalDate expiryDate = LocalDate.now().plusDays(1);
        Product product = new Product(1L, 15, 5, ProductType.EXPIRABLE, "Milk", expiryDate, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        assertEquals(4, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void whenInStockButExpired_thenNotifiesExpirationAndSetsAvailableToZero() {
        LocalDate expiryDate = LocalDate.now().minusDays(2);
        Product product = new Product(1L, 90, 6, ProductType.EXPIRABLE, "Milk", expiryDate, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService, times(1)).sendExpirationNotification("Milk", expiryDate);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void whenOutOfStockAndNotExpired_thenNotifiesExpiration() {
        // Current behavior: sends expiration notification even when just out of stock but not expired.
        // This may be a business logic bug worth clarifying with the team.
        LocalDate expiryDate = LocalDate.now().plusDays(5);
        Product product = new Product(1L, 10, 0, ProductType.EXPIRABLE, "Cheese", expiryDate, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService, times(1)).sendExpirationNotification("Cheese", expiryDate);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void whenOutOfStockAndExpired_thenNotifiesExpiration() {
        LocalDate expiryDate = LocalDate.now().minusDays(3);
        Product product = new Product(1L, 10, 0, ProductType.EXPIRABLE, "Yogurt", expiryDate, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService, times(1)).sendExpirationNotification("Yogurt", expiryDate);
        verify(productRepository, times(1)).save(product);
    }
}
