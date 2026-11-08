package com.houjiang.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.activity.dto.RedpacketSendUserDTO;
import com.houjiang.activity.dto.UserAvailableRedpacketQueryDTO;
import com.houjiang.activity.dto.UserRedIdDTO;
import com.houjiang.activity.dto.UserRedpacketSendDTO;
import com.houjiang.activity.entity.ActivityRedpacketTemplateEntity;
import com.houjiang.activity.mapper.ActivityRedpacketBaseMapper;
import com.houjiang.activity.mapper.ActivityRedpacketTemplateMapper;
import com.houjiang.activity.service.ActivityRedpacketTemplateService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.activity.ActivityRedpacketTemplateVO;
import com.houjiang.commons.vo.activity.HouRedpacketTemplateVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.apache.poi.ss.usermodel.CellType.STRING;

/**
 * @author Amos
 */
@Service
public class ActivityRedpacketTemplateServiceImpl extends ServiceImpl<ActivityRedpacketTemplateMapper, ActivityRedpacketTemplateEntity>
    implements ActivityRedpacketTemplateService{

    @Autowired
    private ActivityRedpacketBaseMapper activityRedpacketBaseMapper;
    @Autowired
    private RedisCache redisCache;
    /**
     * 根据ID列表获取活动红包模板信息。
     *
     * @param paraMap 参数映射表，其中应包含一个名为"id"的列表，列表中包含需要查询的红包模板ID。
     * @return 返回一个包含查询结果的ResponseResult对象。如果查询成功，结果将是活动红包模板的列表；如果查询失败，结果将是空列表。
     */
    @Override
    public ResponseResult getList(Map<String, Object> paraMap) {
        // 从参数映射中提取ID列表
        List<Integer> id = (List<Integer>) paraMap.get("id");
        List<ActivityRedpacketTemplateVO> entityList = new ArrayList<>();

        // 如果ID列表不为空且不为空列表，则根据ID列表查询红包模板信息
        if (id != null && id.size() > 0) {
            entityList = this.baseMapper.selectNewRedpacketTem(id);
        }

        // 返回查询结果，如果查询成功，将返回包含模板信息的列表；否则，返回空列表
        return ResponseResult.success(entityList);
    }

    /**
     * 根据条件分页查询活动红包模板信息。
     *
     * @param map 包含查询条件和其他参数的映射表。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult selectByPage(Map<String, Object> map) {
        // 从Redis中获取用户信息，根据token拼接key
        String userInfoStr = (String) redisCache.getCacheObject(HouConfig.HOU_PLATFORM_TOKEN_USER + String.valueOf(map.get("token")));
        // 检查用户信息是否有效
        if (null == userInfoStr || userInfoStr.equals("")) {
            // 如果用户信息无效，返回相应的错误响应
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        // 使用Lambda表达式构建查询条件，查询满足条件的活动红包模板信息
        IPage<ActivityRedpacketTemplateEntity> page = this.page(
                new Query<ActivityRedpacketTemplateEntity>().getPage(map),
                new LambdaQueryWrapper<ActivityRedpacketTemplateEntity>()
                    .eq(ActivityRedpacketTemplateEntity::getRedpacketTType, CommonType.redpacketTType.PLUS_MEMBERSHIP.getCode())
                    .eq(map.containsKey("redpacketTOrderlimit") && map.get("redpacketTOrderlimit").toString().equals("0"), ActivityRedpacketTemplateEntity::getRedpacketTOrderlimit, 0)
                    .ne(!map.containsKey("redpacketTOrderlimit") || !map.get("redpacketTOrderlimit").toString().equals("0"), ActivityRedpacketTemplateEntity::getRedpacketTOrderlimit, 0)
                    .orderByDesc(ActivityRedpacketTemplateEntity::getRedpacketTStartDate)
        );

        // 构建并返回查询成功的响应结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据条件查询红包模板列表。
     *
     * @param map 包含查询条件的Map对象。
     * @return 返回查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult selectList(Map<String, Object> map) {
        // 检查map中是否包含redpacketTTitle键，并判断其值是否为空
        boolean flag = map.containsKey("redpacketTTitle");
        if (flag) {
            flag = StringUtils.isNotEmpty(map.get("redpacketTTitle").toString());
        }

        // 使用LambdaQueryWrapper构造查询条件，动态地根据map中的值设置查询条件
        // 根据redpacketTState进行等于查询，如果map中包含该键且值不为空
        // 根据redpacketTTitle进行like查询，如果map中包含该键且值不为空
        // 按redpacketTStartDate降序排序
        IPage<ActivityRedpacketTemplateEntity> page = this.page(
                new Query<ActivityRedpacketTemplateEntity>().getPage(map),
                new LambdaQueryWrapper<ActivityRedpacketTemplateEntity>()
                        .eq(null != map.get("redpacketTState") && !map.get("redpacketTState").toString().isEmpty(), ActivityRedpacketTemplateEntity::getRedpacketTState, null != map.get("redpacketTState") ? map.get("redpacketTState").toString() : null)
                        .like(flag, ActivityRedpacketTemplateEntity::getRedpacketTTitle, flag ? map.get("redpacketTTitle").toString() : null)
                        .orderByDesc(ActivityRedpacketTemplateEntity::getRedpacketTStartDate));

        // 返回处理后的查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 大转盘 查询红包列表(有效红包)
     *
     * @param map
     * @return
     */
    @Override
    public ResponseResult spinwinSelectList(Map<String, Object> map) {
        // 检查map中是否包含redpacketTTitle键，并判断其值是否为空
        boolean flag = map.containsKey("redpacketTTitle");
        if (flag) {
            flag = StringUtils.isNotEmpty(map.get("redpacketTTitle").toString());
        }

        // 使用LambdaQueryWrapper构造查询条件，动态地根据map中的值设置查询条件
        // 根据redpacketTState进行等于查询，如果map中包含该键且值不为空
        // 根据redpacketTTitle进行like查询，如果map中包含该键且值不为空
        // 按redpacketTStartDate降序排序
        IPage<ActivityRedpacketTemplateEntity> page = this.page(
                new Query<ActivityRedpacketTemplateEntity>().getPage(map),
                new LambdaQueryWrapper<ActivityRedpacketTemplateEntity>()
                        .eq(null != map.get("redpacketTState") && !map.get("redpacketTState").toString().isEmpty(), ActivityRedpacketTemplateEntity::getRedpacketTState, null != map.get("redpacketTState") ? map.get("redpacketTState").toString() : null)
                        .eq(ActivityRedpacketTemplateEntity::getRedpacketTType, CommonType.redpacketTType.ORDINARY.getCode())
                        .like(flag, ActivityRedpacketTemplateEntity::getRedpacketTTitle, flag ? map.get("redpacketTTitle").toString() : null)
                        .orderByDesc(ActivityRedpacketTemplateEntity::getRedpacketTStartDate));

        // 返回处理后的查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }
    /**
     * 查询可用的活动红包模板
     *
     * @param map 查询参数，包含分页信息和过滤条件
     * @return 返回查询结果，包含分页信息和可用的活动红包模板列表
     */
    @Override
    public ResponseResult available(Map<String, Object> map) {
        // 根据查询参数获取可用的活动红包模板分页数据
        IPage<ActivityRedpacketTemplateEntity> page = this.baseMapper.available(
                new Query<ActivityRedpacketTemplateEntity>().getPage(map), map);

        // 将分页数据包装成响应结果返回
        return ResponseResult.success(PageUtils.getPage(page));
    }
    /**
     * 导入用户红包信息并发送
     *
     * @param file 用户红包信息的Excel文件
     * @param redpacketTId 红包模板ID
     * @param token 用户标识token
     * @return 发送结果
     */
    @Override
    public ResponseResult sendUserRedpacketImport(MultipartFile file, Integer redpacketTId, String token) {
        List<UserRedpacketSendDTO> userRedpacket = Lists.newArrayList();
        List<RedpacketSendUserDTO> userInfos = Lists.newArrayList();

        // 尝试解析上传的Excel文件
        // 解析文件 遍历用户数据
        try {
            Workbook wookbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = wookbook.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();

            // 遍历Excel中的行，从第二行开始（第一行为表头）
            for (int i = 1; i < rows; i++) {
                Row row = sheet.getRow(i);
                RedpacketSendUserDTO userInfo = new RedpacketSendUserDTO();

                // 如果行或单元格为空，则跳过当前循环
                if (null == row || row.getPhysicalNumberOfCells() == 0) {
                    continue;
                }

                // 从Excel中读取用户ID和用户名信息
                Cell userId = row.getCell(0);
                if(null != userId) {
                    userInfo.setUserId(getValue(userId));
                }
                Cell userMobile = row.getCell(1);
                if(null != userMobile) {
                    userInfo.setUserName(getValue(userMobile));
                }

                userInfos.add(userInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 根据红包模板ID查询红包模板信息
        UserRedpacketSendDTO redpacketTemplate = this.baseMapper.selectByRedpacketTId(redpacketTId);

        // 计算剩余可发送的红包数量
        Integer surplusNum = redpacketTemplate.getRedpacketTTotal() - redpacketTemplate.getRedpacketTGiveout();

        // 如果剩余红包数量小于待发送的用户数量，返回错误信息
        if (surplusNum.intValue() < userInfos.size()) {
            return ResponseResult.fail(BizCodeEnume.TOTAL_NUMBER_OF_RED_ENVELOPES);
        }

        // 遍历用户信息列表，为每个用户创建红包记录
        List<UserRedIdDTO> userIdList = new ArrayList<>();
        for (RedpacketSendUserDTO user : userInfos) {
            UserRedpacketSendDTO newRedpacketTemplate = new UserRedpacketSendDTO();
            UserRedIdDTO pojo = new UserRedIdDTO();
            newRedpacketTemplate.setRedpacketOwnerId(user.getUserId());
            newRedpacketTemplate.setRedpacketOwnerName(user.getUserName());

            newRedpacketTemplate.setRedpacketTId(redpacketTemplate.getRedpacketTId());
            newRedpacketTemplate.setRedpacketTTitle(redpacketTemplate.getRedpacketTTitle());
            newRedpacketTemplate.setRedpacketTDesc(redpacketTemplate.getRedpacketTDesc());
            newRedpacketTemplate.setRedpacketTStartDate(redpacketTemplate.getRedpacketTStartDate());
            newRedpacketTemplate.setRedpacketTEndDate(redpacketTemplate.getRedpacketTEndDate());
            newRedpacketTemplate.setRedpacketActiveDate(redpacketTemplate.getRedpacketActiveDate());
            newRedpacketTemplate.setRedpacketTPrice(redpacketTemplate.getRedpacketTPrice());
            newRedpacketTemplate.setRedpacketTOrderlimit(redpacketTemplate.getRedpacketTOrderlimit());
            newRedpacketTemplate.setRedpacketState(redpacketTemplate.getRedpacketState());
            newRedpacketTemplate.setRedpacketTTotal(redpacketTemplate.getRedpacketTTotal());
            newRedpacketTemplate.setRedpacketTGiveout(redpacketTemplate.getRedpacketTGiveout());
            newRedpacketTemplate.setRedpacketType(redpacketTemplate.getRedpacketType());
            newRedpacketTemplate.setShopLimit(redpacketTemplate.getShopLimit());
            newRedpacketTemplate.setGoodsCatLimit(redpacketTemplate.getGoodsCatLimit());

            // 设置用户红包的特定属性
            newRedpacketTemplate.setRedpacketType(6);
            newRedpacketTemplate.setRedpacketState(1);
            newRedpacketTemplate.setRedpacketActiveDate(new Date());

            userRedpacket.add(newRedpacketTemplate);

            // 构建与红包关联的用户ID列表
            pojo.setUserId(String.valueOf(user.getUserId()));
            pojo.setRedpacketTPrice(String.valueOf(redpacketTemplate.getRedpacketTPrice()));
            userIdList.add(pojo);
        }

        // 批量更新用户红包信息
        activityRedpacketBaseMapper.updateUserRedpacketBatch(userRedpacket);
        // 更新红包模板的已发送数量
        this.baseMapper.updateRedpacketTGiveout(userInfos.size(),redpacketTId);

        // 返回操作成功提示
        return ResponseResult.fail(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 根据用户信息发送红包
     * @param param 包含红包模板ID和接收红包的用户信息
     * @return 发送结果
     */
    @Override
    public ResponseResult sending(UserRedpacketSendDTO param) {
        // 获取红包模板ID
        Integer redpacketTId = param.getRedpacketTId();
        // 获取接收红包的用户信息列表
        //要赠送的用户信息
        List<RedpacketSendUserDTO> userInfos = param.getUserInfos();

        // 初始化待发送的红包列表
        List<UserRedpacketSendDTO> userRedpacket = Lists.newArrayList();
        // 根据红包模板ID查询红包模板信息
        //查询红包信息
        UserRedpacketSendDTO redpacketTemplate = this.baseMapper.selectByRedpacketTId(redpacketTId);
        // 计算红包模板中剩余可发送的数量
        //计算剩余红包数
        Integer surplusNum = redpacketTemplate.getRedpacketTTotal() - redpacketTemplate.getRedpacketTGiveout();

        // 检查剩余数量是否足够发送给所有用户
        if (surplusNum.intValue() < userInfos.size()) {
            return ResponseResult.fail(BizCodeEnume.TOTAL_NUMBER_OF_RED_ENVELOPES);
        }

        // 初始化接收红包的用户ID列表
        List<UserRedIdDTO> userIdList = new ArrayList<>();
        // 遍历用户信息列表，创建并填充每个用户的红包信息
        //封装数据赠送红包
        for (RedpacketSendUserDTO user : userInfos) {
            UserRedIdDTO pojo = new UserRedIdDTO();
            UserRedpacketSendDTO newRedpacketTemplate = new UserRedpacketSendDTO();

            // 设置用户ID和用户名
            newRedpacketTemplate.setRedpacketOwnerId(user.getUserId());
            newRedpacketTemplate.setRedpacketOwnerName(user.getUserName());

            // 复制红包模板的其他信息
            newRedpacketTemplate.setRedpacketTId(redpacketTemplate.getRedpacketTId());
            newRedpacketTemplate.setRedpacketTTitle(redpacketTemplate.getRedpacketTTitle());
            newRedpacketTemplate.setRedpacketTDesc(redpacketTemplate.getRedpacketTDesc());
            newRedpacketTemplate.setRedpacketTStartDate(redpacketTemplate.getRedpacketTStartDate());
            newRedpacketTemplate.setRedpacketTEndDate(redpacketTemplate.getRedpacketTEndDate());
            newRedpacketTemplate.setRedpacketActiveDate(redpacketTemplate.getRedpacketActiveDate());
            newRedpacketTemplate.setRedpacketTPrice(redpacketTemplate.getRedpacketTPrice());
            newRedpacketTemplate.setRedpacketTOrderlimit(redpacketTemplate.getRedpacketTOrderlimit());
            newRedpacketTemplate.setRedpacketState(redpacketTemplate.getRedpacketState());
            newRedpacketTemplate.setRedpacketTTotal(redpacketTemplate.getRedpacketTTotal());
            newRedpacketTemplate.setRedpacketTGiveout(redpacketTemplate.getRedpacketTGiveout());
            newRedpacketTemplate.setRedpacketType(redpacketTemplate.getRedpacketType());
            newRedpacketTemplate.setShopLimit(redpacketTemplate.getShopLimit());
            newRedpacketTemplate.setGoodsCatLimit(redpacketTemplate.getGoodsCatLimit());
            newRedpacketTemplate.setRedpacketFrom(redpacketTemplate.getRedpacketFrom());

            // 设置发送状态和类型
            newRedpacketTemplate.setRedpacketType(6);
            newRedpacketTemplate.setRedpacketState(1);
            newRedpacketTemplate.setRedpacketActiveDate(new Date());

            // 将新创建的红包信息添加到待发送列表
            userRedpacket.add(newRedpacketTemplate);

            // 构建用户ID和红包金额的映射，用于后续操作
            pojo.setUserId(String.valueOf(user.getUserId()));
            pojo.setRedpacketTPrice(String.valueOf(redpacketTemplate.getRedpacketTPrice()));
            userIdList.add(pojo);
        }

        // 批量更新用户的红包信息
        activityRedpacketBaseMapper.updateUserRedpacketBatch(userRedpacket);
        // 更新红包模板的已发送数量
        this.baseMapper.updateRedpacketTGiveout(userInfos.size(),redpacketTId);
        // 返回操作成功的结果
        return ResponseResult.fail(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 查询已发出的红包列表
     *
     * @return ResponseResult 包含查询结果的响应对象
     */
    @Override
    public ResponseResult giveList() {
        // 调用baseMapper的giveList方法查询已发出的红包信息
        List<ActivityRedpacketTemplateEntity> list = this.baseMapper.giveList();
        // 将查询结果包装在成功响应结果中返回
        return ResponseResult.success(list);
    }
    /**
     * 更新红包模板信息。
     *
     * @param paraMap 包含红包模板相关更新参数的映射。
     * @return 根据操作结果返回相应的响应结果。
     */
    @Override
    public ResponseResult updateRedpacketTemp(Map<String, Object> paraMap) {
        // 查询当前红包模板的总数量
        Integer redpacketCount = this.baseMapper.selectById(String.valueOf(paraMap.get("redpacketTId"))).getRedpacketTTotal();
        // 如果有增加红包数量的参数，则更新红包总数量
        if (null != paraMap.get("addRedCount") && !("".equals(paraMap.get("addRedCount")))) {
            Integer redpacketTTotal = redpacketCount + Integer.parseInt(String.valueOf(paraMap.get("addRedCount")));
            paraMap.put("redpacketTTotal", redpacketTTotal);
        }
        // 更新红包模板信息
        Integer torf = this.baseMapper.updateRedpacketTemp(paraMap);
        // 检查红包是否在有效期内，如果在有效期内，可以修改红包状态
        // 修改用户领到手的红包状态
        // 查询当前红包是否在 有效期区间内
        Integer start = this.baseMapper.selectRedpacketTime(paraMap);
        if (null != start && start > 0) {
            // 如果有设置红包状态的参数，则更新用户领取的红包状态
            // 红包在有效期内才可以修改用户领到手的 红包状态
            if (null != paraMap.get("redpacketTState")) {
                // 修改用户到手的红包状态
                activityRedpacketBaseMapper.updateRedpacketUserStart(paraMap);
            }
        }
        // 根据更新结果返回相应的响应
        if (1 == torf) {
            return ResponseResult.fail(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
    }
    /**
     * 插入红包模板信息
     * @param paraMap 包含红包相关参数的映射
     * @return 插入操作的结果
     */
    @Override
    public ResponseResult insertRedpacketTemp(Map<String, String> paraMap) {
        // 解析红包面额
        // 红包面额
        if (paraMap.get("redpacketTPrice") == null || "".equals(paraMap.get("redpacketTPrice"))){
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
        }
        Double redpacketTPrice = Double.parseDouble(paraMap.get("redpacketTPrice"));
        // 解析订单限额
        // 订单限额
        if (paraMap.get("redpacketTOrderlimit") == null || "".equals(paraMap.get("redpacketTOrderlimit"))){
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
        }
        Double redpacketTOrderlimit = Double.parseDouble(paraMap.get("redpacketTOrderlimit"));
        // 获取用户令牌
        String token = paraMap.get("token");
        // 根据令牌获取用户信息
        UserInfoVO ui = ParseTokenUtils.getPlatformUser(token);
        // 获取当前时间，用于设置操作时间和红包失效时间
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String finishTime = dateformat.format(new Date());

        // 用户姓名，用于记录操作人
        String userRealname = ui.getUserRealname();
        // 设置操作人和操作时间
        paraMap.put("orderOperator", userRealname);
        paraMap.put("orderOperatorTime", finishTime);

        // 校验订单限额是否小于红包面额
        if (redpacketTOrderlimit != 0){
            if (redpacketTOrderlimit < redpacketTPrice) {
                return ResponseResult.fail(BizCodeEnume.ORDER_LIMIT_CANNOT_BE_LESS);
            }
        }

        // 默认设置红包使用和领取平台为0（全部），如果未明确指定
        if (null == paraMap.get("redpacketUsePlatform") || "".equals(paraMap.get("redpacketUsePlatform"))) {
            paraMap.put("redpacketUsePlatform", "0");
        }
        if (null == paraMap.get("redpacketGetPlatform") || "".equals(paraMap.get("redpacketGetPlatform"))) {
            paraMap.put("redpacketGetPlatform", "0");
        }

        // 如果红包类型为7（plus会员专享红包），则设置特定的红包属性
        //plus会员
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        if (paraMap.get("redpacketTType").equals("7")){
            paraMap.put("redpacketTStartDate",format.format(date));
            paraMap.put("shopLimit","1");
            paraMap.put("frontEndIsDisplay","1");
            paraMap.put("redpacketIsBooking","1");
            paraMap.put("redpacketTEachlimit","0");
            paraMap.put("redpacketTTotal","0");
            paraMap.put("redpacketUsePlatform","0");
            paraMap.put("redpacketTGiveout","0");
            paraMap.put("redpacketTEndDate",null);
            paraMap.put("robRedpacketStartDate",null);
            paraMap.put("robRedpacketEndDate",null);
        }

        // 尝试插入红包模板信息
        Integer torf = this.baseMapper.insertRedpacketTemp(paraMap);
        // 根据插入结果返回相应的响应
        if (1 == torf) {
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
    }

    /**
     * 根据红包模板ID获取红包详情。
     *
     * @param paraMap 参数映射表，包含红包模板的ID（redpacketTId）。
     * @return 返回包含红包详情的响应结果。
     */
    @Override
    public ResponseResult getRedpacketDetail(Map<String, Object> paraMap) {
        // 通过红包模板ID查询红包模板信息
        ActivityRedpacketTemplateEntity entity = this.baseMapper.selectById(paraMap.get("redpacketTId").toString());
        // 返回成功的响应结果，其中包含查询到的红包模板信息
        return ResponseResult.success(entity);
    }
    /**
     * 获取单元格的值，将不同类型的单元格值转换为字符串形式。
     *
     * @param xSSFCell 单元格对象，可能为null。
     * @return 单元格的值，如果单元格为null，则返回空字符串。
     */
    private static String getValue(Cell xSSFCell) {
        // 如果单元格为null，直接返回空字符串
        if (null == xSSFCell) {
            return "";
        }
        String cellValue = "";
        // 根据单元格的类型处理
        // 以下是判断数据的类型
        switch (xSSFCell.getCellType()) {
            // 数字类型单元格，转换为字符串形式
            // 数字
            case NUMERIC:
                // 将数字单元格类型转换为字符串类型，以确保获取的是字符串形式的值
                xSSFCell.setCellType(STRING);
                cellValue = xSSFCell.getStringCellValue();
                break;
            // 字符串类型单元格，直接获取字符串值
            // 字符串
            case STRING:
                cellValue = xSSFCell.getStringCellValue();
                break;
            // 布尔类型单元格，将布尔值转换为字符串
            // Boolean
            case BOOLEAN:
                cellValue = xSSFCell.getBooleanCellValue() + "";
                break;
            // 公式类型单元格，获取公式表达式作为字符串
            // 公式
            case FORMULA:
                cellValue = xSSFCell.getCellFormula() + "";
                break;
            // 空单元格，返回空字符串
            // 空值
            case BLANK:
                cellValue = "";
                break;
            // 错误类型单元格，返回空字符串
            // 故障
            case ERROR:
                cellValue = "";
                break;
            // 默认情况，包括未定义的单元格类型，也返回空字符串
            default:
                cellValue = "";
                break;
        }
        return cellValue;
    }

    /**
     * 查询预约红包列表
     *
     * @param param 查询条件参数
     * @return 查询结果，包含预约红包的信息以及是否已领取的状态
     */
    @Override
    public ResponseResult queryBookingRpList(UserAvailableRedpacketQueryDTO param) {
        // 查询所有预约红包信息
        List<HouRedpacketTemplateVO> bookingRpList = this.baseMapper.selectBookingRpList();

        // 如果没有预约红包，返回成功结果并说明无倒计时红包
        if (null == bookingRpList) {
            return ResponseResult.success("无倒计时红包");
        }

        // 遍历预约红包列表，计算红包领取进度，并根据当前时间判断红包状态
        for (HouRedpacketTemplateVO bookingRp : bookingRpList) {
            // 计算红包领取进度，保留两位小数
            BigDecimal redPackageGetProgress = new BigDecimal(bookingRp.getRedpacketTGiveout())
                    .divide(new BigDecimal(bookingRp.getRedpacketTTotal()), 2, RoundingMode.HALF_UP);

            bookingRp.setRedPackageGetProgress(redPackageGetProgress);

            // 判断当前时间是否早于红包开始时间
            if (DateUtils.compareDate2(bookingRp.getRedpacketTStartDate())) {
                // 如果当前时间早于开始时间，计算距离开始时间的剩余时间，并设置红包状态为未领取
                bookingRp.setOverplusTime(DateUtils.getOverplusTime(new Date(), bookingRp.getRedpacketTStartDate()));
                bookingRp.setReceiveOrNot(CommonType.PromotionReceiveOrNot.BOOKING.getCode());

            } else {
                // 如果当前时间晚于或等于开始时间，根据红包发放数量和总数量判断红包是否可领取
                if (bookingRp.getRedpacketTGiveout().intValue() >= bookingRp.getRedpacketTTotal().intValue()) {
                    // 如果已发放数量大于等于总数量，设置红包状态为不可领取
                    bookingRp.setReceiveOrNot(CommonType.PromotionReceiveOrNot.NO_NUM_GET.getCode());
                } else {
                    // 如果还可领取，通过token获取用户信息，检查用户是否已领取该红包
                    // 获取用户信息
                    String token = param.getToken();
                    UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
                    if (null != userInfo) {
                        // 如果用户信息有效，设置用户ID
                        param.setRedpacketOwnerId(userInfo.getUserId());
                    } else {
                        // 如果用户信息无效，直接返回当前处理过的红包列表
                        return ResponseResult.success(bookingRpList);
                    }
                    param.setRedpacketTId(bookingRp.getRedpacketTId());
                    // 检查用户是否已领取该红包
                    Integer redpacketState = activityRedpacketBaseMapper.checkReceivedRpOrNot4(param);
                    // 根据检查结果设置红包领取状态
                    // ReceiveOrNot:0未领取1已领取2不可领取3已用4过期
                    if (null == redpacketState || redpacketState == 0) {
                        bookingRp.setReceiveOrNot(0);
                    } else {
                        bookingRp.setReceiveOrNot(1);
                    }
                }
            }
        }
        // 返回处理后的预约红包列表
        return ResponseResult.success(bookingRpList);
    }
}




