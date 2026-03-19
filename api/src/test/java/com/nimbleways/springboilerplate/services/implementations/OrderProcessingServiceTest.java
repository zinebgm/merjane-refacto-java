package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@UnitTest
public class OrderProcessingServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderProcessingService orderProcessingService;

    @Test
    public void testDecrementAvailableAndSaveNormalProductWhenAvailable() {
        // GIVEN
       // Product product = new Product(null, 15, 3, "NORMAL", "Lait", null, null, null);
       Product product = new Product();
        product.setLeadTime(15);
        product.setAvailable(3);
        product.setType("NORMAL");
        product.setName("Lait");
        Order order = new Order();
        order.setId(1L);
        order.setItems(Set.of(product));

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        orderProcessingService.processOrder(1L);

        // THEN
        assertEquals(2, product.getAvailable());
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(productService, Mockito.never()).notifyDelay(Mockito.anyInt(), Mockito.any(Product.class));
    }

    @Test
    public void testNotifyDelayForNormalProductWhenOutOfStockAndLeadTimePositive() {
        // GIVEN
        //Product product = new Product(null, 5, 0, "NORMAL", "Fromage", null, null, null);
        Product product = new Product();
        product.setLeadTime(5);
        product.setAvailable(0);
        product.setType("NORMAL");
        product.setName("fromage");
        Order order = new Order();
        order.setId(1L);
        order.setItems(Set.of(product));

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // WHEN
        orderProcessingService.processOrder(1L);

        // THEN
        assertEquals(0, product.getAvailable());
        Mockito.verify(productService, Mockito.times(1)).notifyDelay(5, product);
        Mockito.verify(productRepository, Mockito.never()).save(product);
    }
}