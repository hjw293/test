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
     * 根据组名称ID和曲线名称ID获取数据（联表查询解决ID冲突）
     * @param groupNameId 组名称ID
     * @param nameId 曲线名称ID
     * @return 曲线数据列表
     */
    @Select("SELECT cd.* FROM curve_data cd " +
            "INNER JOIN log_curve_group lcg ON cd.name_id = lcg.curve_name_id " +
            "WHERE lcg.group_name_id = #{groupNameId} AND lcg.curve_name_id = CAST(#{nameId} AS UNSIGNED) " +
            "ORDER BY cd.timestamp ASC")
    List<CurveData> getByGroupNameIdAndNameId(Integer groupNameId, String nameId);

    /**
     * 根据多个组名称ID和曲线名称ID获取数据
     * @param groupNameIds 组名称ID列表
     * @param nameIds 曲线名称ID列表
     * @return 曲线数据列表
     */
    @Select("<script>" +
            "SELECT cd.* FROM curve_data cd " +
            "INNER JOIN log_curve_group lcg ON cd.name_id = lcg.curve_name_id " +
            "WHERE lcg.group_name_id IN <foreach collection='groupNameIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "AND lcg.curve_name_id IN <foreach collection='nameIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "ORDER BY cd.timestamp ASC" +
            "</script>")
    List<CurveData> getByGroupNameIdsAndNameIds(List<Integer> groupNameIds, List<String> nameIds);

    /**
     * 获取所有不重复的月份
     * @return 月份列表
     */
    @Select("SELECT DISTINCT month FROM curve_data WHERE month IS NOT NULL ORDER BY month DESC")
    List<String> getDistinctMonths();

    /**
     * 获取指定月份下的所有不重复日期
     * @param month 月份 (格式: yyyy-MM)
     * @return 日期列表 (格式: MM-dd)
     */
    @Select("SELECT DISTINCT DATE_FORMAT(date, '%m-%d') AS date FROM curve_data WHERE month = #{month} AND date IS NOT NULL AND date != '' ORDER BY date ASC")
    List<String> getDistinctDatesByMonth(String month);
}