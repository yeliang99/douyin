package com.jxw.mapper;

import com.jxw.pojo.Videos;
import com.jxw.pojo.vo.VideosVO;
import com.jxw.utils.MyMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideosMapperCustom extends MyMapper<VideosVO> {
    /**
     * 查询所有视频
     * @return
     */
    public List<VideosVO> queryAllVideos(@Param("videoDesc") String videoDesc ,@Param("userId")String userId);
    /**
     * 查询该用户的视频
     * @return
     */
//    public List<VideosVO> queryMyVideos(@Param("userId") String userId );
    /**
     * @Description: 查询关注的视频
     */
    public List<VideosVO> queryMyFollowVideos(String userId);

    /**
     * @Description: 查询点赞视频
     */
    public List<VideosVO> queryMyLikeVideos(@Param("userId") String userId);
    /**
     * @Description: 对视频喜欢的数量进行累加
     */
    public void addVideoLikeCount(String videoId);

    /**
     * @Description: 对视频喜欢的数量进行累减
     */
    public void reduceVideoLikeCount(String videoId);

}