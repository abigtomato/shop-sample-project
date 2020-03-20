package com.abigtomato.shop.core.bean;

import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.core.utils.SQLFilter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 查询参数
 *
 * @author Mark sunlightcs@gmail.com
 */
public class Query<T> {

    public IPage<T> getPage(QueryCondition params) {
        return this.getPage(params, null, false);
    }

    public IPage<T> getPage(QueryCondition params, String defaultOrderField, boolean isAsc) {
        // 分页参数
        long curPage = 1;
        long limit = 10;

        if (params.getPage() != null) {
            curPage = params.getPage();
        }
        if (params.getLimit() != null) {
            limit = params.getLimit();
        }

        // 分页对象
        Page<T> page = new Page<>(curPage, limit);

        // 分页参数
//        params.put(Constant.PAGE, page);

        // 排序字段
        // 防止SQL注入（因为sidx、order是通过拼接SQL实现排序的，会有SQL注入风险）
        String orderField = SQLFilter.sqlInject(params.getSidx());
        String order = params.getOrder();

        // 前端字段排序
        if (StrUtil.isNotEmpty(orderField) && StrUtil.isNotEmpty(order)) {
            if ("asc".equalsIgnoreCase(order)) {
                return page.addOrder(OrderItem.asc(orderField));
            } else {
                return page.addOrder(OrderItem.desc(orderField));
            }
        }

        // 默认排序
        if (isAsc) {
            page.addOrder(OrderItem.asc(orderField));
        } else {
            page.addOrder(OrderItem.desc(orderField));
        }

        return page;
    }
}
