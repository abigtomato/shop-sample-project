package com.abigtomato.shop.api.ums;

import com.abigtomato.shop.api.ums.entity.MemberEntity;
import com.abigtomato.shop.api.ums.entity.MemberReceiveAddressEntity;
import com.abigtomato.shop.core.bean.Resp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface UmsApi {

    @GetMapping("ums/member/info/{id}")
    Resp<MemberEntity> queryMemberById(@PathVariable("id") String id);

    @GetMapping("ums/member/query")
    Resp<MemberEntity> queryUser(@RequestParam("username") String username, @RequestParam("password") String password);

    @GetMapping("ums/memberreceiveaddress/{userId}")
    Resp<List<MemberReceiveAddressEntity>> queryAddressesByUserId(@PathVariable("userId") String userId);
}
