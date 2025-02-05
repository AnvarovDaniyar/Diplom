package com.da.shoppingservice.client;

import com.da.shoppingservice.config.FeignConfig;
import com.da.shoppingservice.model.Product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;



@FeignClient(name = "product-service", configuration = FeignConfig.class, fallback = ProductFallback.class)
public interface ProductClient {
  public final String INVOICES = "invoices";
  public final String STOCK = "stock";


  @CircuitBreaker(name = INVOICES)
  @GetMapping("/products/invoices")
  public ResponseEntity<Product> getInfo(@RequestParam(required = true) Long productBarCode,
                                                       @RequestParam(required = true) String productName);

  @CircuitBreaker(name = STOCK)
  @PutMapping("/products/{productBarCode}/stock")
  public ResponseEntity<Void> updateStock(@PathVariable Long productBarCode, @RequestParam(required = true) Integer quantity);
}
