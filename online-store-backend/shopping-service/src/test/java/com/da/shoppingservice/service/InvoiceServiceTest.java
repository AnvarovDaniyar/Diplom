package com.da.shoppingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.List;
import java.util.Optional;

import com.da.shoppingservice.util.Provider;
import com.da.shoppingservice.client.ProductClient;
import com.da.shoppingservice.dto.InvoiceRequest;
import com.da.shoppingservice.entity.Invoice;
import com.da.shoppingservice.exception.ResourceNotFoundException;
import com.da.shoppingservice.repository.InvoiceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class InvoiceServiceTest {
  @Mock
  private InvoiceRepository invoiceRepository;
  @Mock
  private ProductClient productClient;

  private InvoiceService invoiceService;

  private static Invoice staticInvoiceWithItems = Provider.createInvoiceRandomValuesItems();
  private static InvoiceRequest staticInvoiceRequest = Provider.createInvoiceRequestRandomValues();
  private static PageImpl<Invoice> staticInvoicePage = new PageImpl<>(List.of(staticInvoiceWithItems));

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    invoiceService = new InvoiceServiceImpl(invoiceRepository, productClient);
  }

  @Test
  public void create_ReturnInvoice_WhenSuccessful() {
    BDDMockito.when(invoiceRepository.existsByInvoiceNumber(anyLong())).thenReturn(false);
    BDDMockito.when(productClient.getInfo(anyLong(), anyString()))
        .thenReturn(ResponseEntity.ok(Provider.createProductRandomPrice()));
    BDDMockito.when(productClient.updateStock(anyLong(), anyInt()))
        .thenReturn(ResponseEntity.noContent().build());
    BDDMockito.when(invoiceRepository.save(any())).thenReturn(staticInvoiceWithItems);

    var invoice = invoiceService.create(staticInvoiceRequest);

    assertThat(invoice).isNotNull();
    assertThat(invoice.getCustomer()).isNotNull();
    assertFalse(invoice.getItems().isEmpty());
  }

  @Test
  public void create_ThrowDataIntegrityViolationException_WhenInvoiceAlreadyExistsInTheLog() {
    BDDMockito.when(invoiceRepository.existsByInvoiceNumber(anyLong())).thenReturn(true);

    assertThatExceptionOfType(DataIntegrityViolationException.class)
        .isThrownBy(() -> invoiceService.create(staticInvoiceRequest));
  }

  @Test
  public void create_NoExceptionIsTrowed_WhenProductsServiceIsNotAvailable() {
    BDDMockito.when(invoiceRepository.existsByInvoiceNumber(anyLong())).thenReturn(false);
    BDDMockito.when(productClient.getInfo(anyLong(), anyString()))
        .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Provider.createProductRandomPrice()));
    BDDMockito.when(invoiceRepository.save(any())).thenReturn(staticInvoiceWithItems);

    assertThatCode(() -> invoiceService.create(Provider.createInvoiceRequestRandomValues())).doesNotThrowAnyException();
  }

  @Test
  public void delete_NoExceptionIsThrowed_WhenTheInvoiceIsRemoved() {
    BDDMockito.when(invoiceRepository.findById(anyLong())).thenReturn(Optional.of(staticInvoiceWithItems));
    BDDMockito.doNothing().when(invoiceRepository).delete(any());

    assertThatCode(() -> invoiceService.deleteById(Provider.getRandomLongNumber())).doesNotThrowAnyException();
  }

  @Test
  public void delete_ThrowResourceNotFoundException_WhenTheInvoiceIsNotFound() {
    BDDMockito.when(invoiceRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> invoiceService.deleteById(Provider.getRandomLongNumber()));
  }

  @Test
  public void getByUserName_ReturnInvoicePage_WhenSuccessful() {
    BDDMockito.when(invoiceRepository.findByCustomerUserName(anyString(), any(Pageable.class)))
        .thenReturn(staticInvoicePage);

    var invoices = invoiceService.getByUserName(Provider.TEST, Provider.PAGE);

    assertFalse(invoices.isEmpty());
    assertThat(invoices.getContent().contains(staticInvoiceWithItems));
  }

  @Test
  public void getByUserName_ThrowResourceNotFoundException_WhenInvoicePageIsEmpty() {
    BDDMockito.when(invoiceRepository.findByCustomerUserName(anyString(), any(Pageable.class)))
        .thenReturn(Page.empty());

    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> invoiceService.getByUserName(Provider.TEST, Provider.PAGE));
  }

  @Test
  public void getByInvoiceNumber_ReturnInvoice_WhenSuccessful() {
    BDDMockito.when(invoiceRepository.findByInvoiceNumber(anyLong())).thenReturn(Optional.of(staticInvoiceWithItems));

    var invoice = invoiceService.getByInvoiceNumber(Provider.getRandomLongNumber());

    assertThat(invoice).isNotNull();
    assertThat(invoice.getCustomer()).isNotNull();
    assertFalse(invoice.getItems().isEmpty());
  }

  @Test
  public void getByInvoiceNumber_ThrowResourceNotFoundException_WhenInvoiceIsNotFound() {
    BDDMockito.when(invoiceRepository.findByInvoiceNumber(anyLong())).thenReturn(Optional.empty());

    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> invoiceService.getByInvoiceNumber(Provider.getRandomLongNumber()));
  }

  @Test
  public void getByProductBarCode_ReturnInvoicePage_WhenSuccessful() {
    BDDMockito.when(invoiceRepository.findByItemsProductBarCode(anyLong(), any(Pageable.class)))
        .thenReturn(staticInvoicePage);

    var invoices = invoiceService.getByProductBarCode(Provider.getRandomLongNumber(), Provider.PAGE);

    assertFalse(invoices.isEmpty());
    assertThat(invoices.getContent().contains(staticInvoiceWithItems));
  }

  @Test
  public void getByProductBarCode_ThrowResourceNotFoundException_WhenInvoicePageIsEmpty() {
    BDDMockito.when(invoiceRepository.findByItemsProductBarCode(anyLong(), any(Pageable.class)))
        .thenReturn(Page.empty());

    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> invoiceService.getByProductBarCode(Provider.getRandomLongNumber(), Provider.PAGE));
  }
}
