package com.da.productservice.mapper;

import com.da.productservice.dto.SubCategoryResponse;
import com.da.productservice.entity.MainCategory;
import com.da.productservice.entity.SubCategory;

public interface SubCategoryMapper {

  public SubCategoryResponse subCategoryToSubCategoryResponse(SubCategory subCategory);

  public SubCategory subCategoryRequestToSubCategory(String subCategoryName, MainCategory mainCategory);
}
