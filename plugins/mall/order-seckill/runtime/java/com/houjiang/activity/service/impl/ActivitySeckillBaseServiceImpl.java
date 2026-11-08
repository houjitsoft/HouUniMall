package com.houjiang.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.activity.dto.*;
import com.houjiang.activity.entity.ActivitySeckillBaseEntity;
import com.houjiang.activity.entity.ActivitySeckillGoodsEntity;
import com.houjiang.activity.feign.MerchantServiceClient;
import com.houjiang.activity.feign.GoodsServiceClient;
import com.houjiang.activity.mapper.ActivitySeckillBaseMapper;
import com.houjiang.activity.mapper.ActivitySeckillGoodsMapper;
import com.houjiang.activity.service.ActivitySeckillBaseService;
import com.houjiang.activity.service.ActivitySeckillGoodsService;
import com.houjiang.activity.vo.GdbSeckillBaseVO;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.dto.goods.CommonIsXianUpdateDTO;
import com.houjiang.commons.dto.goods.ManageSeckillBaseUpdateDTO;
import com.houjiang.commons.dto.goods.SeckillGoodsDTO;
import com.houjiang.commons.dto.goods.SeckillGoodsListDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.goods.SeckillGoodsQueryResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName ActivitySeckillBaseServiceImpl
 * @Description  限时秒杀活动表
 * @Author Amos
 * @Date 2023-07-04 17:26:46
 * @Version 1.0
 * @author Amos
 */
@Service
public class ActivitySeckillBaseServiceImpl extends ServiceImpl<ActivitySeckillBaseMapper, ActivitySeckillBaseEntity>
        implements ActivitySeckillBaseService {

    @Autowired
    private ActivitySeckillGoodsMapper activitySeckillGoodsMapper;

    @Autowired
    private ActivitySeckillGoodsService activitySeckillGoodsService;

    @Autowired
    private GoodsServiceClient goodsServiceClient;

    @Autowired
    private MerchantServiceClient merchantServiceClient;
    /**
     * 根据输入参数获取秒杀活动结束时间的列表信息。
     *
     * @param param 包含秒杀活动结束时间查询条件的DTO对象。
     * @return 返回查询结果的ResponseResult对象，其中包含分页信息和秒杀活动数据。
     */
    /**
     * 登录态校验（替代 UserService.judgePermissions）。
     *
     * <p>老 {@code RedisUtil} 是独立 Jedis 连接、按老部署的本机配置文件取参，
     * 在容器化宿主里连不上 → judgePermissions 恒 false → 插件写接口一律 31005。
     * 这里统一改走宿主 RedisCache（与 {@link ParseTokenUtils} 同源），
     * 只保留「登录态有效」这一层语义；店铺维度过滤由各业务原有的 shopId 逻辑负责。
     */
    private static UserPermissionsResult loginPermissions(String token) {
        UserPermissionsResult result = new UserPermissionsResult();
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        if (userInfo != null) {
            result.setFlag(true);
            result.setUserId(userInfo.getUserId());
            result.setShopId(userInfo.getShopId());
            result.setShopType(userInfo.getShopType());
            result.setUserName(userInfo.getUserName());
            result.setUserMobile(userInfo.getUserMobile());
            result.setUserNickname(userInfo.getUserNickname());
        }
        return result;
    }

    @Override
    public ResponseResult getList(FinishTimeSecondkillDTO param) {
        // 初始化一个Map对象，用于存储查询参数
        Map map = null;
        try {
            // 将输入的DTO对象转换为Map对象，便于后续的查询操作
            map = ParseMapUtils.beanToMap(param);
        } catch (IllegalAccessException e) {
            // 打印异常堆栈信息，用于问题排查
            e.printStackTrace();
        }

        // 根据转换后的Map参数和查询条件，调用baseMapper进行查询，并返回分页结果
        IPage<Map<String,Object>> iPage = this.baseMapper.selectFinishTimeSecondkillPojo(new Query<Map<String,Object>>().getPage(map), param);

        // 构建并返回查询结果成功的ResponseResult对象，其中包含分页信息和查询数据
        return ResponseResult.success(PageUtils.getPage(iPage));
    }

    /**
     * 根据条件分页查询秒杀活动信息。
     *
     * @param param 包含秒杀活动查询条件及分页信息的数据传输对象。
     * @return 返回查询结果的响应对象，其中包含分页处理后的秒杀活动信息。
     */
    @Override
    public ResponseResult getByPage(FinishTimeSecondkillDTO param) {
        // 通过token获取用户信息，用于后续的权限校验
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 校验token是否有效，若无效则返回错误响应
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 校验用户是否有权限：平台超管（permissions=[*]）不绑店铺，跳过店铺过滤看全部
        if(userInfo.getShopId()==null && !ParseTokenUtils.isPlatformAdmin(param.getToken())){
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 将用户信息中的店铺ID转换为Integer类型，并设置到查询参数中，用于后续的查询过滤
        Integer shopId = null == userInfo.getShopId() ? null : Integer.valueOf(userInfo.getShopId());
        param.setShopId(shopId);
        Map map = null;
        try {
            // 将查询参数对象转换为Map，便于后续的数据库查询
            map = ParseMapUtils.beanToMap(param);
        } catch (IllegalAccessException e) {
            // 处理转换过程中的异常，打印堆栈信息
            e.printStackTrace();
        }
        // 执行数据库查询，根据查询条件及分页信息获取秒杀活动的分页结果
        IPage<Map<String,Object>> iPage = this.baseMapper.getByPage(new Query<Map<String,Object>>().getPage(map), param);
        // 构建成功响应，包含分页处理后的秒杀活动信息
        return ResponseResult.success(PageUtils.getPage(iPage));
    }

    /**
     * 删除秒杀商品数据。
     *
     * 本方法通过接收一个包含秒杀商品信息的参数对象，调用底层Mapper接口，
     * 实现对特定秒杀商品的删除操作。删除操作成功后，返回一个表示操作成功的响应结果。
     *
     * @param param 包含秒杀商品ID等信息的数据传输对象，用于指定要删除的秒杀商品。
     * @return 返回一个表示操作结果的响应对象，如果操作成功，则包含成功代码和消息。
     */
    @Override
    public ResponseResult deleteData(SecondKillGoodsDTO param) {
        // 根据秒杀ID删除秒杀商品记录
        this.baseMapper.deleteById(param.getSeckillId());

        // 返回操作成功的响应结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 根据秒杀ID查询秒杀活动基本信息。
     *
     * @param param 包含秒杀ID和用户令牌的查询参数DTO。
     * @return 返回包含秒杀活动基本信息的响应结果。
     * @Override
     */
    @Override
    public ResponseResult queryShopSeckillBase(ManageSeckillBaseQueryDTO param) {
        // 通过令牌获取用户信息，用于后续的权限验证。
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());

        // 验证令牌是否有效，若无效则返回错误响应。
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }

        // 根据秒杀ID从数据库查询秒杀活动基本信息。
        GdbSeckillBaseVO result = this.baseMapper.selectByPrimaryKey(param.getSeckillId());

        // 返回查询结果，若查询成功则返回成功响应并附带秒杀活动基本信息。
        return ResponseResult.success(result);
    }
    /**
     * 删除秒杀活动基础信息。
     *
     * @param param 包含秒杀ID等信息的参数映射。
     * @return 删除操作的结果。
     */
    @Override
    public ResponseResult deleteShopSeckillBase(Map<String, Object> param) {
        // 判断用户权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = loginPermissions(param.get("token").toString());

        if (userPermissionsResult.getFlag()) {
            // 根据秒杀ID查询关联的商品ID列表
            List<Integer> commonIdList =activitySeckillGoodsMapper.selectList(new LambdaQueryWrapper<ActivitySeckillGoodsEntity>()
                                       .eq(ActivitySeckillGoodsEntity::getSeckillId, param.get("seckillId").toString()))
                                       .stream().map(ActivitySeckillGoodsEntity::getCommonId).collect(Collectors.toList());

            // 如果存在关联的商品ID
            if (!CollectionUtils.isEmpty(commonIdList)) {
                // 查询当前秒杀活动中其他仍在进行中的商品ID列表
                // 获取其他正在进行的活动
                List<Integer> activeCommonIdList = activitySeckillGoodsMapper.selectActiveSeckillGoods(param.get("seckillId").toString(),null);
                // 如果存在进行中的商品
                if(!CollectionUtils.isEmpty(activeCommonIdList)){
                    // 将这些商品的状态更新为非可见
                    goodsServiceClient.updateCommonIsXian2(commonIdList);
                }
            }

            // 删除秒杀活动关联的商品信息
            activitySeckillGoodsMapper.delete(new LambdaQueryWrapper<ActivitySeckillGoodsEntity>()
                                      .eq(ActivitySeckillGoodsEntity::getSeckillId, param.get("seckillId").toString()));
            // 删除秒杀活动本身
            this.baseMapper.deleteById(param.get("seckillId").toString());
        } else {
            // 如果用户权限不足，返回相应的错误结果
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 如果操作成功，返回相应的成功结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 根据秒杀ID查询秒杀活动及商品信息
     * @param param 查询参数，包含秒杀ID和用户令牌
     * @return 返回包含秒杀活动和商品信息的响应结果
     */
    @Override
    public ResponseResult queryShopSeckilCopy(Map<String, Object> param) {
        // 通过用户令牌获取用户信息
        // 权限判断
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(String.valueOf(param.get("token")));

        if (userInfoVO != null) {
            // 根据秒杀ID查询秒杀活动基础信息
            //查询秒杀活动信息
            ManageSeckillBaseUpdateDTO seckillBase = this.baseMapper.selectSeckilById(Integer.valueOf(param.get("seckillId").toString()));
            // 根据秒杀ID查询所有关联商品信息
            //查询商品信息
            List<SeckillGoodsListDTO> seckillGoodsList = this.baseMapper.selectSeckilGoodsById(Integer.valueOf(param.get("seckillId").toString()));
            List<SeckillGoodsListDTO> seckillGoods = new ArrayList<>();
            // 如果商品信息不为空，则进行商品信息的聚合处理
            if(null != seckillGoodsList && seckillGoodsList.size() > 0){
                // 根据commonId对商品信息进行分组，以便聚合相同商品的不同规格
                Map<Object, List<SeckillGoodsListDTO>> listMap = seckillGoodsList.stream().collect(Collectors.groupingBy(item -> {
                    return item.getCommonId();
                }));
                // 遍历分组后的商品信息，聚合每个商品的规格信息
                for (Map.Entry<Object, List<SeckillGoodsListDTO>> entry : listMap.entrySet()) {
                    List<SeckillGoodsListDTO> value = entry.getValue();
                    SeckillGoodsListDTO goodsVO = new SeckillGoodsListDTO();
                    // 设置商品的基本信息（名称、图片、价格等）来自同一个commonId的第一个商品
                    goodsVO.setGoodsName(value.get(0).getGoodsName());
                    goodsVO.setGoodsImage(value.get(0).getGoodsImage());
                    goodsVO.setGoodsPrice(value.get(0).getGoodsPrice());
                    goodsVO.setCommonId(value.get(0).getCommonId());
                    goodsVO.setGoodsId(value.get(0).getGoodsId());
                    goodsVO.setGoodsUpperLimit(value.get(0).getGoodsUpperLimit());
                    // 计算商品的秒杀价格范围和总量
                    BigDecimal seckillMaxPrice = value.get(0).getSeckillPrice();
                    BigDecimal seckillMinPrice = value.get(0).getSeckillPrice();
                    Integer seckillAllNum = 0;
                    List<SeckillGoodsDTO> seckillGoodsDTOS = new ArrayList<>();
                    for (SeckillGoodsListDTO seckillGoodsListDTO : value) {
                        SeckillGoodsDTO seckillGoodsDTO = new SeckillGoodsDTO();
                        seckillGoodsDTO.setCommonId(seckillGoodsListDTO.getCommonId());
                        seckillGoodsDTO.setGoodsId(seckillGoodsListDTO.getGoodsId());
                        seckillGoodsDTO.setGoodsPrice(seckillGoodsListDTO.getGoodsPrice());
                        seckillGoodsDTO.setSeckillPrice(seckillGoodsListDTO.getSeckillPrice());
                        seckillGoodsDTO.setSeckillNum(seckillGoodsListDTO.getSeckillNum());
                        seckillAllNum = seckillAllNum + seckillGoodsListDTO.getSeckillNum();
                        // 更新商品的最高秒杀价格和最低秒杀价格
                        if(seckillMaxPrice.compareTo(seckillGoodsListDTO.getSeckillPrice()) > 0 ){
                            seckillMinPrice = seckillGoodsListDTO.getSeckillPrice();
                        }else{
                            seckillMaxPrice = seckillGoodsListDTO.getSeckillPrice();
                        }
                        seckillGoodsDTOS.add(seckillGoodsDTO);
                    }
                    goodsVO.setSeckillMaxPrice(seckillMaxPrice);
                    goodsVO.setSeckillMinPrice(seckillMinPrice);
                    goodsVO.setSeckillAllNum(seckillAllNum);
                    goodsVO.setSpecLists(seckillGoodsDTOS);
                    seckillGoods.add(goodsVO);
                }
            }
            // 将聚合后的商品信息设置到秒杀活动中
            seckillBase.setSeckillGoodsList(seckillGoods);
            // 返回成功的响应结果，包含秒杀活动信息
            return ResponseResult.success(seckillBase);
        } else {
            // 如果用户信息为空，表示权限不足，返回相应的失败响应
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
    }

    /**
     * 查询商家秒杀基础信息列表
     *
     * @param param 包含秒杀活动基础信息查询条件的DTO对象
     * @return 返回查询结果的ResponseResult对象
     * @throws IllegalAccessException 当访问字段权限不足时抛出此异常
     *
     * 本方法用于根据商家的权限查询相应的秒杀活动基础信息列表。
     * 首先，通过权限判断确定用户是否有查询权限。
     * 如果有权限，将查询条件中的商家ID设置为用户拥有的商家ID，并进行查询。
     * 如果没有权限，返回权限不足的错误信息。
     */
    @Override
    public ResponseResult queryShopSeckillBaseList(ManageSeckillBaseUpdateDTO param) throws IllegalAccessException {
        // 进行权限判断，确定用户是否有查询秒杀活动信息的权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = loginPermissions(param.getToken());

        // 平台超管（permissions=["*"]）不绑店铺：不加店铺过滤，看全部店铺活动
        boolean platformAdmin = ParseTokenUtils.isPlatformAdmin(param.getToken());

        // 如果权限判断通过
        if (platformAdmin || userPermissionsResult.getFlag()) {
            // 设置查询条件中的商家ID为用户拥有的商家ID；平台超管留空表示全部
            if (!platformAdmin && null != userPermissionsResult.getShopId()) {
                param.setShopId(Integer.valueOf(userPermissionsResult.getShopId()));
            }
            // 将查询条件转换为Map对象，以便进行数据库查询
            Map data= ParseMapUtils.beanToMap(param);
            // 执行数据库查询，获取秒杀活动基础信息的分页数据
            IPage<ActivitySeckillBaseEntity> page=this.baseMapper.selectShopSeckillBaseList(new Query<ActivitySeckillBaseEntity>().getPage(data),
                    param);
            // 构造并返回查询结果成功的ResponseResult对象
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            // 如果权限判断不通过，返回权限不足的错误信息
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
    }

    /**
     * 添加店铺秒杀基础信息
     *
     * @param param 包含秒杀活动基础信息和商品信息的更新DTO
     * @return 成功添加后返回包含秒杀ID的结果，如果权限不足则返回失败结果
     *
     * 此方法首先判断用户是否有权限进行操作，然后根据输入参数设置秒杀活动的起始时间、状态等信息。
     * 如果秒杀活动已经开始或正在进行，则设置相应的状态。此外，方法还处理了秒杀时间和日期的格式化。
     * 根据秒杀活动是否已存在，方法将插入或更新秒杀基础信息，并相应地添加或更新参与秒杀活动的商品信息。
     */
    @Override
    public ResponseResult addShopSeckillBase(ManageSeckillBaseUpdateDTO param) {
        // 判断用户权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = loginPermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            SeckillGoodsQueryResultVO seckillGoodsQueryResult = new SeckillGoodsQueryResultVO();
            // 平台超管不绑店铺：活动落 shop_id=0（全平台），跳过店铺名查询
            boolean platformAdmin = ParseTokenUtils.isPlatformAdmin(param.getToken());
            Integer shopId = platformAdmin ? 0 : Integer.valueOf(userPermissionsResult.getShopId());
            // 根据店铺ID获取店铺名称
            String shopName = platformAdmin ? "全平台" : merchantServiceClient.selectShopNameByShopId(shopId);
            // 设置秒杀活动的店铺名称、用户ID、用户昵称和店铺ID
            param.setShopName(shopName);
            param.setUserId(userPermissionsResult.getUserId());
            param.setUserNickName(userPermissionsResult.getUserNickname());
            param.setShopId(shopId);
            // 确保秒杀说明不为空
            if(param.getSeckillExplain() == null){
                param.setSeckillExplain("");
            }
            // 根据秒杀小时设置秒杀开始时间
            if(null != param.getSeckillHour() && null != param.getSeckillStartDate()){
                String seKillHourStr = "";
                switch (param.getSeckillHour()){
                    case 1:
                        seKillHourStr = "08:00:00";
                        break;
                    case 2:
                        seKillHourStr = "12:00:00";
                        break;
                    case 3:
                        seKillHourStr = "16:00:00";
                        break;
                    case 4:
                        seKillHourStr = "20:00:00";
                        break;
                    case 5:
                        seKillHourStr = "00:00:00";
                        break;
                    default:
                        seKillHourStr = "00:00:00";
                }
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
                    String seckillStartTime = simpleDateFormat2.format(param.getSeckillStartDate()) + " " + seKillHourStr;
                    param.setSeckillStartTime(simpleDateFormat.parse(seckillStartTime));
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }

            }
            // 根据当前时间与秒杀开始、结束时间的关系设置秒杀状态
            if(new Date().before(param.getSeckillStartTime())){
                param.setSeckillState(0);
            }else if (DateUtils.belongCalendar(new Date(), param.getSeckillStartTime(), param.getSeckillEndTime())) {
                param.setSeckillState(1);
            } else {
                param.setSeckillState(2);
            }
            // 设置秒杀创建时间
            if(null == param.getSeckillCreateTime()) {
                param.setSeckillCreateTime(new Date());
            }
            SeckillGoodsUpdateDTO goodsParam = new SeckillGoodsUpdateDTO();
            // 根据秒杀ID是否存在，进行新增或更新操作
            if (null == param.getSeckillId()) {
                if (null == param.getComboId()) {
                    param.setComboId(0);
                }
                this.baseMapper.insertShopSeckillBase(param);
                seckillGoodsQueryResult.setSeckillId(param.getSeckillId());
                goodsParam.setSeckillId(param.getSeckillId());
                goodsParam.setSeckillGoodsList(param.getSeckillGoodsList());
                goodsParam.setToken(param.getToken());
                activitySeckillGoodsService.addShopSeckillGoods(goodsParam);
            } else {
                this.baseMapper.updateShopSeckillBase(param);
                // 查询参与此限时折扣的商品
                List<Integer> commonList = activitySeckillGoodsMapper.selectList(new LambdaQueryWrapper<ActivitySeckillGoodsEntity>()
                                                .eq(ActivitySeckillGoodsEntity::getSeckillId,param.getSeckillId()))
                                                .stream().map(ActivitySeckillGoodsEntity::getCommonId).collect(Collectors.toList());
                CommonIsXianUpdateDTO commonIsXianUpdateParam = new CommonIsXianUpdateDTO();
                // 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
                commonIsXianUpdateParam.setCommonIdList(commonList);
                commonIsXianUpdateParam.setCommonIsXian(0);
                if (!CollectionUtils.isEmpty(commonList) && commonList.size() > 0) {
                    goodsServiceClient.updateCommonIsXian3(commonIsXianUpdateParam);
                }
                activitySeckillGoodsMapper.delete(new LambdaQueryWrapper<ActivitySeckillGoodsEntity>()
                                              .eq(ActivitySeckillGoodsEntity::getSeckillId,param.getSeckillId()));
                goodsParam.setSeckillId(param.getSeckillId());
                goodsParam.setSeckillGoodsList(param.getSeckillGoodsList());
                goodsParam.setToken(param.getToken());
                activitySeckillGoodsService.addShopSeckillGoods(goodsParam);
            }
            // 返回成功结果，包含新增的秒杀ID
            return ResponseResult.success(seckillGoodsQueryResult.getSeckillId());
        } else {
            // 返回权限不足的失败结果
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }

    }
}
