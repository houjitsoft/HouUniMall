package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * GOODS（域 40）商品错误码，号段 40001-40999。
 *
 * <p>解决的问题：旧码 15010 在 BizCodeEnume 里被 6-8 个语义共用
 * （SPU_ID_IS_NULL / SKU_ID_IS_NULL / STOCK_ID_IS_NULL / GOODS_IMAGE_IS_NULL /
 * SPEC_VALUE_IS_NULL / ADD_GOODS_RELEASED），前端根本无法按码反查到底缺什么。
 * 本域把这些语义拆成独立码，逐个给出明确的下一步动作。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum GoodsErrorCode implements ErrorCodeDef {

    /**
     * 商品不存在（已下架或已删除）。
     *
     * <p>触发：按商品 ID 查不到记录。用户看到「商品不存在或已下架」。
     *
     * <p>排查：确认商品 ID 与上下架状态。
     */
    GOODS_NOT_FOUND(40001, ErrorMeta.builder(40001, "商品不存在或已下架")
            .devMessage("按商品 ID 未查到 SPU 记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .legacyCodes(15008)
            .since("v1.2.0")
            .build()),

    /**
     * 商品参数不合法。
     *
     * <p>触发：商品维护接口参数校验失败。用户看到「商品信息填写有误，请检查」。
     *
     * <p>排查：确认必填字段（名称、分类、价格）是否齐全。
     */
    GOODS_PARAM_INVALID(40002, ErrorMeta.builder(40002, "商品信息填写有误，请检查")
            .devMessage("商品参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 商品状态不允许（上架中不允许改规格等）。
     *
     * <p>触发：对当前状态的商品执行不允许的操作。用户看到「商品当前状态不允许该操作」。
     *
     * <p>排查：确认商品状态机；改规格通常需先下架。
     */
    GOODS_STATE_NOT_ALLOWED(40003, ErrorMeta.builder(40003, "商品当前状态不允许该操作")
            .devMessage("商品状态机流转非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(15001, 15013)
            .since("v1.2.0")
            .build()),

    /**
     * 商品名称重复。
     *
     * <p>触发：同名商品已存在。用户看到「已存在同名商品，请更换名称」。
     *
     * <p>排查：确认是否应改为编辑已有商品。
     */
    GOODS_DUPLICATE_NAME(40004, ErrorMeta.builder(40004, "已存在同名商品，请更换名称")
            .devMessage("商品名称唯一约束冲突")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 查询不到商品数据（筛选条件无命中）。
     *
     * <p>触发：商品列表查询返回空。用户看到空状态占位，不弹 Toast。
     *
     * <p>排查：这是 INFO 级别，正常业务结果。
     */
    GOODS_QUERY_RESULT_EMPTY(40007, ErrorMeta.builder(40007, "没有找到符合条件的商品")
            .devMessage("商品列表查询无命中")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .legacyCodes(11074, 11075)
            .since("v1.2.0")
            .build()),

    /**
     * 商品规格（SKU）不存在。
     *
     * <p>触发：按 SKU ID 查不到记录，或下单时 SKU 为空。用户看到「请选择商品规格」。
     *
     * <p>排查：旧码 15010 曾把「SPU 为空 / SKU 为空 / 库存 ID 为空」混成一个码；本码只表示 SKU 缺失。
     */
    GOODS_SKU_NOT_FOUND(40010, ErrorMeta.builder(40010, "请选择商品规格")
            .devMessage("SKU ID 为空或查不到 SKU 记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .remark("拆分自歧义旧码 15010；15010 本轮不建新码，见《码表.md》附录 B")
            .build()),

    /**
     * 商品分类不存在。
     *
     * <p>触发：按分类 ID 查不到记录。用户看到「请选择商品分类」。
     *
     * <p>排查：确认分类是否已维护、是否被删除。
     */
    GOODS_CATEGORY_NOT_FOUND(40011, ErrorMeta.builder(40011, "请选择商品分类")
            .devMessage("分类 ID 为空或查不到分类记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(15039)
            .since("v1.2.0")
            .build()),

    /**
     * 商品主图缺失。
     *
     * <p>触发：上架前校验发现没有主图。用户看到「请上传商品主图」。
     *
     * <p>排查：旧码 15010 曾把「商品图片为空」混在里面；本码明确只表示图片缺失。
     */
    GOODS_IMAGE_REQUIRED(40012, ErrorMeta.builder(40012, "请上传商品主图")
            .devMessage("商品主图为空")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(15010)
            .since("v1.2.0")
            .remark("拆分自歧义旧码 15010")
            .build()),

    /**
     * 规格值缺失（如颜色/尺码未选）。
     *
     * <p>触发：规格值校验失败。用户看到「请补全商品规格值」。
     *
     * <p>排查：旧码 15010 曾把「规格值为空」混在里面；本码明确只表示规格值缺失。
     */
    GOODS_SPEC_VALUE_REQUIRED(40013, ErrorMeta.builder(40013, "请补全商品规格值")
            .devMessage("规格值为空或未选择")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(15010)
            .since("v1.2.0")
            .remark("拆分自歧义旧码 15010")
            .build()),

    /**
     * 商品无货（下单校验时发现所选商品买不到）。
     *
     * <p>触发：下单/算价时校验商品在该售卖区域或该供应商处无可用库存。用户看到「该商品暂时无货，请重新选择」。
     *
     * <p>排查：检查库存同步任务是否执行、商品库存字段是否被负库存占用、售卖区域与配送区域配置是否覆盖收货地址、
     * 下单链路是否走了库存预占。
     *
     * <p><b>游离码认领说明</b>：前端 pc / uniapp 历史上用 {@code response.code === 15030} 判断「无货」，
     * 但 <b>15030 在 BizCodeEnume 里的真身是「超出最大配送距离」</b>（见《码表.md》原始清单），
     * 后端并没有任何一处用 15030 表示无货。因此本码<b>不认领 15030</b>，
     * 无货语义请直接使用本码；15030 归 {@link #DELIVERY_DISTANCE_EXCEEDED}。
     */
    GOODS_OUT_OF_STOCK(40801, ErrorMeta.builder(40801, "该商品暂时无货，请重新选择")
            .devMessage("下单校验时商品无可用库存（售卖区域不可售或供应商侧无货）")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.DIALOG).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .troubleshooting("检查库存同步任务是否执行、商品库存字段是否被负库存占用；"
                    + "确认售卖区域/配送区域配置是否覆盖收货地址；确认下单链路是否走了库存预占与释放")
            .legacyCodes(15011)
            .since("v1.2.0")
            .remark("域内扩展槽 801；不认领旧码 15030（15030 真身是超出最大配送距离），前端须把无货判断从 15030 改到本码")
            .build()),

    /**
     * 超出最大配送距离。
     *
     * <p>触发：收货地址距发货门店超过商家配置的配送半径。用户看到「超出配送范围，请更换地址」。
     *
     * <p>排查：确认商家配送半径配置与收货地址经纬度；确认距离计算用的坐标系（GCJ-02 / WGS-84）与地图厂商一致。
     *
     * <p><b>游离码认领说明</b>：本码认领旧码 15030。15030 此前既不在统一码表、也不在附录 B 冲突清单里，
     * 是一枚「游离码」；前端 pc / uniapp 误把它当成「无货」，实际 BizCodeEnume 里
     * {@code EXCEEDED_MAXIMUM_DELIVERY_DISTANCE(15030, "超出最大配送距离")}。
     */
    DELIVERY_DISTANCE_EXCEEDED(40802, ErrorMeta.builder(40802, "超出配送范围，请更换地址")
            .devMessage("收货地址与发货门店的直线/骑行距离超过配置的配送半径")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(15030)
            .troubleshooting("核对商家配送半径配置与收货地址经纬度；"
                    + "确认距离计算使用的坐标系（GCJ-02 / WGS-84）与地图厂商一致；"
                    + "确认 15030 是否被前端误当成「无货」使用（无货应为 GOODS 域 40801）")
            .since("v1.2.0")
            .remark("域内扩展槽 802；认领游离旧码 15030，其真身为超出最大配送距离而非无货")
            .build()),
    /**
     * 不能购买自己店铺的商品。
     *
     * <p>触发/排查：买家与卖家主体相同被拦截。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    GOODS_SELF_PURCHASE_FORBIDDEN(40101, ErrorMeta.builder(40101, "不能购买自己店铺的商品")
            .devMessage("买家与卖家主体相同被拦截")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(15003)
            .since("v1.3.0")
            .build()),

    /**
     * 商品规格重复，请调整。
     *
     * <p>触发/排查：同一商品下规格项重复。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    GOODS_SPEC_DUPLICATE(40102, ErrorMeta.builder(40102, "商品规格重复，请调整")
            .devMessage("同一商品下规格项重复")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(15010)
            .since("v1.3.0")
            .build()),

    /**
     * 商品版本不存在，请刷新。
     *
     * <p>触发/排查：按版本号查不到商品版本。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    GOODS_VERSION_NOT_FOUND(40103, ErrorMeta.builder(40103, "商品版本不存在，请刷新")
            .devMessage("按版本号查不到商品版本")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .legacyCodes(15041)
            .since("v1.3.0")
            .build()),

    /**
     * 所选规格与商品不匹配。
     *
     * <p>触发/排查：下单规格与商品规格不一致。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    GOODS_SPEC_NOT_MATCH(40104, ErrorMeta.builder(40104, "所选规格与商品不匹配")
            .devMessage("下单规格与商品规格不一致")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(15042)
            .since("v1.3.0")
            .build()),

    /**
     * 商品暂不满足上架条件。
     *
     * <p>触发/排查：上架前置条件（功能开关/资质）未满足。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    GOODS_ONLINE_NOT_ALLOWED(40105, ErrorMeta.builder(40105, "商品暂不满足上架条件")
            .devMessage("上架前置条件（功能开关/资质）未满足")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(15011)
            .since("v1.3.0")
            .build()),

    /**
     * 品牌名称已存在，请更换。
     *
     * <p>触发/排查：品牌唯一键冲突。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    GOODS_BRAND_DUPLICATE(40106, ErrorMeta.builder(40106, "品牌名称已存在，请更换")
            .devMessage("品牌唯一键冲突")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(15020)
            .since("v1.3.0")
            .build()),

    /**
     * 该品牌下还有商品，无法删除。
     *
     * <p>触发/排查：删除品牌时品牌下仍有商品。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    GOODS_BRAND_IN_USE(40107, ErrorMeta.builder(40107, "该品牌下还有商品，无法删除")
            .devMessage("删除品牌时品牌下仍有商品")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(15020)
            .since("v1.3.0")
            .build()),

    /**
     * 该属性已被商品使用，无法删除。
     *
     * <p>触发/排查：删除属性时仍有商品引用。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    GOODS_ATTRIBUTE_IN_USE(40108, ErrorMeta.builder(40108, "该属性已被商品使用，无法删除")
            .devMessage("删除属性时仍有商品引用")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(15019)
            .since("v1.3.0")
            .build()),


    GOODS_SELF_OPERATION_FORBIDDEN(40803, ErrorMeta.builder(40803, "自己的商品不能操作")
            .devMessage("对属于自己的商品执行了不允许的操作")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .since("v1.3.0")
            .legacyCodes(15018)
            .build()),
    GOODS_ALREADY_SOLD_OUT(40804, ErrorMeta.builder(40804, "商品已经销")
            .devMessage("商品已经销，不可再变更")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.3.0")
            .legacyCodes(15022)
            .build()),
    GOODS_SYNC_DUPLICATED(40805, ErrorMeta.builder(40805, "商品不能重复同步")
            .devMessage("同一商品在同步窗口内被重复提交")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.3.0")
            .legacyCodes(15023)
            .build()),
    GOODS_IMAGE_THUMBNAIL_UNSUPPORTED(40806, ErrorMeta.builder(40806, "该图片格式不支持缩略图")
            .devMessage("商品图片格式不在缩略图支持列表内")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.3.0")
            .legacyCodes(10059)
            .build()),
    ;

    private final int code;
    private final ErrorMeta meta;

    GoodsErrorCode(int code, ErrorMeta meta) {
        this.code = code;
        this.meta = meta;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public ErrorMeta meta() {
        return meta;
    }
}
