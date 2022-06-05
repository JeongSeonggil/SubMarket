package com.submarket.orderservice.mapper;

import com.submarket.orderservice.dto.OrderDto;

import java.util.List;

public interface IOrderMapper {

    int insertOrder(OrderDto orderDto, String colNm) throws Exception;

    List<OrderDto> findOrderInfoByUserId(String userId, String colNm) throws Exception;

    List<OrderDto> findOrderInfoBySellerId(String sellerId, String colNm) throws Exception;
}
