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

    /**
     * 获取所有不重复的月份
     * @return 月份列表
     */
    @Select("SELECT DISTINCT month FROM curve_data WHERE month IS NOT NULL ORDER BY month DESC")
    List<String> getDistinctMonths();

    /**
     * 根据月份获取所有不重复的日期
     * @param month 月份
     * @return 日期列表
     */
    @Select("SELECT DISTINCT date FROM curve_data WHERE month = #{month} AND date IS NOT NULL ORDER BY date DESC")
    List<String> getDistinctDatesByMonth(String month);
}