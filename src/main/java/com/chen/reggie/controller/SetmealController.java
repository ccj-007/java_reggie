package com.chen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.reggie.common.R;
import com.chen.reggie.dto.SetmealDto;
import com.chen.reggie.entity.Category;
import com.chen.reggie.entity.Setmeal;
import com.chen.reggie.service.CategoryService;
import com.chen.reggie.service.SetmealDishService;
import com.chen.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
  @Autowired
  private SetmealService setmealService;
  
  @Autowired
  private SetmealDishService setmealDishService;
  
  @Autowired
  private CategoryService categoryService;
  
  /**
   * 新增套餐
   *
   * @param setmealDto
   * @return
   */
  @PostMapping
  public R<String> save(@RequestBody SetmealDto setmealDto) {
    log.info("套餐信息={}", setmealDto);
    setmealService.saveWithSetmealDish(setmealDto);
    return R.success("成功新增套餐");
  }
  
  /**
   * 查询套餐列表
   *
   * @param page
   * @param pageSize
   * @return
   */
  @GetMapping("/page")
  public R<Page> page(int page, int pageSize, String name) {
    Page<Setmeal> pageInfo = new Page<>(page, pageSize);
    Page<SetmealDto> dtoPage = new Page<>();
    
    LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(StringUtils.isNotEmpty(name), Setmeal::getName, name);
    queryWrapper.orderByDesc(Setmeal::getUpdateTime);
    setmealService.page(pageInfo, queryWrapper);
    
    BeanUtils.copyProperties(pageInfo, dtoPage, "records");
    List<Setmeal> records = pageInfo.getRecords();
    
    List<SetmealDto> list = records.stream().map((item) -> {
      // 对象拷贝
      SetmealDto setmealDto = new SetmealDto();
      BeanUtils.copyProperties(item, setmealDto);
      
      Long id = setmealDto.getCategoryId();
      Category category = categoryService.getById(id);
      
      if (id != null) {
        String categoryName = category.getName();
        setmealDto.setCategoryName(categoryName);
      }
      return setmealDto;
    }).collect(Collectors.toList());
    
    dtoPage.setRecords(list);
    return R.success(dtoPage);
  }
  
  /**
   * 删除套餐
   *
   * @param ids 套餐id
   * @return
   */
  @DeleteMapping
  public R<String> delete(@RequestParam List<Long> ids) {
    setmealService.removeWithDish(ids);
    return R.success("成功删除套餐");
  }
  
  /**
   * 设置停售状态
   *
   * @param ids
   * @return
   */
  @PostMapping("/status/{status}")
  public R<String> status(@RequestBody @PathVariable int status, Long ids) {
    Setmeal setmeal = setmealService.getById(ids);
    log.info("停售状态={}, ids={}, setmeal={}", status, ids, setmeal);
    setmeal.setStatus(status);
    setmealService.updateById(setmeal);
    return R.success("成功设置售卖状态");
  }
  
  /**
   * 根据条件查询套餐数据
   *
   * @param setmeal
   * @return
   */
  @GetMapping("/list")
  public R<List<Setmeal>> list(Setmeal setmeal) {
    LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
    queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
    queryWrapper.orderByDesc(Setmeal::getUpdateTime);
    
    List<Setmeal> list = setmealService.list(queryWrapper);
    return R.success(list);
  }
}
