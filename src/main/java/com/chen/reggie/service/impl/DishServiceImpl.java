package com.chen.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.reggie.dto.DishDto;
import com.chen.reggie.entity.Dish;
import com.chen.reggie.entity.DishFlavor;
import com.chen.reggie.mapper.DishMapper;
import com.chen.reggie.service.DishFlavorService;
import com.chen.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
  @Resource
  private DishFlavorService dishFlavorService;
  
  /**
   * 新增菜品，同时保存对应的口味数据
   *
   * @param dishDto
   */
  @Override
  public void saveWithFlavor(DishDto dishDto) {
    // 保存到菜品表 dish
    this.save(dishDto);
    
    // 从dish表获取dish id
    Long dishId = dishDto.getId();
    
    List<DishFlavor> flavors = dishDto.getFlavors();
    flavors = flavors.stream().map(item -> {
      item.setDishId(dishId);
      return item;
    }).collect(Collectors.toList());
    
    // 保存到口味表 dish_flavor
    dishFlavorService.saveBatch(flavors);
  }
  
  /**
   * 根据id查询菜品信息和对应的口味信息
   *
   * @param id
   * @return
   */
  @Override
  public DishDto getByIdWithFlavor(Long id) {
    Dish dish = this.getById(id);
    DishDto dishDto = new DishDto();
    BeanUtils.copyProperties(dish, dishDto);
    
    LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(DishFlavor::getDishId, id);
    List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
    
    dishDto.setFlavors(flavors);
    
    return dishDto;
  }
  
  /**
   * 更新菜品信息和对应的口味信息
   *
   * @param dishDto
   */
  @Override
  @Transactional
  public void updateWithFlavor(DishDto dishDto) {
    this.updateById(dishDto);
    
    // 口味的删除，修改数量是不一致的，可以先删除后新增
    LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
    
    dishFlavorService.remove(queryWrapper);
    
    // 新增口味
    //添加当前提交过来的口味数据---dish_flavor表的insert操作
    List<DishFlavor> flavors = dishDto.getFlavors();
    
    flavors = flavors.stream().map((item) -> {
      item.setDishId(dishDto.getId());
      return item;
    }).collect(Collectors.toList());
    
    dishFlavorService.saveBatch(flavors);
  }
}
