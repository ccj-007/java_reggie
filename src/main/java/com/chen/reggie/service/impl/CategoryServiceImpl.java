package com.chen.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.reggie.common.CustomException;
import com.chen.reggie.entity.Category;
import com.chen.reggie.entity.Dish;
import com.chen.reggie.entity.Setmeal;
import com.chen.reggie.mapper.CategoryMapper;
import com.chen.reggie.service.CategoryService;
import com.chen.reggie.service.DishService;
import com.chen.reggie.service.SetmealService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
  @Resource
  private DishService dishService;
  
  @Resource
  private SetmealService setmealService;
  
  /**
   * 根据id删除分类，删除之前需要判断是否关联分类
   *
   * @param id
   */
  @Override
  public void remove(Long id) {
    // 查询条件构造器
    LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
    dishQueryWrapper.eq(Dish::getCategoryId, id);
    int count1 = dishService.count(dishQueryWrapper);
    
    if (count1 > 0) {
      throw new CustomException("当前分类关联了菜品，不能删除");
    }
    
    //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
    LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
    //添加查询条件，根据分类id进行查询
    setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
    int count2 = setmealService.count();
    if (count2 > 0) {
      //已经关联套餐，抛出一个业务异常
      throw new CustomException("当前分类下关联了套餐，不能删除");
    }
    
    //正常删除分类
    super.removeById(id);
  }
}
