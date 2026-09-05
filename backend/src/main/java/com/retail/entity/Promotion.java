package com.retail.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 优惠活动（供 AI「优惠查询」工具检索）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promotion")
public class Promotion extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** 类型：1 满减 2 折扣 3 赠品 */
    private Integer type;

    /** 规则描述，如「满20减3」「第二杯半价」 */
    private String content;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 状态：1 进行中 0 停用 */
    private Integer status;
}
