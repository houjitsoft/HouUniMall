package com.houjiang.plugin.signature;

/**
 * 制品签名相关异常。
 *
 * <p>仅在「无法完成签名动作」时抛出（私钥缺失、kid 未配置、算法不可用等），
 * 用于与「验签不通过」区分：验签不通过是正常业务分支（返回 false + 错误码 3011），
 * 不是异常。
 *
 * @author HouCloud Plugin Platform
 * @since M0
 */
public class ArtifactSignatureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造异常。
     *
     * @param message 中文错误描述
     */
    public ArtifactSignatureException(String message) {
        super(message);
    }

    /**
     * 构造异常。
     *
     * @param message 中文错误描述
     * @param cause   原始异常
     */
    public ArtifactSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
