package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * ORG（域 34）组织与员工错误码，号段 34001-34999。
 *
 * <p>解决的问题：组织架构操作（删部门、解绑员工）的失败原因过去只有一句「操作失败」，
 * 用户不知道是「有子部门」还是「没权限」。本域逐条登记。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum OrgErrorCode implements ErrorCodeDef {

    /**
     * 组织不存在（部门被删除或 ID 无效）。
     *
     * <p>触发：按组织 ID 查不到记录。用户看到「组织不存在或已被删除」。
     *
     * <p>排查：确认组织 ID 与组织架构树是否同步。
     */
    ORG_NOT_FOUND(34001, ErrorMeta.builder(34001, "组织不存在或已被删除")
            .devMessage("按组织 ID 未查到组织记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 组织参数不合法（名称为空、父级无效）。
     *
     * <p>触发：组织维护接口参数校验失败。用户看到「组织信息填写有误，请检查」。
     *
     * <p>排查：确认父级组织是否存在、名称是否重复。
     */
    ORG_PARAM_INVALID(34002, ErrorMeta.builder(34002, "组织信息填写有误，请检查")
            .devMessage("组织维护参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 组织状态不允许（已停用的组织不允许加人）。
     *
     * <p>触发：对停用组织执行操作。用户看到「该组织已停用，无法操作」。
     *
     * <p>排查：确认组织启用状态。
     */
    ORG_STATE_NOT_ALLOWED(34003, ErrorMeta.builder(34003, "该组织已停用，无法操作")
            .devMessage("组织状态为停用，操作被拒绝")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 组织名称重复（同级下已存在同名组织）。
     *
     * <p>触发：新增/改名时名称冲突。用户看到「同级下已存在同名组织」。
     *
     * <p>排查：确认是否应改为移动到该组织下。
     */
    ORG_DUPLICATE_NAME(34004, ErrorMeta.builder(34004, "同级下已存在同名组织")
            .devMessage("同级组织名称唯一约束冲突")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 越权访问其它组织的数据。
     *
     * <p>触发：数据权限校验发现组织不匹配。用户看到「你不能操作其它组织的数据」。
     *
     * <p>排查：确认当前组织上下文切换是否正确。
     */
    ORG_ACCESS_DENIED(34005, ErrorMeta.builder(34005, "你不能操作其它组织的数据")
            .devMessage("组织数据权限校验未通过")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.PERMISSION)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 员工不存在。
     *
     * <p>触发：按员工 ID 查不到记录。用户看到「员工不存在或已离职」。
     *
     * <p>排查：确认员工 ID 与在职状态。
     */
    ORG_EMPLOYEE_NOT_FOUND(34010, ErrorMeta.builder(34010, "员工不存在或已离职")
            .devMessage("按员工 ID 未查到员工记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 员工已绑定（账号已被其它员工占用）。
     *
     * <p>触发：绑定账号时冲突。用户看到「该账号已被其它员工绑定」。
     *
     * <p>排查：确认是否应先解绑原员工。
     */
    ORG_EMPLOYEE_ALREADY_BOUND(34011, ErrorMeta.builder(34011, "该账号已被其它员工绑定")
            .devMessage("员工账号绑定唯一约束冲突")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.DIALOG).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 部门下仍有子部门（不允许直接删除）。
     *
     * <p>触发：删除部门时存在下级。用户看到「请先删除下级部门」。
     *
     * <p>排查：这是保护性拦截，先清理子部门再删。
     */
    ORG_DEPT_HAS_CHILDREN(34012, ErrorMeta.builder(34012, "请先删除下级部门")
            .devMessage("待删除部门存在下级组织")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.DIALOG).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),
    /**
     * 账号名称已存在，请更换。
     *
     * <p>触发/排查：组织/员工账号唯一键冲突。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ORG_ACCOUNT_DUPLICATE(34101, ErrorMeta.builder(34101, "账号名称已存在，请更换")
            .devMessage("组织/员工账号唯一键冲突")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(10033)
            .since("v1.3.0")
            .build()),

    /**
     * 该部门下还有用户，无法删除。
     *
     * <p>触发/排查：删除部门时部门下仍存在成员。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ORG_DEPT_HAS_USER(34102, ErrorMeta.builder(34102, "该部门下还有用户，无法删除")
            .devMessage("删除部门时部门下仍存在成员")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(10034)
            .since("v1.3.0")
            .build()),

    ;

    private final int code;
    private final ErrorMeta meta;

    OrgErrorCode(int code, ErrorMeta meta) {
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
