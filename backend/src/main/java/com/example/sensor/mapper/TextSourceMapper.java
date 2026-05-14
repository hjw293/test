package com.example.sensor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sensor.entity.TextSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文字源 Mapper 接口
 */
@Mapper
public interface TextSourceMapper extends BaseMapper<TextSource> {

    /**
     * 根据 name_id 获取文字内容
     * name_id = type_id * 65536 + sub_id
     * @param nameId 曲线名称ID
     * @return 文字内容
     */
    @Select("SELECT string_content FROM text_source WHERE type_id = #{nameId} / 65536 AND sub_id = #{nameId} % 65536 AND lang_name = '中文' LIMIT 1")
    String getNameByNameId(Integer nameId);

    /**
     * 根据 name_id 获取描述内容
     * @param nameId 曲线名称ID
     * @return 描述内容
     */
    @Select("SELECT desc_content FROM text_source WHERE type_id = #{nameId} / 65536 AND sub_id = #{nameId} % 65536 AND lang_name = '中文' LIMIT 1")
    String getDescByNameId(Integer nameId);

    /**
     * 根据 name_id 列表批量获取文字内容
     * @param nameIds 曲线名称ID列表
     * @return Map<nameId, stringContent>
     */
    @Select("<script>" +
            "SELECT type_id, sub_id, string_content FROM text_source " +
            "WHERE lang_name = '中文' AND (type_id * 65536 + sub_id) IN " +
            "<foreach item='item' collection='nameIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<TextSource> getNameIdsByNameIds(List<Integer> nameIds);
}