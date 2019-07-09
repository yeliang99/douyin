package com.jxw.mapper;


import com.jxw.pojo.Comments;
import com.jxw.pojo.vo.CommentsVO;
import com.jxw.utils.MyMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CommentsMapperCustom extends MyMapper<Comments> {
	
	public List<CommentsVO> queryComments(String videoId);
}