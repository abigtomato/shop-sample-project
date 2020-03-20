package com.abigtomato.shop.model.ums;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Data
@TableName(value = "xc_role")
public class XcRole {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String roleName;
    private String role_code;
    private String description;
    private String status;
    private Date create_time;
    private Date updateTime;
}
