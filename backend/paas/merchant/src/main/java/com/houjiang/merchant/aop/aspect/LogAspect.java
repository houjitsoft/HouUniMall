package com.houjiang.merchant.aop.aspect;

import com.alibaba.fastjson2.JSON;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.common.entity.admin.Admin;
import com.houjiang.merchant.common.entity.system.OperationLog;
import com.houjiang.merchant.common.mapper.admin.AdminMapper;
import com.houjiang.merchant.common.mapper.system.SystemLogOperateMapper;
import com.houjiang.merchant.common.util.IpUtils;
import com.houjiang.merchant.common.util.RequestUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Amos
 */
@Component
@Aspect
public class LogAspect {

    @Resource
    SystemLogOperateMapper systemLogOperateMapper;

    @Resource
    AdminMapper adminMapper;

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /**
     * 线程本地变量
     */
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 单线程化的线程池
     */
    private final ExecutorService executor =  Executors.newSingleThreadExecutor();

    /**
     * 声明切面点拦截那些类
     */
    @Pointcut("@annotation(com.houjiang.merchant.aop.Log)")
    private void pointCutMethodController() {}

    /**
     * 环绕通知前后增强
     */
    @Around(value = "pointCutMethodController()")
    public Object doAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        // 开始时间
        threadLocal.set(System.currentTimeMillis());
        // 执行方法
        Object result = joinPoint.proceed();
        // 保存日志
        recordLog(joinPoint, null);
        // 返回结果
        return result;
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e 异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        recordLog(joinPoint, e);
    }

    /**
     * 记录日志信息
     *
     * @param joinPointObj joinPoint
     * @param e Exception 错误异常
     */
    private void recordLog(Object joinPointObj, final Exception e) {
        try {
            long beginTime = threadLocal.get();
            long endTime = System.currentTimeMillis();
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                // 取得请求对象
                HttpServletRequest request = requestAttributes.getRequest();

                // 获取当前的用户
                Integer adminId = MerchantThreadLocal.getAdminId();

                // 获取日志注解
                ProceedingJoinPoint joinPoint = (ProceedingJoinPoint) joinPointObj;
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                Method method = signature.getMethod();
                Log logAnnotation = method.getAnnotation(Log.class);

                // 方法名称
                String className = joinPoint.getTarget().getClass().getName();
                String methodName = joinPoint.getSignature().getName();

                // 获取请求参数
                String queryString = request.getQueryString();
                Object[] args = joinPoint.getArgs();
                String params = "";
                if (args.length > 0) {
                    if("POST".equals(request.getMethod())){
                        if (RequestType.File.equals(logAnnotation.requestType())){
                            //文件类型获取文件名称作为参数
                            StandardMultipartHttpServletRequest standardMultipartHttpServletRequest = (StandardMultipartHttpServletRequest) args[0];
                            //提取文件名
                            params = standardMultipartHttpServletRequest
                                    .getMultiFileMap()
                                    .values()
                                    .stream()
                                    .map(m -> m.stream()
                                            .map(MultipartFile::getOriginalFilename)
                                            .collect(Collectors.joining(",")))
                                    .collect(Collectors.joining(","));
                        } else {
                            params = JSON.toJSONString(args);
                        }

                    } else if("GET".equals(request.getMethod())){
                        params = queryString;
                    }
                }

                // 错误信息
                String error = "";
                int status = 1;
                if (e != null) {
                    error = e.getMessage();
                    // 1=成功, 2=失败
                    status = 2;
                }

                String adminName = "";
                Admin admin = adminMapper.selectById(adminId);
                if (admin != null) {
                    adminName = admin.getName();
                }
                // 数据库日志
                OperationLog model = new OperationLog();
                model.setAdminId(adminId);
                model.setAdminName(adminName);
                model.setAction(logAnnotation.title());
                model.setIp(IpUtils.getIpAddress());
                model.setType(request.getMethod());
//                model.setMethod(className + "." + methodName + "()");
                model.setUrl(RequestUtils.route());
                model.setParams(params);
                model.setResult(error);
//                model.setStatus(status);
//                model.setStartTime(beginTime / 1000);
//                model.setEndTime(endTime / 1000);
//                model.setTaskTime(endTime - beginTime);
                model.setCreateTime(System.currentTimeMillis() / 1000);
                executor.submit(() -> {
                    systemLogOperateMapper.insert(model);
                });
            }
        } catch (Exception ex) {
            log.error("异常信息:{}", ex.getMessage());
        }
    }

}
