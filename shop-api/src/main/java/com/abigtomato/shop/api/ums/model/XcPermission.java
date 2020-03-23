package com.abigtomato.shop.api.ums.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "xc_permission")
public class XcPermission {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String role_id;
    private String menu_id;
    private Date create_time;
}
