package com.ra.orderservice.service;

import com.ra.orderservice.dto.request.CreateOrderRequest;
import com.ra.orderservice.dto.response.ProductResponseDTO;
import com.ra.orderservice.entity.Order;

public interface OrderService {
    String getHealthStatus();
    Order getOrderById(Long id);
    Order createOrder(CreateOrderRequest request);
    ProductResponseDTO getProductFromProductService(String productId);
    ProductResponseDTO getProductFromProductService2(String productId);
}