package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.handlers.ProductHandlerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductHandlerFactory handlerFactory;

    public OrderService(OrderRepository orderRepository, ProductHandlerFactory handlerFactory) {
        this.orderRepository = orderRepository;
        this.handlerFactory = handlerFactory;
    }

    @Transactional
    public ProcessOrderResponse processOrder(Long orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        for (Product product : order.getItems()) {
            handlerFactory.getHandler(product.getType()).handle(product);
        }

        return new ProcessOrderResponse(order.getId());
    }
}
