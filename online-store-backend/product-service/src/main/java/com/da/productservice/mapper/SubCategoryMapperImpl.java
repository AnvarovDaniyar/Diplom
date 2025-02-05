package com.da.productservice.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.da.productservice.dto.SubCategoryResponse;
import com.da.productservice.entity.MainCategory;
import com.da.productservice.entity.Product;
import com.da.productservice.entity.SubCategory;

@Component
public class SubCategoryMapperImpl implements SubCategoryMapper {

  @Override
  public SubCategory subCategoryRequestToSubCategory(String subCategoryName, MainCategory mainCategory) {
    return SubCategory.builder()
                      .subCategoryName(subCategoryName)
                      .mainCategory(mainCategory)
                      .build();
  }

  @Override
  public SubCategoryResponse subCategoryToSubCategoryResponse(SubCategory subCategory) {
    return subCategory.getProducts() == null || subCategory.getProducts().isEmpty()
    //@formatter:off
                ? SubCategoryResponse.builder()
                                          .subCategoryId(subCategory.getSubCategoryId())
                                          .subCategoryName(subCategory.getSubCategoryName())
                                          .mainCategory(subCategory.getMainCategory().getMainCategoryName())
                                          .products(Set.of("This subcategory has no products"))
                                          .build()

                : SubCategoryResponse.builder()
                                          .subCategoryId(subCategory.getSubCategoryId())
                                          .subCategoryName(subCategory.getSubCategoryName())
                                          .mainCategory(subCategory.getMainCategory().getMainCategoryName())
                                          .products(subCategory.getProducts().stream().map(Product::getProductName).collect(Collectors.toSet()))
                                          .build();
    //@formatter:on
  }
}
