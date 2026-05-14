package com.example.sensor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sensor.entity.TextSource;

import java.util.List;
import java.util.Map;

/**
 * 文字源 Service 业务接口
 */
public interface TextSourceService extends IService<TextSource> {

    /**
     * 根据 name_id 获取文字内容
     * @param nameId 曲线名称ID
     * @return 文字内容
     */
    String getNameByNameId(Integer nameId);

    /**
     * 根据 name_id 获取描述内容
     * @param nameId 曲线名称ID
     * @return 描述内容
     */
    String getDescByNameId(Integer nameId);

    /**
     * 批量获取 name_id 对应的文字内容
     * @param nameIds 曲线名称ID列表
     * @return Map<nameId, stringContent>
     */
    Map<Integer, String> getNamesByNameIds(List<Integer> nameIds);
}