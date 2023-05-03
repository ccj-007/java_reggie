package com.chen.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.reggie.dto.DishDto;
import com.chen.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
  
  public void saveWithFlavor(DishDto dishDto);
  
  public DishDto getByIdWithFlavor(Long id);
  
  public void updateWithFlavor(DishDto dishDto);
}
