package com.abigtomato.shop.api.ums.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "xc_user_role")
public class XcUserRole {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String roleId;
    private String creator;
    private Date createTime;
}
