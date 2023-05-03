package com.chen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.reggie.common.R;
import com.chen.reggie.entity.Category;
import com.chen.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 分类管理
 */
@RestController // 代表 @Controller + @ResponseBody
@RequestMapping("/category")
@Slf4j
public class CategoryController {
  @Resource
  private CategoryService categoryService;
  
  /**
   * 新增分类
   *
   * @param category
   * @return
   */
  @PostMapping
  public R<String> save(@RequestBody Category category) {
    log.info("新增的分类={}", category);
    categoryService.save(category);
    return R.success("成功新增分类");
  }
  
  /**
   * 分页查询
   *
   * @param page
   * @param pageSize
   * @return
   */
  @GetMapping("/page")
  public R<Page> page(int page, int pageSize) {
    Page<Category> pageInfo = new Page<>(page, pageSize);
    LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.orderByAsc(Category::getSort);
    
    // 传入分页和条件的构造器
    categoryService.page(pageInfo, queryWrapper);
    return R.success(pageInfo);
  }
  
  /**
   * 删除分类
   *
   * @param ids
   * @return
   */
  @DeleteMapping
  public R<String> delete(Long ids) {
    categoryService.remove(ids);
    return R.success("分类删除成功");
  }
  
  /**
   * 修改分类
   *
   * @param category
   * @return
   */
  @PutMapping
  public R<String> update(@RequestBody Category category) {
    categoryService.updateById(category);
    return R.success("修改分类信息成功");
  }
  
  /**
   * 查询菜品分类列表
   *
   * @return
   */
  @GetMapping("/list")
  public R<List<Category>> list(Category category) {
    LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
    
    if (category == null) {
      List<Category> list = categoryService.list(queryWrapper);
      return R.success(list);
    }
    log.info("查询菜品分类列表={}", category);
    // 条件构造器
    queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
    
    queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
    
    List<Category> list = categoryService.list(queryWrapper);
    return R.success(list);
  }
}
