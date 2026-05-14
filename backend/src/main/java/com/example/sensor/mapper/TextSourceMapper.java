package com.example.sensor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sensor.entity.TextSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文本资源 Mapper 接口
 */
@Mapper
public interface TextSourceMapper extends BaseMapper<TextSource> {

    /**
     * 根据 name_id 获取中文名称
     * name_id = type_id * 65536 + sub_id
     * @param nameId 曲线名称ID
     * @return 中文名称列表
     */
    @Select("SELECT * FROM text_source WHERE lang_name = '中文' AND (type_id * 65536 + sub_id) = #{nameId}")
    List<TextSource> findByNameId(Integer nameId);
}
