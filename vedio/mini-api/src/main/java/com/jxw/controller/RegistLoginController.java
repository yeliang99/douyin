package com.jxw.controller;

import com.jxw.pojo.*;
import com.jxw.pojo.vo.UsersVO;
import com.jxw.service.UserService;
import com.jxw.utils.MD5Utils;
import com.jxw.utils.myJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController //与小程序接口 都是返回消息
@Api(value="用户注册登录的接口", tags= {"注册和登录的controller"})
public class RegistLoginController extends BasicController{

    @Autowired
    private UserService userService;


    @PostMapping("/regist")
    @ApiOperation(value="用户注册", notes="用户注册的接口")
    public myJSONResult regist(@RequestBody Users user) throws Exception { //一个json对象

        //1.0判断用户名密码 !=null length!=0
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())){
            return myJSONResult.errorMsg("用户名/密码不能为空");
        }
        //2.0判断用户名是否存在
        boolean usernameIsExist = userService.queryUsernameIsExist(user.getUsername());
        //3.0不存在，则创建新的用户
        if (!usernameIsExist){
            user.setNickname(user.getUsername());
            user.setPassword(MD5Utils.getMD5Str(user.getPassword())); //加密用户密码
            user.setFansCounts(0);
            user.setReceiveLikeCounts(0);
            user.setFollowCounts(0);
            userService.saveUser(user);
        }else{
            return myJSONResult.errorMsg("用户名已存在，请换一个试试");
        }
        user.setPassword(""); //这样返回到小程序端的密码为空  较为安全
        UsersVO userVO = setuniqueToken(user);
        return myJSONResult.ok(userVO);

    }
    //直接返回一个对象，小程序端/前端会转成json
//    @RequestBody主要用来接收前端传递给后端的json字符串中的数据的(请求体中的数据的)

    /**
     * 创建redis函数
     * @param userModel
     * @return
     */
    public UsersVO setuniqueToken(Users userModel){
        userModel.setPassword(""); //这样返回到小程序端的密码为空  较为安全
        String uniqueToken = UUID.randomUUID().toString();
        redis.set(USER_REDIS_SESSION+":"+userModel.getId(),uniqueToken,60*60*12);  //redis的时间限制 设计60s
        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(userModel,userVO);
        userVO.setUserToken(uniqueToken);
        return userVO;
    }

    @PostMapping("/login")
    @ApiOperation(value="用户登录", notes="用户登录的接口")
    public myJSONResult login(@RequestBody Users user) throws Exception {
//        Thread.sleep(3000);

        //1.0判断用户名密码 !=null length!=0
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())){
            System.out.println(user.getUsername()+","+user.getPassword());
            return myJSONResult.errorMsg("用户名/密码不能为空");
        }
        //2.0判断用户名是否存在
        boolean usernameIsExist = userService.queryUsernameIsExist(user.getUsername());
        //3.0不存在，则提示
        if (!usernameIsExist){
            return myJSONResult.errorMsg("用户名不存在");
        }else {
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            Users userResult = userService.queryUserForLogin(user);
            if (userResult!=null){
                userResult.setPassword("");
                UsersVO userVO = setuniqueToken(userResult);
                return myJSONResult.ok(userVO);
            }else{
                return myJSONResult.errorMsg("用户名或密码不正确请重试");
            }
        }

    }

    //用户注销，将redis中的key值删除即可。
    @PostMapping("/logout")
    @ApiImplicitParam(name="userId", value="用户id", required=true,
            dataType="String", paramType="query")  //通过问好？拼接 所以放一个query
    @ApiOperation(value="用户注销", notes="用户注销的接口")
    public myJSONResult logout(String userId) throws Exception {
        redis.del(USER_REDIS_SESSION + ":" + userId);
        return myJSONResult.ok();
    }
}
