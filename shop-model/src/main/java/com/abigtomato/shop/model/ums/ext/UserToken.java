package com.abigtomato.shop.model.ums.ext;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class UserToken{

    String userId;  // 用户id
    String utype;   // 用户类型
    String companyId;   // 用户所属企业信息
}
