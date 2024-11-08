package com.da.productservice.mapper;

import java.util.Set;

import com.da.productservice.dto.ProductRequest;
import com.da.productservice.dto.ProductResponse;
import com.da.productservice.dto.ProductView;
import com.da.productservice.entity.MainCategory;
import com.da.productservice.entity.Product;
import com.da.productservice.entity.SubCategory;

public interface ProductMapper {

  public Product productRequestToProduct(ProductRequest productRequest, MainCategory mainCategory,
      Set<SubCategory> subCategories);

  public ProductResponse productToProductResponse(Product product);

  public ProductView productToProductView(Product product);

}
