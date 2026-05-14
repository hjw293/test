package com.example.sensor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sensor.entity.LogCurveGroup;

import java.util.List;
import java.util.Map;

/**
 * 日志曲线组 Service 业务接口
 */
public interface LogCurveGroupService extends IService<LogCurveGroup> {

    /**
     * 分页查询曲线组配置
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param groupNameId 组名称ID（可选）
     * @param curveType 曲线类型（可选）
     * @param curveMode 曲线模式（可选）
     * @return 分页结果
     */
    IPage<LogCurveGroup> getLogCurveGroupPage(int pageNum, int pageSize,
                                               Integer groupNameId, Integer curveType, Integer curveMode);

    /**
     * 获取所有不同的组名称ID
     * @return 组名称ID列表
     */
    List<Integer> getDistinctGroupNameIds();

    /**
     * 按组名称ID获取该组下所有曲线
     * @param groupNameId 组名称ID
     * @return 曲线列表
     */
    List<LogCurveGroup> getCurvesByGroupId(Integer groupNameId);

    /**
     * 获取所有数据，按组名称ID分组
     * @return Map<groupNameId, List<LogCurveGroup>>
     */
    Map<Integer, List<LogCurveGroup>> getAllGrouped();

    /**
     * 获取指定月份的数据，按组名称ID分组
     * @param month 月份 (格式: yyyy-MM)
     * @return Map<groupNameId, List<LogCurveGroup>>
     */
    Map<Integer, List<LogCurveGroup>> getGroupedByMonth(String month);

    /**
     * 获取指定月份和日期的数据，按组名称ID分组
     * @param month 月份 (格式: yyyy-MM)
     * @param date 日期 (格式: dd)
     * @return Map<groupNameId, List<LogCurveGroup>>
     */
    Map<Integer, List<LogCurveGroup>> getGroupedByMonthAndDate(String month, String date);
}
