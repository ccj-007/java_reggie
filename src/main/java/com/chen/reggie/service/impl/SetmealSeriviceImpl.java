package com.chen.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.reggie.common.CustomException;
import com.chen.reggie.dto.SetmealDto;
import com.chen.reggie.entity.Setmeal;
import com.chen.reggie.entity.SetmealDish;
import com.chen.reggie.mapper.SetmealMapper;
import com.chen.reggie.service.SetmealDishService;
import com.chen.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealSeriviceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
  @Autowired
  private SetmealDishService setmealDishService;
  
  /**
   * 保存套餐及菜品关系表
   *
   * @param setmealDto
   */
  @Override
  public void saveWithSetmealDish(SetmealDto setmealDto) {
    this.save(setmealDto);
    
    List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
    setmealDishes = setmealDishes.stream().map(item -> {
      item.setSetmealId(setmealDto.getId());
      return item;
    }).collect(Collectors.toList());
    
    // 保存到口味表 dish_flavor
    setmealDishService.saveBatch(setmealDishes);
  }
  
  /**
   * 删除套餐
   *
   * @param ids
   */
  @Override
  @Transactional
  public void removeWithDish(List<Long> ids) {
    //查询套餐状态，确定是否可用删除
    LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.in(Setmeal::getId, ids);
    queryWrapper.eq(Setmeal::getStatus, 1);
    
    int count = this.count(queryWrapper);
    if (count > 0) {
      throw new CustomException("套餐正在售卖中，不能删除");
    }
    
    this.removeByIds(ids);
    
    // 删除关系表数据
    LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
    
    setmealDishService.remove(lambdaQueryWrapper);
  }
}
