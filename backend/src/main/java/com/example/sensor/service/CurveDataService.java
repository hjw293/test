package com.example.sensor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sensor.entity.CurveData;

import java.util.List;

/**
 * 曲线数据 Service 业务接口
 */
public interface CurveDataService extends IService<CurveData> {

    /**
     * 根据曲线名称ID获取曲线数据
     * @param nameId 曲线名称ID
     * @return 曲线数据列表
     */
    List<CurveData> getByNameId(String nameId);

    /**
     * 根据多个曲线名称ID获取数据
     * @param nameIds 曲线名称ID列表
     * @return 曲线数据列表
     */
    List<CurveData> getByNameIds(List<String> nameIds);

    /**
     * 根据组名称ID和曲线名称ID获取数据（联表查询解决ID冲突）
     * @param groupNameId 组名称ID
     * @param nameId 曲线名称ID
     * @return 曲线数据列表
     */
    List<CurveData> getByGroupNameIdAndNameId(Integer groupNameId, String nameId);

    /**
     * 根据多个组名称ID和曲线名称ID获取数据
     * @param groupNameIds 组名称ID列表
     * @param nameIds 曲线名称ID列表
     * @return 曲线数据列表
     */
    List<CurveData> getByGroupNameIdsAndNameIds(List<Integer> groupNameIds, List<String> nameIds);

    /**
     * 获取所有不重复的月份
     * @return 月份列表
     */
    List<String> getDistinctMonths();

    /**
     * 获取指定月份下的所有不重复日期
     * @param month 月份 (格式: yyyy-MM)
     * @return 日期列表 (格式: MM-dd)
     */
    List<String> getDistinctDatesByMonth(String month);

    /**
     * 根据曲线名称ID列表和月份获取数据
     * @param nameIds 曲线名称ID列表
     * @param month 月份
     * @return 曲线数据列表
     */
    List<CurveData> getByNameIdsAndMonth(List<String> nameIds, String month);
}