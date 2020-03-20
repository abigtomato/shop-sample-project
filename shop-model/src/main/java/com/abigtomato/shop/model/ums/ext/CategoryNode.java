package com.abigtomato.shop.model.ums.ext;

import com.abigtomato.shop.model.ums.Category;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryNode extends Category {

    List<CategoryNode> children;
}
