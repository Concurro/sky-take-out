package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    void add(ShoppingCartDTO cart);

    List<ShoppingCart> list();

    void clear();

    void sub(ShoppingCartDTO shoppingCartDTO);
}
