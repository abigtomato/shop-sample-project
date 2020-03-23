package com.abigtomato.shop.api.ums.model.ext;

import com.abigtomato.shop.model.ums.Category;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryNode extends Category {

    List<CategoryNode> children;
}
