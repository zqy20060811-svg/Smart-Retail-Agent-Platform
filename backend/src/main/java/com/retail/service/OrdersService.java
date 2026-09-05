package com.retail.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.retail.common.result.PageResult;
import com.retail.dto.OrderCreateDTO;
import com.retail.dto.OrderPageQueryDTO;
import com.retail.entity.Orders;
import com.retail.vo.OrderVO;

public interface OrdersService extends IService<Orders> {

    /** 创建订单（骨架版），返回订单号 */
    String createOrder(Long userId, OrderCreateDTO createDTO);

    /** 分页查询；userId 为空表示管理端查全部 */
    PageResult<Orders> pageQuery(Long userId, OrderPageQueryDTO queryDTO);

    /** 订单详情（含明细）；userId 不为空时校验归属 */
    OrderVO getDetail(Long id, Long userId);

    /** 用户取消订单 */
    void cancel(Long userId, Long id);

    /** 管理端更新订单状态，并通过 WebSocket 推送用户 */
    void updateStatus(Long id, Integer status);
}
