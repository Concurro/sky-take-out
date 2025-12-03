package com.sky.controller.user;


import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.impl.ShoppingCartServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端-购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartServiceImpl shoppingCartService;


    @PostMapping("/add")
    @ApiOperation("添加购物车")
    @CacheEvict(value = "shoppingCart", key = "T(com.sky.context.BaseContext).getCurrentId()")
    public Result<Void> add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查看购物车")
    @Cacheable(value = "shoppingCart", key = "T(com.sky.context.BaseContext).getCurrentId()")
    public Result<List<ShoppingCart>> list() {
        return Result.success(shoppingCartService.list());
    }

    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    @CacheEvict(value = "shoppingCart", key = "T(com.sky.context.BaseContext).getCurrentId()")
    public Result<Void> clean() {
        shoppingCartService.clear();
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation("删除购物车")
    @CacheEvict(value = "shoppingCart", key = "T(com.sky.context.BaseContext).getCurrentId()")
    public Result<Void> sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
         shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }
}
