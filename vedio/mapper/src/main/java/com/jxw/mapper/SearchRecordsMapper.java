package com.jxw.mapper;

import com.jxw.pojo.SearchRecords;
import com.jxw.utils.MyMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchRecordsMapper extends MyMapper<SearchRecords> {
    public List<String> getHotwords();
}