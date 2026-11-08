package com.houjiang.commons.annnotation;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.sensitive.BaseSensitiveConfigVO;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Amos
 */
@Component
@Aspect
public class SensitiveCheckAspect {

    @Value("${domainName}")
    private String domainName;

    @Around("@annotation(sensitiveCheck)")
    public Object around(ProceedingJoinPoint joinPoint, SensitiveCheck sensitiveCheck) throws Throwable {
        //从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取切入点所在的方法
        Method method = signature.getMethod();
        //获取请求的方法名
        String methodName = method.getName();
        //获取请求的类名
        String className = joinPoint.getTarget().getClass().getName();
        //拼接方法的类路径
        String methodUrl = className + "." + methodName;


        // 获取接口中的请求参数
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        request.setCharacterEncoding("utf8");
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }


        //请求参数
        String requestBody = sb.toString();
        Map<String, Object> map = new HashMap<>();
        List<Map<String,Object>> list = new ArrayList<>();
        String token = null;
        if(requestBody.startsWith("{")){
            map = JSON.parseObject(requestBody, Map.class);
            if(map.get("token") != null){
                token = map.get("token").toString();
            }
        }else{
            list = JsonUtils.string2Obj(requestBody, List.class, Map.class);
        }



        //获取需要检查敏感字 的字段
        String[] field = sensitiveCheck.field();

//        //根据敏感词规则对不同场景下接口请求参数检查是否包含敏感词
        ResponseResult methodPathSensitive = getMethodPathSensitive(methodUrl, map, field,list,token);
        if (methodPathSensitive.getCode() == BizCodeEnume.EXIST_SENSITIVE.getCode()) {
            return methodPathSensitive.setMessage("文本中所含敏感词有："+ methodPathSensitive.getData());
        }

        return joinPoint.proceed();
    }

    /** 根据敏感词规则对不同场景下接口请求参数检查是否包含敏感词 */
    public ResponseResult getMethodPathSensitive(String methodUrl, Map<String, Object> map, String[] field, List<Map<String, Object>> list,String token) {
        ResponseResult result = null;
        //从redis中读取敏感词配置
        String s = RedisUtil.get(HouConfig.HOU_SENSITIVE_CONFIG);
        BaseSensitiveConfigVO baseSensitiveVO = JSON.parseObject(s, BaseSensitiveConfigVO.class);
        //如果关闭敏感词直接跳出
        if (null == baseSensitiveVO || CommonType.commonStatus.NO.getCode().equals(baseSensitiveVO.getSensitiveStart())) {
            return ResponseResult.success();
        }
        switch (methodUrl) {
            //pc修改用户昵称
            case "com.houjiang.userms.controller.UserInfoController.updateUserInfo":
                //会员信息 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getMemberInformationStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //h5修改用户昵称
            case "com.houjiang.userms.controller.UserInfoDetailController.updateUserNickname":
                //会员信息 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getMemberInformationStart() )) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //用户新增收货地址
            case "com.houjiang.userms.controller.UserAddressController.add":
                //会员信息 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getMemberInformationStart() )) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //用户更新收货地址
            case "com.houjiang.userms.controller.UserAddressController.update":
                //会员信息 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getMemberInformationStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //用户发布评论
            case "com.houjiang.order.controller.OrderBaseController.addComments":
                //用户发布评论 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getProductEvaluationStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //添加发票模版信息
            case "com.houjiang.order.controller.OrderInvoiceTemplateInfoController.insertInvoiceTmp":
                //添加发票模版信息 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getMemberInformationStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //更新发票模版信息
            case "com.houjiang.order.controller.OrderInvoiceTemplateInfoController.updateInvoiceTmp":
                //更新发票模版信息 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getMemberInformationStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //插入发票地址模版信息
            case "com.houjiang.order.controller.OrderInvoiceAddressController.add":
                //插入发票地址模版信息 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getMemberInformationStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //商家或供应商入驻
            case "com.houjiang.business.controller.ShopJoinBaseController.insertShopJoinBase":
            case "com.houjiang.supplier.controller.SupplierShopJoinBaseController.insertShopJoinBase":
                //商家 供应商信息 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getSupplierInformationStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //商品发布
            case "com.houjiang.goods.controller.GoodsBaseController.newAddGoods":
                //供应商商品发布
            case "com.houjiang.supplier.controller.SupplierCommonBaseController.newAddGoods":
                //商品修改
            case "com.houjiang.supplier.controller.SupplierCommonBaseController.updateGoods":
                //产品信息 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getProductInformationStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //商品修改
            case "com.houjiang.goods.controller.GoodsBaseController.updateGoods":
                //产品信息 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getProductInformationStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //商品分类添加
            case "com.houjiang.goods.controller.GoodsCatController.addGoodsCat":
                //产品信息 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getProductInformationStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //平台添加红包
            case "com.houjiang.activity.controller.ActivityRedpacketTemplateController.insertRedpacketTemp":
                //促销活动 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getSalesPromotionStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //平台修改红包
            case "com.houjiang.activity.controller.ActivityRedpacketTemplateController.updateRedpacketTemp":
                //促销活动 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getSalesPromotionStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //商家创建优惠券
            case "com.houjiang.activity.controller.ActivityVoucherTemplateController.insertVoucherTemp":
                //促销活动 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getSalesPromotionStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //商家创建限时折扣
            case "com.houjiang.activity.controller.ActivityDiscountBaseController.addDiscountBase":
                //促销活动 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getSalesPromotionStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //商家创建限时秒杀
            case "com.houjiang.activity.controller.ActivitySeckillBaseController.addShopSeckillBase":
                //促销活动 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getSalesPromotionStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //商家创建拼团
            case "com.houjiang.activity.controller.ActivityPiecingTogetherController.addPinTuanBase":
                //促销活动 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getSalesPromotionStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //商家创建满减赠折
            case "com.houjiang.activity.controller.ActivityReductionPolicyGoodsController.addManjianzengzheBase":
                //促销活动 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getSalesPromotionStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //平台pc首页装修
            case "com.houjiang.base.controller.BasePcIndexConfigController.UPdatePcIndexConfig":
                //装修模块 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getDecorationModuleStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //平台pc首页楼层装修
            case "com.houjiang.base.controller.BaseFloorAdpageController.updateAdpageFloor":
                //装修模块 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getDecorationModuleStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //移动端首页装修
            case "com.houjiang.base.controller.BaseModularizationdController.ModularizationdAddOrUPdate":
                //装修模块 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getDecorationModuleStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //移动端店铺首页装修
            case "com.houjiang.base.controller.BaseShopModularizationdController.ModularizationdAddOrUPdate":
                //装修模块 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getDecorationModuleStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            // 新增退款/退货理由
            case "com.houjiang.order.controller.OrderCancelReasonController.addCancelreason":
                //原因管理 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getCauseManagementStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //更新退款/退货理由
            case "com.houjiang.order.controller.OrderCancelReasonController.updateCancelreason":
                //原因管理 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getCauseManagementStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //咨询分类修改/添加
            case "com.houjiang.base.controller.BaseInformationClassifyController.addGoodsCat":
                //咨询修改 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getArticleManagementStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
            //咨询修改/添加
            case "com.houjiang.base.controller.BaseInformationInfoController.add":
                //咨询修改 是否开启敏感词 1 是 0 否
                if (CommonType.commonStatus.YES.getCode().equals(baseSensitiveVO.getArticleManagementStart())) {
                    //检查是否包含敏感词
                    result = checkSensitive(field, map,token);
                }
                break;
        }
        return result;
    }


    /**
     * 检查是否包含敏感词
     * 1.敏感词在一级  如{key:value}                                               @SensitiveCheck(field = {"key"})
     * 2.敏感词在一级  如{key:[value,value,value]                                  @SensitiveCheck(field = {"key"})

     * 3.敏感词在二级  如{key1:{key2:value,kwy3:[value,value]}}                    @SensitiveCheck(field = {"key1.key2","key1.key3"})
     * 4.敏感词在二级  如{key1:[{key2:value,kwy3:[value,value]}]}                  @SensitiveCheck(field = {"key1.key2","key1.key3"})

     * 5.敏感词在三级  如{key1:{key2:{key3:value}}}                                @SensitiveCheck(field = {"key1.key2.key3"})
     * 6.敏感词在三级  如{key1:{key2:{key3:[value,value]}}}                        @SensitiveCheck(field = {"key1.key2.key3"})
     * 7.敏感词在三级  如{key1:{key2:[{key3:[value,value]}]}}                      @SensitiveCheck(field = {"key1.key2.key3"})
     * 8.敏感词在三级  如{key1:[{key2:{key3:[value,value],key4:value}}]}           @SensitiveCheck(field = {"key1.key2.key3","key1.key2.key4"})
     */

    public ResponseResult checkSensitive(String[] fields, Map<String, Object> map,String token) {
        List<String> sensitiveList = new ArrayList<>();
        //获取要校验敏感词的字段
        for (String field : fields) {
            //是否只有一级
                if (field.contains(".")) {
                String[] splits = field.split("\\.");
                if (null == map.get(splits[0])) {
                    continue;
                }
                //判断一级是否是数组
                String str0 = map.get(splits[0]).toString();
                if(str0.startsWith("[")){
                    List<Map<String,Object>> stringObjectMapList = (List<Map<String,Object>>) map.get(splits[0]);
                    for (Map<String, Object> stringObjectMap : stringObjectMapList) {
                        if(null == stringObjectMap.get(splits[1])){
                            continue;
                        }
                        String str = stringObjectMap.get(splits[1]).toString();
                        //判断二级是否是数组
                        if(str.startsWith("[")){
                            // 进入这里 证明存在三级 数组包对象的
                            //判断二级数组包含的是对象 还是基本数据类型
                            if(str.startsWith("[{")){
                                List<Map<String,Object>> stringObjectMap1 = (List<Map<String,Object>>) stringObjectMap.get(splits[1]);
                                for (Map<String, Object> objectMap : stringObjectMap1) {
                                    if(null == objectMap.get(splits[2])){
                                        continue;
                                    }
                                    //证明存在三级  三级只判断  基本数据类型的数组 暂不处理    三级是对象 三级数组包对象
                                    String str2 = objectMap.get(splits[2]).toString();
                                    //判断三级是否是数组
                                    if(str2.startsWith("[")){
                                        //三级是基本数量类型数组
                                        List<String> list = (List<String>)  objectMap.get(splits[2]);
                                        //校验敏感词 list
                                        List<String> sensitiveStrList = checkSensitiveStrList(list);
                                        sensitiveList.addAll(sensitiveStrList);
                                    }else{
                                        //二级是对象
                                        //校验敏感词
                                        List<String> list = checkSensitiveStr(str2,token);
                                        sensitiveList.addAll(list);
                                    }
                                }
                            }else{
                                // 包含的是基本数据类型
                                List<String> list = (List<String>) stringObjectMap.get(splits[1]);
                                //校验敏感词 list
                                List<String> sensitiveStrList = checkSensitiveStrList(list);
                                sensitiveList.addAll(sensitiveStrList);
                            }
                        }else{
                            if(splits.length == 3){
                                Map<String,Object> stringObjectMap1 = (Map<String,Object>) stringObjectMap.get(splits[1]);
                                if(null == stringObjectMap1.get(splits[2])){
                                    continue;
                                }
                                //证明存在三级  三级只判断  基本数据类型的数组 暂不处理    三级是对象 三级数组包对象
                                String str2 = stringObjectMap1.get(splits[2]).toString();
                                //判断三级是否是数组
                                if(str2.startsWith("[")){
                                    //三级是基本数量类型数组
                                    List<String> list = (List<String>)  stringObjectMap1.get(splits[2]);
                                    //校验敏感词 list
                                    List<String> sensitiveStrList = checkSensitiveStrList(list);
                                    sensitiveList.addAll(sensitiveStrList);
                                }else{
                                    //二级是对象
                                    //校验敏感词
                                    List<String> list = checkSensitiveStr(str2,token);
                                    sensitiveList.addAll(list);
                                }
                            }else{
                                //二级是对象
                                //校验敏感词
                                List<String> list = checkSensitiveStr(str,token);
                                sensitiveList.addAll(list);
                            }
                        }
                    }
                }else{
                    Map<String,Object> stringObjectMap = (Map<String,Object>) map.get(splits[0]);
                    if(null == stringObjectMap.get(splits[1])){
                        continue;
                    }
                    String str = stringObjectMap.get(splits[1]).toString();
                    //判断二级是否是数组
                    if(str.startsWith("[")){
                        // 进入这里 证明存在三级 数组包对象的
                        //判断二级数组包含的是对象 还是基本数据类型
                        if(str.startsWith("[{")){
                            List<Map<String,Object>> stringObjectMap1 = (List<Map<String,Object>>) stringObjectMap.get(splits[1]);
                            for (Map<String, Object> objectMap : stringObjectMap1) {
                                if(null == objectMap.get(splits[2])){
                                    continue;
                                }
                                //证明存在三级  三级只判断  基本数据类型的数组 暂不处理    三级是对象 三级数组包对象
                                String str2 = objectMap.get(splits[2]).toString();
                                //判断三级是否是数组
                                if(str2.startsWith("[")){
                                    //三级是基本数量类型数组
                                    List<String> list = (List<String>)  objectMap.get(splits[2]);
                                    //校验敏感词 list
                                    List<String> sensitiveStrList = checkSensitiveStrList(list);
                                    sensitiveList.addAll(sensitiveStrList);
                                }
                            }
                        }else{
                            // 包含的是基本数据类型
                            List<String> list = (List<String>) stringObjectMap.get(splits[1]);
                            //校验敏感词 list
                            List<String> sensitiveStrList = checkSensitiveStrList(list);
                            sensitiveList.addAll(sensitiveStrList);
                        }
                    }else{
                        if(splits.length == 3){
                            Map<String,Object> stringObjectMap1 = (Map<String,Object>) stringObjectMap.get(splits[1]);
                            if(null == stringObjectMap1.get(splits[2])){
                                continue;
                            }
                            //证明存在三级  三级只判断  基本数据类型的数组 暂不处理    三级是对象 三级数组包对象
                            String str2 = stringObjectMap1.get(splits[2]).toString();
                            //判断三级是否是数组
                            if(str2.startsWith("[")){
                                //三级是基本数量类型数组
                                List<String> list = (List<String>)  stringObjectMap1.get(splits[2]);
                                //校验敏感词 list
                                List<String> sensitiveStrList = checkSensitiveStrList(list);
                                sensitiveList.addAll(sensitiveStrList);
                            }
                        }else{
                            //二级是对象
                            //校验敏感词
                            List<String> list = checkSensitiveStr(str,token);
                            sensitiveList.addAll(list);
                        }
                    }
                }
            } else {
                Map<String, Object> result = new HashMap<>();
                if (null == map.get(field)) {
                    continue;
                }
                String string = map.get(field).toString();
                //判断是否是数组 因为是一级 所以数组内容只会是基本数据类型
                if(string.startsWith("[")){
                    List<String> list = (List<String>)map.get(field);
                    //校验敏感词 list
                    List<String> sensitiveStrList = checkSensitiveStrList(list);
                    sensitiveList.addAll(sensitiveStrList);
                }else{
                    //校验敏感词
                    List<String> sensitiveStrList = checkSensitiveStr(map.get(field).toString(),token);
                    sensitiveList.addAll(sensitiveStrList);
                }

            }
        }
        if (sensitiveList.size() > 0) {
            return ResponseResult.success(BizCodeEnume.EXIST_SENSITIVE.getCode(), BizCodeEnume.EXIST_SENSITIVE.getMsg(), sensitiveList);
        } else {
            return ResponseResult.success();
        }
    }

    public ResponseResult checkSensitiveList(String[] field,List<Map<String,Object>> list,String token){
        for (Map<String, Object> stringObjectMap : list) {
            ResponseResult responseResult = checkSensitive(field, stringObjectMap,token);
            if(responseResult.getCode() == BizCodeEnume.EXIST_SENSITIVE.getCode()){
                return responseResult;
            }
        }
        return ResponseResult.success();
    }

    public List<String> checkSensitiveStr(String sensitiveName,String token){
        List<String> list = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        result.put("sensitiveName", sensitiveName);
        result.put("token", token);
        postResponse(list, result);
        return list;
    }
    public List<String> checkSensitiveStrList(List<String> sensitiveName){
        List<String> list = new ArrayList<>();
        for (String s : sensitiveName) {
            Map<String, Object> result = new HashMap<>();
            result.put("sensitiveName", s);
            postResponse(list, result);
        }
        return list;
    }

    private void postResponse(List<String> list, Map<String, Object> result) {
        String studentJsonObject = JSON.toJSONString(result);
        HttpResponse post = HttpRequest.post(domainName + "/goods/es/esSeachSensitive").body(studentJsonObject)
                .contentType("application/json;charset=UTF-8").execute();
        ResponseResult responseBody = JSON.parseObject(post.body(), ResponseResult.class);
        if (responseBody.getCode() == BizCodeEnume.EXIST_SENSITIVE.getCode()) {
            List<String> data = (List<String>) responseBody.getData();
            list.addAll(data);
        }
    }

//    public String queryNacosServerHost(String serverName){
//        List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances(serverName);
//        if(null !=  serviceInstanceList && serviceInstanceList.size() > 0){
//            String hostName = "http://";
//            hostName = hostName + serviceInstanceList.get(0).getHost() + ":" + serviceInstanceList.get(0).getPort();
//            return hostName;
//        }
//        return null;
//    }
}
