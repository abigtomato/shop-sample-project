package com.abigtomato.shop.model.ums;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "category")
public class Category implements Serializable {

    private static final long serialVersionUID = -906357110051689484L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String name;
    private String label;
    private String parentid;
    private String isshow;
    private Integer orderby;
    private String isleaf;
}
