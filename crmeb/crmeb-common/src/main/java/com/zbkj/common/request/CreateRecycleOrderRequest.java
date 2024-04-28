package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;

/**
 * 创建回收订单请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="CreateRecycleOrderRequest", description="创建回收订单请求对象")
public class CreateRecycleOrderRequest implements Serializable {

    private static final long serialVersionUID = -5222774384185333872L;

    @ApiModelProperty(value = "商品id")
    private Integer productId;

    @ApiModelProperty(value = "商品规格属性id（立即购买、活动购买必填）")
    private Integer attrValueId;

    @ApiModelProperty(value = "商品数量（立即购买、活动购买必填）")
    private Integer productNum;

    @ApiModelProperty(value = "真实名称")
    private String realName;

    @ApiModelProperty(value = "手机号码")
    private String phone;

    @ApiModelProperty(value = "收货地址")
    private String address;

    @ApiModelProperty(value = "商品详情")
    private StoreProductAddRequest storeProduct;

}
