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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@UnitTest
class NormalProductHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private NormalProductHandler handler;

    @Test
    void whenAvailableGreaterThanZero_thenDecrementsStockAndSaves() {
        Product product = new Product(1L, 15, 10, ProductType.NORMAL, "USB Cable", null, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        assertEquals(9, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void whenLastUnitInStock_thenDecrementsToZeroAndSaves() {
        Product product = new Product(1L, 15, 1, ProductType.NORMAL, "USB Cable", null, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void whenAvailableIsZeroAndLeadTimeGreaterThanZero_thenNotifiesDelay() {
        Product product = new Product(1L, 15, 0, ProductType.NORMAL, "RJ45 Cable", null, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        handler.handle(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendDelayNotification(15, "RJ45 Cable");
    }

    @Test
    void whenAvailableIsZeroAndLeadTimeIsZero_thenNoActionTaken() {
        Product product = new Product(1L, 0, 0, ProductType.NORMAL, "USB Dongle", null, null, null);

        handler.handle(product);

        assertEquals(0, product.getAvailable());
        verifyNoInteractions(productRepository);
        verifyNoInteractions(notificationService);
    }
}
