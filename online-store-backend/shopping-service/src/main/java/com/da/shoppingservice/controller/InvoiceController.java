package com.da.shoppingservice.controller;

import javax.validation.Valid;

import com.da.shoppingservice.dto.InvoiceRequest;
import com.da.shoppingservice.entity.Invoice;
import com.da.shoppingservice.service.InvoiceService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/invoices")
public class InvoiceController {

  private static final String JSON_VALUE = MediaType.APPLICATION_JSON_VALUE;
  private static final MediaType JSON = MediaType.APPLICATION_JSON;

  private final InvoiceService invoiceService;

  @PostMapping(consumes = JSON_VALUE)
  public ResponseEntity<Invoice> create(@Valid @RequestBody InvoiceRequest invoiceRequest){
    return ResponseEntity.status(HttpStatus.CREATED).contentType(JSON).body(invoiceService.create(invoiceRequest));
  }

  @DeleteMapping("/{invoiceId}")
  public ResponseEntity<Void> delete(@PathVariable Long invoiceId){
    invoiceService.deleteById(invoiceId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{invoiceNumber}")
  public ResponseEntity<Invoice> getByInvoiceNumber(@PathVariable Long invoiceNumber){
    return getBodyBuilder().body(invoiceService.getByInvoiceNumber(invoiceNumber));
  }

  @GetMapping("/users")
  public ResponseEntity<Page<Invoice>> getByUserName(@RequestParam(required = true) String userName, Pageable pageable){
    return getBodyBuilder().body(invoiceService.getByUserName(userName, pageable));
  }

  @GetMapping("/products/{productBarCode}")
  public ResponseEntity<Page<Invoice>> getByProductBarCode(@PathVariable Long productBarCode, Pageable pageable){
    return getBodyBuilder().body(invoiceService.getByProductBarCode(productBarCode, pageable));
  }

  private BodyBuilder getBodyBuilder(){
    return ResponseEntity.ok().contentType(JSON);
  }
}
