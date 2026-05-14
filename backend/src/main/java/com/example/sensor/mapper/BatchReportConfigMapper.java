package com.example.sensor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sensor.entity.BatchReportConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 批量报表配置 Mapper 接口
 */
public interface BatchReportConfigMapper extends BaseMapper<BatchReportConfig> {

    /**
     * 分页查询批量报表配置（带文本关联）
     * @param page 分页对象
     * @param reportCategory 报告类别（可选）
     * @param reportType 报告类型（可选）
     * @param nameId 名称ID（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT " +
            "brc.id, " +
            "brc.source_file as sourceFile, " +
            "brc.report_type as reportType, " +
            "brc.image_id as imageId, " +
            "brc.name_id as nameId, " +
            "brc.enable_name_id as enableNameId, " +
            "brc.ref_name_id as refNameId, " +
            "brc.report_category as reportCategory, " +
            "brc.enable_invalid_marker as enableInvalidMarker, " +
            "brc.invalid_value as invalidValue, " +
            "brc.step_param_id as stepParamId, " +
            "brc.created_at as createdAt, " +
            "ts.string_content as nameText " +
            "FROM batch_report_config brc " +
            "LEFT JOIN text_source ts ON brc.name_id = ts.sub_id " +
            "WHERE 1=1 " +
            "<if test='reportCategory != null'>" +
            "AND brc.report_category = #{reportCategory} " +
            "</if>" +
            "<if test='reportType != null'>" +
            "AND brc.report_type = #{reportType} " +
            "</if>" +
            "<if test='nameId != null'>" +
            "AND brc.name_id = #{nameId} " +
            "</if>" +
            "ORDER BY brc.id " +
            "</script>")
    IPage<BatchReportConfig> selectPageWithText(
            Page<BatchReportConfig> page,
            @Param("reportCategory") Integer reportCategory,
            @Param("reportType") Integer reportType,
            @Param("nameId") Integer nameId
    );
}