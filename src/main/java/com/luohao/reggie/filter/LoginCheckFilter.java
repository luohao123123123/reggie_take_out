package com.luohao.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.luohao.reggie.R.R;
import com.luohao.reggie.common.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;

        //1.获取本次请求的uri
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);

        //定义不需要处理的请求路径
        String [] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg", //移动端发送短信
                "/user/login"  //移动端登录
        };
        //2.判断本次请求是否需要处理
        boolean checkFlag = check(urls, requestURI);
        //3.如果不需要处理，则直接放行
        if(checkFlag){
            log.info("本次请求：{} 不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        //4-1.判断后台登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee")!=null){
            log.info("后台用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));
            /**
             * ThreadLocal为每个线程提供单独一份存储空间，具有线程隔离的效果
             * 使用封装了TreadLocal的工具类：BaseContext，存入后台员工的id，然后在元数据处理器中获取员工id，进行字段的自动填充
             */
            BaseContext.setId((Long) request.getSession().getAttribute("employee"));  //把当前用户id存入ThreadLocal，方便在MyMetaObjectHandler中获取用户id

            filterChain.doFilter(request,response);
            return;
        }

        //4-2.判断移动端用户登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user")!=null){
            log.info("移动端用户已登录，用户id为：{}",request.getSession().getAttribute("user"));
            /**
             * ThreadLocal为每个线程提供单独一份存储空间，具有线程隔离的效果
             * 使用封装了TreadLocal的工具类：BaseContext，存入移动端用户的id，然后在元数据处理器中获取员工id，进行字段的自动填充
             */
            BaseContext.setId((Long) request.getSession().getAttribute("user"));  //把当前移动端用户id存入ThreadLocal，方便在MyMetaObjectHandler中获取用户id

            filterChain.doFilter(request,response);
            return;
        }

        //5.如果未登录，则返回未登录结果，通过输出流方式向客户端页面响应数据,前端通过msg?=NOTLOGIN判断是否需要跳转
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param checkUrl
     * @return
     */
    public boolean check(String [] urls,String checkUrl){
        for(String url:urls){
            boolean flag = PATH_MATCHER.match(url, checkUrl);
            if(flag){
                return true;
            }
        }
        return false;
    }
}
