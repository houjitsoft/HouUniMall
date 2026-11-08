package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * CMS（域 50）内容站点错误码，号段 50001-50999。
 *
 * <p>解决的问题：cmsadmin 是「白名单放行」历史 bug 的重灾区（VALID_CODES 含 5 个错误码），
 * 内容侧「查不到」与「真出错」混在一起。本域把内容实体类错误独立登记，
 * 「查不到」统一按 EMPTY_STATE 处理，不再靠放行白名单。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum CmsErrorCode implements ErrorCodeDef {

    /**
     * 内容不存在（文章/页面已删除）。
     *
     * <p>触发：按内容 ID 查不到记录。用户看到「内容不存在或已删除」。
     *
     * <p>排查：确认内容 ID 与站点归属。
     */
    CMS_CONTENT_NOT_FOUND(50001, ErrorMeta.builder(50001, "内容不存在或已删除")
            .devMessage("按内容 ID 未查到内容记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 内容参数不合法（标题为空、SEO 字段超长）。
     *
     * <p>触发：内容维护接口校验失败。用户看到「内容信息填写有误，请检查」。
     *
     * <p>排查：确认必填字段与长度限制。
     */
    CMS_PARAM_INVALID(50002, ErrorMeta.builder(50002, "内容信息填写有误，请检查")
            .devMessage("内容参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 内容状态不允许（草稿不允许发布等）。
     *
     * <p>触发：对当前内容状态执行不允许的操作。用户看到「内容当前状态不允许该操作」。
     *
     * <p>排查：确认内容状态机；草稿需先审核。
     */
    CMS_STATE_NOT_ALLOWED(50003, ErrorMeta.builder(50003, "内容当前状态不允许该操作")
            .devMessage("内容状态机流转非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 内容别名重复（同一站点下 slug 冲突）。
     *
     * <p>触发：新增/修改别名冲突。用户看到「该别名已被占用，请更换」。
     *
     * <p>排查：确认是否应改为编辑已有内容。
     */
    CMS_DUPLICATE_SLUG(50004, ErrorMeta.builder(50004, "该别名已被占用，请更换")
            .devMessage("内容别名唯一约束冲突")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 查询不到内容数据（筛选条件无命中）。
     *
     * <p>触发：内容列表查询返回空。用户看到空状态占位，不弹 Toast。
     *
     * <p>排查：这是 INFO 级别，正常业务结果。
     */
    CMS_QUERY_RESULT_EMPTY(50007, ErrorMeta.builder(50007, "没有找到符合条件的内容")
            .devMessage("内容列表查询无命中")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 站点不存在。
     *
     * <p>触发：按站点 ID 查不到记录。用户看到「站点不存在或未启用」。
     *
     * <p>排查：确认站点配置与启用状态。
     */
    CMS_SITE_NOT_FOUND(50010, ErrorMeta.builder(50010, "站点不存在或未启用")
            .devMessage("按站点 ID 未查到站点记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 模板不存在。
     *
     * <p>触发：按模板 ID/编码查不到记录。用户看到「模板不存在，请先在模板管理中创建」。
     *
     * <p>排查：确认模板是否已导入；这是常见的初始化遗漏。
     */
    CMS_TEMPLATE_NOT_FOUND(50011, ErrorMeta.builder(50011, "模板不存在，请先创建模板")
            .devMessage("按模板 ID/编码未查到模板记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build())
    ;

    private final int code;
    private final ErrorMeta meta;

    CmsErrorCode(int code, ErrorMeta meta) {
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
