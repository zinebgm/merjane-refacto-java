package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessingService {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    public void processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).get();

        for (Product product : order.getItems()) {
            processProduct(product);
        }
    }

    private void processProduct(Product product) {
        if (product.getType().equals("NORMAL")) {
            processNormalProduct(product);
        } else if (product.getType().equals("SEASONAL")) {
            processSeasonalProduct(product);
        } else if (product.getType().equals("EXPIRABLE")) {
            processExpirableProduct(product);
        }
    }

    private void processNormalProduct(Product product) {
        if (product.getAvailable() > 0) {
            decrementAvailableAndSave(product);
        } else {
            int leadTime = product.getLeadTime();
            if (leadTime > 0) {
                productService.notifyDelay(leadTime, product);
            }
        }
    }

    private void processSeasonalProduct(Product product) {
        LocalDate today = LocalDate.now();

        if (today.isAfter(product.getSeasonStartDate())
                && today.isBefore(product.getSeasonEndDate())
                && product.getAvailable() > 0) {
            decrementAvailableAndSave(product);
        } else {
            productService.handleSeasonalProduct(product);
        }
    }

    private void processExpirableProduct(Product product) {
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            decrementAvailableAndSave(product);
        } else {
            productService.handleExpiredProduct(product);
        }
    }

    private void decrementAvailableAndSave(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }
}