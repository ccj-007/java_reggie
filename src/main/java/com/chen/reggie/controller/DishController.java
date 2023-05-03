package com.chen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.reggie.common.R;
import com.chen.reggie.dto.DishDto;
import com.chen.reggie.entity.Category;
import com.chen.reggie.entity.Dish;
import com.chen.reggie.entity.DishFlavor;
import com.chen.reggie.service.CategoryService;
import com.chen.reggie.service.DishFlavorService;
import com.chen.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
  @Resource
  private DishService dishService;
  
  @Resource
  private DishFlavorService dishFlavorService;
  
  @Resource
  private CategoryService categoryService;
  
  /**
   * 新增菜品
   *
   * @param dishDto
   * @return
   */
  @PostMapping
  public R<String> save(@RequestBody DishDto dishDto) {
    dishService.saveWithFlavor(dishDto);
    
    return R.success("新增菜品成功");
  }
  
  /**
   * 菜品列表查询
   *
   * @param page
   * @param pageSize
   * @param name
   * @return
   */
  @GetMapping("/page")
  public R<Page> page(int page, int pageSize, String name) {
    Page<Dish> pageInfo = new Page<>(page, pageSize);
    Page<DishDto> dishDtoPage = new Page<>();
    
    LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.like(name != null, Dish::getName, name);
    queryWrapper.orderByDesc(Dish::getUpdateTime);
    dishService.page(pageInfo, queryWrapper);
    
    // 对象拷贝
    BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
    
    List<Dish> records = pageInfo.getRecords();
    List<DishDto> list = records.stream().map((item) -> {
      DishDto dishDto = new DishDto();
      BeanUtils.copyProperties(item, dishDto);
      
      Long categoryId = item.getCategoryId();
      Category category = categoryService.getById(categoryId);
      
      if (categoryId != null) {
        String categoryName = category.getName();
        dishDto.setCategoryName(categoryName);
      }
      return dishDto;
    }).collect(Collectors.toList());
    
    dishDtoPage.setRecords(list);
    return R.success(dishDtoPage);
  }
  
  /**
   * 根据 id查询菜品信息和对应的口味信息
   *
   * @param id
   * @return
   */
  @GetMapping("/{id}")
  public R<DishDto> get(@PathVariable Long id) {
    DishDto dishDto = dishService.getByIdWithFlavor(id);
    return R.success(dishDto);
  }
  
  /**
   * 更新菜品信息和对应的口味信息
   *
   * @param dishDto
   * @return
   */
  @PutMapping
  public R<String> update(@RequestBody DishDto dishDto) {
    dishService.updateWithFlavor(dishDto);
    
    return R.success("修改菜品信息和口味成功");
  }

//  /**
//   * 根据菜品分类id获取所有菜品
//   *
//   * @param dish
//   * @return
//   */
//  @GetMapping("/list")
//  public R<List<Dish>> list(Dish dish) {
//    Long categoryId = dish.getCategoryId();
//
//    LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//
//    queryWrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
//    queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//    List<Dish> list = dishService.list(queryWrapper);
//    return R.success(list);
//  }
  
  /**
   * 根据菜品分类id获取所有菜品
   *
   * @param dish
   * @return
   */
  @GetMapping("/list")
  public R<List<DishDto>> list(Dish dish) {
    Long categoryId = dish.getCategoryId();
    
    LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
    
    queryWrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
    queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
    
    List<Dish> list = dishService.list(queryWrapper);
    
    List<DishDto> dishDtoData = list.stream().map((item) -> {
      DishDto dishDto = new DishDto();
      BeanUtils.copyProperties(item, dishDto);
      
      Long categoryId2 = item.getCategoryId();
      Category category = categoryService.getById(categoryId2);
      
      if (categoryId2 != null) {
        String categoryName = category.getName();
        dishDto.setCategoryName(categoryName);
      }
      
      // 当前菜品的id
      Long dishId = item.getId();
      LambdaQueryWrapper<DishFlavor> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
      objectLambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
      List<DishFlavor> dishFlavorList = dishFlavorService.list(objectLambdaQueryWrapper);
      dishDto.setFlavors(dishFlavorList);
      return dishDto;
    }).collect(Collectors.toList());
    
    return R.success(dishDtoData);
  }
}
