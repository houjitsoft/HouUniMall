package com.houjiang.commons.exception;


import com.houjiang.commons.errorcode.domain.SystemErrorCode;
import com.houjiang.commons.errorcode.handler.ErrorCodeHandlerSupport;
import com.houjiang.commons.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author Amos
 *
 * <p>统一错误码改造说明（v1.2.0）：本类原先的错误分支全部走 {@code ResponseResult.success(...)} ——
 * 用「成功通道」返回错误码是反模式，前端按成功解析会拿到一个语义不明的失败码。
 * 本轮全部改为失败通道，并接入统一错误码：码值由
 * {@link ErrorCodeHandlerSupport#render(int, com.houjiang.commons.errorcode.ErrorCodeDef)} 产出，
 * 文案取新码的 {@code userMessage}（中文、可读、脱敏）。
 *
 * <p><b>已知风险（本轮不修，登记在案）</b>：本类没有 {@code @Order}，而
 * {@link GlobalExceptionHandler} 标注了 {@code @Order(-1)} 且同样拦截 {@code Exception.class}，
 * 因此本类实际上是被压制的死代码，不会被触发。之所以仍然改：一旦将来有人调整注册顺序让它生效，
 * 它必须不能再用成功通道返回错误码。调整注册顺序属于独立变更，需单独评估。
 */
@Slf4j
@RestControllerAdvice
public class SpringExceptionHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(SpringExceptionHandler.class);

	/**
	 * 全局处理Exception 错误的情况下返回2
	 * 
	 * @param ex
	 * @param
	 * @return
	 * @throws IOException
	 */
	@ExceptionHandler(value = { Exception.class })
	public ResponseResult handlerException(final Exception ex, final HttpServletRequest request) throws IOException {
		ResponseResult result = null;
		try {

			if (ex instanceof UserDefinedException ude) {

				// 业务自带码值原样透传（架构 §4.2 不换码）；文案过脱敏出口，异常原文只进日志
				LOG.error("handlerException 业务异常根因（仅日志，不出屏）", ex);
				result = ResponseResult.fail(ude.getErrorCode().getErrorCode(),
						ErrorCodeHandlerSupport.safeMessage(ude.getErrorCode().getErrorMsg(),
								SystemErrorCode.UNKNOWN_ERROR));

			} else if (ex instanceof BadSqlGrammarException || ex instanceof MyBatisSystemException
					|| ex instanceof HandlerExceptionResolver) {
				// SQL 原文与堆栈只进日志，出屏用统一码的中文文案
				LOG.error("handlerException 数据访问异常根因（仅日志，不出屏）", ex);
				ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(SystemErrorCode.DB_ACCESS_FAILED);
				result = ResponseResult.fail(render.code(), render.message());
			} else if (ex instanceof RejectedExecutionException) {
				LOG.error("handlerException 线程池拒绝，根因见日志", ex);
				ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(SystemErrorCode.SYSTEM_THREAD_POOL_REJECTED);
				result = ResponseResult.fail(render.code(), render.message());
			} else {
				LOG.error("handlerException 未分类异常根因（仅日志，不出屏）", ex);
				ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(SystemErrorCode.UNKNOWN_ERROR);
				result = ResponseResult.fail(render.code(), render.message());
			}

		} catch (Exception e) {
			LOG.error("handlerException 兜底：异常处理过程再次出错，根因见日志", e);
			ErrorCodeHandlerSupport.ErrorRenderResult render = ErrorCodeHandlerSupport.of(SystemErrorCode.UNKNOWN_ERROR);
			result = ResponseResult.fail(render.code(), render.message());
		}
		return result;
	}
}
