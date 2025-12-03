package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.BaseException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderDetailMapper orderDetailMapper;

    @Resource
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 获取地址
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new BaseException("地址有误");
        }
        // 获取购物车
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(ShoppingCart.builder().userId(BaseContext.getCurrentId()).build());
        if (shoppingCarts == null || shoppingCarts.isEmpty()) {
            throw new BaseException("购物车为空");
        }

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(java.time.LocalDateTime.now()); // Changed from java.util.Date to java.time.LocalDateTime
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setNumber(UUID.randomUUID().toString());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());

        orderMapper.insert(orders);
        Long ordersId = orders.getId();
        List<OrderDetail> orderDetailList = new ArrayList<>();
        // 安全的金额类
        BigDecimal amount;
        shoppingCarts.forEach(sc -> {
            orderDetailList.add(OrderDetail.builder().orderId(ordersId).dishId(sc.getDishId()).amount(sc.getAmount()).image(sc.getImage()).dishFlavor(sc.getDishFlavor()).setmealId(sc.getSetmealId()).number(sc.getNumber()).build());
        });

        amount = shoppingCarts.stream().map(sc -> sc.getAmount().multiply(new BigDecimal(sc.getNumber())).add(new BigDecimal(sc.getNumber()))).reduce(BigDecimal.ZERO, BigDecimal::add).add(new BigDecimal(6));

        orderDetailMapper.insertBatch(orderDetailList);
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());

        return OrderSubmitVO.builder().id(ordersId).orderNumber(orders.getNumber()).orderAmount(amount).orderTime(orders.getOrderTime()).build();
    }
}
