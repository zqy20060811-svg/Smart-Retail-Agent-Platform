package com.retail.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 套餐-商品关联明细
 */
@Data
@TableName("setmeal_item")
public class SetmealItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long setmealId;

    private Long productId;

    /** 该商品在套餐中的份数 */
    private Integer copies;
}
