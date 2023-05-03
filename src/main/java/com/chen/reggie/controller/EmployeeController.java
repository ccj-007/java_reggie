package com.chen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.reggie.common.R;
import com.chen.reggie.entity.Employee;
import com.chen.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
  
  @Resource
  private EmployeeService employeeService;
  
  /**
   * 员工登录
   *
   * @param request
   * @param employee
   * @return
   */
  @PostMapping("/login")
  public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
    String password = employee.getPassword();
    password = DigestUtils.md5DigestAsHex(password.getBytes());
    
    // 根据页面username查询数据库
    LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Employee::getUsername, employee.getUsername());
    Employee emp = employeeService.getOne(queryWrapper);
    
    if (emp == null || emp.getStatus() == 0) {
      return R.error("登录失败");
    }
    
    if (!emp.getPassword().equals(password)) {
      return R.error("登录失败");
    }
    log.info("登录成功的用户id={}", emp.getId());
    // 登录成功
    request.getSession().setAttribute("employee", emp.getId());
    return R.success(emp);
  }
  
  /**
   * 退出登录
   *
   * @param request
   * @return
   */
  @PostMapping("/logout")
  public R<String> logout(HttpServletRequest request) {
    request.getSession().removeAttribute("employee");
    return R.success("退出登录");
  }
  
  /**
   * 新增员工
   *
   * @param employee
   * @return
   */
  @PostMapping
  public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
    log.info("新增员工信息", employee.toString());
    
    // 设置初始密码
    employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
    
    employee.setCreateTime(LocalDateTime.now());
    employee.setUpdateTime(LocalDateTime.now());
    
    Long empId = (Long) request.getSession().getAttribute("employee");
    
    log.info("员工信息empId", empId);
    
    employee.setUpdateUser(empId);
    employee.setCreateUser(empId);
    
    employeeService.save(employee);
    
    return R.success("新增员工成功");
  }
  
  /**
   * 员工列表
   *
   * @param page
   * @param pageSize
   * @param name
   * @return
   */
  @GetMapping("/page")
  public R<Page> page(int page, int pageSize, String name) {
    log.info("page={}, pageSize={}, name={}", page, pageSize, name);
    
    Page pageInfo = new Page(page, pageSize);
    LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
    
    // 过滤条件
    queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
    
    // 排序条件
    queryWrapper.orderByDesc(Employee::getUpdateTime);
    
    // 查询
    employeeService.page(pageInfo, queryWrapper);
    
    return R.success(pageInfo);
  }
  
  /**
   * 根据id更新员工信息
   *
   * @param request
   * @param employee
   * @return
   */
  @PutMapping
  public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
    log.info(employee.toString());
    
    // 前端id只能16位，会存在丢失精度，返回字符串
    long empId = (long) request.getSession().getAttribute("employee");
    employee.setUpdateTime(LocalDateTime.now());
    employee.setUpdateUser(empId);
    
    employeeService.updateById(employee);
    
    return R.success("员工信息修改成功");
  }
  
  /**
   * 编辑员工 —— 获取路径的id
   *
   * @param id
   * @return
   */
  @GetMapping("/{id}")
  public R<Employee> getById(@PathVariable Long id) {
    log.info("根据id得到的员工信息" + id);
    Employee employee = employeeService.getById(id);
    return R.success(employee);
  }
}
