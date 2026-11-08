package com.houjiang.mall.service.plugin;

import com.alibaba.fastjson.JSON;
import java.util.List;
import com.houjiang.mall.service.plugin.PluginScaffoldService.ScaffoldSpec;

/** 服务所属插件模板；standalone 的业务实现始终位于已有 backend/paas 服务。 */
final class PluginScaffoldTemplates {
    private PluginScaffoldTemplates() {}

    /**
     * 生成 plugin.yaml。
     *
     * <p>字段与插件框架（plugin-sdk 的 PluginManifest + 13 个域插件）严格对齐：
     * <ul>
     *   <li><b>不写 type</b>：schema 里没有这个字段，归属只由 {@code runtime: embedded|standalone} 表达；</li>
     *   <li><b>必须写 minFrameworkVersion</b>：PluginManifestValidator 第 ⑤ 条强制非空，缺了发布审核直接失败；</li>
     *   <li><b>必须写 permissions</b>：菜单注入的根权限是 {@code {code}:root}；</li>
     *   <li>menus 用 M（目录）+ C（菜单，parentPerms 指向 M）两级结构，字段与 PluginMenuSyncPort 读取的一致。</li>
     * </ul>
     */
    static String pluginYaml(ScaffoldSpec spec) {
        String rootPerms = spec.code() + ":root";
        return """
                code: %s
                name: %s
                version: %s
                author: UniMall
                description: %s
                category: %s
                owner: official
                runtime: %s
                hostService: %s
                supportedInstallProfiles: [LIGHTWEIGHT]
                license: %s
                minFrameworkVersion: 1.0.0
                terminals: %s
                dependencies: []
                permissions:
                  - %s
                menus:
                  - type: M
                    name: %s
                    icon: Box
                    sort: 900
                    perms: %s
                    path: %s
                    group: growth
                    groupSort: 100
                  - type: C
                    name: %s
                    icon: Box
                    sort: 1
                    perms: %s
                    path: %s
                    parentPerms: %s
                frontend:
                  entry: /plugin-assets/%s/%s/index.mjs
                tables:
                  prefix: %s
                """.formatted(spec.code(), JSON.toJSONString(spec.name()), spec.version(),
                JSON.toJSONString(spec.description()), JSON.toJSONString(spec.category()),
                spec.runtime(), spec.hostService(), spec.licenseType(), flowList(spec.terminals()),
                rootPerms,
                JSON.toJSONString(spec.menuTitle()), rootPerms, JSON.toJSONString(spec.menuPath()),
                JSON.toJSONString(spec.menuTitle() + "管理"), rootPerms,
                JSON.toJSONString(spec.menuPath() + "/index"), rootPerms,
                spec.code(), spec.version(), spec.tablePrefix());
    }

    /** YAML flow 序列：{@code [admin, seller]}；空列表写 {@code []}。 */
    private static String flowList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        return "[" + String.join(", ", values) + "]";
    }

    /**
     * 生成 pom.xml。源码目录固定 {@code runtime/java}（与全部现有插件一致，不是 Maven 默认的 src/main/java），
     * resources 必须带上 plugin.yaml 与 frontend，并排除 node_modules/dist 等构建中间物。
     */
    static String pomXml(ScaffoldSpec spec) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.houjiang.plugin</groupId>
                  <artifactId>%1$s</artifactId>
                  <version>%2$s</version>
                  <packaging>jar</packaging>
                  <properties><maven.compiler.release>21</maven.compiler.release><project.build.sourceEncoding>UTF-8</project.build.sourceEncoding></properties>
                  <dependencies>
                    <dependency><groupId>com.houjiang</groupId><artifactId>plugin-sdk</artifactId><version>1.0.0</version><scope>provided</scope></dependency>
                    <dependency><groupId>com.houjiang</groupId><artifactId>plugin-api</artifactId><version>1.0.0</version><scope>provided</scope></dependency>
                    <dependency><groupId>org.pf4j</groupId><artifactId>pf4j</artifactId><version>3.12.0</version><scope>provided</scope></dependency>
                  </dependencies>
                  <build>
                    <finalName>%1$s-plugin-${project.version}</finalName>
                    <sourceDirectory>${project.basedir}/runtime/java</sourceDirectory>
                    <resources>
                      <resource><directory>${project.basedir}/runtime/resources</directory></resource>
                      <resource><directory>${project.basedir}</directory><includes><include>plugin.yaml</include></includes></resource>
                      <resource>
                        <directory>${project.basedir}/frontend</directory>
                        <targetPath>frontend</targetPath>
                        <includes><include>**/*</include></includes>
                        <!-- 依赖与构建中间物绝不入包：node_modules 体积是产物的一百倍 -->
                        <excludes>
                          <exclude>node_modules/**</exclude>
                          <exclude>dist/**</exclude>
                          <exclude>package.json</exclude>
                          <exclude>vite.config.mjs</exclude>
                        </excludes>
                      </resource>
                    </resources>
                    <plugins>
                      <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId><version>3.11.0</version>
                        <configuration><release>21</release><encoding>UTF-8</encoding></configuration>
                      </plugin>
                      <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-jar-plugin</artifactId><version>3.3.0</version>
                        <configuration><archive><manifestEntries>
                          <Plugin-Id>%1$s</Plugin-Id><Plugin-Version>%2$s</Plugin-Version>
                          <Plugin-Class>%3$s.%4$sPlugin</Plugin-Class>
                          <Plugin-Provider>UniMall</Plugin-Provider>
                        </manifestEntries></archive></configuration>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """.formatted(spec.code(), spec.version(), spec.packageName(), spec.pascalCode());
    }

    static String pluginJava(ScaffoldSpec spec) {
        return """
                package %s;

                import org.pf4j.Plugin;
                import org.pf4j.PluginWrapper;
                import com.houjiang.plugin.sdk.annotation.HouPlugin;

                @HouPlugin(code = %s, name = %s, version = %s, minFrameworkVersion = "1.0.0")
                public class %sPlugin extends Plugin {
                    public %sPlugin(PluginWrapper wrapper) { super(wrapper); }
                }
                """.formatted(spec.packageName(), JSON.toJSONString(spec.code()),
                JSON.toJSONString(spec.name()), JSON.toJSONString(spec.version()),
                spec.pascalCode(), spec.pascalCode());
    }

    static String frontendIndexJs(ScaffoldSpec spec) {
        return """
                // API 请求通过宿主注入的 ctx.callApi 发出；业务由 plugin.yaml 的 hostService 负责。
                export default {
                  menus: [{ path: %1$s, title: %2$s }],
                  routes: [{ path: %1$s, component: () => import('./pages/Home.vue') }],
                  setup(ctx) {},
                  teardown() {}
                };
                """.formatted(JSON.toJSONString(spec.menuPath()), JSON.toJSONString(spec.menuTitle()));
    }

    static String frontendHomeVue(String title) {
        String safe = title.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("{", "&#123;").replace("}", "&#125;");
        return """
                <template>
                  <div class="plugin-home">
                    <h2>%s</h2>
                    <p>请在此实现插件的业务页面。</p>
                  </div>
                </template>
                <style scoped>.plugin-home { padding: 24px; }</style>
                """.formatted(safe);
    }

    static String readme(ScaffoldSpec spec) {
        String implementation = "standalone".equals(spec.runtime())
                ? "业务实现归属已有服务 `backend/paas/" + spec.hostService() + "`。此目录仅保存产品描述与前端资源。"
                : "扩展实现位于此目录的 `runtime/java`，由 `backend/paas/" + spec.hostService() + "` 服务装配。运行 `mvn package` 构建扩展 JAR。";
        return "# " + spec.name() + "\n\n归属服务：`" + spec.hostService() + "`\n\n"
                + implementation + "\n\n菜单路径：`" + spec.menuPath() + "`\n\n"
                + "维护 plugin.yaml 的 permissions 与 menus；前端通过宿主 ctx.callApi 请求所属服务。\n"
                + "构建 JAR 后由所属服务加载验证，再提交市场审核；无需创建额外服务、端口或独立部署。\n";
    }

    static List<String> reservedCodes() {
        return List.of("erp", "webpos", "oa", "mes", "finance", "activity", "cms", "crm",
                "im", "logging", "geo", "orderfood", "pm", "demo-coupon", "supplier",
                "mall-advertising", "mall", "auth", "userms", "payment", "order", "job", "bff", "admin");
    }
}
