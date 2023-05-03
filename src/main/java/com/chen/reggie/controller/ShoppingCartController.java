package com.chen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.reggie.common.BaseContext;
import com.chen.reggie.common.R;
import com.chen.reggie.entity.ShoppingCart;
import com.chen.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
  
  @Autowired
  private ShoppingCartService shoppingCartService;
  
  /**
   * 查看购物车
   *
   * @return
   */
  @GetMapping("/list")
  public R<List<ShoppingCart>> list() {
    log.info("查看购物车...");
    
    LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
    queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
    
    List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
    
    return R.success(list);
  }
  
  /**
   * 减少购物车
   *
   * @return
   */
  @PostMapping("/sub")
  public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
    Long currentId = BaseContext.getCurrentId();
    shoppingCart.setUserId(currentId);
    
    // 获取当前用户、菜品|套餐的数据
    Long dishId = shoppingCart.getDishId();
    LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ShoppingCart::getUserId, currentId);
    
    if (dishId != null) {
      //添加到购物车的是菜品
      queryWrapper.eq(ShoppingCart::getDishId, dishId);
      
    } else {
      //添加到购物车的是套餐
      queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
    }
    
    // 检查是否已存在，已存在就+1，不存在就新增
    ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
    Integer num = cartServiceOne.getNumber();
    if (num > 0) {
      cartServiceOne.setNumber(num - 1);
      shoppingCartService.updateById(cartServiceOne);
    }
    
    return R.success(cartServiceOne);
  }
  
  /**
   * 添加购物车
   *
   * @return
   */
  @PostMapping("/add")
  public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
    Long currentId = BaseContext.getCurrentId();
    shoppingCart.setUserId(currentId);
    
    // 获取当前用户、菜品|套餐的数据
    Long dishId = shoppingCart.getDishId();
    LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ShoppingCart::getUserId, currentId);
    
    if (dishId != null) {
      //添加到购物车的是菜品
      queryWrapper.eq(ShoppingCart::getDishId, dishId);
      
    } else {
      //添加到购物车的是套餐
      queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
    }
    
    // 检查是否已存在，已存在就+1，不存在就新增
    ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
    
    if (cartServiceOne != null) {
      Integer num = cartServiceOne.getNumber();
      cartServiceOne.setNumber(num + 1);
      shoppingCartService.updateById(cartServiceOne);
    } else {
      shoppingCart.setNumber(1);
      shoppingCart.setCreateTime(LocalDateTime.now());
      shoppingCartService.save(shoppingCart);
      cartServiceOne = shoppingCart;
    }
    
    return R.success(cartServiceOne);
  }
  
  /**
   * 清空购物车
   *
   * @return
   */
  @DeleteMapping("/clean")
  public R<String> clean() {
    //SQL:delete from shopping_cart where user_id = ?
    
    LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
    
    shoppingCartService.remove(queryWrapper);
    
    return R.success("清空购物车成功");
  }
}