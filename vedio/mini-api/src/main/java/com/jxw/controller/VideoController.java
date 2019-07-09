package com.jxw.controller;


import com.jxw.enums.VideoStatusEnum;
import com.jxw.pojo.*;
import com.jxw.pojo.vo.VideosVO;
import com.jxw.service.BgmService;
import com.jxw.service.VideoService;
import com.jxw.utils.FetchVideoCover;
import com.jxw.utils.MergeVideoMp3;
import com.jxw.utils.PagedResult;
import com.jxw.utils.myJSONResult;
import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@RestController
@Api(value="视频相关业务的接口", tags= {"视频相关业务的controller"})
@RequestMapping("/video")
public class VideoController extends BasicController {
	@Autowired
	private BgmService bgmService;

	@Autowired
	private VideoService videoService;

	
	@ApiOperation(value="上传视频", notes="上传视频的接口")
	@ApiImplicitParams({
		@ApiImplicitParam(name="userId", value="用户id", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="bgmId", value="背景音乐id", required=false,
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoSeconds", value="背景音乐播放长度", required=true,
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoWidth", value="视频宽度", required=true,
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoHeight", value="视频高度", required=true,
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="desc", value="视频描述", required=false,
				dataType="String", paramType="form")
	})
	@PostMapping(value="/upload", headers="content-type=multipart/form-data")
	public myJSONResult upload(String userId,
				String bgmId, double videoSeconds, 
				int videoWidth, int videoHeight,
				String desc,
				@ApiParam(value="短视频", required=true)
                                          MultipartFile file) throws Exception {
		System.out.println("进入上传视频接口...");

		// 文件保存的命名空间
//		String fileSpace = "C:/imooc_videos_dev";
		// 保存到数据库中的相对路径
		String uploadPathDB = "/" + userId + "/video";
		String coverPathDB = "/" + userId + "/video";

		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;
		// 文件上传的最终保存路径
		String finalVideoPath = "";
		try {
			if (file != null) {

				String fileName = file.getOriginalFilename();

				String arrayFilenameItem[] =  fileName.split("\\.");
				String fileNamePrefix = "";
				for (int i = 0 ; i < arrayFilenameItem.length-1 ; i ++) {
					fileNamePrefix += arrayFilenameItem[i];
				}
				// fix bug: 解决小程序端OK，PC端不OK的bug，原因：PC端和小程序端对临时视频的命名不同
//				String fileNamePrefix = fileName.split("\\.")[0];

				if (StringUtils.isNotBlank(fileName)) {

					finalVideoPath = FILE_SPACE + uploadPathDB + "/" + fileName;

					// 设置数据库保存的路径
					uploadPathDB += ("/" + fileName);
					coverPathDB = coverPathDB + "/" + fileNamePrefix + ".jpg";

					File outFile = new File(finalVideoPath);
					if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
						// 创建父文件夹
						outFile.getParentFile().mkdirs();
					}

					fileOutputStream = new FileOutputStream(outFile);
					inputStream = file.getInputStream();
					IOUtils.copy(inputStream, fileOutputStream);
				}

			} else {
				return myJSONResult.errorMsg("上传出错...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return myJSONResult.errorMsg("上传出错...");
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		}

		// 判断bgmId是否为空，如果不为空，
		// 那就查询bgm的信息，并且合并视频，生产新的视频，所以有背景音乐的会有两个视频
		if (StringUtils.isNotBlank(bgmId)) {
			Bgm bgm = bgmService.queryBgmById(bgmId);
			String mp3InputPath = FILE_SPACE + bgm.getPath();

			MergeVideoMp3 tool = new MergeVideoMp3(FFMPEG_EXE);
			String videoInputPath = finalVideoPath;

			String videoOutputName = UUID.randomUUID().toString() + ".mp4";
			uploadPathDB = "/" + userId + "/video" + "/" + videoOutputName;
			finalVideoPath = FILE_SPACE + uploadPathDB;
			tool.convertor(videoInputPath, mp3InputPath, videoSeconds, finalVideoPath);
		}
		System.out.println("uploadPathDB=" + uploadPathDB);
		System.out.println("finalVideoPath=" + finalVideoPath);

		// 对视频进行截图
		FetchVideoCover videoInfo = new FetchVideoCover(FFMPEG_EXE);
		videoInfo.getCover(finalVideoPath, FILE_SPACE + coverPathDB);

		// 保存视频信息到数据库
		Videos video = new Videos();
		video.setAudioId(bgmId);
		video.setUserId(userId);
		video.setVideoSeconds((float)videoSeconds);
		video.setVideoHeight(videoHeight);
		video.setVideoWidth(videoWidth);
		video.setVideoDesc(desc);
		video.setVideoPath(uploadPathDB);
		video.setCoverPath(coverPathDB);
		video.setStatus(VideoStatusEnum.SUCCESS.value);
		video.setCreateTime(new Date());
		System.out.println(video);
		String videoId = videoService.saveVideo(video);

		return myJSONResult.ok(videoId);

	}



	@ApiOperation(value="上传封面", notes="上传封面的接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name="userId", value="用户id", required=true,
					dataType="String", paramType="form"),
			@ApiImplicitParam(name="videoId", value="视频主键id", required=true,
					dataType="String", paramType="form")
	})
	@PostMapping(value="/uploadCover", headers="content-type=multipart/form-data")
	public myJSONResult uploadCover(String userId,
									   String videoId,
									   @ApiParam(value="视频封面", required=true)
											   MultipartFile file) throws Exception {

		if (StringUtils.isBlank(videoId) || StringUtils.isBlank(userId)) {
			return myJSONResult.errorMsg("视频主键id和用户id不能为空...");
		}

		// 文件保存的命名空间
//		String fileSpace = "C:/imooc_videos_dev";
		// 保存到数据库中的相对路径
		String uploadPathDB = "/" + userId + "/video";

		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;
		// 文件上传的最终保存路径
		String finalCoverPath = "";
		try {
			if (file != null) {

				String fileName = file.getOriginalFilename();
				if (StringUtils.isNotBlank(fileName)) {

					finalCoverPath = FILE_SPACE + uploadPathDB + "/" + fileName;
					// 设置数据库保存的路径
					uploadPathDB += ("/" + fileName);

					File outFile = new File(finalCoverPath);
					if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
						// 创建父文件夹
						outFile.getParentFile().mkdirs();
					}

					fileOutputStream = new FileOutputStream(outFile);
					inputStream = file.getInputStream();
					IOUtils.copy(inputStream, fileOutputStream);
				}

			} else {
				return myJSONResult.errorMsg("上传出错...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return myJSONResult.errorMsg("上传出错...");
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		}

		videoService.saveCover(videoId, uploadPathDB);

		return myJSONResult.ok();
	}

	@ApiOperation(value="查询所有视频", notes="分页查询所有视频")
	@ApiImplicitParams({
			@ApiImplicitParam(name="page", value="查询当前页数",
			dataType="Integer", paramType="query"),
	})
	/**
	 * isSaveRecord 1：需要保存 0或为空：不需要保存
	 */
	@PostMapping(value="/showAll")
	public myJSONResult showAll(@RequestBody Videos video,String userId,Integer isSaveRecord,Integer page) throws Exception {  //Requestbody会把前端查询词封装成video作为一个对象传递过来
		if (page == null){
			page =1;
		}
		System.out.println("视频用户Id："+userId);
		PagedResult result = videoService.queryAllvideo(video,isSaveRecord,page, PAGE_SIZE, userId);
		List<VideosVO> rows = (List<VideosVO>) result.getRows();
		return myJSONResult.ok(result);
	}

//	/**
//	 * 查询我的视频
//	 */
//	@PostMapping(value="/showMyVideos")
//	public myJSONResult showMyVideos(@Param("userId") String userId, Integer page) throws Exception {  //Requestbody会把前端查询词封装成video作为一个对象传递过来
//		if (page == null){
//			page =1;
//		}
//		PagedResult result = videoService.queryMyvideo(userId,page, PAGE_SIZE);
//		List<VideosVO> rows = (List<VideosVO>) result.getRows();
//		return myJSONResult.ok(result);
//	}
	/**
	 * 热搜关键词
	 */
	@PostMapping(value="/hot")
	public List<String> hot() throws Exception {  //Requestbody会把前端查询词封装成video作为一个对象传递过来
		System.out.println("进入接口");
		return videoService.getHotwords();
	}
	@PostMapping(value="/userLike")
	public myJSONResult userLike(String userId, String videoId, String videoCreaterId)
			throws Exception {
		videoService.userLikeVideo(userId, videoId, videoCreaterId);
		return myJSONResult.ok();
	}

	@PostMapping(value="/userUnLike")
	public myJSONResult userUnLike(String userId, String videoId, String videoCreaterId) throws Exception {
		videoService.userUnLikeVideo(userId, videoId, videoCreaterId);
		return myJSONResult.ok();
	}
	@PostMapping("/saveComment")
	public myJSONResult saveComment(@RequestBody Comments comment,
									   String fatherCommentId,
									String toUserId) throws Exception {

		comment.setFatherCommentId(fatherCommentId);
		comment.setToUserId(toUserId);

		videoService.saveComment(comment);
		return myJSONResult.ok();
	}

	/**
	 * 获取评论列表
	 */
	@PostMapping("/getVideoComments")
	public myJSONResult getVideoComments(String videoId, Integer page, Integer pageSize) throws Exception {

		if (StringUtils.isBlank(videoId)) {
			return myJSONResult.ok();
		}

		// 分页查询视频列表，时间顺序倒序排序
		if (page == null) {
			page = 1;
		}

		if (pageSize == null) {
			pageSize = 10;
		}

		PagedResult list = videoService.getAllComments(videoId, page, pageSize);

		return myJSONResult.ok(list);
	}
	/**
	 * @Description: 我关注的人发的视频
	 */
	@PostMapping("/showMyFollow")
	public myJSONResult showMyFollow(String userId, Integer page) throws Exception {

		if (StringUtils.isBlank(userId)) {
			return myJSONResult.ok();
		}

		if (page == null) {
			page = 1;
		}

		int pageSize = 6;

		PagedResult videosList = videoService.queryMyFollowVideos(userId, page, pageSize);

		return myJSONResult.ok(videosList);
	}

	/**
	 * @Description: 我收藏(点赞)过的视频列表
	 */
	@PostMapping("/showMyLike")
	public myJSONResult showMyLike(String userId, Integer page, Integer pageSize) throws Exception {

		if (StringUtils.isBlank(userId)) {
			return myJSONResult.ok();
		}

		if (page == null) {
			page = 1;
		}

		if (pageSize == null) {
			pageSize = 6;
		}

		PagedResult videosList = videoService.queryMyLikeVideos(userId, page, pageSize);

		return myJSONResult.ok(videosList);
	}
	
}
