package com.example.sensor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sensor.entity.LogCurveGroup;
import com.example.sensor.entity.CurveData;
import com.example.sensor.mapper.LogCurveGroupMapper;
import com.example.sensor.service.LogCurveGroupService;
import com.example.sensor.service.CurveDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 日志曲线组 Service 业务实现
 */
@Service
public class LogCurveGroupServiceImpl extends ServiceImpl<LogCurveGroupMapper, LogCurveGroup> implements LogCurveGroupService {

    private static final Logger logger = LoggerFactory.getLogger(LogCurveGroupServiceImpl.class);

    @Autowired
    private CurveDataService curveDataService;

    @Override
    public IPage<LogCurveGroup> getLogCurveGroupPage(int pageNum, int pageSize,
                                                      Integer groupNameId, Integer curveType, Integer curveMode) {
        logger.debug("查询曲线组配置，页码: {}, 每页数量: {}, 组ID: {}, 类型: {}, 模式: {}",
                pageNum, pageSize, groupNameId, curveType, curveMode);

        Page<LogCurveGroup> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<LogCurveGroup> queryWrapper = new LambdaQueryWrapper<>();

        if (groupNameId != null) {
            queryWrapper.eq(LogCurveGroup::getGroupNameId, groupNameId);
        }

        if (curveType != null) {
            queryWrapper.eq(LogCurveGroup::getCurveType, curveType);
        }

        if (curveMode != null) {
            queryWrapper.eq(LogCurveGroup::getCurveMode, curveMode);
        }

        queryWrapper.orderByAsc(LogCurveGroup::getGroupNameId, LogCurveGroup::getId);

        IPage<LogCurveGroup> result = this.page(page, queryWrapper);

        logger.debug("查询完成，总记录数: {}", result.getTotal());

        return result;
    }

    @Override
    public List<Integer> getDistinctGroupNameIds() {
        LambdaQueryWrapper<LogCurveGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(LogCurveGroup::getGroupNameId);
        queryWrapper.groupBy(LogCurveGroup::getGroupNameId);
        queryWrapper.orderByAsc(LogCurveGroup::getGroupNameId);

        return this.list(queryWrapper).stream()
                .map(LogCurveGroup::getGroupNameId)
                .collect(Collectors.toList());
    }

    @Override
    public List<LogCurveGroup> getCurvesByGroupId(Integer groupNameId) {
        LambdaQueryWrapper<LogCurveGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogCurveGroup::getGroupNameId, groupNameId);
        queryWrapper.orderByAsc(LogCurveGroup::getId);
        return this.list(queryWrapper);
    }

    @Override
    public Map<Integer, List<LogCurveGroup>> getAllGrouped() {
        LambdaQueryWrapper<LogCurveGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(LogCurveGroup::getGroupNameId, LogCurveGroup::getId);
        return this.list(queryWrapper).stream()
                .collect(Collectors.groupingBy(LogCurveGroup::getGroupNameId));
    }

    @Override
    public Map<Integer, List<LogCurveGroup>> getGroupedByMonth(String month) {
        logger.info("getGroupedByMonth called with month: {}", month);
        if (month == null || month.isEmpty()) {
            logger.info("month is empty, calling getAllGrouped");
            return getAllGrouped();
        }
        // 获取该月份有数据的曲线名称ID
        List<String> months = curveDataService.getDistinctMonths();
        logger.info("distinct months from DB: {}", months);
        if (!months.contains(month)) {
            logger.info("month {} not in months list", month);
            return new HashMap<>();
        }
        // 获取所有曲线配置
        List<LogCurveGroup> allCurves = this.list();
        logger.info("total curves: {}", allCurves.size());
        if (allCurves.isEmpty()) {
            return new HashMap<>();
        }
        // 获取该月份所有曲线名称ID
        List<String> allNameIds = allCurves.stream()
                .map(c -> String.valueOf(c.getCurveNameId()))
                .distinct()
                .collect(Collectors.toList());
        logger.info("distinct nameIds count: {}", allNameIds.size());
        // 查询该月份有数据的曲线
        List<CurveData> monthData = curveDataService.getByNameIdsAndMonth(allNameIds, month);
        logger.info("monthData size: {}", monthData != null ? monthData.size() : "null");
        if (monthData == null || monthData.isEmpty()) {
            return new HashMap<>();
        }
        Set<String> nameIdsWithData = new HashSet<>();
        for (CurveData cd : monthData) {
            nameIdsWithData.add(cd.getNameId());
        }
        logger.info("nameIdsWithData count: {}", nameIdsWithData.size());
        // 筛选有数据的曲线
        Map<Integer, List<LogCurveGroup>> result = new LinkedHashMap<>();
        for (LogCurveGroup curve : allCurves) {
            if (nameIdsWithData.contains(String.valueOf(curve.getCurveNameId()))) {
                result.computeIfAbsent(curve.getGroupNameId(), k -> new ArrayList<>()).add(curve);
            }
        }
        logger.info("result grouped by groupNameId: {}", result.keySet());
        return result;
    }
}
