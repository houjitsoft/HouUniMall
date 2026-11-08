package com.houjiang.plugin.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.houjiang.plugin.sdk.annotation.HouHook;
import com.houjiang.plugin.sdk.annotation.HouPlugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件清单，映射 <code>plugin.yaml</code>。
 *
 * <p><strong>plugin.yaml 是运行期唯一事实来源</strong>；{@link HouPlugin} 注解仅用于编译期提示，
 * 宿主在 <code>VERIFIED</code> 阶段校验两者（code / version / minFrameworkVersion）一致，不一致直接拒绝安装。
 *
 * @author HouCloud
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginManifest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 插件唯一编码，全局不可重复 */
    private String code;

    /** 插件显示名称 */
    private String name;

    /** 版本号，遵循语义化版本 */
    private String version;

    /** 作者 */
    private String author;

    /** 插件描述 */
    private String description;

    /** 分类 */
    private String category;

    /** 归属：official=官方插件，thirdparty=商业第三方插件，custom=构建期二开能力包 */
    private String owner = "official";

    /** 运行形态：embedded=嵌入宿主进程，standalone=独立插件进程 */
    private String runtime = "embedded";

    /** 唯一业务归属，必须是架构基线中的服务名；不能填写 URL。 */
    private String hostService;

    /** 历史服务编码或远程能力 URL；仅显式兼容模式允许，不能覆盖 hostService。 */
    private String host;

    /** 最低框架兼容版本 */
    private String minFrameworkVersion;

    /** 适用端 */
    private List<String> terminals = new ArrayList<>();

    /** 依赖插件 */
    private List<PluginDependency> dependencies = new ArrayList<>();

    /** 许可类型：FREE / PAID / TRIAL */
    private String license = "FREE";

    /**
     * 历史制品兼容字段（分）。运营定价不再来自 manifest，而是来自市场表
     * {@code hou_market_plugin.price_cents}；新插件不要填写此字段。
     */
    @Deprecated
    private long price = 0L;

    /** 前端入口路径 */
    private String entryPath;

    /** 配置面板 Schema（JSON 字符串） */
    private String configSchema;

    /** 权限声明，如 DB_OWN_TABLE、HTTP_OUTBOUND */
    private List<String> permissions = new ArrayList<>();

    /** 前端资源 */
    private PluginFrontend frontend;

    /** 生命周期钩子：事件 → 钩子类全限定名 */
    private Map<HouHook.HookEvent, String> hooks = new LinkedHashMap<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getHostService() {
        return hostService;
    }

    public void setHostService(String hostService) {
        this.hostService = hostService;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getMinFrameworkVersion() {
        return minFrameworkVersion;
    }

    public void setMinFrameworkVersion(String minFrameworkVersion) {
        this.minFrameworkVersion = minFrameworkVersion;
    }

    public List<String> getTerminals() {
        return terminals;
    }

    public void setTerminals(List<String> terminals) {
        this.terminals = terminals;
    }

    public List<PluginDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<PluginDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public String getEntryPath() {
        return entryPath;
    }

    public void setEntryPath(String entryPath) {
        this.entryPath = entryPath;
    }

    public String getConfigSchema() {
        return configSchema;
    }

    public void setConfigSchema(String configSchema) {
        this.configSchema = configSchema;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public PluginFrontend getFrontend() {
        return frontend;
    }

    public void setFrontend(PluginFrontend frontend) {
        this.frontend = frontend;
    }

    public Map<HouHook.HookEvent, String> getHooks() {
        return hooks;
    }

    public void setHooks(Map<HouHook.HookEvent, String> hooks) {
        this.hooks = hooks;
    }

    /** 是否声明了指定权限 */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
}
