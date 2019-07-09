package com.jxw.service;

import com.jxw.pojo.Users;
import com.jxw.pojo.UsersReport;

public interface UserService {
    /**
     根据用户名判断用户是否存在
     */
     boolean queryUsernameIsExist(String username);

    /**
     * 保存用户/注册用户
     */
     void saveUser(Users user);

    /**
     根据用户名密码判断用户是否存在
     */
    Users queryUserForLogin(Users user);

    /**
     * @Description: 用户修改信息
     */
    public void updateUserInfo(Users user);

    Users queryUserInfo(String userId);
    /**
     * @Description: 查询用户是否关注
     */
    public boolean queryIfFollow(String userId, String fanId);
    /**
     * @Description: 查询用户是否喜欢点赞视频
     */
    public boolean isUserLikeVideo(String userId, String videoId);

    /**
     * @Description: 关注用户
     */
    public void saveUserFanRelation(String userId, String fanId);
    /**
     * @Description: 取消关注用户
     */
    public void deleteUserFanRelation(String userId, String fanId);

    /**
     * @Description: 举报用户
     */
    public void reportUser(UsersReport usersReport);
}
