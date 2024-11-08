package com.da.shoppingservice.service;

import com.da.shoppingservice.dto.InvoiceRequest;
import com.da.shoppingservice.entity.Invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {

  public Invoice create(InvoiceRequest invoiceRequest);

  public void deleteById(Long invoiceId);

  public Page<Invoice> getByUserName(String userName, Pageable pageable);

  public Invoice getByInvoiceNumber(Long invoiceNumber);

  public Page<Invoice> getByProductBarCode(Long productBarCode, Pageable pageable);
}
