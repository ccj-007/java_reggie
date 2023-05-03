package com.chen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chen.reggie.common.BaseContext;
import com.chen.reggie.common.R;
import com.chen.reggie.entity.AddressBook;
import com.chen.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {
  @Autowired
  private AddressBookService addressBookService;
  
  /**
   * 新增
   *
   * @param addressBook
   * @return
   */
  @PostMapping
  public R<AddressBook> save(@RequestBody AddressBook addressBook) {
    addressBook.setUserId(BaseContext.getCurrentId());
    addressBookService.save(addressBook);
    return R.success(addressBook);
  }
  
  /**
   * 设置默认地址
   *
   * @param addressBook
   * @return
   */
  @PutMapping("default")
  public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
    // 先将所有的保存为非默认，然后单独选择一个为默认
    LambdaUpdateWrapper<AddressBook> queryWrapper = new LambdaUpdateWrapper<>();
    queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
    queryWrapper.set(AddressBook::getIsDefault, 0);
    
    addressBookService.update(queryWrapper);
    
    addressBook.setIsDefault(1);
    addressBookService.updateById(addressBook);
    return R.success(addressBook);
  }
  
  /**
   * 根据id查询地址
   *
   * @param id
   * @return
   */
  @GetMapping("/{id}")
  public R get(@PathVariable Long id) {
    AddressBook addressBook = addressBookService.getById(id);
    if (addressBook != null) {
      return R.success(addressBook);
    } else {
      return R.error("没有找到该对象");
    }
  }
  
  /**
   * 查询默认地址
   *
   * @return
   */
  @GetMapping("default")
  public R<AddressBook> getDefault() {
    LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
    queryWrapper.eq(AddressBook::getIsDefault, 1);
    
    AddressBook addressBook = addressBookService.getOne(queryWrapper);
    
    if (null == addressBook) {
      return R.error("没有找到该对象");
    } else {
      return R.success(addressBook);
    }
  }
  
  /**
   * 查询指定用户的全部地址
   */
  @GetMapping("/list")
  public R<List<AddressBook>> list(AddressBook addressBook) {
    addressBook.setUserId(BaseContext.getCurrentId());
    log.info("addressBook:{}", addressBook);
    
    //条件构造器
    LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
    queryWrapper.orderByDesc(AddressBook::getUpdateTime);
    
    //SQL:select * from address_book where user_id = ? order by update_time desc
    return R.success(addressBookService.list(queryWrapper));
  }
}
