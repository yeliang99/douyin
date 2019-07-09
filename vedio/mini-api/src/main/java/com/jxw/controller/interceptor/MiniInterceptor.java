package com.jxw.controller.interceptor;

import com.jxw.utils.JsonUtils;
import com.jxw.utils.RedisOperator;
import com.jxw.utils.myJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * 拦截器
 */
public class MiniInterceptor implements HandlerInterceptor {
    @Autowired
    public RedisOperator redis;
    public static final String USER_REDIS_SESSION = "user-redis-session";
    /**
     * 拦截请求，是在controller之前
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String userId = request.getHeader("headerUserId");
        String userToken = request.getHeader("headerUserToken");
        System.out.println(userId);
        System.out.println(userToken);
        if(StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)){
            String uniqueToken = redis.get(USER_REDIS_SESSION+":"+userId);
            if (StringUtils.isBlank(uniqueToken) ||StringUtils.isEmpty(uniqueToken)){
                System.out.println("redis超时，需要重新登录");
                returnErrorResponse(response,new myJSONResult().errorTokenMsg("登录超时") );

                return false;
            }else {
                if (!uniqueToken.equals(userToken)){
//                    token不匹配，有可能是 用户在另一处已登录，覆盖了一个新的token
                    returnErrorResponse(response,new myJSONResult().errorTokenMsg("账号被挤出") );
                    System.out.println("异地登录，请重新登录");
                    return false;
                }
            }

            return true;
        }else {
            returnErrorResponse(response,new myJSONResult().errorTokenMsg("请登录") );
            System.out.println("请登录....");
            return false;
        }
        //返回false 请求被拦截
        // true 请求OK 可以继续执行。
    }

    /**
     * 将拦截器中的一些错误信息可以以对象的形式返回，让前端获取
     */
    public void returnErrorResponse(HttpServletResponse response, myJSONResult result)
            throws IOException, UnsupportedEncodingException {
        OutputStream out=null;   //信息以流的形式抛出去
        try{
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/json");
            out = response.getOutputStream();
            //将对象转换为json字符串
            out.write(JsonUtils.objectToJson(result).getBytes("utf-8"));
            out.flush();
        } finally{
            if(out!=null){
                out.close();
            }
        }
    }
    /**
     * 请求controller之后，渲染视图之前
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    /**
     * 请求controller，渲染视图之后
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
