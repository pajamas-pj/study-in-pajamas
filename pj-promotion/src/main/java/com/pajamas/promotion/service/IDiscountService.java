package com.pajamas.promotion.service;

import com.pajamas.api.dto.promotion.CouponDiscountDTO;
import com.pajamas.api.dto.promotion.OrderCouponDTO;
import com.pajamas.api.dto.promotion.OrderCourseDTO;

import java.util.List;

public interface IDiscountService {
    List<CouponDiscountDTO> findDiscountSolution(List<OrderCourseDTO> orderCourses);

    CouponDiscountDTO queryDiscountDetailByOrder(OrderCouponDTO orderCouponDTO);
}
