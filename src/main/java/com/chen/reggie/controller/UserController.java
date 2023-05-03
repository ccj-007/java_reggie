package com.chen.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.reggie.common.R;
import com.chen.reggie.entity.User;
import com.chen.reggie.service.UserService;
import com.chen.reggie.utils.SMSUtils;
import com.chen.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
  
  @Autowired
  private UserService userService;
  
  /**
   * 发送手机短信验证码
   *
   * @param user
   * @return
   */
  @PostMapping("/sendMsg")
  public R<String> sendMsg(@RequestBody User user, HttpSession session) {
    String phone = user.getPhone();
    
    if (StringUtils.isNotEmpty(phone)) {
      // 生成4位验证码
      String code = ValidateCodeUtils.generateValidateCode(4).toString();
      
      // 调用阿里云提供的短信服务api发送的短信
      SMSUtils.sendMessage("瑞吉外卖", "", phone, code);
      // 需要将生成的验证码保存到session
      session.setAttribute(phone, code);
      
      return R.success("短信发送成功");
    }
    return R.error("短信发送失败");
  }
  
  /**
   * 移动端登录
   *
   * @param user
   * @return
   */
  @PostMapping("/login")
  public R<User> login(@RequestBody Map user, HttpServletRequest request) {
    // 这里前端返回code、user，可以用Dto，或者map
    
    // 获取手机号
    String phone = user.get("phone").toString();
    // 获取验证码
    // String code = user.get("code").toString();
    String code = "111111";
    
    // 比对session
    // Object codeInSession = session.getAttribute(phone);
    String codeInSession = "111111";
    log.info("login");
    User newUser = new User();
    if (codeInSession != null && codeInSession.equals(code)) {
      
      LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
      queryWrapper.eq(User::getPhone, phone);
      User one = userService.getOne(queryWrapper);
      if (one == null) {
        // 当前是新用户
        newUser.setPhone(phone);
        newUser.setStatus(1);
        userService.save(newUser);
      }
      log.info("保存移动端的登录sessionId={}", one.getId());
      request.getSession().setAttribute("user", one.getId());
      return R.success(newUser);
    }
    return R.error("短信发送失败");
  }
}
