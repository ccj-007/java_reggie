package com.chen.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.chen.reggie.common.BaseContext;
import com.chen.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录校验
 */
@Slf4j
@Component
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
  public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
  
  /**
   * 过滤器
   *
   * @param servletRequest
   * @param servletResponse
   * @param filterChain
   * @throws IOException
   * @throws ServletException
   */
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    
    String requestURI = request.getRequestURI();
    
    // 放行urls
    String[] urls = new String[]{
        "/employee/login",
        "/employee/logout",
        "/backend/**",
        "/front/**",
        "/common/**",
        "/user/sendMsg",
        "/user/login"
    };
    log.info("拦截到的请求", request.getRequestURL());
    boolean check = check(urls, requestURI);
    // 通用页面直接放行
    if (check) {
      log.info("本次请求{}不需要处理", requestURI);
      filterChain.doFilter(request, response);
      return;
    }
    
    // 判断后台是否带session后可登入
    if (request.getSession().getAttribute("employee") != null) {
      // 在metaObject元数据做了公共字段填充，但是无法获得session的employee的id，通过多线程获取
      Long empId = (Long) request.getSession().getAttribute("employee");
      BaseContext.setCurrentId(empId);
      
      filterChain.doFilter(request, response);
      return;
    }
    
    // 判断后台是否带session后可登入
    if (request.getSession().getAttribute("user") != null) {
      log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("user"));
      
      Long userId = (Long) request.getSession().getAttribute("user");
      BaseContext.setCurrentId(userId);
      
      filterChain.doFilter(request, response);
      return;
    }
    log.info("未登录");
    
    // 未登录，前端跳转登录页
    response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    return;
  }
  
  /**
   * 路径匹配，检查本次请求是否放行
   *
   * @param urls
   * @param requestURI
   * @return
   */
  public boolean check(String[] urls, String requestURI) {
    for (String url : urls) {
      boolean match = PATH_MATCHER.match(url, requestURI);
      if (match) return true;
    }
    return false;
  }
}
