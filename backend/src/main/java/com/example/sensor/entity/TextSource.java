package com.example.sensor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文字源实体类
 * name_id = type_id * 65536 + sub_id
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("text_source")
public class TextSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String creatorId;

    private String versionId;

    private String sourceFile;

    private Integer langUniqueId;

    private String langName;

    /** name_id 的类型编号 */
    private Integer typeId;

    /** name_id 的子编号 */
    private Integer subId;

    /** 文字内容 */
    private String stringContent;

    /** 描述内容 */
    private String descContent;
}