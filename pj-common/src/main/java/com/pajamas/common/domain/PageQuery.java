package com.pajamas.common.domain;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;

@Data
@ApiModel(description = "分页查询条件")
@Accessors(chain = true)
public class PageQuery {
    public static final Integer DEFAULT_PAGE_SIZE = 20;
    public static final Integer DEFAULT_PAGE_NUM = 1;
    @ApiModelProperty("页码")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = DEFAULT_PAGE_NUM;
    @ApiModelProperty("每页大小")
    @Min(value = 1, message = "每页查询数量不能小于1")
    private Integer pageSize = DEFAULT_PAGE_SIZE;
    @ApiModelProperty("是否升序")
    private Boolean isAsc = true;
    @ApiModelProperty("排序方式")
    private String sortBy;

    public int from(){
        return (pageNo - 1) * pageSize;
    }

    public <T> Page<T> toMpPage(OrderItem... orderItems) {
        Page<T> page = new Page<>(pageNo, pageSize);
        // 是否手动指定排序方式
        if (orderItems != null && orderItems.length > 0) {
            page.addOrder(orderItems);
            return page;
        }
        // 前端是否有排序字段
        if (StrUtil.isNotEmpty(sortBy)){
            page.addOrder(new OrderItem(sortBy, isAsc));
        }
        return page;
    }

    public <T> Page<T> toMpPage(String defaultSortBy, Boolean isAsc) {
        return toMpPage(new OrderItem(defaultSortBy, isAsc));
    }
    public <T> Page<T> toMpPageDefaultSortByCreateTimeDesc() {
        return toMpPage("create_time", false);
    }
    public <T> Page<T> toMpPageDefaultSortByUpdateTimeDesc() {
        return toMpPage("update_time", false);
    }
}
