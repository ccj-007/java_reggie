package com.chen.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.reggie.dto.SetmealDto;
import com.chen.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
  
  public void saveWithSetmealDish(SetmealDto setmealDto);
  
  public void removeWithDish(List<Long> ids);
}
