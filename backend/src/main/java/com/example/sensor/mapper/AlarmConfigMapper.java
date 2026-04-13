package com.example.sensor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sensor.entity.AlarmConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 警报配置 Mapper 接口
 */
@Mapper
public interface AlarmConfigMapper extends BaseMapper<AlarmConfig> {

    /**
     * 分页查询警报配置
     * @param page 分页对象
     * @param alarmKey 警报ID（可选）
     * @return 分页结果
     */
    IPage<AlarmConfig> selectPageByAlarmKey(
            Page<AlarmConfig> page,
            @Param("alarmKey") String alarmKey
    );
}