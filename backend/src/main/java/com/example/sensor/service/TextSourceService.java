package com.example.sensor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sensor.entity.TextSource;

import java.util.List;

/**
 * 文本资源 Service 接口
 */
public interface TextSourceService extends IService<TextSource> {

    /**
     * 根据 name_id 获取中文名称
     * @param nameId 曲线名称ID
     * @return 中文名称，如果没有则返回 null
     */
    String getNameByNameId(Integer nameId);

    /**
     * 根据多个 name_id 批量获取中文名称
     * @param nameIds 曲线名称ID列表
     * @return 名称映射 Map<nameId, name>
     */
    java.util.Map<Integer, String> getNamesByNameIds(List<Integer> nameIds);
}
