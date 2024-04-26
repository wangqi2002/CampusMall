package com.zbkj.front.controller;


import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.ProductListRequest;
import com.zbkj.common.request.ProductRequest;
import com.zbkj.common.request.StoreProductAddRequest;
import com.zbkj.common.response.*;
import com.zbkj.common.vo.CategoryTreeVo;
import com.zbkj.front.service.ProductService;
import com.zbkj.service.service.StoreProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 -- 用户中心
 *  +----------------------------------------------------------------------
 *  | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2022 https://www.crmeb.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 *  +----------------------------------------------------------------------
 *  | Author: CRMEB Team <admin@crmeb.com>
 *  +----------------------------------------------------------------------
 */
@Slf4j
@RestController("ProductController")
@RequestMapping("api/front")
@Api(tags = "商品")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private StoreProductService storeProductService;

    /**
     * 新增商品
     * @param request 新增参数
     */
    @ApiOperation(value = "新增商品")
    @RequestMapping(value = "/product/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated StoreProductAddRequest request) {
        if (storeProductService.save(request)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 热门商品推荐
     */
    @ApiOperation(value = "热门商品推荐")
    @RequestMapping(value = "/product/hot", method = RequestMethod.GET)
    public CommonResult<CommonPage<IndexProductResponse>> getHotProductList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(productService.getHotProductList(pageParamRequest));
    }

    /**
     * 优选商品推荐
     */
    @ApiOperation(value = "优选商品推荐")
    @RequestMapping(value = "/product/good", method = RequestMethod.GET)
    public CommonResult<CommonPage<IndexProductResponse>> getGoodProductList() {
        return CommonResult.success(productService.getGoodProductList());
    }

    /**
     * 获取分类
     */
    @ApiOperation(value = "获取分类")
    @RequestMapping(value = "/category", method = RequestMethod.GET)
    public CommonResult<List<CategoryTreeVo>> getCategory() {
        return CommonResult.success(productService.getCategory());
    }

    /**
     * 查询分类表信息
     */
    @ApiOperation(value = "获取tree结构的列表")
    @RequestMapping(value = "/category/list/tree", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name="type", value="类型ID | 类型，1 产品分类，2 附件分类，3 文章分类， 4 设置分类， 5 菜单分类， 6 配置分类， 7 秒杀配置", example = "1"),
            @ApiImplicitParam(name="status", value="-1=全部，0=未生效，1=已生效", example = "1"),
            @ApiImplicitParam(name="name", value="模糊搜索", example = "电视")
    })
    public CommonResult<List<CategoryTreeVo>> getListTree(@RequestParam(name = "type") Integer type,
                                                          @RequestParam(name = "status") Integer status,
                                                          @RequestParam(name = "name", required = false) String name) {
        List<CategoryTreeVo> listTree = productService.getListTree(type,status,name);
        return CommonResult.success(listTree);
    }

    /**
     * 商品列表
     */
    @ApiOperation(value = "商品列表")
    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public CommonResult<CommonPage<IndexProductResponse>> getList(@Validated ProductRequest request, @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(productService.getList(request, pageParamRequest));
    }

    /**
     * 商品详情
     */
    @ApiOperation(value = "商品详情")
    @RequestMapping(value = "/product/detail/{id}", method = RequestMethod.GET)
    @ApiImplicitParam(name = "type", value = "normal-正常，video-视频")
    public CommonResult<ProductDetailResponse> getDetail(@PathVariable Integer id, @RequestParam(value = "type", defaultValue = "normal") String type) {
        return CommonResult.success(productService.getDetail(id, type));
    }

    /**
     * 商品评论列表
     */
    @ApiOperation(value = "商品评论列表")
    @RequestMapping(value = "/reply/list/{id}", method = RequestMethod.GET)
    @ApiImplicitParam(name = "type", value = "评价等级|0=全部,1=好评,2=中评,3=差评", allowableValues = "range[0,1,2,3]")
    public CommonResult<CommonPage<ProductReplyResponse>> getReplyList(@PathVariable Integer id,
                                                                       @RequestParam(value = "type") Integer type, @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(productService.getReplyList(id, type, pageParamRequest)));
    }

    /**
     * 商品评论数量
     */
    @ApiOperation(value = "商品评论数量")
    @RequestMapping(value = "/reply/config/{id}", method = RequestMethod.GET)
    public CommonResult<StoreProductReplayCountResponse> getReplyCount(@PathVariable Integer id) {
        return CommonResult.success(productService.getReplyCount(id));
    }

    /**
     * 商品详情评论
     */
    @ApiOperation(value = "商品详情评论")
    @RequestMapping(value = "/reply/product/{id}", method = RequestMethod.GET)
    public CommonResult<ProductDetailReplyResponse> getProductReply(@PathVariable Integer id) {
        return CommonResult.success(productService.getProductReply(id));
    }

    /**
     * 商品列表
     */
    @ApiOperation(value = "商品列表(个别分类模型使用)")
    @RequestMapping(value = "/product/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<IndexProductResponse>> getProductList(@Validated ProductListRequest request, @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(productService.getCategoryProductList(request, pageParamRequest));
    }

    /**
     * 商品规格详情
     */
    @ApiOperation(value = "商品规格详情")
    @RequestMapping(value = "/product/sku/detail/{id}", method = RequestMethod.GET)
    public CommonResult<ProductDetailResponse> getSkuDetail(@PathVariable Integer id) {
        return CommonResult.success(productService.getSkuDetail(id));
    }

    /**
     * 商品排行榜
     */
    @ApiOperation(value = "商品排行榜")
    @RequestMapping(value = "/product/leaderboard", method = RequestMethod.GET)
    public CommonResult<List<StoreProduct>> getLeaderboard() {
        return CommonResult.success(productService.getLeaderboard());
    }
}



