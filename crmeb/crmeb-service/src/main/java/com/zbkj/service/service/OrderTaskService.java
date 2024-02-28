package com.zbkj.service.service;


/**
 * 订单任务服务 StoreOrderService 接口
 * +----------------------------------------------------------------------
 * | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2022 https://www.crmeb.com All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 * +----------------------------------------------------------------------
 * | Author: CRMEB Team <admin@crmeb.com>
 * +----------------------------------------------------------------------
 */
 public interface OrderTaskService{

     void cancelByUser();

     void refundApply();

     void complete();

    void orderPaySuccessAfter();

    /**
     * 自动取消未支付订单
     */
    void autoCancel();

    /**
     * 订单收货
     */
    void orderReceiving();

    /**
     * 订单自动完成
     */
    void autoComplete();
}
