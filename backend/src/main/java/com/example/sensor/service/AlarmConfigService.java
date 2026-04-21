package com.example.sensor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sensor.entity.AlarmConfig;

import java.util.List;

/**
 * 警报配置 Service 业务接口
 */
public interface AlarmConfigService extends IService<AlarmConfig> {

    /**
     * 分页查询警报配置
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param alarmKey 警报ID（可选）
     * @param responseReq 报警性质（可选）
     * @param machineAction 处理方式（可选）
     * @return 分页结果
     */
    IPage<AlarmConfig> getAlarmConfigPage(int pageNum, int pageSize, String alarmKey,
                                           String responseReq, String machineAction);

    /**
     * 获取所有不重复的报警性质
     * @return 报警性质列表
     */
    List<String> getAllResponseReq();

    /**
     * 获取所有不重复的处理方式
     * @return 处理方式列表
     */
    List<String> getAllMachineAction();
}