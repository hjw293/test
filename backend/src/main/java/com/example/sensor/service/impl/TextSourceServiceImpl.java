package com.example.sensor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sensor.entity.TextSource;
import com.example.sensor.mapper.TextSourceMapper;
import com.example.sensor.service.TextSourceService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文字源 Service 实现类
 */
@Service
public class TextSourceServiceImpl extends ServiceImpl<TextSourceMapper, TextSource> implements TextSourceService {

    @Override
    public String getNameByNameId(Integer nameId) {
        return baseMapper.getNameByNameId(nameId);
    }

    @Override
    public String getDescByNameId(Integer nameId) {
        return baseMapper.getDescByNameId(nameId);
    }

    @Override
    public Map<Integer, String> getNamesByNameIds(List<Integer> nameIds) {
        Map<Integer, String> result = new HashMap<>();
        if (nameIds == null || nameIds.isEmpty()) {
            return result;
        }
        List<TextSource> list = baseMapper.getNameIdsByNameIds(nameIds);
        for (TextSource ts : list) {
            if (ts != null && ts.getStringContent() != null) {
                int nameId = ts.getTypeId() * 65536 + ts.getSubId();
                result.put(nameId, ts.getStringContent());
            }
        }
        return result;
    }
}