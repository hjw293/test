package com.example.sensor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sensor.entity.CurveData;
import com.example.sensor.mapper.CurveDataMapper;
import com.example.sensor.service.CurveDataService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 曲线数据 Service 实现类
 */
@Service
public class CurveDataServiceImpl extends ServiceImpl<CurveDataMapper, CurveData> implements CurveDataService {

    @Override
    public List<CurveData> getByNameId(String nameId) {
        return baseMapper.getByNameId(nameId);
    }

    @Override
    public List<CurveData> getByNameIds(List<String> nameIds) {
        if (nameIds == null || nameIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<CurveData> result = new ArrayList<>();
        for (String nameId : nameIds) {
            result.addAll(baseMapper.getByNameId(nameId));
        }
        return result;
    }
}