package com.ra.orderservice.service.impl;

import com.ra.orderservice.dto.request.CreateOrderRequest;
import com.ra.orderservice.dto.response.ProductResponseDTO;
import com.ra.orderservice.entity.Order;
import com.ra.orderservice.exception.ResourceNotFoundException;
import com.ra.orderservice.repository.OrderRepository;
import com.ra.orderservice.service.OrderService;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DiscoveryClient discoveryClient;
    private final RestClient restClient;

    public OrderServiceImpl(OrderRepository orderRepository,
                            DiscoveryClient discoveryClient,
                            RestClient restClient) {
        this.orderRepository = orderRepository;
        this.discoveryClient = discoveryClient;
        this.restClient = restClient;
    }

    @Override
    public String getHealthStatus() {
        return "Order Service is Up";
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order với ID " + id + " không tồn tại trên hệ thống!"));
    }

    @Override
    public Order createOrder(CreateOrderRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "quantity phải lớn hơn 0"
            );
        }

        ProductResponseDTO product = getProductFromProductService(
                String.valueOf(request.getProductId())
        );

        if (product == null || product.getPrice() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Không lấy được thông tin sản phẩm hoặc giá sản phẩm"
            );
        }

        Double totalAmount = product.getPrice() * request.getQuantity();

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .build();

        try {
            return orderRepository.save(order);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lưu đơn hàng thất bại: " + e.getMessage()
            );
        }
    }

    @Override
    public ProductResponseDTO getProductFromProductService(String productId) {
        List<ServiceInstance> instances = discoveryClient.getInstances("PRODUCT-SERVICE");

        if (instances == null || instances.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "PRODUCT-SERVICE hiện không khả dụng (Không tìm thấy instance nào)"
            );
        }

        ServiceInstance productInstance = instances.get(0);
        String baseUrl = productInstance.getUri().toString();
        String targetUrl = baseUrl + "/api/v1/products/" + productId;

        return restClient.get()
                .uri(targetUrl)
                .retrieve()
                .body(ProductResponseDTO.class);
    }

    @Override
    public ProductResponseDTO getProductFromProductService2(String productId) {
        return getProductFromProductService(productId);
    }
}