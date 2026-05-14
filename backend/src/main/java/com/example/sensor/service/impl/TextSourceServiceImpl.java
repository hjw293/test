package com.example.sensor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sensor.entity.TextSource;
import com.example.sensor.mapper.TextSourceMapper;
import com.example.sensor.service.TextSourceService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文本资源 Service 实现类
 */
@Service
public class TextSourceServiceImpl extends ServiceImpl<TextSourceMapper, TextSource> implements TextSourceService {

    @Override
    public String getNameByNameId(Integer nameId) {
        if (nameId == null) {
            return null;
        }
        LambdaQueryWrapper<TextSource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextSource::getLangName, "中文");
        // type_id = name_id / 65536, sub_id = name_id % 65536
        queryWrapper.apply("type_id * 65536 + sub_id = {0}", nameId);
        List<TextSource> list = this.list(queryWrapper);
        if (list != null && !list.isEmpty()) {
            return list.get(0).getStringContent();
        }
        return null;
    }

    @Override
    public Map<Integer, String> getNamesByNameIds(List<Integer> nameIds) {
        Map<Integer, String> result = new HashMap<>();
        if (nameIds == null || nameIds.isEmpty()) {
            return result;
        }
        for (Integer nameId : nameIds) {
            String name = getNameByNameId(nameId);
            if (name != null) {
                result.put(nameId, name);
            }
        }
        return result;
    }
}
