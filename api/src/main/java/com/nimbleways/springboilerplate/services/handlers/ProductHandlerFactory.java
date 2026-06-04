package com.nimbleways.springboilerplate.services.handlers;

import com.nimbleways.springboilerplate.entities.ProductType;
import org.springframework.stereotype.Component;

@Component
public class ProductHandlerFactory {

    private final NormalProductHandler normalProductHandler;
    private final SeasonalProductHandler seasonalProductHandler;
    private final ExpirableProductHandler expirableProductHandler;

    public ProductHandlerFactory(
            NormalProductHandler normalProductHandler,
            SeasonalProductHandler seasonalProductHandler,
            ExpirableProductHandler expirableProductHandler) {
        this.normalProductHandler = normalProductHandler;
        this.seasonalProductHandler = seasonalProductHandler;
        this.expirableProductHandler = expirableProductHandler;
    }

    public ProductHandler getHandler(ProductType type) {
        switch (type) {
            case NORMAL: return normalProductHandler;
            case SEASONAL: return seasonalProductHandler;
            case EXPIRABLE: return expirableProductHandler;
            default: throw new IllegalArgumentException("Unknown product type: " + type);
        }
    }
}
