package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
public class ShopController {
    public static final String SHOP_STATUS_KEY = "SHOP_STATUS";
    @Resource
    private RedisTemplate<String, Integer> redisTemplate;

    /**
     * 设置店铺状态
     * @param status 店铺状态，1为营业，0为关闭
     * @return 店铺状态设置结果
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺状态")
    public Result<Void> setStatus(@PathVariable Integer status) {
        redisTemplate.opsForValue().set(SHOP_STATUS_KEY, status);
        return Result.success();
    }

    /**
     * 获取店铺状态
     * @return 店铺状态
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺状态")
    public Result<Integer> getStatus() {
        Integer status = redisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        return Result.success(status);
    }
}
