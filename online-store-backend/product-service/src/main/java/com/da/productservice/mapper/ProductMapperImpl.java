package com.da.productservice.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import com.da.productservice.dto.ProductRequest;
import com.da.productservice.dto.ProductResponse;
import com.da.productservice.dto.ProductView;
import com.da.productservice.entity.MainCategory;
import com.da.productservice.entity.Product;
import com.da.productservice.entity.SubCategory;

import org.springframework.stereotype.Component;

@Component
public class ProductMapperImpl implements ProductMapper {

  @Override
  public Product productRequestToProduct(ProductRequest productRequest, MainCategory mainCategory,
      Set<SubCategory> subCategories) {
    return Product.builder()
                  .productBarCode(productRequest.getProductBarCode())
                  .productName(productRequest.getProductName())
                  .productDescription(productRequest.getProductDescription())
                  .productStock(productRequest.getProductStock())
                  .productPrice(productRequest.getProductPrice())
                  .mainCategory(mainCategory).subCategories(subCategories)
                  .build();
  }

  @Override
  public ProductResponse productToProductResponse(Product product) {
    return ProductResponse.builder()
                          .productId(product.getProductId())
                          .productBarCode(product.getProductBarCode())
                          .productName(product.getProductName())
                          .productDescription(product.getProductDescription())
                          .productStock(product.getProductStock())
                          .productPrice(product.getProductPrice())
                          .createDate(product.getCreateDate())
                          .lastModifiedDate(product.getLastModifiedDate())
                          .productStatus(product.getProductStatus())
                          .mainCategoryName(product.getMainCategory().getMainCategoryName())
                          .subCategories(product.getSubCategories().stream().map(SubCategory::getSubCategoryName).collect(Collectors.toSet()))
                          .build();
  }

  @Override
  public ProductView productToProductView(Product product) {
    return ProductView.builder()
                      .productBarCode(product.getProductBarCode())
                      .productName(product.getProductName())
                      .productDescription(product.getProductDescription())
                      .productPrice(product.getProductPrice())
                      .mainCategoryName(product.getMainCategory().getMainCategoryName())
                      .subCategories(product.getSubCategories().stream().map(SubCategory::getSubCategoryName).collect(Collectors.toSet()))
                      .build();
  }
}
