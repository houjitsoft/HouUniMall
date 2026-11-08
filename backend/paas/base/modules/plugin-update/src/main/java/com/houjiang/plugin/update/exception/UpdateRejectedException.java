package com.houjiang.plugin.update.exception;

import com.houjiang.plugin.update.UpdateStep;

/**
 * 安装 / 升级链路的拒绝异常（携带插件分发错误码与失败阶段）。
 *
 * <p>为什么单独建一个异常类型而不是抛 {@code IllegalStateException}：
 * 编排链路里「预检不通过」「校验和不匹配」「验签失败」「并发冲突」是<b>预期内的业务结果</b>，
 * 各自对应 3001-3018 里的明确错误码，必须由控制器原样回给前端
 * （前端按 code 分支，例如 3016 要提示「任务进行中」而不是「服务器错误」）。
 * 用通用异常会丢掉错误码，最后只能统一回 500，前端就没法区分处理了。
 *
 * @author Amos
 * @since M3
 */
public class UpdateRejectedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 插件分发错误码（3001-3018），非分发类失败用 500。
     */
    private final int code;

    /**
     * 失败发生在哪个阶段，可为 null（预检阶段之前就失败时）。
     */
    private final UpdateStep step;

    /**
     * 构造拒绝异常。
     *
     * @param code    错误码
     * @param message 可直接展示给用户的原因文案
     */
    public UpdateRejectedException(int code, String message) {
        this(code, message, null);
    }

    /**
     * 构造拒绝异常。
     *
     * @param code    错误码
     * @param message 可直接展示给用户的原因文案
     * @param step    失败阶段
     */
    public UpdateRejectedException(int code, String message, UpdateStep step) {
        super(message);
        this.code = code;
        this.step = step;
    }

    /**
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * @return 失败阶段，可能为 null
     */
    public UpdateStep getStep() {
        return step;
    }
}
