package com.example.sensor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sensor.entity.AlarmConfig;
import com.example.sensor.mapper.AlarmConfigMapper;
import com.example.sensor.service.AlarmConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 警报配置 Service 业务实现
 */
@Service
public class AlarmConfigServiceImpl extends ServiceImpl<AlarmConfigMapper, AlarmConfig> implements AlarmConfigService {

    private static final Logger logger = LoggerFactory.getLogger(AlarmConfigServiceImpl.class);

    /**
     * 分页查询警报配置
     */
    @Override
    public IPage<AlarmConfig> getAlarmConfigPage(int pageNum, int pageSize, String alarmKey,
                                                   String responseReq, String machineAction) {
        logger.debug("查询警报配置，页码: {}, 每页数量: {}, 警报ID: {}, 报警性质: {}, 处理方式: {}",
                pageNum, pageSize, alarmKey, responseReq, machineAction);

        // 创建分页对象
        Page<AlarmConfig> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        LambdaQueryWrapper<AlarmConfig> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询 language_name 为"中文"的记录
        queryWrapper.eq(AlarmConfig::getLanguageName, "中文");

        // 如果提供了 alarmKey，则添加查询条件
        if (alarmKey != null && !alarmKey.trim().isEmpty()) {
            queryWrapper.like(AlarmConfig::getAlarmKey, alarmKey);
        }

        // 如果提供了 responseReq（报警性质），则添加查询条件
        if (responseReq != null && !responseReq.trim().isEmpty()) {
            queryWrapper.eq(AlarmConfig::getResponseReq, responseReq);
        }

        // 如果提供了 machineAction（处理方式），则添加查询条件
        if (machineAction != null && !machineAction.trim().isEmpty()) {
            queryWrapper.eq(AlarmConfig::getMachineAction, machineAction);
        }

        // 按 alarmKey 排序
        queryWrapper.orderByAsc(AlarmConfig::getAlarmKey);

        // 执行分页查询
        IPage<AlarmConfig> result = this.page(page, queryWrapper);

        logger.debug("查询完成，总记录数: {}", result.getTotal());

        return result;
    }

    /**
     * 获取所有不重复的报警性质
     */
    @Override
    public List<String> getAllResponseReq() {
        LambdaQueryWrapper<AlarmConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlarmConfig::getLanguageName, "中文");
        queryWrapper.select(AlarmConfig::getResponseReq);
        queryWrapper.isNotNull(AlarmConfig::getResponseReq);

        List<AlarmConfig> list = this.list(queryWrapper);
        return list.stream()
                .map(AlarmConfig::getResponseReq)
                .filter(req -> req != null && !req.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获取所有不重复的处理方式
     */
    @Override
    public List<String> getAllMachineAction() {
        LambdaQueryWrapper<AlarmConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlarmConfig::getLanguageName, "中文");
        queryWrapper.select(AlarmConfig::getMachineAction);
        queryWrapper.isNotNull(AlarmConfig::getMachineAction);

        List<AlarmConfig> list = this.list(queryWrapper);
        return list.stream()
                .map(AlarmConfig::getMachineAction)
                .filter(action -> action != null && !action.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}