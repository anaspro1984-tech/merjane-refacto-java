package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.handlers.ExpirableProductHandler;
import com.nimbleways.springboilerplate.services.handlers.NormalProductHandler;
import com.nimbleways.springboilerplate.services.handlers.ProductHandlerFactory;
import com.nimbleways.springboilerplate.services.handlers.SeasonalProductHandler;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@UnitTest
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductHandlerFactory handlerFactory;

    @Mock
    private NormalProductHandler normalProductHandler;

    @Mock
    private SeasonalProductHandler seasonalProductHandler;

    @Mock
    private ExpirableProductHandler expirableProductHandler;

    @InjectMocks
    private OrderService orderService;

    @Test
    void whenOrderNotFound_thenThrowsOrderNotFoundException() {
        Mockito.when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.processOrder(99L));
    }

    @Test
    void whenOrderHasNoItems_thenReturnsOkWithoutCallingHandlers() {
        Order order = new Order(1L, Set.of());
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var response = orderService.processOrder(1L);

        assertEquals(1L, response.id());
        verifyNoInteractions(handlerFactory);
    }

    @Test
    void whenOrderHasNormalProduct_thenDelegatestoNormalHandler() {
        Product product = new Product(1L, 15, 10, ProductType.NORMAL, "USB Cable", null, null, null);
        Order order = new Order(1L, Set.of(product));
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.when(handlerFactory.getHandler(ProductType.NORMAL)).thenReturn(normalProductHandler);

        orderService.processOrder(1L);

        verify(handlerFactory, times(1)).getHandler(ProductType.NORMAL);
        verify(normalProductHandler, times(1)).handle(product);
    }

    @Test
    void whenOrderHasMultipleProductTypes_thenEachProductIsHandledIndependently() {
        Product normalProduct = new Product(1L, 15, 10, ProductType.NORMAL, "USB Cable", null, null, null);
        Product expirableProduct = new Product(2L, 10, 5, ProductType.EXPIRABLE, "Butter", LocalDate.now().plusDays(5), null, null);
        Product seasonalProduct = new Product(3L, 5, 8, ProductType.SEASONAL, "Watermelon", null,
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(30));
        Order order = new Order(1L, Set.of(normalProduct, expirableProduct, seasonalProduct));
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.when(handlerFactory.getHandler(ProductType.NORMAL)).thenReturn(normalProductHandler);
        Mockito.when(handlerFactory.getHandler(ProductType.EXPIRABLE)).thenReturn(expirableProductHandler);
        Mockito.when(handlerFactory.getHandler(ProductType.SEASONAL)).thenReturn(seasonalProductHandler);

        var response = orderService.processOrder(1L);

        assertEquals(1L, response.id());
        verify(normalProductHandler, times(1)).handle(normalProduct);
        verify(expirableProductHandler, times(1)).handle(expirableProduct);
        verify(seasonalProductHandler, times(1)).handle(seasonalProduct);
    }
}
