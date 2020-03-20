package com.abigtomato.shop.model.ums;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Data
@TableName(value = "xc_company_user")
public class XcCompanyUser implements Serializable {

    private static final long serialVersionUID = -916357110051689786L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String companyId;
    private String userId;
}