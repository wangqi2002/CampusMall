package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.constants.TaskConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.combination.StoreCombination;
import com.zbkj.common.model.combination.StorePink;
import com.zbkj.common.model.finance.UserRecharge;
import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.wechat.WechatPayInfo;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.common.utils.DateUtil;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.common.utils.WxPayUtil;
import com.zbkj.common.vo.AttachVo;
import com.zbkj.common.vo.CallbackVo;
import com.zbkj.common.vo.MyRecord;
import com.zbkj.service.service.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.*;


/**
 * 订单支付回调 CallbackService 实现类
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
@Service
public class CallbackServiceImpl implements CallbackService {

    private static final Logger logger = LoggerFactory.getLogger(CallbackServiceImpl.class);

    @Autowired
    private RechargePayService rechargePayService;

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRechargeService userRechargeService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private StoreCombinationService storeCombinationService;

    @Autowired
    private StorePinkService storePinkService;

    @Autowired
    private WechatPayInfoService wechatPayInfoService;

    /**
     * 微信支付回调
     */
    @Override
    public String weChat(String xmlInfo) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        if(StrUtil.isBlank(xmlInfo)){
            sb.append("<return_code><![CDATA[FAIL]]></return_code>");
            sb.append("<return_msg><![CDATA[xmlInfo is blank]]></return_msg>");
            sb.append("</xml>");
            logger.error("wechat callback error : " + sb.toString());
            return sb.toString();
        }

        try{
            HashMap<String, Object> map = WxPayUtil.processResponseXml(xmlInfo);
            // 通信是否成功
            String returnCode = (String) map.get("return_code");
            if (!returnCode.equals(Constants.SUCCESS)) {
                sb.append("<return_code><![CDATA[SUCCESS]]></return_code>");
                sb.append("<return_msg><![CDATA[OK]]></return_msg>");
                sb.append("</xml>");
                logger.error("wechat callback error : wx pay return code is fail returnMsg : " + map.get("return_msg"));
                return sb.toString();
            }
            // 交易是否成功
            String resultCode = (String) map.get("result_code");
            if (!resultCode.equals(Constants.SUCCESS)) {
                sb.append("<return_code><![CDATA[SUCCESS]]></return_code>");
                sb.append("<return_msg><![CDATA[OK]]></return_msg>");
                sb.append("</xml>");
                logger.error("wechat callback error : wx pay result code is fail");
                return sb.toString();
            }

            //解析xml
            CallbackVo callbackVo = CrmebUtil.mapToObj(map, CallbackVo.class);
            AttachVo attachVo = JSONObject.toJavaObject(JSONObject.parseObject(callbackVo.getAttach()), AttachVo.class);

            //判断openid
            User user = userService.getById(attachVo.getUserId());
            if (ObjectUtil.isNull(user)) {
                //用户信息错误
                throw new CrmebException("用户信息错误！");
            }

            //根据类型判断是订单或者充值
            if (!Constants.SERVICE_PAY_TYPE_ORDER.equals(attachVo.getType()) && !Constants.SERVICE_PAY_TYPE_RECHARGE.equals(attachVo.getType())) {
                logger.error("wechat pay err : 未知的支付类型==》" + callbackVo.getOutTradeNo());
                throw new CrmebException("未知的支付类型！");
            }
            // 订单
            if (Constants.SERVICE_PAY_TYPE_ORDER.equals(attachVo.getType())) {
                StoreOrder orderParam = new StoreOrder();
                orderParam.setOutTradeNo(callbackVo.getOutTradeNo());
                orderParam.setUid(attachVo.getUserId());

                StoreOrder storeOrder = storeOrderService.getInfoByEntity(orderParam);
                if (ObjectUtil.isNull(storeOrder)) {
                    logger.error("wechat pay error : 订单信息不存在==》" + callbackVo.getOutTradeNo());
                    throw new CrmebException("wechat pay error : 订单信息不存在==》" + callbackVo.getOutTradeNo());
                }
                if (storeOrder.getPaid()) {
                    logger.error("wechat pay error : 订单已处理==》" + callbackVo.getOutTradeNo());
                    sb.append("<return_code><![CDATA[SUCCESS]]></return_code>");
                    sb.append("<return_msg><![CDATA[OK]]></return_msg>");
                    sb.append("</xml>");
                    return sb.toString();
                }
                WechatPayInfo wechatPayInfo = wechatPayInfoService.getByNo(storeOrder.getOutTradeNo());
                if (ObjectUtil.isNull(wechatPayInfo)) {
                    logger.error("wechat pay error : 微信订单信息不存在==》" + callbackVo.getOutTradeNo());
                    throw new CrmebException("wechat pay error : 微信订单信息不存在==》" + callbackVo.getOutTradeNo());
                }
                wechatPayInfo.setIsSubscribe(callbackVo.getIsSubscribe());
                wechatPayInfo.setBankType(callbackVo.getBankType());
                wechatPayInfo.setCashFee(callbackVo.getCashFee());
                wechatPayInfo.setCouponFee(callbackVo.getCouponFee());
                wechatPayInfo.setTransactionId(callbackVo.getTransactionId());
                wechatPayInfo.setTimeEnd(callbackVo.getTimeEnd());

                // 添加支付成功redis队列
                Boolean execute = transactionTemplate.execute(e -> {
                    storeOrder.setPaid(true);
                    storeOrder.setPayTime(DateUtil.nowDateTime());
                    storeOrderService.updateById(storeOrder);
                    if (storeOrder.getUseIntegral() > 0) {
                        userService.updateIntegral(user, storeOrder.getUseIntegral(), "sub");
                    }
                    wechatPayInfoService.updateById(wechatPayInfo);

                    // 处理拼团
                    if (storeOrder.getCombinationId() > 0) {
                        // 判断拼团团长是否存在
                        StorePink headPink = new StorePink();
                        Integer pinkId = storeOrder.getPinkId();
                        if (pinkId > 0) {
                            headPink = storePinkService.getById(pinkId);
                            if (ObjectUtil.isNull(headPink) || headPink.getIsRefund().equals(true) || headPink.getStatus() == 3) {
                                pinkId = 0;
                            }
                        }
                        StoreCombination storeCombination = storeCombinationService.getById(storeOrder.getCombinationId());
                        // 如果拼团人数已满，重新开团
                        if (pinkId > 0) {
                            Integer count = storePinkService.getCountByKid(pinkId);
                            if (count >= storeCombination.getPeople()) {
                                pinkId = 0;
                            }
                        }
                        // 生成拼团表数据
                        StorePink storePink = new StorePink();
                        storePink.setUid(user.getUid());
                        storePink.setAvatar(user.getAvatar());
                        storePink.setNickname(user.getNickname());
                        storePink.setOrderId(storeOrder.getOrderId());
                        storePink.setOrderIdKey(storeOrder.getId());
                        storePink.setTotalNum(storeOrder.getTotalNum());
                        storePink.setTotalPrice(storeOrder.getTotalPrice());
                        storePink.setCid(storeCombination.getId());
                        storePink.setPid(storeCombination.getProductId());
                        storePink.setPeople(storeCombination.getPeople());
                        storePink.setPrice(storeCombination.getPrice());
                        Integer effectiveTime = storeCombination.getEffectiveTime();// 有效小时数
                        DateTime dateTime = cn.hutool.core.date.DateUtil.date();
                        storePink.setAddTime(dateTime.getTime());
                        if (pinkId > 0) {
                            storePink.setStopTime(headPink.getStopTime());
                        } else {
                            DateTime hourTime = cn.hutool.core.date.DateUtil.offsetHour(dateTime, effectiveTime);
                            long stopTime =  hourTime.getTime();
                            if (stopTime > storeCombination.getStopTime()) {
                                stopTime = storeCombination.getStopTime();
                            }
                            storePink.setStopTime(stopTime);
                        }
                        storePink.setKId(pinkId);
                        storePink.setIsTpl(false);
                        storePink.setIsRefund(false);
                        storePink.setStatus(1);
                        storePinkService.save(storePink);
                        // 如果是开团，需要更新订单数据
                        storeOrder.setPinkId(storePink.getId());
                        storeOrderService.updateById(storeOrder);
                    }

                    return Boolean.TRUE;
                });
                if (!execute) {
                    logger.error("wechat pay error : 订单更新失败==》" + callbackVo.getOutTradeNo());
                    sb.append("<return_code><![CDATA[SUCCESS]]></return_code>");
                    sb.append("<return_msg><![CDATA[OK]]></return_msg>");
                    sb.append("</xml>");
                    return sb.toString();
                }
                redisUtil.lPush(TaskConstants.ORDER_TASK_PAY_SUCCESS_AFTER, storeOrder.getOrderId());
            }
            // 充值
            if (Constants.SERVICE_PAY_TYPE_RECHARGE.equals(attachVo.getType())) {
                UserRecharge userRecharge = new UserRecharge();
                userRecharge.setOrderId(callbackVo.getOutTradeNo());
                userRecharge.setUid(attachVo.getUserId());
                userRecharge = userRechargeService.getInfoByEntity(userRecharge);
                if(ObjectUtil.isNull(userRecharge)){
                    throw new CrmebException("没有找到订单信息");
                }
                if(userRecharge.getPaid()){
                    sb.append("<return_code><![CDATA[SUCCESS]]></return_code>");
                    sb.append("<return_msg><![CDATA[OK]]></return_msg>");
                    sb.append("</xml>");
                    return sb.toString();
                }
                // 支付成功处理
                Boolean rechargePayAfter = rechargePayService.paySuccess(userRecharge);
                if (!rechargePayAfter) {
                    logger.error("wechat pay error : 数据保存失败==》" + callbackVo.getOutTradeNo());
                    throw new CrmebException("wechat pay error : 数据保存失败==》" + callbackVo.getOutTradeNo());
                }
            }
            sb.append("<return_code><![CDATA[SUCCESS]]></return_code>");
            sb.append("<return_msg><![CDATA[OK]]></return_msg>");
        }catch (Exception e){
            sb.append("<return_code><![CDATA[FAIL]]></return_code>");
            sb.append("<return_msg><![CDATA[").append(e.getMessage()).append("]]></return_msg>");
            logger.error("wechat pay error : 业务异常==》" + e.getMessage());
        }
        sb.append("</xml>");
        logger.error("wechat callback response : " + sb.toString());
        return sb.toString();
    }

    /**
     * 微信退款回调
     * @param xmlInfo 微信回调json
     * @return MyRecord
     */
    @Override
    public String weChatRefund(String xmlInfo) {
        MyRecord notifyRecord = new MyRecord();
        MyRecord refundRecord = refundNotify(xmlInfo, notifyRecord);
        if ("fail".equals(refundRecord.getStr("status"))) {
            logger.error("微信退款回调失败==>" + refundRecord.getColumns() + ", rawData==>" + xmlInfo + ", data==>" + notifyRecord);
            return refundRecord.getStr("returnXml");
        }

        if (!refundRecord.getBoolean("isRefund")) {
            logger.error("微信退款回调失败==>" + refundRecord.getColumns() + ", rawData==>" + xmlInfo + ", data==>" + notifyRecord);
            return refundRecord.getStr("returnXml");
        }
        String outRefundNo = notifyRecord.getStr("out_refund_no");
        StoreOrder storeOrder = storeOrderService.getByOderId(outRefundNo);
        if (ObjectUtil.isNull(storeOrder)) {
            logger.error("微信退款订单查询失败==>" + refundRecord.getColumns() + ", rawData==>" + xmlInfo + ", data==>" + notifyRecord);
            return refundRecord.getStr("returnXml");
        }
        if (storeOrder.getRefundStatus() == 2) {
            logger.warn("微信退款订单已确认成功==>" + refundRecord.getColumns() + ", rawData==>" + xmlInfo + ", data==>" + notifyRecord);
            return refundRecord.getStr("returnXml");
        }
        storeOrder.setRefundStatus(2);
        boolean update = storeOrderService.updateById(storeOrder);
        if (update) {
            // 退款task
            redisUtil.lPush(Constants.ORDER_TASK_REDIS_KEY_AFTER_REFUND_BY_USER, storeOrder.getId());
        } else {
            logger.warn("微信退款订单更新失败==>" + refundRecord.getColumns() + ", rawData==>" + xmlInfo + ", data==>" + notifyRecord);
        }
        return refundRecord.getStr("returnXml");
    }

    /**
     * 支付订单回调通知
     * @return MyRecord
     */
    private MyRecord refundNotify(String xmlInfo, MyRecord notifyRecord) {
        MyRecord refundRecord = new MyRecord();
        refundRecord.set("status", "fail");
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        if(StrUtil.isBlank(xmlInfo)){
            sb.append("<return_code><![CDATA[FAIL]]></return_code>");
            sb.append("<return_msg><![CDATA[xmlInfo is blank]]></return_msg>");
            sb.append("</xml>");
            logger.error("wechat refund callback error : " + sb.toString());
            return refundRecord.set("returnXml", sb.toString()).set("errMsg", "xmlInfo is blank");
        }

        Map<String, String> respMap;
        try {
            respMap = WxPayUtil.xmlToMap(xmlInfo);
        } catch (Exception e) {
            sb.append("<return_code><![CDATA[FAIL]]></return_code>");
            sb.append("<return_msg><![CDATA[").append(e.getMessage()).append("]]></return_msg>");
            sb.append("</xml>");
            logger.error("wechat refund callback error : " + e.getMessage());
            return refundRecord.set("returnXml", sb.toString()).set("errMsg", e.getMessage());
        }

        notifyRecord.setColums(_strMap2ObjMap(respMap));
        // 这里的可以应该根据小程序还是公众号区分
        String return_code = respMap.get("return_code");
        if (return_code.equals(Constants.SUCCESS)) {
            String appid = respMap.get("appid");
            String signKey = getSignKey(appid);
            // 解码加密信息
            String reqInfo = respMap.get("req_info");
            System.out.println("encodeReqInfo==>" + reqInfo);
            try {
                String decodeInfo = decryptToStr(reqInfo, signKey);
                Map<String, String> infoMap = WxPayUtil.xmlToMap(decodeInfo);
                notifyRecord.setColums(_strMap2ObjMap(infoMap));

                String refund_status = infoMap.get("refund_status");
                refundRecord.set("isRefund", refund_status.equals(Constants.SUCCESS));
            } catch (Exception e) {
                refundRecord.set("isRefund", false);
                logger.error("微信退款回调异常，e==》" + e.getMessage());
            }
        } else {
            notifyRecord.set("return_msg", respMap.get("return_msg"));
            refundRecord.set("isRefund", false);
        }
        sb.append("<return_code><![CDATA[SUCCESS]]></return_code>");
        sb.append("<return_msg><![CDATA[OK]]></return_msg>");
        sb.append("</xml>");
        return refundRecord.set("returnXml", sb.toString()).set("status", "ok");
    }

    private String getSignKey(String appid) {
        String publicAppid = systemConfigService.getValueByKey(Constants.CONFIG_KEY_PAY_WE_CHAT_APP_ID);
        String miniAppid = systemConfigService.getValueByKey(Constants.CONFIG_KEY_PAY_ROUTINE_APP_ID);
        String appAppid = systemConfigService.getValueByKey(Constants.CONFIG_KEY_PAY_WE_CHAT_APP_APP_ID);
        String signKey = "";
        if (StrUtil.isBlank(publicAppid) && StrUtil.isBlank(miniAppid) && StrUtil.isBlank(appAppid)) {
            throw new CrmebException("pay_weixin_appid或pay_routine_appid不能都为空");
        }
        if (StrUtil.isNotBlank(publicAppid) && appid.equals(publicAppid)) {
            signKey = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_WE_CHAT_APP_KEY);
        }
        if (StrUtil.isNotBlank(miniAppid) && appid.equals(miniAppid)) {
            signKey = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_ROUTINE_APP_KEY);
        }
        if (StrUtil.isNotBlank(appAppid) && appid.equals(appAppid)) {
            signKey = systemConfigService.getValueByKeyException(Constants.CONFIG_KEY_PAY_WE_CHAT_APP_APP_KEY);
        }
        return signKey;
    }

    /**
     * java自带的是PKCS5Padding填充，不支持PKCS7Padding填充。
     * 通过BouncyCastle组件来让java里面支持PKCS7Padding填充
     * 在加解密之前加上：Security.addProvider(new BouncyCastleProvider())，
     * 并给Cipher.getInstance方法传入参数来指定Java使用这个库里的加/解密算法。
     */
    public static String decryptToStr(String reqInfo, String signKey) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
//        byte[] decodeReqInfo = Base64.decode(reqInfo);
        byte[] decodeReqInfo = base64DecodeJustForWxPay(reqInfo).getBytes(StandardCharsets.ISO_8859_1);
        SecretKeySpec key = new SecretKeySpec(SecureUtil.md5(signKey).toLowerCase().getBytes(), "AES");
        Cipher cipher;
        cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(decodeReqInfo), StandardCharsets.UTF_8);
    }

    private static final List<String> list = new ArrayList<>();
    static {
        list.add("total_fee");
        list.add("cash_fee");
        list.add("coupon_fee");
        list.add("coupon_count");
        list.add("refund_fee");
        list.add("settlement_refund_fee");
        list.add("settlement_total_fee");
        list.add("cash_refund_fee");
        list.add("coupon_refund_fee");
        list.add("coupon_refund_count");
    }

    private Map<String, Object> _strMap2ObjMap(Map<String, String> params) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (list.contains(entry.getKey())) {
                try {
                    map.put(entry.getKey(), Integer.parseInt(entry.getValue()));
                } catch (NumberFormatException e) {
                    map.put(entry.getKey(), 0);
                    logger.error("字段格式错误，key==》" + entry.getKey() + ", value==》" + entry.getValue());
                }
                continue;
            }

            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    /**
     * 仅仅为微信解析密文使用
     * @param source 待解析密文
     * @return 结果
     */
    public static String base64DecodeJustForWxPay(final String source) {
        String result = "";
        final Base64.Decoder decoder = Base64.getDecoder();
        try {
            result = new String(decoder.decode(source), "ISO-8859-1");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
