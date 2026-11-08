package com.houjiang.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.dto.GoodsParaDTO;
import com.houjiang.order.dto.RiderEvaluationDTO;
import com.houjiang.order.dto.ShopParaDTO;
import com.houjiang.order.entity.OrderGoodsEntity;
import com.houjiang.order.entity.OrderGoodsEvaluationEntity;
import com.houjiang.order.feign.BaseServiceClient;
import com.houjiang.order.mapper.OrderBaseMapper;
import com.houjiang.order.mapper.OrderGoodsEvaluationMapper;
import com.houjiang.order.mapper.OrderGoodsMapper;
import com.houjiang.order.service.OrderGoodsEvaluationService;
import com.houjiang.order.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName OrderGoodsEvaluationServiceImpl
 * @Description 商品评论表
 * @Author Amos
 * @Date 2023-05-26 13:28:36
 * @Version 1.0
 * @author Amos
 */
@Service
public class OrderGoodsEvaluationServiceImpl extends ServiceImpl<OrderGoodsEvaluationMapper, OrderGoodsEvaluationEntity>
        implements OrderGoodsEvaluationService {

    @Resource
    private OrderGoodsMapper orderGoodsMapper;

    @Resource
    private OrderGoodsEvaluationMapper orderGoodsEvaluationMapper;

    @Resource
    private BaseServiceClient baseServiceClient;

    @Resource
    private OrderBaseMapper orderBaseMapper;

    @Override
    public ResponseResult queryCommentsCount(Integer commonId) {
        return ResponseResult.success(this.baseMapper.queryCommentsCount(commonId));
    }

    /**
     * 根据传入的参数获取评论信息，并计算好评、中评、差评和晒图的数量，以及好评率。
     *
     * @param map 包含查询参数的映射，其中包含commonId（通用ID）和evaluation（评价类型）等信息。
     * @return 返回包含评论数据和评价统计数据的ResponseResult对象。
     */
    @Override
    public ResponseResult getComments(Map<String, Object> map) {
        // 初始化响应结果对象
        ResponseResult rr = new ResponseResult();
        // 初始化分页评论信息对象
        PagedByCommentsVO pi = new PagedByCommentsVO();

        // 从map中获取commonId和evaluation（Jackson 反序列化 Map 时数字常为 Integer/Long）
        Integer commonId = toInteger(map.get("commonId"));
        Integer evaluation = toInteger(map.get("evaluation"));

        // 根据evaluation值设置结果数量和图片数量的描述；缺省/1=全部，避免 NPE
        String resultNum = null;
        String imageNum = null;
        if (evaluation != null && evaluation == 2) {
            resultNum = "good";
        } else if (evaluation != null && evaluation == 3) {
            resultNum = "neutral";
        } else if (evaluation != null && evaluation == 4) {
            resultNum = "bad";
        } else if (evaluation != null && evaluation == 5) {
            imageNum = "";
        }

        // 将结果数量和图片数量的描述放入map中
        map.put("resultNum", resultNum);
        map.put("imageNum", imageNum);

        // 根据map中的条件查询评论信息，返回分页结果
        IPage<CommentsEntityVO> page = this.baseMapper.commentsQuery(new Query<CommentsEntityVO>().getPage(map), map);
        List<CommentsEntityVO> commentsQueryList = page.getRecords();

        // 遍历评论列表，处理额外的计算和格式化
        for (int i = 0; i < commentsQueryList.size(); i++) {
            CommentsEntityVO ce = new CommentsEntityVO();
            ce = commentsQueryList.get(i);
            // 计算额外内容时间与创建时间的天数差
            Integer additionalDay = DateUtils.differentDaysByMillisecond(ce.getCreateTime(), ce.getAdditionalContentTime());
            ce.setAdditionalDay(additionalDay);

            // 如果是匿名评论，隐藏用户名中间部分
            // 方法描述 隐藏用户名中间位置字符，显示前后1个字符
            if (ce.getIsanonymous() == 1) {
                String userName = ce.getUserName();
                String name = userName.substring(0, 1) + "****" + userName.substring(userName.length() - 1, userName.length());
                ce.setUserName(name);
            }

            // 处理评论和额外图片的列表，去除空格并分割字符串
            if (ce.getImage() != null) {
                String[] imagesList = ce.getImage().replace(" ", "").split(",");
                ce.setImagesList(imagesList);
            } else {
                ce.setImagesList(new String[]{});
            }
            if (ce.getAdditionalImage() != null) {
                String[] additionalImagesList = ce.getAdditionalImage().replace(" ", "").split(",");
                ce.setAdditionalImagesList(additionalImagesList);
            } else {
                ce.setAdditionalImagesList(new String[]{});
            }
        }

        // 更新分页结果的记录列表
        page.setRecords(commentsQueryList);

        // 分别计算中评、差评、晒图和好评的数量
        //中评数
        Integer neutralTotalCount = this.baseMapper.getEvaluationTotalCount(commonId, "neutral");
        //差评数
        Integer badTotalCount = this.baseMapper.getEvaluationTotalCount(commonId, "bad");
        //晒单数
        Integer imageTotalCount = this.baseMapper.getEvaluationTotalCount(commonId, "image");
        //好评数
        Integer goodsOrderNum = this.baseMapper.getEvaluationTotalCount(commonId, "good");
        //所有评论
        Integer allOrderNum = this.baseMapper.getEvaluationTotalCount(commonId, null);

        // 计算好评率
        BigDecimal aon = new BigDecimal(allOrderNum);
        BigDecimal gon = new BigDecimal(goodsOrderNum);
        String rate = null;
        BigDecimal num = new BigDecimal(100);
        try {
            if (allOrderNum == 0) {
                rr.setCode(2);
                rr.setMessage("该商品暂时无评论！");
            } else {
                rate = (gon.divide(aon, 2, RoundingMode.HALF_UP).multiply(num)).toString() + "%";
            }
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }

        // 设置评论统计数据到pi对象
        //好评率
        pi.setRate(rate);
        pi.setAllOrderNum(allOrderNum);
        pi.setBadTotalCount(badTotalCount);
        pi.setGoodTotalCount(goodsOrderNum);
        pi.setNeutralTotalCount(neutralTotalCount);
        pi.setImageTotalCount(imageTotalCount);
        pi.setRecords(PageUtils.getPage(page));

        // 返回包含评论数据和评价统计数据的响应结果
        return ResponseResult.success(pi);
    }

    /**
     * 根据用户token查询用户的评价信息。
     * @param map 包含token和其他查询参数的映射。
     * @return 返回查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult queryMyEvaluation(Map<String, Object> map) {
        // 从map中获取token
        String token = (String) map.get("token");
        // 根据token解析用户信息
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(token);
        // 检查用户信息是否有效
        if (userInfoVO != null) {
            try {
                // 获取用户ID
                String userId = userInfoVO.getUserId();
                // 从map中获取评价类型
                // 1:全部评价,2:晒图评价
                Integer type = (Integer) map.get("type");

                // 根据评价类型查询评价信息
                // 分页查询评价信息
                IPage<OrderGoodsEvaluationEntity> page = this.baseMapper.selectPage(new Query<OrderGoodsEvaluationEntity>().getPage(map), new LambdaQueryWrapper<OrderGoodsEvaluationEntity>()
                        .eq(type == 2, OrderGoodsEvaluationEntity::getResult, "good")
                        .eq(type == 3, OrderGoodsEvaluationEntity::getResult, "neutral")
                        .eq(type == 4, OrderGoodsEvaluationEntity::getResult, "bad")
                        .ne(type == 5, OrderGoodsEvaluationEntity::getImage, "")
                        .eq(OrderGoodsEvaluationEntity::getUserId, userId)
                        .orderByDesc(OrderGoodsEvaluationEntity::getCreateTime));

                // 获取查询结果列表
                List<OrderGoodsEvaluationEntity> records = page.getRecords();
                // 封装分页信息
                PageUtils pageUtils = PageUtils.getPage(page);

                /**
                 * 封装数据
                 */
                List<MyEvaluationVO> evaluationList = new ArrayList<>();
                // 如果查询结果不为空，转换为MyEvaluationVO对象
                if (CollectionUtils.isNotEmpty(records)) {
                    evaluationList = records.stream().map(entity -> {
                        MyEvaluationVO catResult = new MyEvaluationVO();
                        // 使用BeanUtils复制属性
                        BeanUtils.copyProperties(entity, catResult);
                        return catResult;
                    }).collect(Collectors.toList());
                }

                // 遍历评价列表，设置额外的信息
                for (MyEvaluationVO mep : evaluationList) {
                    // 计算评价时间和追加内容时间的间隔天数
                    Integer additionalDay = DateUtils.differentDaysByMillisecond(mep.getCreateTime(), mep.getAdditionalContentTime());
                    mep.setAdditionalDay(additionalDay);

                    // 查询关联的商品信息
                    OrderGoodsEntity orderGoodsEntity = orderGoodsMapper.selectOne(new LambdaQueryWrapper<OrderGoodsEntity>()
                            .eq(OrderGoodsEntity::getGoodsId, mep.getGoodsId()).orderByDesc(OrderGoodsEntity::getOrderGoodsTime).last("limit 1"));
                    // 设置商品图片
                    String goodImage = orderGoodsEntity.getGoodsImage();
                    mep.setGoodsImage(goodImage);

                    // 处理评价图片列表
                    if (mep.getImage() != null) {
                        String[] imagesList = mep.getImage().replace(" ", "").split(",");
                        // 初始化图片列表
                        for (int j = 0; j < imagesList.length; j++) {
                            if ("".equals(mep.getImage())) {
                                String[] emptyList = {};
                                mep.setImagesList(emptyList);
                            } else {
                                mep.setImagesList(imagesList);
                            }
                        }
                    }
                    // 处理追加图片列表
                    if (mep.getAdditionalImage() != null) {
                        String[] addItionalImagesList = mep.getAdditionalImage().replace(" ", "").split(",");
                        for (int j = 0; j < addItionalImagesList.length; j++) {
                            if ("".equals(mep.getAdditionalImage())) {
                                String[] emptyList = {};
                                mep.setAdditionalImagesList(emptyList);
                            } else {
                                mep.setAdditionalImagesList(addItionalImagesList);
                            }
                        }
                    }
                }
                // 设置查询结果到分页对象
                pageUtils.setList(evaluationList);
                // 返回成功的响应结果
                return ResponseResult.success(pageUtils);
            } catch (Exception e) {
                // 返回失败的响应结果，附带错误信息
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            // 返回失败的响应结果，表示token无效
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 平台买家评论查询接口。
     * 该方法用于查询商品评论信息，支持分页和脱敏处理。
     *
     * @param goodsPara 包含商品参数和分页信息的数据传输对象。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult platFormBuyerComments(GoodsParaDTO goodsPara) {
        // 尝试将商品参数对象转换为Map，用于后续处理
        //将对象转换为map
        Map data = null;
        try {
            data = ParseMapUtils.beanToMap(goodsPara);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // 初始化分页参数
        Page<GoodsCommentsVO> pageParam = new Page<>(goodsPara.getPage(), goodsPara.getLimit());
        // 执行查询
        //分页查询数据
        IPage<GoodsCommentsVO> page = this.baseMapper.selectGoodsComments(pageParam, goodsPara);

        // 获取是否开启脱敏处理的配置值
        String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
        // 如果开启脱敏且查询结果不为空，则对查询结果进行脱敏处理
        if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString()) && !page.getRecords().isEmpty()) {
            for (GoodsCommentsVO vo : page.getRecords()) {
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(vo, GoodsCommentsVO.class), vo);
            }
        }

        // 返回查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询平台店铺的评论信息
     *
     * @param goodsPara 商品参数DTO，包含查询所需的条件信息
     * @return 返回查询结果的响应对象，其中包含分页后的评论数据
     *
     * 该方法首先将商品参数DTO转换为Map对象，以便后续查询使用。
     * 接着，利用baseMapper的selectShopComments方法进行评论数据的查询，
     * 并对查询结果进行分页处理。最后，将分页后的数据封装进响应结果对象中返回。
     */
    @Override
    public ResponseResult platFormShopComments(ShopParaDTO goodsPara) {
        // 尝试将商品参数DTO转换为Map对象
        //将参数转为map
        Map data = null;
        try {
            data = ParseMapUtils.beanToMap(goodsPara);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // 根据转换后的Map对象和商品参数DTO查询评论信息，并进行分页处理
        IPage<ShopCommentsVO> page = this.baseMapper.selectShopComments(new Query<ShopCommentsVO>().getPage(data), goodsPara);

        // 将分页后的评论数据封装进响应结果对象中并返回
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 删除购买者评论的方法。
     *
     * @param map 一个包含删除评论所需信息的键值对映射表。
     *             其中，键 "evaluation_goods_id" 对应于要删除的评论的商品ID。
     * @return 如果删除成功，则返回一个表示成功的ResponseResult对象，包含DELETE_SUCCESS业务码；
     *         如果删除失败，则返回一个表示失败的ResponseResult对象，包含DELETE_ERROR业务码。
     */
    @Override
    public ResponseResult deleteBuyerComments(Map<String, String> map) {
        // 根据传入的map中的"evaluation_goods_id"值，删除对应的评论记录
        int delete = this.baseMapper.deleteById(map.get("evaluation_goods_id"));

        // 检查删除操作是否成功，如果删除了至少一条记录，则认为成功
        if (delete > 0){
            return ResponseResult.success(BizCodeEnume.DELETE_SUCCESS);
        }

        // 如果删除操作没有成功删除任何记录，则认为失败
        return ResponseResult.fail(BizCodeEnume.DELETE_ERROR);
    }

    /**
     * 删除店铺评论的方法
     *
     * @param map 包含请求参数的映射，其中必须包含token信息用于用户身份验证
     * @return 返回操作结果，如果操作成功，则返回成功的响应结果；如果操作失败，例如token无效，则返回失败的响应结果
     */
    @Override
    public ResponseResult deleteShopComments(Map<String, String> map) {
        // 通过token获取用户信息，用于后续的身份验证
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token"));

        // 验证用户信息是否有效，如果无效则返回错误响应
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        // 调用基本Mapper接口删除店铺评论
        this.baseMapper.deleteShopComments(map);

        // 如果删除操作成功，则返回成功的响应结果
        return ResponseResult.success();
    }

    /**
     * 根据传入的条件获取所有评论信息。
     * 该方法首先验证token的有效性，然后根据评价等级对查询条件进行处理，
     * 最后执行查询并处理返回的评论数据，包括设置评价状态和处理图片信息。
     *
     * @param map 包含查询条件和其他必要信息的Map对象，如token、评价等级、页码和每页数量等。
     * @return 返回包含评论数据的响应结果对象，如果token无效，则返回失败结果。
     */
    @Override
    public ResponseResult getAllComments(Map<String, Object> map) {
        // 从map中获取token并验证其在Redis中的存在性
        String token = (String) map.get("token");
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExistToken) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        try {
            // 解析token获取用户信息
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            // 从用户信息中获取店铺ID
            String shopId = ui.getShopId();
            // 从map中获取评价等级，并根据评价等级设置查询条件
            Integer evaluation = (Integer) map.get("evaluation");
            String resultNum = null;
            String imageNum = null;
            switch (evaluation) {
                case 1:
                    resultNum = "";
                    break;
                case 2:
                    resultNum = "'good'";
                    break;
                case 3:
                    resultNum = "'neutral'";
                    break;
                case 4:
                    resultNum = "'bad'";
                    break;
                case 5:
                    imageNum = "";
                    break;
            }
            // 将店铺ID和评价条件加入到map中
            map.put("shopId", shopId);
            map.put("resultNum", resultNum);
            map.put("imageNum", imageNum);
            // 创建分页对象并根据map中的页码和每页数量初始化
            Page<CommentsEntityVO> page = new Page<>((Integer) map.get("page"), (Integer) map.get("limit"));
            // 执行查询并获取评论数据
            IPage<CommentsEntityVO> CommentsQueryList = orderGoodsEvaluationMapper.commentsQuery(page, map);
            // 遍历评论数据，设置评价状态和处理图片信息
            for (CommentsEntityVO record : CommentsQueryList.getRecords()) {
                if (StrUtil.isNotEmpty(record.getContent())) {
                    record.setEvaluateStatus(1);
                    if (StrUtil.isNotEmpty(record.getExplainContent())) {
                        record.setEvaluateStatus(2);
                    }
                } else {
                    record.setEvaluateStatus(0);
                }
                // 处理记录的图片信息，包括主图和附加图
                if (record.getImage() != null) {
                    String[] imagesList = record.getImage().replace(" ", "").split(",");
                    record.setImagesList(imagesList);
                }
                if (record.getAdditionalImage() != null) {
                    String[] additionalImagesList = record.getAdditionalImage().replace(" ", "").split(",");
                    record.setAdditionalImagesList(additionalImagesList);
                }
            }
            // 构建成功响应结果，并返回评论数据的分页处理结果
            return ResponseResult.success(PageUtils.getPage(CommentsQueryList));
        } catch (Exception e) {
            // 捕获异常，构建失败响应结果，并返回错误信息
            return ResponseResult.fail(500, "GetAllCommentsErrorResults:" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseResult replyToComment(Map<String, Object> map) {
        // 从传入的map中获取token值，用于后续的token有效性检查
        String token = (String) map.get("token");
        // 检查token在Redis中的存在性，确保用户会话的有效性
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        // 如果token无效，则返回相应的错误信息
        if (!isExistToken) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 更新map中的UpdateTime字段为当前时间，确保数据的时效性
        map.put("updateTime", TimeStampUtil.getNowDate());
        // 尝试更新回复信息，这里假设baseMapper中的updateForReply方法用于更新数据
        boolean isUpdateForReply = this.baseMapper.updateForReply(map);
        // 如果更新成功，则返回成功的响应信息
        if (isUpdateForReply) {
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        // 如果更新失败，则返回失败的响应信息
        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);

    }

    @Override
    @Transactional
    public ResponseResult add(Map<String, Object> map) {
        // 从传入的map中获取token值，用于后续的token有效性检查
        String token = (String) map.get("token");
        // 检查token在Redis中的存在性，确保用户会话的有效性
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        // 如果token无效，则返回相应的错误信息
        if (!isExistToken) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 更新map中的UpdateTime字段为当前时间，确保数据的时效性
        map.put("updateTime", TimeStampUtil.getNowDate());
        // 尝试更新回复信息的相关数据，返回更新操作的结果
        boolean isUpdateForReply = this.baseMapper.updateForAdditionalReply(map);
        // 如果更新成功，则返回成功的响应；否则返回失败的响应
        if (isUpdateForReply) {
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);

    }

    @Override
    public ResponseResult insertEvaluated(AcquiescenceVO acquiescenceVO) {
        // 创建OrderGoodsEvaluationEntity实例，用于存储评价信息
        OrderGoodsEvaluationEntity evaluationEntity=new OrderGoodsEvaluationEntity();

        // 将AcquiescenceVO对象的属性值复制到evaluationEntity对象中
        // 这里使用BeanUtils.copyProperties是为了减少手动赋值的错误和提高代码的可维护性
        BeanUtils.copyProperties(acquiescenceVO, evaluationEntity);

        // 设置评价实体的用户ID，该ID来源于acquiescenceVO对象
        // 这一步是为确保评价信息与购买者关联起来
        evaluationEntity.setUserId(acquiescenceVO.getBuyerUserId());

        // 将评价实体插入数据库，通过baseMapper的insert方法实现
        // insert方法返回值为插入的行数，用于判断操作是否成功
        int flag=this.baseMapper.insert(evaluationEntity);

        // 根据插入操作的结果判断返回值
        // 如果插入成功（即flag==1），返回成功的响应结果
        if(flag==1){
            return  ResponseResult.success();
        }else {
            return  ResponseResult.fail();
        }

    }

    /**
     * 处理骑手评价信息的提交。
     *
     * @param riderEvaluationDTO 包含骑手评价详细信息的数据传输对象。
     * @return 根据评价提交是否成功返回相应的响应结果。
     */
    @Override
    public ResponseResult riberEvaluated(RiderEvaluationDTO riderEvaluationDTO) {
        // 检查令牌是否存在，以验证用户身份
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + riderEvaluationDTO.getToken());
        if (!isExistToken) {
            // 如果令牌无效，返回错误响应
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 通过令牌获取用户信息，用于后续的用户关联操作
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken( riderEvaluationDTO.getToken());
        // 将用户ID设置到评价DTO中，用于插入评价数据时关联用户
        riderEvaluationDTO.setUserId(ui.getUserId());

        // 将骑手评价信息插入数据库
        int flag=this.baseMapper.insertRiberEvaluated(riderEvaluationDTO);
        // 更新订单的评价状态
        // 修改订单评价状态
        orderBaseMapper.updateOrderRiberEvaluated(riderEvaluationDTO.getOrderId());
        if(flag==1){
            // 如果插入成功，返回成功响应
            return  ResponseResult.success();
        }else {
            // 如果插入失败，返回失败响应
            return  ResponseResult.fail();
        }
    }

    private static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }
}
