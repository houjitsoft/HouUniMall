package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserPlusOrderEntity;
import com.houjiang.userms.mapper.UserPlusOrderMapper;
import com.houjiang.userms.service.UserPlusOrderService;
import com.houjiang.userms.vo.PlusOrderEntityExpVO;
import com.houjiang.userms.vo.PlusOrderEntityVO;
import io.seata.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UserPlusOrderServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023-07-04 09:36:32
 * @Version 1.0
 * @author Amos
 */
@Service
public class UserPlusOrderServiceImpl extends ServiceImpl<UserPlusOrderMapper, UserPlusOrderEntity>
        implements UserPlusOrderService {

    /**
     * 根据用户令牌获取充值记录
     *
     * @param pageUserTokenBeanDTO 包含用户令牌和分页信息的数据传输对象
     * @return 返回充值记录的响应结果
     */
    @Override
    public ResponseResult get(PageUserTokenBeanDTO pageUserTokenBeanDTO) {
        // 初始化一个哈希映射用于存储数据
        Map<String, Object> hashMap = new HashMap<>();
        if(null == pageUserTokenBeanDTO.getUserId() || !StringUtils.isNotBlank(pageUserTokenBeanDTO.getUserId())){
            // 尝试通过令牌获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(pageUserTokenBeanDTO.getToken());

            // 如果用户信息为空，则令牌无效，返回失败响应
            if (null == userInfo ) {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
            }
            // 设置用户ID到pageUserTokenBeanDTO对象中，用于后续的查询
            pageUserTokenBeanDTO.setUserId(userInfo.getUserId());
        }

        try {
            // 将pageUserTokenBeanDTO对象转换为Map，用于查询条件构建
            // 查询会员充值列表
            Map data = ParseMapUtils.beanToMap(pageUserTokenBeanDTO);
            // 执行分页查询充值记录
            // 分页查询数据
            IPage<PlusOrderEntityVO> page = this.baseMapper.selectplusorder(new Query<PlusOrderEntityVO>().getPage(data),pageUserTokenBeanDTO);

            // 返回查询结果的成功响应
            return ResponseResult.success(PageUtils.getPage(page));
        } catch (IllegalAccessException e) {
            // 如果发生异常，打印堆栈跟踪并返回失败响应
            e.printStackTrace();
        }
        // 如果执行到此处，表示查询过程中发生未知异常，返回失败响应
        return ResponseResult.fail(BizCodeEnume.UNKNOW_EXCEPTION.getCode(),BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
    /**
     * 根据传入的参数查询会员充值记录，并导出为Excel文件。
     *
     * @param map 包含查询条件和其他参数的映射表。
     * @return 返回生成的Excel文件的URL。
     */
    @Override
    public ResponseResult exp(Map<String, Object> map) {
        Map<String, Object> hashMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 从Redis中获取用户信息，验证token的有效性
        String userInfoStr = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + map.get("token"));
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 处理查询参数中的startTime和endTime，如果为空字符串，则设置为null
        if (map.get("startTime") == ""){
            map.put("startTime",null);
        }
        if (map.get("endTime") == ""){
            map.put("endTime",null);
        }
        // 定义Excel表格的标题
        String[] title = {"会员ID", "会员名称", "会员手机号", "套餐类型", "本次开通金额", "充值方式", "本次生效时间", "本次到期时间","支付时间","充值单号"};
        List<Map<String, String>> list = Lists.newArrayList();
        // 根据查询条件查询会员充值列表
        //查询会员充值列表
        IPage<PlusOrderEntityExpVO> page=this.baseMapper.selectrechargelistexp(new Query<PlusOrderEntityExpVO>().getPage(map),
                map);
        // 遍历查询结果，格式化数据，并添加到list中
        for (PlusOrderEntityExpVO plusOrderEntity : page.getRecords()) {
            // 根据套餐类型设置套餐类型名称
            if (plusOrderEntity.getThaliType() == 1){
                plusOrderEntity.setThaliTypes("月卡");
            }else if (plusOrderEntity.getThaliType() == 2){
                plusOrderEntity.setThaliTypes("季卡");
            }else if (plusOrderEntity.getThaliType() == 3){
                plusOrderEntity.setThaliTypes("年卡");
            }

            // 根据支付方式设置支付方式名称
            //支付方式 1-余额支付，2-购物卡，3-微信支付，4 支付宝 5 平台赠送
            if (plusOrderEntity.getPayType() == 1){
                plusOrderEntity.setPayTypes("余额支付");
            }else if(plusOrderEntity.getPayType() == 2){
                plusOrderEntity.setPayTypes("购物卡");
            }else if(plusOrderEntity.getPayType() == 3){
                plusOrderEntity.setPayTypes("微信支付");
            }else if(plusOrderEntity.getPayType() == 4){
                plusOrderEntity.setPayTypes("支付宝");
            }else if(plusOrderEntity.getPayType() == 5){
                plusOrderEntity.setPayTypes("平台赠送");
            }

            // 将格式化后的数据添加到list中
            HashMap<String, String> map1 = com.google.common.collect.Maps.newHashMap();
            map1.put("会员ID", plusOrderEntity.getUserId() + "");
            map1.put("会员名称", plusOrderEntity.getUserName() + "");
            map1.put("会员手机号", plusOrderEntity.getUserMobile() + "");
            map1.put("套餐类型", plusOrderEntity.getThaliTypes() + "");
            map1.put("本次开通金额", plusOrderEntity.getPayPrice() + "");
            map1.put("充值方式", plusOrderEntity.getPayTypes() + "");
            map1.put("本次生效时间", dateFormat.format(plusOrderEntity.getStartTime()) + "");
            map1.put("本次到期时间", dateFormat.format(plusOrderEntity.getEndTime()) + "");
            map1.put("支付时间", dateFormat.format(plusOrderEntity.getCreatTime()) + "");
            map1.put("充值单号", plusOrderEntity.getPlusOrderid() + "");

            list.add(map1);
        }
        // 检查查询结果数量，如果超过30000条，则返回错误
        if(page.getRecords().size()>=30000){
            return ResponseResult.fail(BizCodeEnume.PLEASE_BE_LESS_THAN_30000_PIECES);
        }
        // 准备导出的Excel数据和标题，生成Excel文件
        // excel文件名
        Map<String, List<Map<String, String>>> map2 = Maps
                .newHashMap();
        map2.put("合并数据", list);
        String url = ExcelsUtil.createExcel(title, map2, new int[]{0});
        return ResponseResult.success(url);
    }



    /**
     * 根据传入的参数查询会员充值列表，并返回充值统计结果。
     *
     * @param map 包含查询条件和其他参数的映射表，其中"token"用于验证用户权限，
     *            "startTime"和"endTime"指定查询的时间范围。
     * @return 返回查询结果，如果用户信息无效，则返回失败响应。
     */
    @Override
    public ResponseResult list(Map<String, Object> map) {
        // 从Redis中获取用户信息，验证用户token的有效性
        String userInfoStr = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + map.get("token"));
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        // 处理查询时间范围的参数，如果为空则设置为null，以便后续处理
        if (map.get("startTime") == ""){
            map.put("startTime",null);
        }
        if (map.get("endTime") == ""){
            map.put("endTime",null);
        }

        // 执行查询，获取会员充值列表的分页结果
        //查询会员充值列表
        IPage<Map<String,Object>> page=this.baseMapper.selectplusorderMap(new Query<Map<String,Object>>().getPage(map),
                map);

        // 计算当前月份的开始和结束时间，用于统计本月的充值总额
        //获取当前月份第一天和最后一天
        LocalDate date = LocalDate.now();
        // 获取当前月的第一天
        LocalDate firstDay = date.with(TemporalAdjusters.firstDayOfMonth());
        // 获取当前月的最后一天
        LocalDate lastDay = date.with(TemporalAdjusters.lastDayOfMonth());

        LocalDateTime startTime = firstDay.atTime(0, 0, 0);
        LocalDateTime endTime = lastDay.atTime(23, 59, 59);

        // 构建用于统计本月充值总额的查询参数
        HashMap<String, Object> timemap = new HashMap<>();
        timemap.put("startTime",startTime);
        timemap.put("endTime",endTime);

        // 统计本月的充值总额
        Map<String,Object> result=new HashMap<>();
        result=this.baseMapper.selectPriceTotal(timemap);
        // 将充值列表的分页结果添加到统计结果中
        result.put("page",PageUtils.getPage(page));

        // 返回查询和统计结果的成功响应
        return ResponseResult.success(result);
    }

    /**
     * 创建阿里云微信支付PLUS订单
     * 本方法用于在阿里云微信支付系统中创建PLUS订单。通过传入一个Map对象，其中包含订单的各种信息，
     * 最终调用底层的mapper插入订单数据到数据库中。
     *
     * @param map 一个包含订单信息的Map对象。在这个方法中，我们会向这个Map中添加订单的开始时间、结束时间
     *            以及支付状态等信息，然后将这个Map对象传递给底层的mapper进行订单数据的插入操作。
     * @return 返回一个表示操作成功的结果对象。如果订单创建成功，将会返回一个成功的ResponseResult对象。
     */
    @Override
    public ResponseResult aLiWxCreatePlusOrder(Map<String, Object> map) {
        // 设置订单的开始时间和结束时间为当前时间
        map.put("startTime", new Date());
        map.put("endTime", new Date());

        // 设置订单的支付状态为未支付
        //是否已支付 0 否  1 是
        map.put("isPay", CommonType.commonStatus.NO.getCode());

        // 调用底层的mapper插入订单数据
        this.baseMapper.insertplusorder(map);

        // 返回表示操作成功的结果对象
        return ResponseResult.success();
    }

    @Override
    public Integer queryplusorder(String userId, String plusOrder) {
        Integer result = this.baseMapper.queryplusorder(userId,plusOrder);
        return result;
    }

    @Override
    public Integer queryStatusByPlus(String userId) {
        Integer result = this.baseMapper.queryStatusByPlus(userId);
        return result;
    }
}




