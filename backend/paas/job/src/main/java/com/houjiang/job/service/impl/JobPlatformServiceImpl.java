package com.houjiang.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.job.dto.JobDTO;
import com.houjiang.job.entity.JobPlatformEntity;
import com.houjiang.job.job.BaseJob;
import com.houjiang.job.mapper.JobPlatformMapper;
import com.houjiang.job.service.JobPlatformService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * @author Amos
 */
@Service
public class JobPlatformServiceImpl extends ServiceImpl<JobPlatformMapper, JobPlatformEntity>
        implements JobPlatformService {

    private static final Logger logger = LoggerFactory.getLogger(JobPlatformServiceImpl.class);

    /** 加入Qulifier注解，通过名称注入bean */
    @Autowired
    @Qualifier("Scheduler")
    private Scheduler scheduler;

    /**
     * 获取指定包下的所有类名。
     *
     * @param packageName 包的名称，使用Java的包命名规范。
     * @param isRecursion 是否递归查找子包中的类。
     * @return 返回一个包含所有找到的类名的集合，如果未找到任何类，则返回空集合。
     */
    /**
     * 获取某包下所有类
     *
     * @param packageName 包名
     * @param isRecursion 是否遍历子包
     * @return 类的完整名称
     */
    public static Set<String> getClassName(String packageName, boolean isRecursion) {
        /* 存储类名的集合 */
        Set<String> classNames = null;
        /* 获取当前线程的类加载器 */
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        /* 将包名转换为文件路径格式 */
        String packagePath = packageName.replace(".", "/");

        /* 尝试从类加载器中获取指定包的资源路径或JAR文件 */
        URL url = loader.getResource(packagePath);
        if (url != null) {
            /* 获取资源路径的协议类型 */
            String protocol = url.getProtocol();
            /* 根据协议类型分别处理文件路径和JAR文件 */
            if (protocol.equals("file")) {
                /* 处理文件路径下的类 */
                classNames = getClassNameFromDir(url.getPath(), packageName, isRecursion);
            } else if (protocol.equals("jar")) {
                /* 处理JAR文件中的类 */
                JarFile jarFile = null;
                try {
                    jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (jarFile != null) {
                    /* 从JAR文件中提取类名 */
                    return getClassNameFromJar(jarFile.entries(), packageName, isRecursion);
                }
            }
        } else {
            /* 如果无法通过类加载器获取资源，尝试从所有的JAR文件中查找类 */
            /*从所有的jar包中查找包名*/
            classNames = getClassNameFromJars(((URLClassLoader) loader).getURLs(), packageName, isRecursion);
        }
        return classNames;
    }


    /**
     * 从项目文件获取某包下所有类
     *
     * @param filePath    文件路径
     * @param packageName 类名集合
     * @param isRecursion 是否遍历子包
     * @return 类的完整名称
     */
    private static Set<String> getClassNameFromDir(String filePath, String packageName, boolean isRecursion) {
        // 初始化一个Set集合，用于存储找到的类名。
        Set<String> className = new HashSet<String>();
        // 根据文件路径创建File对象。
        File file = new File(filePath);
        // 列出当前目录下的所有文件和目录。
        File[] files = file.listFiles();
        // 遍历当前目录下的所有文件和目录。
        for (File childFile : files) {
            // 检查当前项是否为目录。
            if (childFile.isDirectory()) {
                // 如果允许递归，并且当前项是目录，则递归调用本方法处理子目录。
                if (isRecursion) {
                    // 递归调用本方法处理子目录，并更新包名以反映当前子目录。
                    className.addAll(getClassNameFromDir(childFile.getPath(), packageName + "." + childFile.getName(), isRecursion));
                }
            } else {
                // 处理当前项为.class文件的情况。
                String fileName = childFile.getName();
                // 检查文件名是否以.class结尾，并且不是内部类（内部类的文件名包含'$'）。
                if (fileName.endsWith(".class") && !fileName.contains("$")) {
                    // 构造类的完整命名空间，并添加到类名集合中。
                    className.add(packageName + "." + fileName.replace(".class", ""));
                }
            }
        }
        // 返回找到的类名集合。
        return className;
    }


    /**
     * 从JAR文件中获取指定包名下的类名集合。
     *
     * @param jarEntries  JAR文件中的条目枚举。
     * @param packageName 要搜索的包名。
     * @param isRecursion 是否递归搜索子包。
     * @return 指定包名下所有类的集合。
     */
    private static Set<String> getClassNameFromJar(Enumeration<JarEntry> jarEntries, String packageName, boolean isRecursion) {
        Set<String> classNames = new HashSet<String>();

        // 遍历JAR文件中的所有条目
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            // 排除目录条目
            if (!jarEntry.isDirectory()) {
                // 将文件路径中的斜杠替换为点，以匹配Java的类名命名规范
                String entryName = jarEntry.getName().replace("/", ".");
                // 检查是否为class文件，并且不属于内部类
                if (entryName.endsWith(".class") && !entryName.contains("$") && entryName.startsWith(packageName)) {
                    // 移除class文件的后缀
                    entryName = entryName.replace(".class", "");
                    // 如果允许递归，则直接添加类名
                    if (isRecursion) {
                        classNames.add(entryName);
                    } else {
                        // 如果不允许递归，只添加顶级类名
                        // 通过检查类名是否包含额外的点来确定是否为顶级类
                        if (!entryName.replace(packageName + ".", "").contains(".")) {
                            classNames.add(entryName);
                        }
                    }
                }
            }
        }
        return classNames;
    }


    /**
     * 从给定的JAR文件集中搜索指定包下的所有类名。
     *
     * @param urls JAR文件的URL数组，可以包含类路径和其他资源路径。
     * @param packageName 要搜索的包的名称。
     * @param isRecursion 是否递归搜索子包。
     * @return 指定包下所有类的完整类名的集合。
     */
    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     *
     * @param urls        URL集合
     * @param packageName 包路径
     * @param isRecursion 是否遍历子包
     * @return 类的完整名称
     */
    private static Set<String> getClassNameFromJars(URL[] urls, String packageName, boolean isRecursion) {
        // 初始化一个集合来存储找到的类名
        Set<String> classNames = new HashSet<String>();

        // 遍历所有的URL
        for (int i = 0; i < urls.length; i++) {
            // 获取URL的路径
            String classPath = urls[i].getPath();

            // 跳过classes目录，因为它不是JAR文件
            //不必搜索classes文件夹
            if (classPath.endsWith("classes/")) {
                continue;
            }

            JarFile jarFile = null;
            try {
                // 从路径中创建JarFile对象
                jarFile = new JarFile(classPath.substring(classPath.indexOf("/")));
            } catch (IOException e) {
                // 打印堆栈跟踪以记录错误
                e.printStackTrace();
            }

            // 如果JarFile成功创建，则进一步处理
            if (jarFile != null) {
                // 从JAR文件中提取类名，并添加到集合中
                classNames.addAll(getClassNameFromJar(jarFile.entries(), packageName, isRecursion));
            }
        }

        // 返回找到的类名集合
        return classNames;
    }


    /**
     * 根据类名动态实例化BaseJob类的子类。
     * 这个方法允许程序在运行时根据提供的类名创建特定的BaseJob子类实例，增强了程序的灵活性和可扩展性。
     *
     * @param classname 待实例化的BaseJob子类的全限定类名（包括包名）。
     * @return 返回成功实例化的BaseJob子类对象。
     * @throws Exception 如果类加载失败或实例化过程中出现异常，则抛出Exception。
     */
    public static BaseJob getClass(String classname) throws Exception {
        // 通过Class.forName加载指定名称的类，不需明确指定类的包名。
        Class<?> class1 = Class.forName(classname);
        // 使用newInstance方法实例化类。这里隐含地调用了类的无参构造方法。
        return (BaseJob) class1.newInstance();
    }


    /**
     * 根据传入的参数查询作业信息。
     * 该方法主要用于根据用户token获取用户信息，并根据包名获取所有的作业类名，然后根据类名映射到具体的作业名称，最后返回作业名称的映射结果。
     *
     * @param paraMap 查询参数的映射表，其中应包含必要的token信息。
     * @return 返回一个包含作业名称和类名映射的结果集，如果token验证失败或参数不合法，则返回失败结果。
     */
    @Override
    public ResponseResult queryJob(Map<String, String> paraMap) {
        // 验证参数是否有效，包括token
        if (null != paraMap && null != paraMap.get("token")) {
            // 提取token
            String token = paraMap.get("token");
            // 根据token解析用户信息
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            if (null != userInfo) {
                // 定义作业类所在的包名
                String packageName = "com.houjiang.job.job";
                // 创建类名和作业名称的映射表
                Map<String, String> map = new HashMap<>();
                // 获取指定包下的所有类名
                Set<String> classNames = getClassName(packageName, false);
                if (classNames != null) {
                    // 遍历类名集合，根据类名映射到作业名称
                    for (String className : classNames) {
                        // 根据不同的类名，映射到不同的作业名称
                        switch (className) {
                            case "com.houjiang.job.job.UpdateActivityGoodsStatus":
                                map.put("更新限时活动", className);
                                break;
                            case "com.houjiang.job.job.RetentionMoneyJob":
                                map.put("质保金定时任务", className);
                                break;
                            case "com.houjiang.job.job.RetentionMoneyShopJob":
                                map.put("更新质保金基础额度定时任务", className);
                                break;
                            case "com.houjiang.job.job.PlusMemberJob":
                                map.put("plus会员更新，红包发放", className);
                                break;
                            case "com.houjiang.job.job.UpdateSeckillOrderBaseStateJob":
                                map.put("更新限时秒杀超过订单取消时间，未支付订单", className);
                                break;
                            case "com.houjiang.job.job.AfterSalesJob":
                                map.put("售后超时未处理定时任务", className);
                                break;
                            case "com.houjiang.job.job.SalesPromotionsJob":
                                map.put("红包、优惠券过期状态修改", className);
                                break;
                            case "com.houjiang.job.job.UpdateOdbStatisticsCatJob":
                                map.put("按商品类型每月统计销售额", className);
                                break;
                            case "com.houjiang.job.job.DayAndMonthStatisticsJob":
                                map.put("统计每天每月的订单量金额", className);
                                break;
                            case "com.houjiang.job.job.PdbMoneyStatisticsJob":
                                map.put("查询每月激活未充值的总金额", className);
                                break;
                            case "com.houjiang.job.job.UpdateReceiverOrderStatusJob":
                                map.put("更新超过7天未确认收货订单", className);
                                break;
                            case "com.houjiang.job.job.UpdateCommonStatuJob":
                                map.put("定时改变库存为0的商品状态", className);
                                break;
                            case "com.houjiang.job.job.UpdateTimeoutOrderStatusJob":
                                map.put("更新超过1天未支付订单", className);
                                break;
                            case "com.houjiang.job.job.AcquiescenceJob":
                                map.put("确认收货超过30天默认好评,不存在退款退货", className);
                                break;
                            case "com.houjiang.job.job.RefundOrderHandleJob":
                                map.put("退款/仅退款订单超24小时商家未处理,自动同意", className);
                                break;
                            case "com.houjiang.job.job.ReturnOrderHandleJob":
                                map.put("退货订单超24小时商家未处理,自动同意", className);
                                break;
//                            case "com.houjiang.job.job.UpdateBusinessOvertimeOrderJob":
//                                map.put("待发货退款超24小时商家未处理自动处理退款", className);
//                                break;
                            case "com.houjiang.job.job.SettleAccountsJob":
                                map.put("结算单", className);
                                break;
                            //case "com.houjiang.job.job.SettleDealerAccountsJob":
                            //    map.put("经销订单商家结算", className);
                            //    break;
                            //case "com.houjiang.job.job.SettleSupplyAccountsJob":
                            //    map.put("经销商品供应商结算", className);
                            //    break;
                            case "com.houjiang.job.job.UpdateEsGoodsWeightJob":
                                map.put("更新ES索引库", className);
                                break;
                            case "com.houjiang.job.job.UpdateVirtualOrderTimeJob":
                                map.put("修改虚拟订单的过期时间", className);
                                break;
                            case "com.houjiang.job.job.DelOperationDetailJob":
                                map.put("删除用户日志1个月后的日志", className);
                                break;
//                            case "com.houjiang.job.job.AppJiPushOrderJob":
//                                map.put("订单签收推送", className);
//                                break;
                            case "com.houjiang.job.job.UpdateWxLiveGoodsJob":
                                map.put("更新小程序直播商品状态", className);
                                break;
                            case "com.houjiang.job.job.GoodsTimeShelfJob":
                                map.put("商品定时发布上架", className);
                                break;
                            case "com.houjiang.job.job.VkOrderCommissionJob":
                                map.put("微客佣金", className);
                                break;
                            case "com.houjiang.job.job.UpdateSpinwinActivityStatusJob":
                                map.put("大转盘每日次数更新", className);
                                break;
                            case "com.houjiang.job.job.PromotionJob":
                                map.put("更新会员等级删除的用户等级", className);
                                break;
                            case "com.houjiang.job.job.UpdateALiWxOrderStart":
                                map.put("更新支付宝,微信因回调失败而产生的异常单", className);
                                break;
                            case "com.houjiang.job.job.zfbOrWxOrderStatusJob":
                                map.put("webPos查询订单是否支付成功", className);
                                break;
                            case "com.houjiang.job.job.UpdateOrderStatusJob":
                                map.put("webPos订单更新支付状态", className);
                                break;
                            case "com.houjiang.job.job.SettleWebPosOrderJob":
                                map.put("webPos订单结算", className);
                                break;
                            case "com.houjiang.job.job.UpdateCombinationJob":
                                map.put("更改组合商品活动状态", className);
                                break;
                            case "com.houjiang.job.job.UpdateImTimeOutJob":
                                map.put("处理Im超时的会话", className);
                                break;
                            case "com.houjiang.job.job.DcenterJob":
                                map.put("同步dcenter服务所需数据", className);
                                break;
                            case "com.houjiang.job.job.BarterOrderHandleJob":
                                map.put("处理超时的换货订单", className);
                                break;
                            case "com.houjiang.job.job.JingDongGoodsUpdateJob":
                                map.put("京东商品信息修改", className);
                                break;
                            case "com.houjiang.job.job.JingDongOrderUpdateJob":
                                map.put("京东订单信息修改", className);
                                break;
                            case "com.houjiang.job.job.MonthlySalaryCalculationJob":
                                map.put("月度工资自动计算（每月1号）", className);
                                break;
                            case "com.houjiang.job.job.MonthlySalaryCalculationJob5th":
                                map.put("每月5号工资自动计算", className);
                                break;
                            case "com.houjiang.job.job.AttendanceSyncJob":
                                map.put("第三方考勤自动同步（每日）", className);
                                break;
                            case "com.houjiang.job.job.GeoArticleSchedulePublishJob":
                                map.put("GEO文章定时发布", className);
                                break;
                            case "com.houjiang.job.job.GeoVideoSchedulePublishJob":
                                map.put("GEO视频定时发布", className);
                                break;
                            case "com.houjiang.job.job.GeoAccountKeepaliveJob":
                                map.put("GEO账号保活心跳（每日）", className);
                                break;
                        }
                    }
                }
                // 返回成功的响应结果，包含作业名称和类名的映射表
                return ResponseResult.success(map);
            }
            // 如果用户信息解析失败，则返回token非法的错误响应
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 如果参数验证失败，则返回参数异常的错误响应
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg());
    }


    /**
     * 根据用户token查询所有任务信息。
     *
     * @param job 任务DTO，包含查询条件和token信息。
     * @return 返回查询结果，包含分页信息和任务列表。
     */
    @Override
    public ResponseResult selectJobAll(JobDTO job) {
        try {
            // 校验token是否为空
            if (null != job && null != job.getToken()) {
                // 获取用户信息
                String token = job.getToken();
                UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
                // 如果第一次获取用户信息失败，尝试再次获取
                if (null == userInfo) {
                    userInfo = ParseTokenUtils.getPlatformUser(token);
                }
                // 当用户信息获取成功时，进行查询操作
                if (null != userInfo) {
                    // 将查询条件转换为Map格式
                    Map data = ParseMapUtils.beanToMap(job);

                    // 执行分页查询
                    IPage<JobDTO> page = this.baseMapper.selectJobAll(new Query<JobDTO>().getPage(data), job);
                    List<JobDTO> records = page.getRecords();
                    // 解析cron表达式，设置任务的分钟、小时、日期等属性
                    for (JobDTO record : records) {
                        String[] strArray = record.getCronExpression().split(" ");
                        record.setMin(strArray[1]);
                        record.setHour(strArray[2]);
                        record.setDay(strArray[3]);
                        record.setMouth(strArray[4]);
                        record.setWeek(strArray[5]);
                    }
                    // 更新分页信息中的记录列表
                    page.setRecords(records);
                    // 返回查询结果
                    return ResponseResult.success(PageUtils.getPage(page));
                }
                // 当用户信息获取失败时，返回token非法的错误信息
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // 当出现其他异常时，返回通用的验证异常信息
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg());

    }

    /**
     * 添加任务。
     *
     * @param job 任务详情对象，包含任务的相关配置信息。
     * @return 添加任务的结果，成功或失败及原因。
     */
    @Override
    public ResponseResult addJob(JobDTO job) {
        // 校验token是否有效
        if (null != job && null != job.getToken()) {
            // 通过token获取用户信息
            String token = job.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            // 校验用户信息是否存在
            if (null != userInfo) {
                // 查询是否存在相同调用目标的任务
                Integer num = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<JobPlatformEntity>()
                        .eq(JobPlatformEntity::getInvokeTarget, job.getInvokeTarget())));
                // 如果存在相同调用目标的任务，则返回失败结果
                if (num > 0) {
                    return ResponseResult.fail(BizCodeEnume.JOB_IS_EXIST);
                } else {
                    // 根据任务的分钟、小时、日期、月份、星期设置cron表达式
                    String cron = "0 " + job.getMin() + " " + job.getHour() + " " + job.getDay() + " " + job.getMouth() + " " + job.getWeek();
                    job.setCronExpression(cron);
                    // 设置任务状态为启用
                    job.setStatus(CommonType.commonStatus.YES.getCode().toString());
                    // 插入任务到数据库
                    this.baseMapper.insertJob(job);
                    try {
                        // 添加任务到调度中心
                        addJob(job.getInvokeTarget(), job.getJobGroup(), job.getCronExpression());
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 添加任务失败则返回相应失败结果
                        return ResponseResult.fail(BizCodeEnume.JOB_NO_YET_CLOSED);
                    }
                    // 返回任务添加成功的结果
                    return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
                }
            }
            // 用户信息校验失败则返回相应失败结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 任务详情或token为空则返回相应失败结果
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg());
    }

    public void addJob(String jobClassName, String jobGroupName, String cronExpression) throws Exception {

        //定时任务存入redis中
        //RedisUtil.sadd("scheduler", jobClassName);
        //RedisUtil.set(jobClassName, cronExpression);
        // 启动调度器
        scheduler.start();

        //构建job信息
        JobDetail jobDetail = JobBuilder.newJob(getClass(jobClassName).getClass()).withIdentity(jobClassName, jobGroupName).build();

        //表达式调度构建器(即任务执行的时间)
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

        //按新的cronExpression表达式构建一个新的trigger
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobClassName, jobGroupName)
                .withSchedule(scheduleBuilder).build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            System.out.println("创建定时任务失败" + e);
            throw new Exception("创建定时任务失败");
        }
    }

    /**
     * 根据ID编辑任务。
     * 该方法主要用于更新任务的配置信息，并根据新的配置信息重新安排任务。
     * 如果任务被暂停，则先恢复任务运行，然后再更新配置。
     * 如果任务的执行表达式发生变化，则需要先删除旧任务，然后添加新任务。
     *
     * @param job 任务DTO，包含任务的所有配置信息。
     * @return 返回操作结果，成功或失败，并包含可能的错误代码。
     * @throws SchedulerException 如果操作调度器时发生错误。
     */
    @Override
    public ResponseResult editJobById(JobDTO job) throws SchedulerException {
        // 校验token的有效性
        if (null != job && null != job.getToken()) {
            // 校验token
            String token = job.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            if (null == userInfo) {
                // 如果token校验失败，返回非法token错误
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
            }
            // 如果任务状态为暂停（0），则将其改为运行中（1）并暂停任务
            if ("0".equals(job.getStatus())) {
                job.setStatus("1");
                this.baseMapper.editJobById(job);
                scheduler.pauseJob(JobKey.jobKey(job.getInvokeTarget(), job.getJobGroup()));
            }
            // 设置任务的创建者和创建时间
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String finishTime = dateformat.format(new Date());
            job.setCreateBy(userInfo.getUserRealname());
            job.setCreateTime(finishTime);
            // 校验任务的执行规则是否符合预期
            ResponseResult rr = regexFlag(job);
            if (rr.getCode() == 1) {
                // 构造新的Cron表达式
                String cron = "0 " + job.getMin() + " " + job.getHour() + " " + job.getDay() + " " + job.getMouth() + " " + job.getWeek();
                job.setCronExpression(cron);
                try {
                    // 删除旧任务为更新任务配置做准备
                    deleteJob(job.getInvokeTarget(), job.getJobGroup());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    // 如果删除任务失败，返回更新错误信息
                    return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
                }
            } else {
                // 如果任务规则校验通过但不需更新Cron表达式，直接返回成功
                return ResponseResult.success();
            }
            // 更新任务配置信息
            int num = this.baseMapper.update(null, new LambdaUpdateWrapper<JobPlatformEntity>()
                    .set(JobPlatformEntity::getStatus, 0)
                    .set(JobPlatformEntity::getRemark, job.getRemark())
                    .set(JobPlatformEntity::getJobName, job.getJobName())
                    .set(JobPlatformEntity::getCronExpression, job.getCronExpression())
                    .set(JobPlatformEntity::getCreateBy, job.getCreateBy())
                    .set(JobPlatformEntity::getCreateTime, job.getCreateTime())
                    .eq(JobPlatformEntity::getJobId, job.getJobId()));
            if (num > 0) {
                try {
                    // 添加新任务以应用更新的配置
                    addJob(job.getInvokeTarget(), job.getJobGroup(), job.getCronExpression());
                } catch (Exception e) {
                    e.printStackTrace();
                    // 如果添加任务失败，返回更新错误信息
                    return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
                }
                // 返回更新成功信息
                return ResponseResult.success();
            }
            // 如果更新失败，返回非法token错误（尽管这可能不是确切的原因）
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }
        // 如果输入参数无效，返回参数验证错误信息
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg());
    }


    /**
     * 根据ID删除作业。
     *
     * @param job 作业DTO，包含作业ID和认证Token。
     * @return 删除操作的结果。
     * @throws Exception 如果删除过程中出现异常。
     */
    @Override
    public ResponseResult deleteJobById(JobDTO job) throws Exception {
        // 验证job和token是否为空
        if (null != job && null != job.getToken()) {
            // 获取用户信息
            String token = job.getToken();
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            // 如果用户信息为空，则返回认证失败的结果
            if (null == userInfo) {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
            }
            // 删除数据库中的作业记录
            this.baseMapper.deleteJobById(job);
            // 尝试删除作业相关的资源，如调度信息
            try {
                deleteJob(job.getInvokeTarget(), job.getJobGroup());
            } catch (Exception e) {
                // 如果删除资源时发生异常，则抛出异常，异常信息为资源删除失败的具体原因
                throw new Exception(e.getMessage());
            }
            // 返回删除成功的结果
            return ResponseResult.success();
        }
        // 如果job或token为空，则返回参数验证失败的结果
        return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg());
    }


    /**
     * 根据任务ID停止或恢复任务。
     * 如果任务当前状态为暂停（状态为0），则将任务状态更新为运行中（状态为1）并暂停任务。
     * 如果任务当前状态为运行中（状态为1），则将任务状态更新为暂停（状态为0）并恢复任务。
     * 此方法使用了事务处理，确保数据库操作的一致性。
     *
     * @param job 任务DTO，包含任务的ID、调用目标、任务组等信息。
     * @return 返回操作结果，成功则为{@link ResponseResult#success()}。
     * @throws Exception 如果操作失败或删除任务时发生异常，则抛出异常。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult stopJobById(JobDTO job) throws Exception {
        // 根据调用目标查询任务当前状态
        JobPlatformEntity num = this.baseMapper.selectOne(new LambdaQueryWrapper<JobPlatformEntity>()
                .eq(JobPlatformEntity::getInvokeTarget, job.getInvokeTarget()));

        // 如果任务状态为暂停（0），则执行暂停任务的操作
        if ("0".equals(num.getStatus())) {
            // 更新任务状态为运行中（1）
            job.setStatus("1");
            // 更新数据库中的任务状态
            int rows = this.baseMapper.editJobById(job);
            // 如果更新成功，则暂停任务
            if (rows > 0) {
                scheduler.pauseJob(JobKey.jobKey(job.getInvokeTarget(), job.getJobGroup()));
                logger.info("暂停任务" + job.getInvokeTarget());
            }
        } else {
            // 如果任务状态为运行中（1），则执行恢复任务的操作
            try {
                // 尝试删除任务，为重新添加任务做准备
                deleteJob(job.getInvokeTarget(), job.getJobGroup());
            } catch (Exception e) {
                // 如果删除任务失败，则抛出异常，异常信息为删除任务时的异常信息
                throw new Exception(e.getMessage());
            }
            // 更新任务状态为暂停（0）
            job.setStatus("0");
            // 更新数据库中的任务状态
            int rows = this.baseMapper.editJobById(job);
            // 如果更新成功，则重新添加任务
            if (rows > 0) {
                addJob(job.getInvokeTarget(), job.getJobGroup(), job.getCronExpression());
                logger.info("恢复任务" + job.getInvokeTarget());
            }
        }
        // 返回操作成功的响应结果
        return ResponseResult.success();
    }


    /**
     * 停止并删除指定的作业。
     * <p>
     * 此方法首先暂停作业的触发器，然后取消调度该触发器，最后删除指定的作业。
     * 这样做的目的是确保作业不会在删除过程中意外触发。
     *
     * @param jobClassName 作业类的名称，用于唯一标识作业。
     * @param jobGroupName 作业所属的组名，用于进一步定位作业。
     * @throws Exception 如果操作失败，将抛出异常。
     */
    public void deleteJob(String jobClassName, String jobGroupName) throws Exception {
        // 暂停触发器，防止作业在删除过程中被意外触发。
        scheduler.pauseTrigger(TriggerKey.triggerKey(jobClassName, jobGroupName));

        // 取消调度触发器，该作业将不再被调度执行。
        scheduler.unscheduleJob(TriggerKey.triggerKey(jobClassName, jobGroupName));

        // 删除指定的作业。
        scheduler.deleteJob(JobKey.jobKey(jobClassName, jobGroupName));
    }

    /**
     * 根据作业配置的定时规则，验证规则是否符合预期的正则表达式。
     * 此方法专注于验证分钟、小时、日期、月份、星期是否符合特定的正则表达式格式。
     *
     * @param job 作业配置的DTO对象，包含分钟、小时、日期、月份、星期的定时规则。
     * @return 返回验证结果，如果验证失败，会返回错误代码和错误信息。
     */
    private ResponseResult regexFlag(JobDTO job) {
        // 定义并编译分钟的正则表达式，用于匹配*或0/后的数字范围。
        String regex = "^(\\*|0/[0-9]|0/[1-5][0-9])$";
        Pattern pattern = Pattern.compile(regex);

        // 定义并编译日期的正则表达式，用于匹配*或0/后的数字或?。
        String regexRiQiYu = "^(\\*|0/\\d|\\?)$";
        Pattern patternRiQiYu = Pattern.compile(regexRiQiYu);

        // 定义并编译分钟的简化正则表达式，用于直接匹配0-59的数字。
        String regexMin = "^([0-9]|[1-5][0-9])$";
        Pattern patternMin = Pattern.compile(regexMin);

        // 定义并编译小时的正则表达式，用于匹配0-23的数字。
        String regexMinHour = "^([0-1]?\\d|2[0-3])$";
        Pattern patternHour = Pattern.compile(regexMinHour);

        // 定义并编译日期的简化正则表达式，用于匹配1-31的数字。
        String regexDay = "^([0-6])$";
        Pattern patternDay = Pattern.compile(regexDay);

        // 定义并编译月份的正则表达式，用于匹配1-12的数字。
        String regexMouth = "^([1-9]|1[0-1])$";
        Pattern patternMouth = Pattern.compile(regexMouth);

        // 定义并编译星期的正则表达式，用于匹配1-7或*的数字。
        String regexWeek = "^([1-7]|)$";
        Pattern patternWeek = Pattern.compile(regexWeek);

        // 验证日期和星期是否至少有一个为"?"，这是合法的特殊情况。
        if (!job.getDay().contains("?") && !job.getWeek().contains("?")) {
            return ResponseResult.fail(2, null, "日期跟星期需有一者为？");
        }

        // 依次验证分钟、小时、日期、月份、星期是否符合对应的正则表达式。
        if (pattern.matcher(job.getMin()).matches() || patternMin.matcher(job.getMin()).matches()) {
            if (pattern.matcher(job.getHour()).matches() || patternHour.matcher(job.getHour()).matches()) {
                if (patternRiQiYu.matcher(job.getDay()).matches() || patternDay.matcher(job.getDay()).matches()) {
                    if (patternRiQiYu.matcher(job.getMouth()).matches() || patternMouth.matcher(job.getMouth()).matches()) {
                        if (patternRiQiYu.matcher(job.getWeek()).matches() || patternWeek.matcher(job.getWeek()).matches()) {
                            return ResponseResult.success(true);
                        } else {
                            return ResponseResult.fail(2, null, "星期格式有误！");
                        }
                    } else {
                        return ResponseResult.fail(2, null, "月份格式有误！");
                    }
                } else {
                    return ResponseResult.fail(2, null, "日期格式有误！");
                }
            } else {
                return ResponseResult.fail(2, null, "小时格式有误！");
            }
        } else {
            return ResponseResult.fail(2, null, "分钟格式有误！");
        }
    }

}




