package com.houjiang.plugin.update.precheck;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.plugin.update.config.UpdateProperties;
import com.houjiang.commons.version.PluginDependencyExpression;
import com.houjiang.commons.version.PluginDependencyParser;
import com.houjiang.commons.version.VersionComparison;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * 兼容性预检（P0-9）：安装前判断「这个包在本站点到底能不能装」。
 *
 * <p>检查三项：
 * <ol>
 *   <li><b>宿主匹配</b>（2026-10-27 新增）：制品声明的 {@code hostService} 必须是本服务，
 *       否则 3020——embedded 插件由宿主进程的引擎装载，跨宿主装载会被引擎
 *       fail-closed 拒绝（历史上它以 500 ENGINE_INSTALL_FAILED 的形态泄漏）；</li>
 *   <li><b>框架版本</b>：宿主 {@code houcloud.update.framework-version} ≥
 *       插件声明的 {@code hou_market_plugin.min_framework_version}，否则 3014；</li>
 *   <li><b>依赖插件</b>：{@code dependencies_json} 里每个依赖都已安装且已启用，否则 3015。
 *       解析走 commons 的 {@link PluginDependencyParser}——目录「有更新」判定用的是同一份，
 *       避免「目录说能升、预检说缺依赖」。</li>
 * </ol>
 *
 * <p><b>弱校验口径（与 M1 清单校验一致）</b>：声明本身缺失或格式非法一律<b>跳过</b>而不是拒绝。
 * 理由：商品行缺 dependencies 段、min_framework_version 为 null 的存量插件很多，
 * 「声明缺失」与「不兼容」是两件事——前者不该让已上架的插件整体装不上。
 * 但<b>声明了就必须满足</b>，这是硬门槛。
 *
 * <p><b>为什么安装流程内部也要先调它</b>：只在前端调等于把门禁放在客户端，
 * 攻击者直接打 {@code /install} 就穿过去了；预检必须是服务端的强制前置步骤。
 *
 * @author Amos
 * @since M3
 */
@Slf4j
@Service
public class PluginCompatibilityPrecheckService {

    /**
     * 版本已发布（published）。
     */
    private static final int VERSION_PUBLISHED = 1;

    private final MarketPluginMapper pluginMapper;

    private final MarketPluginVersionMapper versionMapper;

    private final MallPluginInstallMapper installMapper;

    private final UpdateProperties properties;

    /** 宿主身份：{@code houcloud.plugin.host-service}，与引擎 PluginEngineProperties 同源；空时回退 spring.application.name。 */
    @Value("${houcloud.plugin.host-service:}")
    private String configuredHostService;

    /** 本服务名（host-service 未显式配置时的宿主身份回退）。 */
    @Value("${spring.application.name:}")
    private String applicationName;

    private String localHostService() {
        if (StringUtils.hasText(configuredHostService)) {
            return configuredHostService.trim();
        }
        return applicationName == null ? "" : applicationName.trim();
    }

    /**
     * 构造预检服务。
     *
     * @param pluginMapper  商品 Mapper
     * @param versionMapper 版本 Mapper
     * @param installMapper 安装态 Mapper
     * @param properties    编排配置（宿主框架版本）
     */
    public PluginCompatibilityPrecheckService(MarketPluginMapper pluginMapper,
                                              MarketPluginVersionMapper versionMapper,
                                              MallPluginInstallMapper installMapper,
                                              UpdateProperties properties) {
        this.pluginMapper = Objects.requireNonNull(pluginMapper, "MarketPluginMapper 不能为空");
        this.versionMapper = Objects.requireNonNull(versionMapper, "MarketPluginVersionMapper 不能为空");
        this.installMapper = Objects.requireNonNull(installMapper, "MallPluginInstallMapper 不能为空");
        this.properties = Objects.requireNonNull(properties, "UpdateProperties 不能为空");
    }

    /**
     * 预检指定插件版本（版本为空时按最新已发布版本判定）。
     *
     * @param pluginCode 插件编码
     * @param version    版本号，可为空
     * @return 预检结果
     */
    public PrecheckResult precheck(String pluginCode, String version) {
        if (!StringUtils.hasText(pluginCode)) {
            return PrecheckResult.fail(PluginDistErrorCode.ARTIFACT_NOT_FOUND, "插件编码不能为空");
        }
        String code = pluginCode.trim();
        MarketPluginVersion target = resolveVersion(code, version);
        if (target == null) {
            return PrecheckResult.fail(PluginDistErrorCode.ARTIFACT_NOT_FOUND,
                    "插件版本不存在或未发布：" + code + "@" + (StringUtils.hasText(version) ? version.trim() : "latest"));
        }
        MarketPlugin plugin = pluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, code)
                .last("limit 1"));
        if (plugin == null) {
            // 商品行缺失时无从判断，按弱校验放行（与授权门的 FREE 兜底同一口径）
            log.warn("预检跳过：商品行不存在，无法判定兼容性: {}", code);
            return PrecheckResult.pass();
        }
        PrecheckResult hostResult = checkHostService(target);
        if (!hostResult.isPass()) {
            return hostResult;
        }
        PrecheckResult frameworkResult = checkFramework(plugin, target);
        if (!frameworkResult.isPass()) {
            return frameworkResult;
        }
        return checkDependencies(plugin, target);
    }

    /**
     * 宿主匹配检查：制品声明的 hostService 不是本服务时拒绝（3020）。
     *
     * <p>embedded 插件由宿主进程内的 PF4J 引擎装载，引擎的 validateLocalPlugin
     * 对「插件归属服务 != 引擎宿主」fail-closed 拒绝（Plugin hostService does not
     * match this service）。不在这里提前拦的话，用户在插件中心对 goods 插件点安装，
     * 只能拿到一个 500 ENGINE_INSTALL_FAILED——真正的安装路径（制品投放 goods 的
     * plugins 目录、由 goods 引擎启动装载）完全藏在报错背后。
     *
     * <p>声明缺失时放行：无 hostService 的插件交给引擎按 legacy host / 校验链处理，
     * 预检不重复引擎的失败语义。
     *
     * @param target 目标版本（读制品 plugin_yaml 的 hostService 声明）
     * @return 预检结果
     */
    private PrecheckResult checkHostService(MarketPluginVersion target) {
        String declared = declaredHostService(target);
        if (!StringUtils.hasText(declared)) {
            return PrecheckResult.pass();
        }
        String local = localHostService();
        if (declared.equals(local)) {
            return PrecheckResult.pass();
        }
        return PrecheckResult.fail(PluginDistErrorCode.HOST_SERVICE_MISMATCH,
                "插件宿主为 " + declared + "，本服务为 " + (StringUtils.hasText(local) ? local : "未知")
                        + "；embedded 插件必须由宿主服务装载，请将制品投放 " + declared
                        + " 的 plugins 目录后由宿主引擎启动装载（插件中心 install 流水线不跨宿主）");
    }

    /**
     * 解析制品 plugin_yaml 顶层的 hostService 声明；解析失败一律返回空串（弱校验口径）。
     */
    @SuppressWarnings("unchecked")
    private String declaredHostService(MarketPluginVersion target) {
        String yaml = target == null ? null : target.getPluginYaml();
        if (!StringUtils.hasText(yaml)) {
            return "";
        }
        try {
            Object loaded = new Yaml().load(yaml);
            if (!(loaded instanceof Map<?, ?> root)) {
                return "";
            }
            Object value = ((Map<String, Object>) loaded).get("hostService");
            return value == null ? "" : String.valueOf(value).trim();
        } catch (RuntimeException e) {
            log.warn("解析制品 hostService 声明失败（预检放行，交由引擎判定）: {}",
                    target == null ? "?" : target.getPluginCode(), e);
            return "";
        }
    }

    /**
     * 框架版本检查。
     *
     * @param plugin 商品行
     * @param target 目标版本
     * @return 检查结果
     */
    private PrecheckResult checkFramework(MarketPlugin plugin, MarketPluginVersion target) {
        String required = plugin.getMinFrameworkVersion();
        if (!StringUtils.hasText(required)) {
            return PrecheckResult.pass();
        }
        String current = properties.getFrameworkVersion();
        if (VersionComparison.atLeast(current, required)) {
            return PrecheckResult.pass();
        }
        return PrecheckResult.fail(PluginDistErrorCode.INCOMPATIBLE_FRAMEWORK,
                "当前框架版本 " + (StringUtils.hasText(current) ? current : "未知")
                        + " 低于插件要求的最低版本 " + required.trim()
                        + "，请先升级系统后再安装（插件：" + target.getPluginCode() + " " + target.getVersion() + "）");
    }

    /**
     * 依赖检查（<b>传递解析 + 环检测</b>）：每个依赖必须已安装且已启用，
     * 声明了 {@code code@minVersion} 时还比对版本。
     *
     * <p><b>为什么必须做传递解析</b>：早期只看直接依赖。A 依赖 B、B 依赖 C 的场景下，
     * A 能通过预检（B 已装），装完却发现 C 没装导致 A 运行期报错——
     * 预检明明是「装之前告诉你行不行」，却漏掉了最需要它拦的一类问题。
     *
     * <p><b>为什么必须做环检测</b>：成环的依赖图<b>不存在合法安装顺序</b>，
     * 无论先装哪个都会撞上「依赖未安装」，用户只能反复试错。
     * 这里在预检阶段沿依赖边 DFS，命中回环直接报出完整链路。
     *
     * @param plugin 商品行
     * @param target 目标版本
     * @return 检查结果
     */
    private PrecheckResult checkDependencies(MarketPlugin plugin, MarketPluginVersion target) {
        List<PluginDependencyExpression> dependencies =
                PluginDependencyParser.parse(plugin.getDependenciesJson());
        if (dependencies.isEmpty()) {
            return PrecheckResult.pass();
        }
        String root = target.getPluginCode();
        Deque<String> path = new ArrayDeque<>();
        path.push(root);
        return walkDependencies(dependencies, path, new HashSet<>(Set.of(root)));
    }

    /**
     * 沿依赖边深度遍历：校验每个依赖的安装态，并递归它的依赖。
     *
     * @param dependencies 当前插件的直接依赖
     * @param path         依赖链（栈顶为当前插件），用于环检测与报错定位
     * @param visited      已遍历过的插件，防止菱形依赖被重复展开（指数级放大）
     * @return 首个不满足的依赖对应的失败结果；全部满足返回 pass
     */
    private PrecheckResult walkDependencies(List<PluginDependencyExpression> dependencies,
                                            Deque<String> path,
                                            Set<String> visited) {
        for (PluginDependencyExpression dependency : dependencies) {
            String code = dependency.code();
            if (path.contains(code)) {
                return PrecheckResult.fail(PluginDistErrorCode.DEPENDENCY_CYCLE,
                        "插件依赖成环：" + describeCycle(path, code)
                                + " —— 环上的插件不存在合法安装顺序，请先解除循环依赖");
            }
            MallPluginInstall installed = installMapper.selectOne(new LambdaQueryWrapper<MallPluginInstall>()
                    .eq(MallPluginInstall::getPluginCode, code)
                    .last("limit 1"));
            if (installed == null || !Integer.valueOf(1).equals(installed.getInstalled())) {
                return PrecheckResult.fail(PluginDistErrorCode.DEPENDENCY_MISSING,
                        "依赖插件 " + code + " 未安装（依赖链：" + describeChain(path, code)
                                + "），请先安装它再安装本插件");
            }
            if (!Integer.valueOf(1).equals(installed.getEnabled())) {
                return PrecheckResult.fail(PluginDistErrorCode.DEPENDENCY_MISSING,
                        "依赖插件 " + code + " 已安装但未启用（依赖链：" + describeChain(path, code)
                                + "），请先启用它再安装本插件");
            }
            if (dependency.requiresMin() && !dependency.satisfiedBy(installed.getVersion())) {
                return PrecheckResult.fail(PluginDistErrorCode.DEPENDENCY_MISSING,
                        "依赖插件 " + code + " 已装版本 "
                                + (installed.getVersion() == null ? "未知" : installed.getVersion())
                                + " 低于要求的最低版本 " + dependency.minVersion()
                                + "（依赖链：" + describeChain(path, code) + "）");
            }
            if (!visited.add(code)) {
                continue;
            }
            List<PluginDependencyExpression> nested = nestedDependenciesOf(code);
            if (nested.isEmpty()) {
                continue;
            }
            path.push(code);
            PrecheckResult nestedResult = walkDependencies(nested, path, visited);
            path.pop();
            if (!nestedResult.isPass()) {
                return nestedResult;
            }
        }
        return PrecheckResult.pass();
    }

    /**
     * 查某插件自身声明的依赖（用于传递解析）。
     *
     * <p>查不到商品行时返回空列表：依赖链不完整不该让预检直接失败——
     * 那会把「目录数据缺失」误报成「依赖不满足」，方向是错的。
     */
    private List<PluginDependencyExpression> nestedDependenciesOf(String pluginCode) {
        MarketPlugin plugin = pluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, pluginCode)
                .last("limit 1"));
        if (plugin == null) {
            return List.of();
        }
        // 按 code 查却拿回别的插件，说明这次查询不可信（Mock、脏数据、或走了兜底实现）。
        // 此时绝不能拿它的依赖继续推导——否则会把「A 依赖 A」这种假环当成真环报给用户。
        if (plugin.getPluginCode() == null || !pluginCode.equals(plugin.getPluginCode().trim())) {
            return List.of();
        }
        return PluginDependencyParser.parse(plugin.getDependenciesJson());
    }

    /**
     * 依赖链描述（{@code A -> B -> C}）。
     *
     * <p>栈顶是最近访问的插件，而人读的顺序是从根开始，因此需要反转后拼接。
     */
    private static String describeChain(Deque<String> path, String tail) {
        List<String> chain = new ArrayList<>(path);
        Collections.reverse(chain);
        chain.add(tail);
        return String.join(" -> ", chain);
    }

    /** 环描述（{@code A -> B -> A}）：从环的起点截断，避免把环外前缀也拼进来。 */
    private static String describeCycle(Deque<String> path, String repeated) {
        List<String> chain = new ArrayList<>(path);
        Collections.reverse(chain);
        int start = chain.indexOf(repeated);
        List<String> cycle = start >= 0 ? chain.subList(start, chain.size()) : chain;
        cycle = new ArrayList<>(cycle);
        cycle.add(repeated);
        return String.join(" -> ", cycle);
    }

    /**
     * 定位目标版本：指定版本按精确匹配，未指定取最新已发布版本。
     *
     * @param pluginCode 插件编码
     * @param version    版本号，可为空
     * @return 版本行，不存在返回 null
     */
    private MarketPluginVersion resolveVersion(String pluginCode, String version) {
        LambdaQueryWrapper<MarketPluginVersion> wrapper = new LambdaQueryWrapper<MarketPluginVersion>()
                .eq(MarketPluginVersion::getPluginCode, pluginCode)
                .eq(MarketPluginVersion::getStatus, VERSION_PUBLISHED);
        if (StringUtils.hasText(version)) {
            wrapper.eq(MarketPluginVersion::getVersion, version.trim());
        } else {
            wrapper.orderByDesc(MarketPluginVersion::getPublishTime)
                    .orderByDesc(MarketPluginVersion::getId);
        }
        return versionMapper.selectOne(wrapper.last("limit 1"));
    }
}
