package com.example.sensor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sensor.entity.CurveData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 曲线数据 Mapper 接口
 */
@Mapper
public interface CurveDataMapper extends BaseMapper<CurveData> {

    /**
     * 根据曲线名称ID获取数据
     * @param nameId 曲线名称ID
     * @return 曲线数据列表
     */
    @Select("SELECT * FROM curve_data WHERE name_id = #{nameId} ORDER BY timestamp ASC")
    List<CurveData> getByNameId(String nameId);
}