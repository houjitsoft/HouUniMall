package com.houjiang.userms.service.impl;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.MemberPortraitQueryDTO;
import com.houjiang.userms.dto.PortraitCrowdDTO;
import com.houjiang.userms.dto.PortraitSegmentDTO;
import com.houjiang.userms.dto.PortraitTagDTO;
import com.houjiang.userms.mapper.MemberPortraitMapper;
import com.houjiang.userms.service.MemberPortraitService;
import com.houjiang.userms.vo.MemberPortraitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 会员画像服务实现
 *
 * <p>计算口径（全部可解释，不引入外部模型）：</p>
 * <ul>
 *   <li>R/F/M 百分制：R＝距最近活跃（下单或登录）天数，{@code 100 - 天数×3}；
 *       F＝{@code 有效订单×12 + min(20, 登录次数/5)}；M＝累计有效消费 ÷ 100；</li>
 *   <li>R/F/M 五分制：按固定阈值分档，供 RFM 八分群使用，阈值写死在代码里以便复核；</li>
 *   <li>八分群：R/F/M 各以 3 分为高/低分界组合成 8 个象限；无有效订单的会员单独归入
 *       「下单未成交（待付款或已取消）」与「未成交会员」两档，避免把未成交人群强行塞进八象限；</li>
 *   <li>生命周期：未激活 → 新客 → 活跃 → 成长 → 沉默 → 流失，按真实登录/下单/注册时间判定；</li>
 *   <li>流失风险：未登录天数 + 无订单 + 长期未复购三项加权，输出 0-100。</li>
 * </ul>
 * 缺失数据不会被补成 0：浏览维度未采集时 {@code browseAvailable=false}，
 * 会员没有浏览记录时 {@code hasBrowseData=false}，由前端如实展示。</p>
 *
 * @author Amos
 */
@Slf4j
@Service
public class MemberPortraitServiceImpl implements MemberPortraitService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String LAYER_CORE = "core";
    private static final String LAYER_GROWTH = "growth";
    private static final String LAYER_EDGE = "edge";

    /** RFM 八分群 + 两个特殊档：key → {中文名, 运营动作}。顺序即页面展示顺序 */
    private static final Map<String, String[]> RFM_DEF = new LinkedHashMap<>();

    static {
        RFM_DEF.put("important_value", new String[]{"重要价值客户",
                "贡献最高的核心客群。给 VIP 专属价、新品提前购、专属客服，重点是别让他流失。"});
        RFM_DEF.put("important_develop", new String[]{"重要发展客户",
                "客单高但来得少。用组合套餐、会员日、周期购把购买频次提上来。"});
        RFM_DEF.put("important_keep", new String[]{"重要保持客户",
                "买得多且金额高，但最近没来。定期关怀 + 专属召回，先弄清楚为什么停了。"});
        RFM_DEF.put("important_retain", new String[]{"重要挽留客户",
                "曾经的高价值客户正在流失。大额召回券 + 人工回访，值得单独投入。"});
        RFM_DEF.put("general_value", new String[]{"一般价值客户",
                "来得勤、买得多但客单低。用满减、凑单、升级推荐把客单价拉起来。"});
        RFM_DEF.put("general_develop", new String[]{"一般发展客户",
                "近期活跃但只买过一两次。低门槛首单/二单券，先把复购习惯养出来。"});
        RFM_DEF.put("general_keep", new String[]{"一般保持客户",
                "买过多次但金额低且久未回来。做品类拓展与交叉销售，别只推原来那件。"});
        RFM_DEF.put("general_retain", new String[]{"一般挽留客户",
                "活跃度与贡献都低。只用低成本触达（短信/公众号），控制投入产出比。"});
        RFM_DEF.put("intent", new String[]{"下单未成交",
                "下过单但还没有有效成交（待付款或已取消）。仍是待付款的先催付，这是最短的成交路径；已取消的先弄清取消原因（价格、运费、时效），再决定要不要挽回。"});
        RFM_DEF.put("never_bought", new String[]{"未成交会员",
                "注册了但一笔订单都没有。先做激活与首单转化，谈复购为时过早。"});
    }

    /** 生命周期定义：key → 中文名 */
    private static final Map<String, String> LIFECYCLE_DEF = new LinkedHashMap<>();

    static {
        LIFECYCLE_DEF.put("active", "活跃期");
        LIFECYCLE_DEF.put("newbie", "新客期");
        LIFECYCLE_DEF.put("growing", "成长期");
        LIFECYCLE_DEF.put("silent", "沉默期");
        LIFECYCLE_DEF.put("churn", "流失期");
        LIFECYCLE_DEF.put("inactive", "未激活");
    }

    @Autowired
    private MemberPortraitMapper memberPortraitMapper;

    // ==================== 对外接口 ====================

    @Override
    public ResponseResult queryPortraitPage(MemberPortraitQueryDTO dto) {
        MemberPortraitQueryDTO query = dto == null ? new MemberPortraitQueryDTO() : dto;

        List<MemberPortraitVO> all = loadAllPortraits();
        List<MemberPortraitVO> matched = applyFilter(all, query.getKeyword(), query.getLayer(), null, null,
                query.getMinOrders(), null, null, null, null, null, null, null, null);

        // 汇总口径基于「过滤后的会员」，不受分页影响
        Map<String, Object> summary = buildSummary(matched, all);

        int total = matched.size();
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 20 : Math.min(query.getSize(), 200);
        List<MemberPortraitVO> records = slice(matched, page, size);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("current", page);
        data.put("size", size);
        data.put("summary", summary);
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult queryPortraitDetail(Long userId) {
        if (userId == null) {
            return ResponseResult.fail();
        }
        List<MemberPortraitVO> all = loadAllPortraits();
        MemberPortraitVO target = all.stream()
                .filter(vo -> userId.equals(vo.getUserId()))
                .findFirst()
                .orElse(null);
        if (target == null) {
            return ResponseResult.fail();
        }
        fillDetail(target);
        return ResponseResult.success(target);
    }

    @Override
    public ResponseResult queryRfmMatrix() {
        List<MemberPortraitVO> all = loadAllPortraits();

        List<Map<String, Object>> segments = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : RFM_DEF.entrySet()) {
            segments.add(buildRfmStat(all, entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }

        int rrHfH = 0, rrHfL = 0, rrLfH = 0, rrLfL = 0;
        for (MemberPortraitVO vo : all) {
            if (vo.getFScore5() == null || vo.getFScore5() == 0) {
                continue;
            }
            boolean rHigh = vo.getRScore5() != null && vo.getRScore5() >= 4;
            boolean fHigh = vo.getFScore5() >= 3;
            if (rHigh && fHigh) {
                rrHfH++;
            } else if (rHigh) {
                rrHfL++;
            } else if (fHigh) {
                rrLfH++;
            } else {
                rrLfL++;
            }
        }
        Map<String, Object> quadrant = new LinkedHashMap<>();
        quadrant.put("rHighFHigh", rrHfH);
        quadrant.put("rHighFLow", rrHfL);
        quadrant.put("rLowFHigh", rrLfH);
        quadrant.put("rLowFLow", rrLfL);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("segments", segments);
        data.put("quadrant", quadrant);
        data.put("total", all.size());
        data.put("tradedTotal", rrHfH + rrHfL + rrLfH + rrLfL);
        data.put("rule", "R/F/M 各按固定阈值打 1-5 分，以 3 分为高/低分界组合成八象限；"
                + "无有效订单的会员不进入八象限，按「下单未成交（待付款或已取消）」与「从未下单」单独成档");
        data.put("generatedAt", LocalDateTime.now().format(TS));
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult previewSegment(PortraitSegmentDTO dto) {
        PortraitSegmentDTO q = dto == null ? new PortraitSegmentDTO() : dto;
        List<MemberPortraitVO> all = loadAllPortraits();
        List<MemberPortraitVO> matched = applyFilter(all, q.getKeyword(), q.getLayer(), q.getRfmSegment(),
                q.getLifecycle(), q.getMinOrders(), q.getMaxOrders(), q.getMinAmount(), q.getMaxAmount(),
                q.getMinPending(), q.getInactiveDays(), q.getHasOrder(), q.getHasTag(), q.getTagIds());

        int page = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int size = q.getSize() == null || q.getSize() < 1 ? 20 : Math.min(q.getSize(), 200);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", matched.size());
        data.put("records", slice(matched, page, size));
        data.put("current", page);
        data.put("size", size);
        data.put("ruleText", describeRule(q));
        data.put("allMemberTotal", all.size());
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult saveCrowd(PortraitCrowdDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getCrowdName())) {
            return ResponseResult.fail("请填写人群包名称");
        }
        List<MemberPortraitVO> all = loadAllPortraits();
        List<MemberPortraitVO> matched = applyFilter(all, dto.getKeyword(), dto.getLayer(), dto.getRfmSegment(),
                dto.getLifecycle(), dto.getMinOrders(), dto.getMaxOrders(), dto.getMinAmount(), dto.getMaxAmount(),
                dto.getMinPending(), dto.getInactiveDays(), dto.getHasOrder(), dto.getHasTag(), dto.getTagIds());
        if (matched.isEmpty()) {
            return ResponseResult.fail("当前条件下没有命中会员，未保存");
        }

        StringBuilder ids = new StringBuilder();
        for (MemberPortraitVO vo : matched) {
            if (ids.length() > 0) {
                ids.append(',');
            }
            ids.append(vo.getUserId());
        }

        Map<String, Object> crowd = new LinkedHashMap<>();
        crowd.put("crowdName", dto.getCrowdName().trim());
        crowd.put("remark", dto.getRemark());
        crowd.put("ruleJson", toJson(dto));
        crowd.put("ruleText", describeRule(dto));
        crowd.put("memberCount", matched.size());
        crowd.put("memberIds", ids.length() > 200000 ? ids.substring(0, 200000) : ids.toString());
        crowd.put("createBy", dto.getCreateBy());
        memberPortraitMapper.insertCrowd(crowd);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", crowd.get("id"));
        data.put("memberCount", matched.size());
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult queryCrowdList() {
        List<Map<String, Object>> list = memberPortraitMapper.selectCrowdList();
        return ResponseResult.success(list == null ? Collections.emptyList() : list);
    }

    @Override
    public ResponseResult deleteCrowd(Long id) {
        if (id == null) {
            return ResponseResult.fail();
        }
        memberPortraitMapper.deleteCrowd(id);
        return ResponseResult.success();
    }

    @Override
    public ResponseResult queryTagList() {
        List<Map<String, Object>> list = memberPortraitMapper.selectTagDict();
        if (list == null) {
            list = new ArrayList<>();
        }
        int usedTags = 0;
        for (Map<String, Object> row : list) {
            if (toInt(row.get("memberCount")) > 0) {
                usedTags++;
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", list.size());
        data.put("usedTagCount", usedTags);
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult saveTag(PortraitTagDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTagName())) {
            return ResponseResult.fail("请填写标签名称");
        }
        String name = dto.getTagName().trim();
        if (name.length() > 40) {
            return ResponseResult.fail("标签名称不超过 40 个字");
        }
        if (dto.getTagId() != null && !isActiveTag(dto.getTagId())) {
            return ResponseResult.fail("标签不存在或已删除，请刷新列表");
        }
        if (dto.getTagColor() != null && !dto.getTagColor().matches("#[0-9a-fA-F]{6}")) {
            return ResponseResult.fail("请选择有效的标签颜色");
        }
        if (dto.getTagDescribe() != null && dto.getTagDescribe().length() > 255) {
            return ResponseResult.fail("标签说明不超过 255 个字");
        }
        Map<String, Object> tag = new LinkedHashMap<>();
        tag.put("tagName", name);
        tag.put("tagWeight", dto.getTagWeight() == null ? 0 : dto.getTagWeight());
        tag.put("tagDescribe", dto.getTagDescribe());
        tag.put("tagColor", dto.getTagColor());

        if (dto.getTagId() == null) {
            memberPortraitMapper.insertTag(tag);
            return ResponseResult.success(tag.get("tagId"));
        }
        tag.put("tagId", dto.getTagId());
        memberPortraitMapper.updateTagInfo(tag);
        if (dto.getTagIsactive() != null) {
            memberPortraitMapper.updateTagActive(dto.getTagId(), dto.getTagIsactive());
        }
        return ResponseResult.success(dto.getTagId());
    }

    @Override
    public ResponseResult deleteTag(Integer tagId) {
        if (tagId == null) {
            return ResponseResult.fail();
        }
        memberPortraitMapper.deleteTag(tagId);
        return ResponseResult.success();
    }

    private boolean isActiveTag(Integer tagId) {
        List<Map<String, Object>> rows = memberPortraitMapper.selectTagDict();
        return rows != null && rows.stream().anyMatch(row -> toInt(row.get("tagId")) == tagId);
    }

    @Override
    public ResponseResult attachTag(PortraitTagDTO dto) {
        if (dto == null || dto.getTagId() == null || dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
            return ResponseResult.fail("请选择标签与会员");
        }
        if (!isActiveTag(dto.getTagId())) {
            return ResponseResult.fail("标签不存在或已删除，请刷新列表");
        }
        int affected = 0;
        for (Long userId : dto.getUserIds()) {
            if (userId == null) {
                continue;
            }
            Set<Integer> tags = currentTags(userId);
            if (tags.add(dto.getTagId())) {
                writeTags(userId, tags);
                affected++;
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("affected", affected);
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult detachTag(PortraitTagDTO dto) {
        if (dto == null || dto.getTagId() == null || dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
            return ResponseResult.fail("请选择标签与会员");
        }
        if (!isActiveTag(dto.getTagId())) {
            return ResponseResult.fail("标签不存在或已删除，请刷新列表");
        }
        int affected = 0;
        for (Long userId : dto.getUserIds()) {
            if (userId == null) {
                continue;
            }
            Set<Integer> tags = currentTags(userId);
            if (tags.remove(dto.getTagId())) {
                writeTags(userId, tags);
                affected++;
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("affected", affected);
        return ResponseResult.success(data);
    }

    // ==================== 数据装载 ====================

    /**
     * 一次性装载全部会员并完成计算。
     *
     * <p>当前库内会员量级很小（万人以内），整表聚合 + 内存筛选比多轮 SQL 更简单也更一致；
     * SQL 侧已有 {@code LIMIT 5000} 上限，超过该量级需改为下推分页与条件过滤。</p>
     */
    private List<MemberPortraitVO> loadAllPortraits() {
        List<MemberPortraitVO> all = memberPortraitMapper.selectPortraitList(null, null);
        if (all == null) {
            all = new ArrayList<>();
        }
        Map<Integer, String> grades = loadGradeMap();
        Map<String, List<String>> browseMap = loadBrowseMap();
        Map<String, Set<Integer>> tagMap = loadTagMap();
        for (MemberPortraitVO vo : all) {
            fillComputed(vo, browseMap.get(String.valueOf(vo.getUserId())), grades,
                    tagMap.get(String.valueOf(vo.getUserId())));
        }
        return all;
    }

    private Map<Integer, String> loadGradeMap() {
        Map<Integer, String> map = new HashMap<>();
        List<Map<String, Object>> rows = memberPortraitMapper.selectGradeList();
        if (rows == null) {
            return map;
        }
        for (Map<String, Object> row : rows) {
            Object id = row.get("gradeId");
            if (id != null) {
                map.put(toInt(id), row.get("gradeName") == null ? null : String.valueOf(row.get("gradeName")));
            }
        }
        return map;
    }

    private Map<String, Set<Integer>> loadTagMap() {
        Map<String, Set<Integer>> map = new HashMap<>();
        List<Map<String, Object>> rows = memberPortraitMapper.selectAllUserTagRows();
        if (rows == null) {
            return map;
        }
        for (Map<String, Object> row : rows) {
            Object userId = row.get("userId");
            if (userId == null) {
                continue;
            }
            map.put(String.valueOf(userId), splitTagIds(row.get("tagIdStr") == null ? null : String.valueOf(row.get("tagIdStr"))));
        }
        return map;
    }

    // ==================== 计算 ====================

    private void fillComputed(MemberPortraitVO vo, List<String> browseGoods, Map<Integer, String> grades,
                              Set<Integer> tagIds) {
        int orderCount = vo.getOrderCount() == null ? 0 : vo.getOrderCount();
        int loginCount = vo.getLoginCount() == null ? 0 : vo.getLoginCount();
        int totalOrderCount = vo.getOrderTotalCount() == null ? 0 : vo.getOrderTotalCount();
        BigDecimal amount = vo.getOrderAmount() == null ? BigDecimal.ZERO : vo.getOrderAmount();

        Integer lastOrderDays = daysSince(vo.getLastOrderTime());
        Integer lastLoginDays = daysSince(vo.getLastLoginTime());

        Integer lastActiveDays = minOf(lastOrderDays, lastLoginDays);
        vo.setLastActiveDays(lastActiveDays);

        // 百分制 R/F/M
        vo.setRScore(lastActiveDays == null ? 0 : clamp(100 - lastActiveDays * 3));
        vo.setFScore(clamp(orderCount * 12 + Math.min(20, loginCount / 5)));
        vo.setMScore(clamp(amount.divide(new BigDecimal("100"), 0, RoundingMode.DOWN).intValue()));

        // 五分制 R/F/M（RFM 八分群用）
        vo.setRScore5(rScore5(lastActiveDays));
        vo.setFScore5(fScore5(orderCount));
        vo.setMScore5(mScore5(amount));

        // 价值分层（三档，用于列表筛选与概览图）
        String layer;
        if (orderCount >= 3 && lastOrderDays != null && lastOrderDays <= 30 && amount.compareTo(new BigDecimal("1000")) >= 0) {
            layer = LAYER_CORE;
        } else if ((orderCount >= 1 && lastOrderDays != null && lastOrderDays <= 60)
                || (lastLoginDays != null && lastLoginDays <= 30 && loginCount >= 5)) {
            layer = LAYER_GROWTH;
        } else {
            layer = LAYER_EDGE;
        }
        vo.setLayer(layer);
        vo.setLayerLabel(layerLabel(layer));

        // RFM 八分群
        String rfm = rfmSegment(orderCount, totalOrderCount, vo.getRScore5(), vo.getFScore5(), vo.getMScore5());
        vo.setRfmSegment(rfm);
        String[] def = RFM_DEF.get(rfm);
        vo.setRfmLabel(def == null ? "未分类" : def[0]);
        vo.setRfmAdvice(def == null ? "" : def[1]);

        // 生命周期
        String lifecycle = lifecycle(vo, orderCount, totalOrderCount, loginCount, lastActiveDays);
        vo.setLifecycle(lifecycle);
        vo.setLifecycleLabel(LIFECYCLE_DEF.getOrDefault(lifecycle, lifecycle));

        // 等级名
        if (vo.getGradeId() != null) {
            vo.setGradeName(grades.get(vo.getGradeId()));
        }

        // 复购节奏：平均每单间隔天数（有效订单 ≥2 且下单天数 ≥2 才有意义）
        int orderDays = vo.getOrderDays() == null ? 0 : vo.getOrderDays();
        if (orderCount >= 2 && orderDays >= 2) {
            vo.setOrderGapDays(Math.max(1, orderDays / (orderCount - 1)));
        }

        // 客单价
        if (orderCount > 0) {
            vo.setAvgOrderAmount(amount.divide(new BigDecimal(orderCount), 2, RoundingMode.HALF_UP));
        } else {
            vo.setAvgOrderAmount(BigDecimal.ZERO);
        }

        // 手工标签
        List<String> manualTags = splitTags(vo.getTagNamesStr());
        vo.setManualTags(manualTags);
        vo.setTagNamesStr(null);
        vo.setHasManualTag(!manualTags.isEmpty());
        vo.setTagIds(tagIds == null ? new ArrayList<>() : new ArrayList<>(tagIds));

        // 浏览
        List<String> goods = browseGoods == null ? Collections.emptyList() : browseGoods;
        vo.setBrowseGoods(goods);
        vo.setHasBrowseData(vo.getBrowseCount() != null && vo.getBrowseCount() > 0);
        vo.setHasOrderData(orderCount > 0);

        // 流失风险
        List<String> reasons = new ArrayList<>();
        int risk = 0;
        if (loginCount <= 0) {
            risk += 40;
            reasons.add("无登录记录");
        } else if (lastLoginDays == null) {
            risk += 20;
            reasons.add("登录时间缺失");
        } else {
            risk += Math.min(50, lastLoginDays * 2);
            if (lastLoginDays > 30) {
                reasons.add("已 " + lastLoginDays + " 天未登录");
            }
        }
        if (orderCount == 0) {
            risk += 15;
            reasons.add("从未成交");
            if (totalOrderCount > 0) {
                reasons.add("已下单未付款，转化链路卡在支付");
            } else if (loginCount > 0) {
                reasons.add("有登录无下单，转化链路存在断点");
            }
        } else if (lastOrderDays != null && lastOrderDays > 60) {
            risk += 20;
            reasons.add("已 " + lastOrderDays + " 天未复购");
        }
        risk = clamp(risk);
        vo.setRiskScore(risk);
        vo.setRiskLabel(risk >= 60 ? "高风险" : (risk >= 30 ? "中风险" : "低风险"));
        vo.setRiskReasons(reasons);

        vo.setAdvice(buildAdvice(vo, orderCount, loginCount, lastOrderDays, lastLoginDays, manualTags));
    }

    /** 单客 360：偏好、价格带、行为时间线，仅在详情接口加载 */
    private void fillDetail(MemberPortraitVO vo) {
        vo.setPrefGoods(memberPortraitMapper.selectPrefGoods(vo.getUserId(), 10));
        vo.setPrefShop(memberPortraitMapper.selectPrefShop(vo.getUserId(), 10));
        vo.setPriceBands(memberPortraitMapper.selectPriceBands(vo.getUserId()));
        vo.setTimeline(memberPortraitMapper.selectTimeline(vo.getUserId(), 50));
        vo.setClassAvailable(safeCount(() -> memberPortraitMapper.countClassAvailable()) > 0);
    }

    private Integer rScore5(Integer lastActiveDays) {
        if (lastActiveDays == null) {
            return 0;
        }
        if (lastActiveDays <= 7) {
            return 5;
        }
        if (lastActiveDays <= 30) {
            return 4;
        }
        if (lastActiveDays <= 60) {
            return 3;
        }
        if (lastActiveDays <= 90) {
            return 2;
        }
        return 1;
    }

    private Integer fScore5(int orderCount) {
        if (orderCount <= 0) {
            return 0;
        }
        if (orderCount == 1) {
            return 1;
        }
        if (orderCount <= 3) {
            return 2;
        }
        if (orderCount <= 6) {
            return 3;
        }
        if (orderCount <= 15) {
            return 4;
        }
        return 5;
    }

    private Integer mScore5(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        if (amount.compareTo(new BigDecimal("100")) < 0) {
            return 1;
        }
        if (amount.compareTo(new BigDecimal("500")) < 0) {
            return 2;
        }
        if (amount.compareTo(new BigDecimal("2000")) < 0) {
            return 3;
        }
        if (amount.compareTo(new BigDecimal("5000")) < 0) {
            return 4;
        }
        return 5;
    }

    private String rfmSegment(int orderCount, int totalOrderCount, Integer r, Integer f, Integer m) {
        if (orderCount <= 0) {
            return totalOrderCount > 0 ? "intent" : "never_bought";
        }
        boolean rHigh = r != null && r >= 4;
        boolean fHigh = f != null && f >= 3;
        boolean mHigh = m != null && m >= 3;
        if (rHigh && fHigh && mHigh) {
            return "important_value";
        }
        if (rHigh && !fHigh && mHigh) {
            return "important_develop";
        }
        if (!rHigh && fHigh && mHigh) {
            return "important_keep";
        }
        if (!rHigh && !fHigh && mHigh) {
            return "important_retain";
        }
        if (rHigh && fHigh) {
            return "general_value";
        }
        if (rHigh) {
            return "general_develop";
        }
        if (fHigh) {
            return "general_keep";
        }
        return "general_retain";
    }

    private String lifecycle(MemberPortraitVO vo, int orderCount, int totalOrderCount, int loginCount,
                             Integer lastActiveDays) {
        if (loginCount <= 0 && totalOrderCount <= 0) {
            return "inactive";
        }
        Integer regDays = daysSince(vo.getRegisterTime());
        if (totalOrderCount <= 0 && regDays != null && regDays <= 30) {
            return "newbie";
        }
        if (lastActiveDays == null) {
            return "churn";
        }
        if (lastActiveDays <= 30) {
            return "active";
        }
        if (orderCount >= 1 && lastActiveDays <= 60) {
            return "growing";
        }
        if (lastActiveDays <= 90) {
            return "silent";
        }
        return "churn";
    }

    private List<String> buildAdvice(MemberPortraitVO vo, int orderCount, int loginCount,
                                     Integer lastOrderDays, Integer lastLoginDays, List<String> manualTags) {
        List<String> advice = new ArrayList<>();

        int pendingCount = vo.getPendingPayCount() == null ? 0 : vo.getPendingPayCount();
        if (pendingCount > 0) {
            BigDecimal pendingAmount = vo.getPendingPayAmount() == null ? BigDecimal.ZERO : vo.getPendingPayAmount();
            advice.add("有 " + pendingCount + " 笔待付款订单（应付合计 ¥" + pendingAmount.setScale(2, RoundingMode.HALF_UP).toPlainString()
                    + "），先做催付——这是最短的转化路径，未支付不计入消费。");
        }

        if (orderCount == 0) {
            if (lastLoginDays != null && lastLoginDays <= 30) {
                advice.add("近 30 天有登录但从未成交：优先做首单转化，发低门槛首单券并推荐其登录后浏览过的品类。");
            } else if (loginCount > 0) {
                advice.add("有登录记录但从未成交，且已 " + (lastLoginDays == null ? "较久" : lastLoginDays + " 天") + " 未登录：先用短信/企微做低门槛召回，再谈转化。");
            } else {
                advice.add("无登录、无订单：属于静默注册用户，先做激活（新人礼包 + 首单包邮）。");
            }
        } else if (lastOrderDays != null && lastOrderDays <= 30) {
            advice.add("最近 " + lastOrderDays + " 天内有成交：保持复购节奏，主推关联品类与会员权益，避免大额券稀释利润。");
        } else if (lastOrderDays != null) {
            advice.add("已有 " + orderCount + " 笔订单但已 " + lastOrderDays + " 天未复购：用老客专属券 + 常购商品补货提醒拉回。");
        } else {
            advice.add("有历史订单但下单时间缺失，建议先排查订单时间字段完整性，再做复购策略。");
        }
        if (StringUtils.hasText(vo.getFavGoodsName())) {
            advice.add("购买集中在「" + vo.getFavGoodsName() + "」，推荐位优先曝光同款/同类商品。");
        }
        if (vo.getRfmAdvice() != null && !vo.getRfmAdvice().isEmpty()) {
            advice.add("分群动作（" + vo.getRfmLabel() + "）：" + vo.getRfmAdvice());
        }
        if (loginCount >= 20) {
            advice.add("累计登录 " + loginCount + " 次，属高频活跃用户，可纳入新品优先购与内测名单。");
        }
        if (!manualTags.isEmpty()) {
            advice.add("运营已打标签：" + String.join("、", manualTags) + "，投放按标签圈人，避免重复触达。");
        }
        return advice;
    }

    // ==================== 汇总 / 分群 ====================

    private Map<String, Object> buildSummary(List<MemberPortraitVO> matched, List<MemberPortraitVO> all) {
        int memberTotal = matched.size();
        int orderMemberCount = 0;
        int orderedMemberCount = 0;
        int loginMemberCount = 0;
        int core = 0;
        int growth = 0;
        int edge = 0;
        int taggedMemberCount = 0;
        int highRiskCount = 0;
        int pendingPayOrders = 0;
        BigDecimal pendingPayAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (MemberPortraitVO vo : matched) {
            if (vo.getOrderCount() != null && vo.getOrderCount() > 0) {
                orderMemberCount++;
                totalAmount = totalAmount.add(vo.getOrderAmount() == null ? BigDecimal.ZERO : vo.getOrderAmount());
            }
            if (vo.getOrderTotalCount() != null && vo.getOrderTotalCount() > 0) {
                orderedMemberCount++;
            }
            if (vo.getLoginCount() != null && vo.getLoginCount() > 0) {
                loginMemberCount++;
            }
            if (Boolean.TRUE.equals(vo.getHasManualTag())) {
                taggedMemberCount++;
            }
            if (vo.getRiskScore() != null && vo.getRiskScore() >= 60) {
                highRiskCount++;
            }
            if (vo.getPendingPayCount() != null && vo.getPendingPayCount() > 0) {
                pendingPayOrders += vo.getPendingPayCount();
                pendingPayAmount = pendingPayAmount.add(vo.getPendingPayAmount() == null ? BigDecimal.ZERO : vo.getPendingPayAmount());
            }
            if (LAYER_CORE.equals(vo.getLayer())) {
                core++;
            } else if (LAYER_GROWTH.equals(vo.getLayer())) {
                growth++;
            } else {
                edge++;
            }
        }

        int orderRows = safeCount(() -> memberPortraitMapper.countAllOrders());
        int validOrderRows = safeCount(() -> memberPortraitMapper.countValidOrders());
        int footprintRows = safeCount(() -> memberPortraitMapper.countFootprintRows());
        int tagRows = safeCount(() -> memberPortraitMapper.countValidTagRows());
        int classRows = safeCount(() -> memberPortraitMapper.countClassAvailable());

        Map<String, Object> dq = new LinkedHashMap<>();
        dq.put("loginAvailable", true);
        dq.put("orderAvailable", orderRows > 0);
        dq.put("orderRows", orderRows);
        dq.put("validOrderRows", validOrderRows);
        dq.put("browseAvailable", footprintRows > 0);
        dq.put("footprintRows", footprintRows);
        dq.put("tagAvailable", tagRows > 0);
        dq.put("tagRows", tagRows);
        dq.put("classAvailable", classRows > 0);
        dq.put("classRows", classRows);
        dq.put("generatedAt", LocalDateTime.now().format(TS));
        dq.put("orderRule", "有效订单＝order_status 不在 (1 待付款, 7 取消)；待付款单独统计，不计入消费");
        dq.put("rfmRule", "R/F/M 各按固定阈值打 1-5 分，以 3 分为高/低分界组合成八象限；无有效订单的会员单独归入「下单未成交 / 未成交会员」");
        dq.put("sources", Arrays.asList(
                "hou_user_info（会员/登录次数/最后登录）",
                "hou_order_base（订单数/金额/待付款/最近下单）",
                "hou_goods_user_footprint（浏览足迹）",
                "hou_base_tag_class + hou_user_tag_info（运营手工标签）",
                "hou_order_goods（购买商品/店铺/价格带）",
                "hou_log_login（登录行为时间线）"));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("memberTotal", memberTotal);
        summary.put("orderMemberCount", orderMemberCount);
        summary.put("orderedMemberCount", orderedMemberCount);
        summary.put("loginMemberCount", loginMemberCount);
        summary.put("taggedMemberCount", taggedMemberCount);
        summary.put("coreCount", core);
        summary.put("growthCount", growth);
        summary.put("edgeCount", edge);
        summary.put("highRiskCount", highRiskCount);
        summary.put("pendingPayOrders", pendingPayOrders);
        summary.put("pendingPayAmount", pendingPayAmount.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalOrderAmount", totalAmount.setScale(2, RoundingMode.HALF_UP));
        summary.put("avgOrderAmount", orderMemberCount == 0 ? BigDecimal.ZERO
                : totalAmount.divide(new BigDecimal(orderMemberCount), 2, RoundingMode.HALF_UP));
        summary.put("dataQuality", dq);
        summary.put("trend", buildTrend(30));
        summary.put("layerStats", Arrays.asList(
                buildLayerStat(matched, LAYER_CORE),
                buildLayerStat(matched, LAYER_GROWTH),
                buildLayerStat(matched, LAYER_EDGE)));
        summary.put("rfmStats", buildRfmStats(matched));
        summary.put("lifecycleStats", buildLifecycleStats(matched));
        summary.put("allMemberTotal", all.size());
        return summary;
    }

    private List<Map<String, Object>> buildRfmStats(List<MemberPortraitVO> all) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : RFM_DEF.entrySet()) {
            list.add(buildRfmStat(all, entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }
        return list;
    }

    private Map<String, Object> buildRfmStat(List<MemberPortraitVO> all, String key, String label, String advice) {
        List<MemberPortraitVO> list = all.stream()
                .filter(vo -> key.equals(vo.getRfmSegment()))
                .collect(Collectors.toList());
        BigDecimal amount = BigDecimal.ZERO;
        int orders = 0;
        for (MemberPortraitVO vo : list) {
            amount = amount.add(vo.getOrderAmount() == null ? BigDecimal.ZERO : vo.getOrderAmount());
            orders += vo.getOrderCount() == null ? 0 : vo.getOrderCount();
        }
        List<Map<String, Object>> members = new ArrayList<>();
        for (MemberPortraitVO vo : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", vo.getUserId());
            m.put("nickname", vo.getNickname());
            m.put("orderCount", vo.getOrderCount());
            m.put("orderAmount", vo.getOrderAmount());
            m.put("pendingPayCount", vo.getPendingPayCount());
            m.put("pendingPayAmount", vo.getPendingPayAmount());
            m.put("lastActiveDays", vo.getLastActiveDays());
            members.add(m);
        }
        Map<String, Object> stat = new LinkedHashMap<>();
        stat.put("segment", key);
        stat.put("label", label);
        stat.put("advice", advice);
        stat.put("count", list.size());
        stat.put("ratio", all.isEmpty() ? 0 : Math.round(list.size() * 100.0f / all.size()));
        stat.put("avgAmount", list.isEmpty() ? BigDecimal.ZERO
                : amount.divide(new BigDecimal(list.size()), 2, RoundingMode.HALF_UP));
        stat.put("totalAmount", amount.setScale(2, RoundingMode.HALF_UP));
        stat.put("totalOrders", orders);
        stat.put("members", members);
        return stat;
    }

    private List<Map<String, Object>> buildLifecycleStats(List<MemberPortraitVO> all) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : LIFECYCLE_DEF.entrySet()) {
            List<MemberPortraitVO> hit = all.stream()
                    .filter(vo -> entry.getKey().equals(vo.getLifecycle()))
                    .collect(Collectors.toList());
            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("lifecycle", entry.getKey());
            stat.put("label", entry.getValue());
            stat.put("count", hit.size());
            stat.put("ratio", all.isEmpty() ? 0 : Math.round(hit.size() * 100.0f / all.size()));
            list.add(stat);
        }
        return list;
    }

    private Map<String, Object> buildLayerStat(List<MemberPortraitVO> all, String layer) {
        List<MemberPortraitVO> list = all.stream()
                .filter(vo -> layer.equals(vo.getLayer()))
                .collect(Collectors.toList());
        int count = list.size();
        BigDecimal amount = BigDecimal.ZERO;
        int orders = 0;
        int risk = 0;
        for (MemberPortraitVO vo : list) {
            amount = amount.add(vo.getOrderAmount() == null ? BigDecimal.ZERO : vo.getOrderAmount());
            orders += vo.getOrderCount() == null ? 0 : vo.getOrderCount();
            if (vo.getRiskScore() != null && vo.getRiskScore() >= 60) {
                risk++;
            }
        }

        List<Map<String, Object>> members = new ArrayList<>();
        for (MemberPortraitVO vo : list) {
            Map<String, Object> member = new LinkedHashMap<>();
            member.put("userId", vo.getUserId());
            member.put("nickname", vo.getNickname());
            members.add(member);
        }

        Map<String, Object> stat = new LinkedHashMap<>();
        stat.put("layer", layer);
        stat.put("count", count);
        stat.put("ratio", all.isEmpty() ? 0 : Math.round(count * 100.0f / all.size()));
        stat.put("avgAmount", count == 0 ? BigDecimal.ZERO
                : amount.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP));
        stat.put("avgOrders", count == 0 ? 0
                : BigDecimal.valueOf(orders).divide(new BigDecimal(count), 1, RoundingMode.HALF_UP));
        stat.put("riskCount", risk);
        stat.put("members", members);
        return stat;
    }

    /**
     * 近 N 天下单趋势，按日期补齐空白天（值为 0），不插值、不造数。
     */
    private List<Map<String, Object>> buildTrend(int days) {
        Map<String, Map<String, Object>> byDate = new HashMap<>();
        List<Map<String, Object>> rows = memberPortraitMapper.selectDailyTrend(days);
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object d = row.get("d");
                if (d != null) {
                    byDate.put(String.valueOf(d), row);
                }
            }
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            String date = today.minusDays(i).toString();
            Map<String, Object> hit = byDate.get(date);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date);
            point.put("created", hit == null ? 0 : toInt(hit.get("created")));
            point.put("paid", hit == null ? 0 : toInt(hit.get("paid")));
            point.put("orders", hit == null ? 0 : toInt(hit.get("paid")));
            point.put("amount", hit == null ? BigDecimal.ZERO : toDecimal(hit.get("amount")));
            point.put("pendingAmount", hit == null ? BigDecimal.ZERO : toDecimal(hit.get("pending_amount")));
            // 待付款/已取消按下单状态单独计数，供前端三段堆叠柱直接使用；
            // 不能用 created-paid 倒推，否则已取消会被算成待付款。
            point.put("pendingCount", hit == null ? 0 : toInt(hit.get("pending_count")));
            point.put("cancelledCount", hit == null ? 0 : toInt(hit.get("cancelled_count")));
            trend.add(point);
        }
        return trend;
    }

    // ==================== 筛选 ====================

    private List<MemberPortraitVO> applyFilter(List<MemberPortraitVO> all, String keyword, String layer,
                                               String rfmSegment, String lifecycle,
                                               Integer minOrders, Integer maxOrders,
                                               BigDecimal minAmount, BigDecimal maxAmount,
                                               Integer minPending, Integer inactiveDays,
                                               Boolean hasOrder, Boolean hasTag, List<Integer> tagIds) {
        String kw = StringUtils.hasText(keyword) ? keyword.trim().toLowerCase() : null;
        Set<Integer> tagFilter = tagIds == null ? null : new HashSet<>(tagIds);

        List<MemberPortraitVO> matched = new ArrayList<>();
        for (MemberPortraitVO vo : all) {
            if (StringUtils.hasText(layer) && !layer.equals(vo.getLayer())) {
                continue;
            }
            if (StringUtils.hasText(rfmSegment) && !rfmSegment.equals(vo.getRfmSegment())) {
                continue;
            }
            if (StringUtils.hasText(lifecycle) && !lifecycle.equals(vo.getLifecycle())) {
                continue;
            }
            if (kw != null) {
                String nick = vo.getNickname() == null ? "" : vo.getNickname().toLowerCase();
                String mobile = vo.getMobile() == null ? "" : vo.getMobile();
                String uid = String.valueOf(vo.getUserId());
                if (!nick.contains(kw) && !mobile.contains(kw) && !uid.contains(kw)) {
                    continue;
                }
            }
            int orderCount = vo.getOrderCount() == null ? 0 : vo.getOrderCount();
            if (minOrders != null && orderCount < minOrders) {
                continue;
            }
            if (maxOrders != null && orderCount > maxOrders) {
                continue;
            }
            BigDecimal amount = vo.getOrderAmount() == null ? BigDecimal.ZERO : vo.getOrderAmount();
            if (minAmount != null && amount.compareTo(minAmount) < 0) {
                continue;
            }
            if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
                continue;
            }
            int pending = vo.getPendingPayCount() == null ? 0 : vo.getPendingPayCount();
            if (minPending != null && pending < minPending) {
                continue;
            }
            if (inactiveDays != null) {
                Integer days = vo.getLastActiveDays();
                if (days == null || days < inactiveDays) {
                    continue;
                }
            }
            if (hasOrder != null) {
                boolean ordered = (vo.getOrderTotalCount() == null ? 0 : vo.getOrderTotalCount()) > 0;
                if (hasOrder != ordered) {
                    continue;
                }
            }
            if (hasTag != null) {
                boolean tagged = Boolean.TRUE.equals(vo.getHasManualTag());
                if (hasTag != tagged) {
                    continue;
                }
            }
            if (tagFilter != null && !tagFilter.isEmpty()) {
                List<Integer> mine = vo.getTagIds() == null ? Collections.emptyList() : vo.getTagIds();
                if (mine.stream().noneMatch(tagFilter::contains)) {
                    continue;
                }
            }
            matched.add(vo);
        }
        return matched;
    }

    private List<MemberPortraitVO> slice(List<MemberPortraitVO> list, int page, int size) {
        int total = list.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        return new ArrayList<>(list.subList(from, to));
    }

    /** 圈人条件的中文可读描述，落库到人群包，便于日后复盘当时的筛选口径 */
    private String describeRule(PortraitSegmentDTO q) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(q.getKeyword())) {
            parts.add("关键词「" + q.getKeyword().trim() + "」");
        }
        if (StringUtils.hasText(q.getLayer())) {
            parts.add("价值分层=" + layerLabel(q.getLayer()));
        }
        if (StringUtils.hasText(q.getRfmSegment())) {
            String[] def = RFM_DEF.get(q.getRfmSegment());
            parts.add("RFM 分群=" + (def == null ? q.getRfmSegment() : def[0]));
        }
        if (StringUtils.hasText(q.getLifecycle())) {
            parts.add("生命周期=" + LIFECYCLE_DEF.getOrDefault(q.getLifecycle(), q.getLifecycle()));
        }
        if (q.getMinOrders() != null || q.getMaxOrders() != null) {
            parts.add("有效订单 " + (q.getMinOrders() == null ? "0" : q.getMinOrders())
                    + "-" + (q.getMaxOrders() == null ? "不限" : q.getMaxOrders()));
        }
        if (q.getMinAmount() != null || q.getMaxAmount() != null) {
            parts.add("累计消费 " + (q.getMinAmount() == null ? "0" : q.getMinAmount().toPlainString())
                    + "-" + (q.getMaxAmount() == null ? "不限" : q.getMaxAmount().toPlainString()) + " 元");
        }
        if (q.getMinPending() != null) {
            parts.add("待付款≥" + q.getMinPending() + " 笔");
        }
        if (q.getInactiveDays() != null) {
            parts.add("已 " + q.getInactiveDays() + " 天以上未活跃");
        }
        if (q.getHasOrder() != null) {
            parts.add(q.getHasOrder() ? "有下单记录" : "从未下单");
        }
        if (q.getHasTag() != null) {
            parts.add(q.getHasTag() ? "已打标" : "未打标");
        }
        if (q.getTagIds() != null && !q.getTagIds().isEmpty()) {
            parts.add("命中标签 " + q.getTagIds().size() + " 个");
        }
        return parts.isEmpty() ? "全部会员（无条件）" : String.join("；", parts);
    }

    // ==================== 标签读写 ====================

    private Set<Integer> currentTags(Long userId) {
        String raw = memberPortraitMapper.selectUserTagIdStr(userId);
        if (raw == null) {
            // 该会员在打标表里还没有记录，先补一行，避免后续 UPDATE 影响 0 行
            memberPortraitMapper.insertUserTagRow(userId);
        }
        return splitTagIds(raw);
    }

    private void writeTags(Long userId, Set<Integer> tags) {
        if (tags == null || tags.isEmpty()) {
            memberPortraitMapper.updateUserTagIdStr(userId, null);
            return;
        }
        String str = tags.stream().map(String::valueOf).collect(Collectors.joining(","));
        memberPortraitMapper.updateUserTagIdStr(userId, str);
    }

    private Set<Integer> splitTagIds(String raw) {
        Set<Integer> set = new LinkedHashSet<>();
        if (!StringUtils.hasText(raw)) {
            return set;
        }
        for (String part : raw.split(",")) {
            String v = part.trim();
            if (!v.isEmpty()) {
                try {
                    set.add(Integer.parseInt(v));
                } catch (NumberFormatException ignored) {
                    // 历史脏数据跳过，不影响其它标签
                }
            }
        }
        return set;
    }

    // ==================== 工具 ====================

    private Map<String, List<String>> loadBrowseMap() {
        Map<String, List<String>> map = new HashMap<>();
        List<Map<String, Object>> rows = memberPortraitMapper.selectBrowseGoods();
        if (rows == null || rows.isEmpty()) {
            return map;
        }
        for (Map<String, Object> row : rows) {
            Object userId = row.get("userId");
            Object goodsName = row.get("goodsName");
            if (userId == null || goodsName == null) {
                continue;
            }
            List<String> list = map.computeIfAbsent(String.valueOf(userId), k -> new ArrayList<>());
            if (list.size() < 10) {
                list.add(String.valueOf(goodsName));
            }
        }
        return map;
    }

    private String toJson(PortraitSegmentDTO dto) {
        StringBuilder sb = new StringBuilder("{");
        appendJson(sb, "keyword", dto.getKeyword());
        appendJson(sb, "layer", dto.getLayer());
        appendJson(sb, "rfmSegment", dto.getRfmSegment());
        appendJson(sb, "lifecycle", dto.getLifecycle());
        appendJson(sb, "minOrders", dto.getMinOrders());
        appendJson(sb, "maxOrders", dto.getMaxOrders());
        appendJson(sb, "minAmount", dto.getMinAmount());
        appendJson(sb, "maxAmount", dto.getMaxAmount());
        appendJson(sb, "minPending", dto.getMinPending());
        appendJson(sb, "inactiveDays", dto.getInactiveDays());
        appendJson(sb, "hasOrder", dto.getHasOrder());
        appendJson(sb, "hasTag", dto.getHasTag());
        appendJson(sb, "tagIds", dto.getTagIds());
        if (sb.length() > 1 && sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }
        return sb.append('}').toString();
    }

    private void appendJson(StringBuilder sb, String key, Object value) {
        if (value == null) {
            return;
        }
        sb.append('"').append(key).append("\":");
        if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof List) {
            sb.append('[');
            List<?> list = (List<?>) value;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                Object v = list.get(i);
                sb.append(v instanceof Number ? String.valueOf(v) : "\"" + escape(String.valueOf(v)) + "\"");
            }
            sb.append(']');
        } else {
            sb.append('"').append(escape(String.valueOf(value))).append('"');
        }
        sb.append(',');
    }

    private String escape(String v) {
        return v.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Integer daysSince(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }
        try {
            LocalDateTime target = LocalDateTime.parse(time, TS);
            long days = Duration.between(target, LocalDateTime.now()).toDays();
            return (int) Math.max(0, days);
        } catch (Exception e) {
            log.warn("会员画像时间解析失败: {}", time);
            return null;
        }
    }

    private Integer minOf(Integer a, Integer b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return Math.min(a, b);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String layerLabel(String layer) {
        if (LAYER_CORE.equals(layer)) {
            return "核心用户";
        }
        if (LAYER_GROWTH.equals(layer)) {
            return "增长用户";
        }
        return "边缘/沉睡";
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private List<String> splitTags(String raw) {
        if (!StringUtils.hasText(raw)) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private int safeCount(CountSupplier supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("会员画像数据量统计失败: {}", e.getMessage());
            return 0;
        }
    }

    @FunctionalInterface
    private interface CountSupplier {
        int get();
    }
}
