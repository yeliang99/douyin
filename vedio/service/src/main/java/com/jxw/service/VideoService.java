package com.jxw.service;

import com.jxw.pojo.Comments;
import com.jxw.pojo.Videos;
import com.jxw.utils.PagedResult;

import java.util.List;

public interface VideoService {

    /**
     * @Description: 保存视频
     */
    public String saveVideo(Videos video);

    /**
     * @Description: 保存封面
     */
    public void saveCover(String videoId, String coverPath);

    /**
     * 分页查询所有视频
     * @param page 当前查询第几页
     * @param pageSize 每页显示的数量
     * @return
     */
//    public PagedResult queryMyvideo(String userId,int page,int pageSize);
    /**
     * 分页查询所有视频
     * @param page 当前查询第几页
     * @param pageSize 每页显示的数量
     * @return
     */
    public PagedResult queryAllvideo(Videos video,Integer isSaveRecord,int page,int pageSize,String userId);

    /**
     * @Description: 查询我喜欢的视频列表
     */
    public PagedResult queryMyLikeVideos(String userId, Integer page, Integer pageSize);

    /**
     * @Description: 查询我关注的人的视频列表
     */
    public PagedResult queryMyFollowVideos(String userId, Integer page, Integer pageSize);
    /**
     *留言
     */
    public List<String> getHotwords();

    void saveComment(Comments comment);

    /**
     * @Description: 留言分页
     */
    public PagedResult getAllComments(String videoId, Integer page, Integer pageSize);
    /**
     * @Description: 用户喜欢/点赞视频
     */
    public void userLikeVideo(String userId, String videoId, String videoCreaterId);
    /**
     * @Description: 用户不喜欢/取消点赞视频
     */
    public void userUnLikeVideo(String userId, String videoId, String videoCreaterId);
}


