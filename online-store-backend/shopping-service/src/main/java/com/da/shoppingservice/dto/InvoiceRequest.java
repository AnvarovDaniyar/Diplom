package com.da.shoppingservice.dto;

import java.util.Set;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import com.da.shoppingservice.model.Customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InvoiceRequest {

  private Long invoiceId;

  @PositiveOrZero(message = "The Invoice Number must be positive!!")
  @Min(value = 100000L, message = "The Invoice Number must be greater than {value}!!")
  @NotNull(message = "The Invoice Number is Mandatory!!")
  private Long invoiceNumber;

  @NotNull(message = "The Invoice Must Have Products!!")
  private Set<ProductDto> products;

  @NotNull(message = "The Customer Is Required To Generate The Invoice!!")
  private Customer customer;
}
