package com.da.shoppingservice.repository;

import java.util.Optional;

import com.da.shoppingservice.entity.Invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

  public Page<Invoice> findByCustomerUserName(String userName, Pageable pageable);

  public Optional<Invoice> findByInvoiceNumber(Long invoiceNumber);

  public Page<Invoice> findByItemsProductBarCode(Long productBarCode, Pageable pageable);

  public boolean existsByInvoiceNumber(Long invoiceNumber);
}
