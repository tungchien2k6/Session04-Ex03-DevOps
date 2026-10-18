package com.ra.orderservice.controller;

import com.ra.orderservice.dto.request.CreateOrderRequest;
import com.ra.orderservice.dto.response.ProductResponseDTO;
import com.ra.orderservice.entity.Order;
import com.ra.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok(orderService.getHealthStatus());
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/getProduct/{id}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getProductFromProductService(id));
    }

    @GetMapping("/getProductLoadBalanced/{id}")
    public ResponseEntity<ProductResponseDTO> getProductLoadBalanced(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getProductFromProductService2(id));
    }
}