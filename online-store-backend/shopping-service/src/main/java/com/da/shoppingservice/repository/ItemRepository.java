package com.da.shoppingservice.repository;

import com.da.shoppingservice.entity.Item;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long>{
}
