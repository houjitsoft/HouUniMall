package com.houjiang.activity.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.activity.dto.JoinVoucherActivityDTO;
import com.houjiang.activity.dto.VoucherGoodsListDTO;
import com.houjiang.activity.dto.VoucherTemplateDTO;
import com.houjiang.activity.dto.VoucherTemplateUpdateDTO;
import com.houjiang.activity.entity.ActivityVoucherBaseEntity;
import com.houjiang.activity.entity.ActivityVoucherTemplateEntity;
import com.houjiang.activity.feign.BaseServiceClient;
import com.houjiang.activity.feign.MerchantServiceClient;
import com.houjiang.activity.feign.GoodsServiceClient;
import com.houjiang.activity.feign.UserServiceClient;
import com.houjiang.activity.mapper.ActivityVoucherBaseMapper;
import com.houjiang.activity.mapper.ActivityVoucherTemplateMapper;
import com.houjiang.activity.service.ActivityVoucherTemplateService;
import com.houjiang.activity.vo.*;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.errorcode.domain.ValidationErrorCode;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.goods.GdsListQueryResultVO;
import com.houjiang.commons.vo.goods.ProductVoucherGoodsVO;
import com.houjiang.commons.vo.shop.ShopUserVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Amos
 */
@Service
public class ActivityVoucherTemplateServiceImpl extends ServiceImpl<ActivityVoucherTemplateMapper, ActivityVoucherTemplateEntity>
    implements ActivityVoucherTemplateService{

    @Value("${domainName}")
    private String domainName;

    @Resource
    private ActivityVoucherTemplateMapper activityVoucherTemplateMapper;

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Resource
    private ActivityVoucherBaseMapper activityVoucherBaseMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private GoodsServiceClient goodsServiceClient;

    @Autowired
    private RedisCache redisCache;

    /**
     * 根据传入的参数查询优惠券模板信息。
     * 该方法首先通过token获取用户信息，然后检查用户是否有权限查询特定类型的优惠券模板。
     * 如果用户无权限，返回失败结果；如果有权限，查询并返回相应的优惠券模板信息。
     *
     * @param paraMap 包含查询参数和用户token的信息。
     * @return 返回查询结果，如果查询到数据，则包含数据的分页信息；如果未查询到数据，则提示优惠券为空。
     */
    @Override
    public ResponseResult selectVoucherTemp(Map<String, Object> paraMap) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token").toString());
        // 将用户所属店铺ID加入查询参数
        paraMap.put("shopId", userInfo.getShopId().toString());

        // 执行查询，获取分页结果
        IPage<HouVoucherTemplateVO> page = this.baseMapper.selectVoucherTemp(new Query<HouVoucherTemplateVO>().getPage(paraMap), paraMap);
        if (0 == page.getTotal()) {
            // 如果未查询到数据，返回提示信息
            return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.VOUCHER_ISEMPTY.getMsg());
        }
        // 如果查询到数据，返回成功结果并包含分页数据
        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.VOUCHER_TEMPLATE_SUCCESS.getMsg(), PageUtils.getPage(page));
    }

    /**
     * 删除优惠券模板的方法
     *
     * @param paraMap 包含请求参数的映射，其中必须包含token以获取用户信息。
     * @return 返回操作结果，如果操作成功，则返回成功信息；如果操作失败或用户无权限，则返回相应的错误信息。
     */
    @Override
    public ResponseResult deleteVoucherTemp(Map<String, String> paraMap) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));

        // 将用户店铺ID添加到参数映射中，以便后续删除操作
        String shopId = userInfo.getShopId().toString();
        paraMap.put("shopId", shopId);

        // 调用接口尝试删除优惠券模板
        Integer deleteOrNot = activityVoucherTemplateMapper.deleteVoucherTemp(paraMap);
        // 同时失效 用户已经领取的优惠券
        activityVoucherBaseMapper.updateVoucherstart(paraMap);
        if (0 == deleteOrNot) {
            // 如果删除操作未成功，则返回操作失败的错误信息
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),BizCodeEnume.OPERATION_FAILED.getMsg());
        }

        // 如果删除操作成功，则返回成功的响应信息
        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(),BizCodeEnume.DELETE_SUCCESS.getMsg());
    }

    /**
     * 插入代金券模板信息。
     *
     * @param param 包含代金券模板更新信息的DTO对象。
     * @return 返回操作结果，成功或失败。
     */
    @Override
    public ResponseResult insertVoucherTemp(VoucherTemplateUpdateDTO param) {

        // 通过token获取用户信息，用于验证权限和提取店铺ID
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 如果用户信息为空，表示token非法，返回失败结果
        if(userInfo == null){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 提取店铺ID，并设置到参数中，用于后续操作
        // 平台超管不绑店铺：创建「全平台」券，shop_id/shop_name 留空
        boolean platformAdminInsert = ParseTokenUtils.isPlatformAdmin(param.getToken());
        String shopId = userInfo.getShopId();
        param.setShopId(shopId);

        // 设置代金券模板状态，默认为有效（1）
        // 代金券模版状态(1-有效,2-失效,3-审核中，4-审核失败)
        param.setVoucherTState(null == param.getVoucherTState() ? 1 : param.getVoucherTState());
        // 设置代金券类型为单个店铺（1）
        // 代金券类型：1-单个店铺
        param.setVoucherTType(platformAdminInsert ? 0 : 1);
        // 设置代金券创建者ID
        param.setVoucherTCreatorId(userInfo.getUserId());

        // 根据店铺ID获取店铺名称，并设置到参数中
        String shopName = platformAdminInsert ? "全平台" : merchantServiceClient.selectShopNameByShopId(shopId);
        param.setShopName(shopName);

        // 插入代金券模板信息
        activityVoucherTemplateMapper.insertVoucherTemplate(param);

        // 如果存在商品ID列表，进行后续的商品和店铺关联操作
        if(param.getCommonIdList() != null && !param.getCommonIdList().isEmpty()){
            // 初始化商品列表
            List<VoucherGoodsListDTO> goodsList = Lists.newArrayList();
            // 获取代金券模板ID
            Integer voucherTId = param.getVoucherTId();
            // 更新代金券模板状态为商品代金券（2）
            activityVoucherTemplateMapper.update(null, new LambdaUpdateWrapper<ActivityVoucherTemplateEntity>()
                    .set(ActivityVoucherTemplateEntity::getIsShopVoucher,2)
                    .eq(ActivityVoucherTemplateEntity::getVoucherTId,voucherTId));

            // 创建参与代金券活动的DTO对象
            JoinVoucherActivityDTO joinVoucherActivityDTO = new JoinVoucherActivityDTO();
            // 复制代金券模板更新信息到参与代金券活动的DTO对象
            BeanUtils.copyProperties(param,joinVoucherActivityDTO);

            // 校验是否有重复的商品参与代金券活动
            // 校验商品是否已有
            Integer count = this.baseMapper.selectVoucherGoodsCount(joinVoucherActivityDTO);
            if (null != count && count > 0) {
                return ResponseResult.fail(10012, "已有商品参加代金券，请勿重复添加");
            }

            // 遍历商品ID列表，创建并填充商品和代金券模板的关联信息
            for (Integer commonId : param.getCommonIdList()) {
                VoucherGoodsListDTO voucherGoodsList = new VoucherGoodsListDTO();
                voucherGoodsList.setShopId(shopId);
                voucherGoodsList.setCommonId(commonId);
                voucherGoodsList.setVoucherTId(voucherTId);
                goodsList.add(voucherGoodsList);
            }

            // 校验是否有重复的店铺参与代金券活动
            joinVoucherActivityDTO.setShopId(shopId);
            Integer shopCount = this.baseMapper.selectVoucherShopCount(joinVoucherActivityDTO);

            // 设置待审核商品数量和总商品数量
            joinVoucherActivityDTO.setWaitVerifyGoodsNum(param.getCommonIdList().size());
            joinVoucherActivityDTO.setAllGoodsNum(param.getCommonIdList().size());
            joinVoucherActivityDTO.setVoucherTId(voucherTId);
            // 如果是首次添加店铺，插入店铺信息；否则更新店铺信息
            if (null == shopCount || shopCount <= 0) {
                this.baseMapper.insertVoucherShop(joinVoucherActivityDTO);
            }
            // 如果存在商品关联信息，更新代金券活动和代金券模板ID信息
            if(!goodsList.isEmpty()) {
                this.baseMapper.updateVoucherActivity(goodsList);
                this.baseMapper.updateVoucherTIdsBatch(goodsList);
            }
        }else{
            // 更新代金券模板状态为店铺代金券（1）
            activityVoucherTemplateMapper.update(null, new LambdaUpdateWrapper<ActivityVoucherTemplateEntity>()
                    .set(ActivityVoucherTemplateEntity::getIsShopVoucher,1)
                    .eq(ActivityVoucherTemplateEntity::getVoucherTId,param.getVoucherTId()));
        }

        // 返回操作成功的结果
        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(),BizCodeEnume.INSERT_VOUCHER_TEMPLATE_SUCCESS.getMsg());
    }

    /**
     * 查询平台代金券列表。
     *
     * @param param 请求参数，包含token信息。
     * @return 返回平台代金券列表的查询结果。
     */
    @Override
    public ResponseResult queryPlatformVoucherList(Map<String,String> param) {
        // 初始化用户信息和店铺ID
        UserInfoVO userInfo =null;
        Integer shopId=null;

        // 从请求参数中获取token
        String token = param.get("token");

        // 从Redis中根据token获取用户信息字符串
        String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER + token);

        // 如果用户信息字符串不为空，解析用户信息，并提取店铺ID
        // 平台超管（permissions=["*"]）不绑店铺：shopId 留空即查全部店铺的券
        boolean platformAdmin = ParseTokenUtils.isPlatformAdmin(token);
        if(StringUtils.isNotBlank(userInfoStr)) {
            userInfo = ParseTokenUtils.getUserInfoByToken(token);
            if(!platformAdmin && (null == userInfo || null == userInfo.getShopId() || "".equals(userInfo.getShopId()))){
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
            }
            // 如果店铺ID存在，按店铺过滤；平台超管为 null 时查全部
            if(null != userInfo && null != userInfo.getShopId() && !"".equals(userInfo.getShopId())){
                shopId = Integer.valueOf(userInfo.getShopId());
            }
        }

        // 根据店铺ID查询平台代金券列表
        List<PlatformVoucherQueryVO> result = activityVoucherTemplateMapper.selectPlatformVoucherList(shopId);

        // 返回查询成功的结果
        return ResponseResult.success(result);
    }

    /**
     * 参加优惠券活动的接口实现。
     *
     * @param param 包含用户信息、优惠券类型ID列表和商品ID列表的参数对象。
     * @return 返回操作结果，成功或失败及原因。
     */
    @Override
    public ResponseResult joinVoucherActivity(JoinVoucherActivityDTO param) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 获取用户所属店铺ID
        String shopId = userInfo.getShopId();
        // 获取选择的优惠券类型ID列表
        List<Integer> voucherTIds = param.getVoucherTIds();
        // 获取选择的商品ID列表
        List<Integer> commonIdList = param.getCommonIdList();

        // 检查是否选择了优惠券类型
        // 旧写法返回裸码 10016。10016 已认领为 AUTH 域 30803「登录信息已失效，请重新登录」，
        // 前端据此清 token 跳登录页 —— 用户没勾优惠券类型会被踢下线。这里改成 VALIDATION 域参数校验码。
        if (voucherTIds.size() == 0) {
            return ResponseResult.fail(ValidationErrorCode.PARAM_INVALID.code(),
                    ValidationErrorCode.PARAM_INVALID.meta().userMessage());
        }
        // 检查是否选择了商品，理由同上
        if (CollectionUtils.isEmpty(commonIdList)) {
            return ResponseResult.fail(ValidationErrorCode.PARAM_INVALID.code(),
                    ValidationErrorCode.PARAM_INVALID.meta().userMessage());
        }

        // 初始化商品和优惠券关联列表
        List<VoucherGoodsListDTO> goodsList = Lists.newArrayList();
        // 遍历每个优惠券类型
        for (Integer voucherTId : voucherTIds) {
            // 设置当前处理的优惠券类型ID
            param.setVoucherTId(voucherTId);
            // 校验当前优惠券类型是否已关联商品
            // 校验商品是否已有
            Integer count = this.baseMapper.selectVoucherGoodsCount(param);
            if (null != count && count.intValue() > 0) {
                return ResponseResult.fail(10012, "已有商品参加代金券，请勿重复添加");
            }

            // 遍历每个商品ID，构建优惠券和商品的关联对象
            for (Integer commonId : commonIdList) {
                VoucherGoodsListDTO voucherGoodsList = new VoucherGoodsListDTO();
                voucherGoodsList.setShopId(shopId);
                voucherGoodsList.setCommonId(commonId);
                voucherGoodsList.setVoucherTId(voucherTId);
                goodsList.add(voucherGoodsList);
            }

            // 校验当前优惠券类型是否已关联店铺
            // 校验店铺是否已有
            param.setShopId(shopId);
            Integer shopCount = this.baseMapper.selectVoucherShopCount(param);

            // 设置待审核商品数量和总商品数量
            param.setWaitVerifyGoodsNum(commonIdList.size());
            param.setAllGoodsNum(commonIdList.size());
            param.setVoucherTId(voucherTId);
            // 根据校验结果，插入或更新优惠券店铺关联信息
            if (null == shopCount || shopCount.intValue() <= 0) {
                this.baseMapper.insertVoucherShop(param);
            }
        }

        // 如果存在商品和优惠券的关联信息，更新优惠券活动和批量更新优惠券类型ID
        if (null != goodsList && !goodsList.isEmpty()) {
            this.baseMapper.updateVoucherActivity(goodsList);
            this.baseMapper.updateVoucherTIdsBatch(goodsList);
        }
        // 返回操作成功结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }
    /**
     * 根据条件查询优惠券活动下的商品信息。
     *
     * @param param 查询参数，包含分页信息和过滤条件。
     * @return 返回查询结果的分页对象。
     */
    @Override
    public ResponseResult queryVoucherGoods(JoinVoucherActivityDTO param) {
        // 通过token获取用户信息，用于过滤查询结果
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID，用于过滤属于该店铺的商品
        param.setShopId(userInfo.getShopId());

        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 初始化分页对象
        Page<VoucherGoodsQueryVO> pageObj = new Page<>(page, limit);
        // 执行查询，返回带分页信息的结果集
        IPage<VoucherGoodsQueryVO> result = this.baseMapper.selectVoucherGoods(pageObj, param);

        // 封装查询结果为响应对象，包含分页信息
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 删除促销优惠券
     *
     * @param paraMap 参数映射表，包含优惠券相关的信息以及操作权限验证所需的token
     * @return 删除操作的结果，成功或失败及原因
     *
     * 该方法首先检查传入参数中是否同时缺少shopId和voucherPriceId，如果是，则通过token获取用户信息，
     * 并检查该用户是否有权限删除促销优惠券。如果用户有权限且关联了店铺，则将店铺ID添加到参数映射表中。
     * 最后，调用删除优惠券的方法，并清理相关的优惠券模板记录。
     */
    @Override
    public ResponseResult deleteSalesPromotionVoucher(Map<String, String> paraMap) {
        // 检查是否同时缺少shopId和voucherPriceId，同时验证用户权限
        if((paraMap.get("shopId")=="" || paraMap.get("shopId")==null) && (paraMap.get("voucherPriceId")=="" || paraMap.get("voucherPriceId")==null)) {
            // 通过token获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token").toString());

            // 如果用户关联了店铺，则将店铺ID添加到参数中
            if(null!= userInfo.getShopId()){
                String shopId = userInfo.getShopId().toString();
                paraMap.put("shopId", shopId);
            }

        }
        // 删除优惠券基础信息
        activityVoucherBaseMapper.deleteVoucherBase(paraMap);
        // 删除优惠券模板信息
        this.baseMapper.delete(new LambdaQueryWrapper<ActivityVoucherTemplateEntity>()
                   .eq(ActivityVoucherTemplateEntity::getShopId,paraMap.get("shopId"))
                   .eq(ActivityVoucherTemplateEntity::getVoucherTId,paraMap.get("voucherTId")));
        // 返回操作成功的结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);

    }

    /**
     * 查询促销优惠券详情
     * 该方法用于根据优惠券模板ID获取促销优惠券的详细信息，包括是否关联了特定商品。
     * 如果用户令牌有效，将返回优惠券详情；否则，返回令牌非法的错误信息。
     *
     * @param paraMap 参数映射表，包含用户令牌和优惠券模板ID。
     * @return ResponseResult 对象，包含请求结果和数据。
     */
    @Override
    public ResponseResult salesPromotionVoucherDetail(Map<String, Object> paraMap) {
        // 通过用户令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(paraMap.get("token").toString());

        // 检查用户信息是否有效
        if (null != userInfo) {
            // 根据优惠券模板ID查询优惠券模板信息
            ActivityVoucherTemplateEntity entity = this.baseMapper.selectById(paraMap.get("voucherTId").toString());

            // 如果优惠券模板存在且为店铺优惠券，查询关联的商品列表
            if(entity != null && entity.getIsShopVoucher() == 2){
                List<GdsListQueryResultVO> goodsList = activityVoucherBaseMapper.queryShopVoucherGoodsById(entity.getVoucherTId());
                // 将商品列表设置到优惠券模板实体中
                entity.setGoodsList(goodsList);
            }
            // 返回成功的响应结果，包含优惠券模板实体
            return ResponseResult.success(entity);
        }

        // 如果用户令牌无效，返回失败的响应结果，错误码为TOKEN_IS_ILLEGAL
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 根据用户token查询促销券列表。
     *
     * @param paraMap 包含用户token、页码和每页数量等参数的映射。
     * @return 如果token有效，返回促销券列表的成功响应；如果token无效，返回失败响应。
     */
    @Override
    public ResponseResult salesPromotionVoucherList(Map<String, Object> paraMap) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(paraMap.get("token").toString());

        // 检查用户信息是否有效
        if (null != userInfo) {
            // 初始化分页对象
            // 分页查询数据
            Page<ActivityVoucherTemplateEntity> page = new Page<>((Integer) paraMap.get("page"), (Integer) paraMap.get("limit"));
            // 调用Mapper查询促销券列表
            IPage<ActivityVoucherTemplateEntity> results = this.baseMapper.salesPromotionVoucherList(page, paraMap);

            // 返回查询结果的成功响应
            return ResponseResult.success(PageUtils.getPage(results));
        }

        // 如果token无效，返回相应的失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }
    /**
     * 更新促销券信息。
     *
     * @param paraMap 包含要更新的促销券属性及其值的映射。
     * @return 如果更新成功，返回成功响应；如果更新失败，返回失败响应。
     */
    @Override
    public ResponseResult salesPromotionVoucherUpdate(Map<String, Object> paraMap) {
        // 使用LambdaUpdateWrapper更新ActivityVoucherTemplateEntity实体的特定字段
        // 根据paraMap中是否包含键来决定是否更新voucherTState、voucherTRecommend和voucherTTitle字段
        int result = this.baseMapper.update(null, new LambdaUpdateWrapper<ActivityVoucherTemplateEntity>()
                .set(paraMap.containsKey("voucherTState"), ActivityVoucherTemplateEntity::getVoucherTState, paraMap.containsKey("voucherTState") ? paraMap.get("voucherTState").toString() : null)
                .set(paraMap.containsKey("voucherTTitle"), ActivityVoucherTemplateEntity::getVoucherTTitle, paraMap.containsKey("voucherTTitle") ? paraMap.get("voucherTTitle").toString() : null)
                .eq(ActivityVoucherTemplateEntity::getVoucherTId, paraMap.get("voucherTId")));

        // 根据更新结果判断返回成功还是失败的响应
        if (result > 0) {
            return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
        }
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
    }

    @Override
    public ResponseResult addProductVoucher(VoucherTemplateUpdateDTO param) {
        // 通过token获取用户信息，用于验证权限和提取店铺ID
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 如果用户信息为空，表示token非法，返回失败结果
        if(userInfo == null){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 提取店铺ID，并设置到参数中，用于后续操作
        String shopId = userInfo.getShopId();
        param.setShopId(shopId);
        // 初始化代金券模板价格、限制和总量
        param.setVoucherTPrice(new BigDecimal(0));
        param.setVoucherTLimit(new BigDecimal(0));
        param.setVoucherTTotal(0);
        // 设置代金券模板状态，默认为有效（1）
        // 代金券模版状态(1-有效,2-失效,3-审核中，4-审核失败)
        param.setVoucherTState(null == param.getVoucherTState() ? 1 : param.getVoucherTState());
        // 代金券类型：1-单个店铺
        param.setVoucherTType(1);
        // 设置代金券创建者ID
        param.setVoucherTCreatorId(userInfo.getUserId());
        // 代金券类型：1 是店铺优惠券 2是商品优惠券 3 商品兑换券
        param.setIsShopVoucher(3);
        // 根据店铺ID获取店铺名称，并设置到参数中
        String shopName = merchantServiceClient.selectShopNameByShopId(shopId);
        param.setShopName(shopName);
        param.setVoucherTAddDate(new Date());
        // 插入代金券模板信息
        activityVoucherTemplateMapper.insertVoucherTemplate(param);
        // 初始化商品列表
        List<VoucherGoodsListDTO> goodsList = Lists.newArrayList();
        // 获取代金券模板ID
        Integer voucherTId = param.getVoucherTId();

        // 遍历商品ID列表，创建并填充商品和代金券模板的关联信息
        for (Integer commonId : param.getCommonIdList()) {
            VoucherGoodsListDTO voucherGoodsList = new VoucherGoodsListDTO();
            voucherGoodsList.setShopId(shopId);
            voucherGoodsList.setCommonId(commonId);
            voucherGoodsList.setVoucherTId(voucherTId);
            goodsList.add(voucherGoodsList);
        }
        // 如果存在商品关联信息，更新代金券活动和代金券模板ID信息
        if(!goodsList.isEmpty()) {
            this.baseMapper.updateVoucherActivity(goodsList);
            this.baseMapper.updateVoucherTIdsBatch(goodsList);
        }
        // 根据参数中的数量，为每个用户赠送兑换券
        Integer vooucherNum = param.getVoucherTEachlimit();
        for (int i = 0; i < vooucherNum; i++) {
            ActivityVoucherBaseEntity activityVoucherBaseEntity = new ActivityVoucherBaseEntity();
            activityVoucherBaseEntity.setVoucherTId(voucherTId);
            activityVoucherBaseEntity.setVoucherTitle(param.getVoucherTTitle());
            activityVoucherBaseEntity.setVoucherStartDate(param.getVoucherTStartDate());
            activityVoucherBaseEntity.setVoucherEndDate(param.getVoucherTEndDate());
            activityVoucherBaseEntity.setVoucherPrice(param.getVoucherTPrice());
            activityVoucherBaseEntity.setVoucherLimit(param.getVoucherTLimit());
            activityVoucherBaseEntity.setVoucherShopId(Integer.valueOf(param.getShopId()));
            activityVoucherBaseEntity.setVoucherState(1);
            // 批量添加用户兑换券
            activityVoucherBaseMapper.addProductVoucherList(param.getUserList(),activityVoucherBaseEntity);
        }
        List<VoucherTemplateDTO> userList = param.getUserList();
        for (VoucherTemplateDTO voucherTemplateDTO : userList) {
            // 发送消息模板
            Map<String, Object> messageMap = new HashMap<>();
            List<String> messageList = new ArrayList<>();
            messageList.add(shopName);
            messageMap.put("list", messageList);
            messageMap.put("shopName", shopName);
            messageMap.put("userId", voucherTemplateDTO.getUserId());
            messageMap.put("templateId", CommonType.messageTemplate.CODE_P.getCode());
            baseServiceClient.sendMessageTemplate(messageMap);
        }

        // 返回操作成功的结果
        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(),BizCodeEnume.INSERT_VOUCHER_TEMPLATE_SUCCESS.getMsg());
    }

    /**
     * 查询产品优惠券信息
     * 通过分页和过滤条件获取指定店铺的优惠券信息
     * 此方法需要特定权限，并关联特定店铺
     *
     * @param param 查询参数对象，包含分页、过滤条件及token等信息
     * @return 包含分页优惠券信息的响应结果
     */
    @Override
    public ResponseResult queryProductVoucher(Map<String, Object> param) {
        // 通过token获取用户信息，用于验证权限和提取店铺ID
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 如果用户信息为空，表示token非法，返回失败结果
        if(userInfo == null){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 提取店铺ID，并设置到参数中，用于后续操作
        String shopId = userInfo.getShopId();
        param.put("shopId",shopId);
        // 初始化分页对象
        Page<ProductVoucherVO> page = new Page<>(Long.valueOf(param.get("page").toString()),Long.valueOf(param.get("limit").toString()));
        // 执行查询，返回带分页信息的结果集
        IPage<ProductVoucherVO> result = this.baseMapper.queryProductVoucher(page, param);
        // 为每个优惠券记录生成活动URL
        List<ProductVoucherVO> records = result.getRecords();
        for (ProductVoucherVO record : records) {
            record.setActivityUrl(domainName + "/h5/#/activities/exchangeCouponList/exchangeCouponList?id=" + record.getVoucherTId());
        }
        // 返回分页结果
        return ResponseResult.success(PageUtils.getPage(result));
    }

    @Override
    public ResponseResult queryProductVoucherDetail(VoucherTemplateUpdateDTO param) {
        // 用于存储最终查询结果的Map
        Map<String,Object> map = new HashMap<>();
        // 通过token获取用户信息，用于验证权限和提取店铺ID
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 如果用户信息为空，表示token非法，返回失败结果
        if(userInfo == null){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 根据优惠券模板ID查询兑换券详情
        ProductVoucherVO productVoucherVO = this.baseMapper.queryProductVoucherDetail(param.getVoucherTId());
        // 查询对象券会员信息
        List<ShopUserVO> shopUserVOS = activityVoucherBaseMapper.queryShopVoucherUser(param.getVoucherTId());
        // 为每个会员设置会员等级名称和会员账号
        for (ShopUserVO shopUserVO : shopUserVOS) {
            // 查询并设置会员等级名称
            String memberLevelName = baseServiceClient.queryMemberNameByUserId(shopUserVO.getUserId());
            shopUserVO.setMemberLevelName(memberLevelName);
            // 查询会员账号
            String userMobile = userServiceClient.queryUserMobileByuserId(shopUserVO.getUserId());
            shopUserVO.setUserMobile(userMobile);
        }
        // 默认查询第一页商品信息
        // 初始化分页对象
        Page<ProductVoucherGoodsVO> page = new Page<>(0, 10);
        // 执行查询，返回带分页信息的结果集
        List<ProductVoucherGoodsVO> productVoucherGoodsVOList = new ArrayList<>();
        IPage<ProductVoucherGoodsVO> result = this.baseMapper.queryProductVoucherGoodsDetail(page, param);
        List<ProductVoucherGoodsVO> records = result.getRecords();
        if(records != null && records.size() > 0 ) {
            productVoucherGoodsVOList = goodsServiceClient.selecProductVoucherGoods(records);
        }
        map.put("productVoucherGoodsTotal",result.getTotal());
        // 将查询到的兑换券详情放入结果Map
        map.put("productVoucherVO",productVoucherVO);
        // 兑换券 商品信息
        map.put("productVoucherGoodsVOList",productVoucherGoodsVOList);
        // 将查询到的对象券会员信息放入结果Map
        map.put("shopUserVOS",shopUserVOS);
        // 将对象券会员信息的数量放入结果Map
        map.put("userTotal",shopUserVOS == null ? 0 : shopUserVOS.size());
        // 返回查询结果成功响应
        return ResponseResult.success(map);
    }

    @Override
    public ResponseResult queryProductVoucherGoodsDetail(VoucherTemplateUpdateDTO param) {
        // 用于存储最终查询结果的Map
        Map<String,Object> map = new HashMap<>();
        // 通过token获取用户信息，用于验证权限和提取店铺ID
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 如果用户信息为空，表示token非法，返回失败结果
        if(userInfo == null){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 初始化分页对象
        Page<ProductVoucherGoodsVO> pageObj = new Page<>(page, limit);
        // 执行查询，返回带分页信息的结果集
        IPage<ProductVoucherGoodsVO> result = this.baseMapper.queryProductVoucherGoodsDetail(pageObj, param);
        List<ProductVoucherGoodsVO> records = result.getRecords();
        if(records != null && records.size() > 0 ){
            List<ProductVoucherGoodsVO> productVoucherGoodsVOList = goodsServiceClient.selecProductVoucherGoods(records);
            result.setRecords(productVoucherGoodsVOList);
            return ResponseResult.success(PageUtils.getPage(result));
        }else{
            return ResponseResult.success();
        }
    }

    @Override
    public ResponseResult queryAllProductVoucherActivity(Map<String, Object> param) {
        // 初始化分页对象
        Page<ProductVoucherVO> page = new Page<>(Long.valueOf(param.get("page").toString()),Long.valueOf(param.get("limit").toString()));
        // 执行查询，返回带分页信息的结果集
        IPage<ProductVoucherVO> result = this.baseMapper.queryAllProductVoucherActivity(page, param);
        List<ProductVoucherVO> records = result.getRecords();
        for (ProductVoucherVO record : records) {
            // 根据shopId 查询店铺名称
            record.setShopName(merchantServiceClient.queryShopNameByShopId(record.getShopId()));
        }
        // 返回分页结果
        return ResponseResult.success(PageUtils.getPage(result));
    }

    @Override
    public ResponseResult queryProductVoucherAllGoods(VoucherTemplateUpdateDTO param) {
        // 初始化兑换券商品信息的VO对象
        ProductVoucherAllGoodsVO productVoucherAllGoodsVO = new ProductVoucherAllGoodsVO();
        // 根据优惠券模板ID查询兑换券详情
        ProductVoucherVO productVoucherVO = this.baseMapper.queryProductVoucherDetail(param.getVoucherTId());
        // 设置兑换券详情到VO对象
        productVoucherAllGoodsVO.setProductVoucherVO(productVoucherVO);
        // 设置分页默认值，防止前端未传参导致NPE
        Integer page = param.getPage() != null ? param.getPage() : 1;
        Integer limit = param.getLimit() != null ? param.getLimit() : 10;
        // 初始化分页对象
        Page<ProductVoucherGoodsVO> pageObj = new Page<>(page, limit);
        // 执行查询，返回带分页信息的结果集
        List<ProductVoucherGoodsVO> productVoucherGoodsVOList = new ArrayList<>();
        // 根据商品名称模糊查询商品id
        if(StringUtils.isNotBlank(param.getCommonName())){
            List<Integer> commonIds = goodsServiceClient.queryLikeGoodsName(param.getCommonName());
            param.setCommonIdList(commonIds);
        }

        IPage<ProductVoucherGoodsVO> result = this.baseMapper.queryProductVoucherGoodsDetail(pageObj, param);
        // 提取结果集中的记录
        List<ProductVoucherGoodsVO> records = result.getRecords();
        // 如果记录非空且数量大于0，进行下一步处理
        if(records != null && records.size() > 0 ) {
            // 调用远程服务查询商品信息
            productVoucherGoodsVOList = goodsServiceClient.selecProductVoucherGoods(records);
        }
        // 设置商品信息列表到VO对象
        productVoucherAllGoodsVO.setProductVoucherGoodsVOList(productVoucherGoodsVOList);
        // 设置商品总数到VO对象
        productVoucherAllGoodsVO.setProductVoucherGoodsTotal(result.getTotal());
        // 通过token获取用户信息，用于验证权限和提取店铺ID
        String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER + param.getToken());
        // 如果用户信息为空，表示token非法，返回失败结果
        if(!com.houjiang.commons.util.StringUtils.isEmpty(userInfoStr)){
            // 解析用户信息
            UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
            // 获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
            // 查询当前用户是否允许使用兑换券
            Integer num = activityVoucherBaseMapper.queryUserroductVoucherNum(param.getVoucherTId(),userInfo.getUserId());
            // 根据查询结果设置兑换券使用状态
            if(num > 0){
                productVoucherAllGoodsVO.setIsProductVoucherStart(true);
            }else{
                productVoucherAllGoodsVO.setIsProductVoucherStart(false);
            }
        }else{
            // 如果用户信息为空，默认设置兑换券使用状态为true
            productVoucherAllGoodsVO.setIsProductVoucherStart(true);
        }
        // 返回查询结果
        return ResponseResult.success(productVoucherAllGoodsVO);
    }

    @Override
    public ResponseResult queryProductVoucherBaseId(String userId, Integer commonId, Integer voucherTId) {
        // 检查 兑换券是否有效
        Integer voucherTemplateNum = this.baseMapper.queryvoucherTemplateNum(voucherTId);
        if(voucherTemplateNum == 0){
            return ResponseResult.fail(BizCodeEnume.PRODUCT_VOUCHET_INVALID.getCode(),BizCodeEnume.PRODUCT_VOUCHET_INVALID.getMsg());
        }
        // 检查 兑换券是否适用当前商品
        Integer voucherTemplateGoodsNum = this.baseMapper.queryVoucherTemplateGoodsNum(voucherTId,commonId);
        if(voucherTemplateGoodsNum == 0){
            return ResponseResult.fail(BizCodeEnume.PRODUCT_VOUCHET_GOODS_ICAPPLICABLE.getCode(),BizCodeEnume.PRODUCT_VOUCHET_GOODS_ICAPPLICABLE.getMsg());
        }
        // 检查 兑换券是否适用当前用户 以及当前用户是否还有未使用的兑换券
        Integer voucherId = activityVoucherBaseMapper.queryUserVoucherId(voucherTId,userId);
        if(voucherId == null){
            return ResponseResult.fail(BizCodeEnume.USER_PRODUCT_VOUCHET_EXPIRED.getCode(),BizCodeEnume.USER_PRODUCT_VOUCHET_EXPIRED.getMsg());
        }
        return ResponseResult.success(voucherId);
    }

    @Override
    public void updateVoucherState(Map<String, Object> param) {
        this.baseMapper.updateVoucherState(param);
    }
}




