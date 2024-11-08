package com.da.productservice.service;

import java.util.Set;

import com.da.productservice.entity.MainCategory;

import org.springframework.data.domain.Pageable;

public interface MainCategoryService {

  public MainCategory create(MainCategory mainCategory);

  public MainCategory getById(Long mainCategoryId);

  public MainCategory getByName(String mainCategoryName);

  public Set<String> getAll(Pageable pageable);

  public void updateName(Long mainCategoryId, String mainCategoryName);

  public void deleteById(Long mainCategoryId);
}
