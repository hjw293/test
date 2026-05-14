package com.example.sensor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本资源实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("text_source")
public class TextSource {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 语言名称，如 "中文" */
    private String langName;

    /** 类型ID */
    private Integer typeId;

    /** 子ID */
    private Integer subId;

    /** 名称内容 */
    private String stringContent;
}
