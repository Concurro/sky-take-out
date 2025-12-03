package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishMapper dishMapper;


    @Override
    public void add(ShoppingCartDTO cart) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(cart, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && !list.isEmpty()) {
            ShoppingCart cartInDB = list.get(0);
            cartInDB.setNumber(cartInDB.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cartInDB);
        } else {
            Long dishId = cart.getDishId();
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            if (dishId != null) {
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setDishId(dishId);
            } else {
                Long setmealId = cart.getSetmealId();
                log.info("setmealId:{}", setmealId);
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setSetmealId(setmealId);
                shoppingCart.setDishFlavor(cart.getDishFlavor());
            }
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> list() {
        return shoppingCartMapper.list(ShoppingCart.builder().userId(BaseContext.getCurrentId()).build());
    }

    @Override
    public void clear() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(BaseContext.getCurrentId()).dishId(shoppingCartDTO.getDishId()).setmealId(shoppingCartDTO.getSetmealId()).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && !list.isEmpty()) {
            ShoppingCart cartInDB = list.get(0);
            cartInDB.setNumber(cartInDB.getNumber() - 1);
            if (cartInDB.getNumber() == 0) {
                shoppingCartMapper.deleteById(cartInDB.getId());
            } else {
                shoppingCartMapper.updateNumberById(cartInDB);
            }
        }
    }
}