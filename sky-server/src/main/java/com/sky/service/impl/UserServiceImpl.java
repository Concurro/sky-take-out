package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {


    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    @Resource
    WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    @Override
    public User wxLogin(UserLoginDTO loginDTO) {
        String openid = getOpenid(loginDTO.getCode());

        User user = userMapper.getByOpenid(openid);
        if (user == null) {
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }
        return user;
    }

    private String getOpenid(String code) {
        String json = HttpClientUtil.doGet(WX_LOGIN_URL,
                Map.of("appid", weChatProperties.getAppid(),
                        "secret", weChatProperties.getSecret(),
                        "js_code", code,
                        "grant_type", "authorization_code"));

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        if (openid == null || openid.isEmpty()) throw new LoginFailedException("登录失败");
        return openid;
    }
}
