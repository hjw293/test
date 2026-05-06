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
}