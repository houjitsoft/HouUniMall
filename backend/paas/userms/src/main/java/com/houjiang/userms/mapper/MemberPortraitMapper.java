package com.houjiang.userms.mapper;

import com.houjiang.userms.vo.MemberPortraitVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 会员画像聚合查询
 *
 * <p>全部读取库内真实数据，不使用任何模拟数据。跨域表（order / goods / base）沿用
 * 本项目 {@code UserInfoMapper.xml} 已有的 {@code ${database.prefix.*}} 前缀写法。</p>
 *
 * @author Amos
 */
@Mapper
public interface MemberPortraitMapper {

    /**
     * 会员画像基础聚合（会员 + 订单 + 浏览 + 手工标签 + 购买偏好）
     *
     * @param keyword  昵称/手机号/用户ID
     * @param minOrders 最少订单数
     * @return 画像基础行，计算字段由 Service 填充
     */
    List<MemberPortraitVO> selectPortraitList(@Param("keyword") String keyword,
                                              @Param("minOrders") Integer minOrders);

    /**
     * 浏览足迹明细（按会员 + 商品聚合），用于填充画像的浏览偏好
     */
    List<Map<String, Object>> selectBrowseGoods();

    /**
     * 近 N 天真实下单趋势（按日聚合订单数与支付金额）
     *
     * @param days 天数
     */
    List<Map<String, Object>> selectDailyTrend(@Param("days") int days);

    /** 足迹表总行数，用于判断浏览维度是否已采集 */
    int countFootprintRows();

    /** 有效手工标签关联数（tag_id 非空），用于判断标签维度是否已启用 */
    int countValidTagRows();

    /** 有效订单数（排除待付款/取消），用于数据完整度提示 */
    int countValidOrders();

    /** 全部订单记录数（含待付款/取消） */
    int countAllOrders();

    // ==================== 等级 / 单客 360 明细 ====================

    /** 会员等级字典：id / gradeName */
    List<Map<String, Object>> selectGradeList();

    /** 单客行为时间线（登录事件 + 下单事件，按时间倒序） */
    List<Map<String, Object>> selectTimeline(@Param("userId") Long userId, @Param("limit") int limit);

    /** 单客常购商品 TOP */
    List<Map<String, Object>> selectPrefGoods(@Param("userId") Long userId, @Param("limit") int limit);

    /** 单客常购店铺 TOP */
    List<Map<String, Object>> selectPrefShop(@Param("userId") Long userId, @Param("limit") int limit);

    /** 单客价格带分布（按商品行金额分档） */
    List<Map<String, Object>> selectPriceBands(@Param("userId") Long userId);

    /** 已落库品类的商品行数，0 表示品类维度未采集 */
    int countClassAvailable();

    // ==================== 标签体系（复用 hou_user_tag_info + hou_base_tag_class） ====================

    /** 标签字典（含每个标签覆盖人数） */
    List<Map<String, Object>> selectTagDict();

    /** 全量会员打标关系：userId / tagIdStr（一次取回，避免逐人查询） */
    List<Map<String, Object>> selectAllUserTagRows();

    /** 更新标签名称/权重/说明 */
    int updateTagInfo(Map<String, Object> tag);

    /** 新建标签，返回自增ID（useGeneratedKeys） */
    int insertTag(Map<String, Object> tag);

    /** 启用/停用标签 */
    int updateTagActive(@Param("tagId") Integer tagId, @Param("tagIsactive") Integer tagIsactive);

    /** 逻辑删除标签（置为停用并清名，避免破坏历史关系） */
    int deleteTag(@Param("tagId") Integer tagId);

    /** 某会员当前已打的标签ID串（逗号分隔，可能为 null） */
    String selectUserTagIdStr(@Param("userId") Long userId);

    /** 确保会员在打标表里有记录（没有则插入一条空记录） */
    int insertUserTagRow(@Param("userId") Long userId);

    /** 写回会员的标签ID串 */
    int updateUserTagIdStr(@Param("userId") Long userId, @Param("tagIdStr") String tagIdStr);

    /** 某标签覆盖的会员ID列表 */
    List<Long> selectUserIdsByTag(@Param("tagId") Integer tagId);

    // ==================== 人群包 ====================

    /** 保存人群包 */
    int insertCrowd(Map<String, Object> crowd);

    /** 人群包列表 */
    List<Map<String, Object>> selectCrowdList();

    /** 逻辑删除人群包 */
    int deleteCrowd(@Param("id") Long id);
}
