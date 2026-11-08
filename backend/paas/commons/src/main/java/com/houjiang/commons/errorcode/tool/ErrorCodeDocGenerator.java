package com.houjiang.commons.errorcode.tool;

import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorCodeDictionaryItem;
import com.houjiang.commons.errorcode.ErrorCodes;
import com.houjiang.commons.errorcode.ErrorDomain;
import com.houjiang.commons.errorcode.LegacyCodeIndex;
import com.houjiang.commons.errorcode.dictionary.ErrorCodeDictionaryService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 错误码手册 / CSV / 前端映射表离线生成器。
 *
 * <p>解决的问题（PRD X6 / 架构 D7）：手册、CSV、前端 {@code errorCodeMap} 如果手写，
 * 必然与 Java 枚举漂移成第二份真源。本生成器是这些产物的<b>唯一写入方</b>，
 * 数据源只有一个：{@link ErrorCodes#all()}。
 *
 * <p><b>运行方式</b>（需要先 <code>cd backend/paas &amp;&amp; mvn -q -pl commons -am install</code>）：
 * <pre>
 *   java -Dfile.encoding=UTF-8 \
 *        -cp "commons/target/classes" \
 *        com.houjiang.commons.errorcode.tool.ErrorCodeDocGenerator [仓库根目录]
 * </pre>
 * 仓库根目录可省略，省略时从 <code>user.dir</code> 向上查找同时含 {@code frontend} 与
 * {@code backend} 子目录的目录（最多向上 8 层）。
 *
 * <p><b>定位：这是离线文档生成工具，不属于运行时 API。业务代码严禁引用本类</b>，
 * 只允许在「改完错误码 → 刷新码表」这条链路上由人手工执行，或挂到 CI 的文档校验步骤里。
 * 之所以放在 commons 的 main 源码而不是单独的 tools 目录：这样它随 commons 一起编译，
 * 一旦某次改码导致 {@link ErrorCodes#verify()} 不过，生成阶段就会直接失败，
 * 不会出现「代码合规但码表已被污染」的半成品状态。
 *
 * <p><b>产物</b>（新增/修改错误码后必须同 PR 内重跑本生成器刷新）：
 * <ol>
 *   <li>{@code public/docs/错误码体系/码表.md} —— 全量码表 + 域分配 + AMBIGUOUS 附录</li>
 *   <li>{@code public/docs/错误码体系/error-codes.csv} —— 全量 CSV</li>
 *   <li>8 个前端目录的 {@code src/utils/errorCode/errorCodeMap.generated.js}：
 *       admin / cmsadmin / cms / dcenter / tcenter / supplier / scmadmin，
 *       以及 pc（路径为 {@code frontend/pc/app/utils/errorCode/}，比其他端多一层 {@code app/}）</li>
 * </ol>
 * 8 份前端文件内容字节一致，均带 {@code GENERATED FILE - DO NOT EDIT} 头，禁止手改。
 *
 * <p><b>重复 key 防线</b>：{@code LEGACY_TO_NEW} 只收录 EXACT 旧码。歧义旧码（被多个新码认领）
 * 若同时写进该对象，JS 会以后写的 key 覆盖先写的，静默翻转语义 —— 该 bug 曾让 admin 端
 * 11006 被判成骑手语义而不再跳登录。歧义码改由 {@code AMBIGUOUS_LEGACY_CODES} /
 * {@code AMBIGUOUS_CANDIDATES} 导出，前端按自身模块显式选用。
 *
 * <p>本生成器只用 JDK（{@code java.nio.file} + 字符串拼接），不依赖 fastjson2，
 * 避免在 CI 上因 JSON 库版本差异踩坑。
 *
 * @author Amos
 * @since v1.2.0
 */
public final class ErrorCodeDocGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 文档输出相对路径。 */
    private static final String DOC_DIR = "public/docs/错误码体系";

    /**
     * 前端映射表输出相对路径（全部 8 端字节一致）。
     *
     * <p>注意 {@code pc} 端多一层 {@code app/}（{@code frontend/pc/app/...}），
     * 与其它端的 {@code frontend/<端>/src/...} 不同，新增端时不要照抄。
     */
    private static final String[] FRONTEND_DIRS = {
            "frontend/admin/src/utils/errorCode",
            "frontend/cmsadmin/src/utils/errorCode",
            "frontend/cms/src/utils/errorCode",
            "frontend/dcenter/src/utils/errorCode",
            "frontend/tcenter/src/utils/errorCode",
            "frontend/supplier/src/utils/errorCode",
            "frontend/scmadmin/src/utils/errorCode",
            "frontend/pc/app/utils/errorCode"
    };

    private ErrorCodeDocGenerator() {
        // 工具类禁止实例化
    }

    /**
     * 入口。
     *
     * @param args args[0] 可传入仓库根目录，省略时自动探测
     * @throws IOException 写文件失败时抛出
     */
    public static void main(String[] args) throws IOException {
        ErrorCodes.verify();
        Path repoRoot = args.length > 0 ? Paths.get(args[0]).toAbsolutePath().normalize() : detectRepoRoot();
        System.out.println("[ErrorCodeDocGenerator] 仓库根目录 = " + repoRoot);

        List<ErrorCodeDef> all = new ArrayList<>(ErrorCodes.all());
        List<ErrorCodeDictionaryItem> items = new ArrayList<>();
        for (ErrorCodeDef def : all) {
            items.add(ErrorCodeDictionaryItem.from(def));
        }

        Path docDir = repoRoot.resolve(DOC_DIR);
        Files.createDirectories(docDir);

        Path md = docDir.resolve("码表.md");
        Files.writeString(md, renderMarkdown(items), StandardCharsets.UTF_8);
        System.out.println("[ErrorCodeDocGenerator] 已写出 " + md);

        Path csv = docDir.resolve("error-codes.csv");
        Files.writeString(csv, ErrorCodeDictionaryService.toCsv(items), StandardCharsets.UTF_8);
        System.out.println("[ErrorCodeDocGenerator] 已写出 " + csv);

        String js = renderFrontendMap(all);
        for (String dir : FRONTEND_DIRS) {
            Path targetDir = repoRoot.resolve(dir);
            Files.createDirectories(targetDir);
            Path file = targetDir.resolve("errorCodeMap.generated.js");
            Files.writeString(file, js, StandardCharsets.UTF_8);
            System.out.println("[ErrorCodeDocGenerator] 已写出 " + file);
        }

        System.out.println("[ErrorCodeDocGenerator] 完成：错误码 " + all.size() + " 个，"
                + "旧码映射 " + LegacyCodeIndex.mappedLegacyCount() + " 条");
    }

    /**
     * 向上探测仓库根目录（同时含 frontend 与 backend 子目录）。
     *
     * @return 仓库根目录
     * @throws IOException 找不到时抛出
     */
    private static Path detectRepoRoot() throws IOException {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (int i = 0; i < 8 && current != null; i++) {
            if (Files.isDirectory(current.resolve("frontend")) && Files.isDirectory(current.resolve("backend"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IOException("未能探测到仓库根目录（需同时含 frontend 与 backend 子目录），"
                + "请显式传入仓库根目录作为第 1 个参数");
    }

    /**
     * 渲染码表 Markdown。
     *
     * @param items 全量字典项
     * @return Markdown 文本
     */
    private static String renderMarkdown(List<ErrorCodeDictionaryItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("# HouCloud 统一错误码表\n\n");
        sb.append("> 本文件由 `ErrorCodeDocGenerator` 从 Java 域枚举**自动生成**，禁止手改。\n");
        sb.append("> 新增/修改错误码后，必须在同一个 PR 内重跑生成器刷新本文件与两端 `errorCodeMap.generated.js`。\n\n");
        sb.append("| 项 | 值 |\n| --- | --- |\n");
        sb.append("| 字典版本 | ").append(ErrorCodeDictionaryService.VERSION).append(" |\n");
        sb.append("| 生成时间 | ").append(LocalDateTime.now().format(FORMATTER)).append(" |\n");
        sb.append("| 错误码总数 | ").append(items.size()).append(" |\n");
        sb.append("| 旧码映射条数 | ").append(LegacyCodeIndex.mappedLegacyCount()).append(" |\n");
        sb.append("| 歧义旧码数 | ").append(LegacyCodeIndex.ambiguousAll().size()).append(" |\n\n");

        sb.append("## 1. 编号规则\n\n");
        sb.append("- 公式：`code = domainId × 1000 + seq`，`domainId ∈ [20,99]`、`seq ∈ [1,999]`。\n");
        sb.append("- 当前码值域 **20001 – 99999**（5 位，书写为 `DDSSS`），与存量旧码最大值 18040 之间留 1960 隔离带。\n");
        sb.append("- 域内 seq：`001-009` 跨域统一语义槽；`010-099` 域内通用；`100-799` 子模块段；`800-899` 扩展预留；`900-999` 保留禁用。\n");
        sb.append("- 码值一经分配不可回收，废弃只置 `status=DEPRECATED`。\n\n");

        sb.append("## 2. 域分配表\n\n");
        sb.append("| 域号 | 域常量 | 中文名 | 号段 | 归属模块 | 已登记码数 |\n");
        sb.append("| --- | --- | --- | --- | --- | --- |\n");
        for (ErrorDomain domain : ErrorDomain.values()) {
            if (domain.reserved()) {
                continue;
            }
            sb.append('|').append(domain.domainId())
                    .append(" | `").append(domain.code()).append("` | ")
                    .append(domain.cnName()).append(" | ")
                    .append(domain.baseCode()).append('-').append(domain.maxCode()).append(" | ")
                    .append(domain.ownerModule()).append(" | ")
                    .append(ErrorCodes.byDomain(domain).size()).append(" |\n");
        }
        sb.append('\n');

        sb.append("## 3. 全量码表\n\n");
        sb.append("| 码值 | 名称 | 域 | 级别 | 类别 | UI 动作 | 可重试 | 出屏 | 用户文案 | 旧码 |\n");
        sb.append("| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |\n");
        String lastDomain = "";
        for (ErrorCodeDictionaryItem item : items) {
            if (!lastDomain.equals(item.getDomain())) {
                lastDomain = item.getDomain();
                sb.append("| **— ").append(lastDomain).append(" —** | | | | | | | | | |\n");
            }
            sb.append('|').append(item.getCode())
                    .append(" | `").append(item.getName()).append("` | ")
                    .append(item.getDomain()).append(" | ")
                    .append(item.getLevel()).append(" | ")
                    .append(item.getCategory()).append(" | ")
                    .append(item.getUiAction()).append(" | ")
                    .append(item.isRetryable() ? "是" : "否").append(" | ")
                    .append(item.isExposeToUser() ? "是" : "否").append(" | ")
                    .append(item.getUserMessage()).append(" | ")
                    .append(item.getLegacyCodes().length == 0 ? "—" : join(item.getLegacyCodes()))
                    .append(" |\n");
        }
        sb.append('\n');

        sb.append("## 4. 排查指引（FATAL / 需人工介入）\n\n");
        sb.append("| 码值 | 名称 | 开发态说明 | 排查步骤 |\n| --- | --- | --- | --- |\n");
        for (ErrorCodeDictionaryItem item : items) {
            if (!"FATAL".equals(item.getLevel())) {
                continue;
            }
            sb.append('|').append(item.getCode())
                    .append(" | `").append(item.getName()).append("` | ")
                    .append(escapeCell(item.getDevMessage())).append(" | ")
                    .append(escapeCell(item.getTroubleshooting())).append(" |\n");
        }
        sb.append('\n');

        sb.append("## 5. 旧码 → 新码映射\n\n");
        sb.append("| 旧码 | 新码 | 新码名称 | 判定 |\n| --- | --- | --- | --- |\n");
        List<ErrorCodeDictionaryItem> mapped = new ArrayList<>();
        for (ErrorCodeDictionaryItem item : items) {
            if (item.getLegacyCodes().length > 0) {
                mapped.add(item);
            }
        }
        mapped.sort((a, b) -> Integer.compare(a.getLegacyCodes()[0], b.getLegacyCodes()[0]));
        for (ErrorCodeDictionaryItem item : mapped) {
            for (int legacy : item.getLegacyCodes()) {
                sb.append('|').append(legacy).append(" | ").append(item.getCode())
                        .append(" | `").append(item.getName()).append("` | ")
                        .append(LegacyCodeIndex.isAmbiguous(legacy) ? "AMBIGUOUS" : "EXACT").append(" |\n");
            }
        }
        sb.append('\n');

        sb.append("## 附录 A：成功语义旧码（不做映射）\n\n");
        sb.append("以下旧码在历史枚举里表示「成功」，**禁止**被新码认领为 `legacyCodes`（自检 V10 拦截）：\n\n");
        sb.append("| 旧码 | 说明 |\n| --- | --- |\n");
        sb.append("| 0 | `CommonStatusEnum.FAIL`，cmsadmin 历史 bug 把它当成功放行 |\n");
        sb.append("| 1 | `CommonStatusEnum.SUCCESS`，唯一规范成功码 |\n");
        sb.append("| 200 | `frontend/admin` 兼容成功码 |\n");
        sb.append("| 10004 | `SENDING_SUCCEEDED`（发送成功） |\n");
        sb.append("| 10006 | `SMS_SUCCESS` |\n");
        sb.append("| 11000 | `LOGIN_SUCCEEDED`（登录成功） |\n");
        sb.append("| 11001 | `EXIT_SUCCESSFULLY`（退出成功） |\n\n");

        sb.append("## 附录 B：AMBIGUOUS / 跨枚举冲突旧码\n\n");
        sb.append("> **v1.3.0 第二阶段重构后**：`BizCodeEnume` / `RiderCodeEnume` / `GlobalExceptionEnum` "
                + "已改为**统一码的引用入口**，全部存活项按语义各自绑定到对应的新码并登记了 `legacyCodes`，"
                + "死项（无人引用）已删除。因此历史枚举里「同一个码值被多个语义复用」的情况，"
                + "在新体系里表现为**同一个旧码被多个新码认领 = AMBIGUOUS**，这是刻意的：\n");
        sb.append("> - 不同语义落到不同域/不同新码，不会被合并成一个码再把语义弄丢；\n");
        sb.append("> - 反向索引对这些旧码**不产出 1:1 映射**，前端映射表 `LEGACY_TO_NEW` 里也不会出现它们"
                + "（JS 对象字面量重复 key 会静默翻转语义，见 §7.2）。\n\n");
        sb.append("以下旧码被多个语义共用或跨枚举冲突。处置分两类：\n\n");
        sb.append("1. **未认领**（如 15010）：前端按 `category` 兜底展示，业务方逐接口确认后再补 `legacyCodes`。\n");
        sb.append("2. **已按模块拆分认领**（如 11002 / 11006）：两个新码都登记了 `legacyCodes`，"
                + "判定为 `AMBIGUOUS`，反向索引**刻意不产出 1:1 映射** —— "
                + "若自动映射成其中一侧，另一侧的历史 BUG 就会原样复现（例：11006 若映射成「未登录」，"
                + "骑手提现填错账户仍会被踢出登录）。这类旧码由各端按自身模块直接使用对应新码。\n\n");
        sb.append("| 旧码 | 冲突语义 | 处置 |\n| --- | --- | --- |\n");
        sb.append("| 15010 | SPU 为空 / SKU 为空 / 库存 ID 为空 / 商品图片为空 / 规格值为空 / 商品已发布（×6-8） | "
                + "已按语义拆分到 GOODS 域 40010 / 40012 / 40013，旧码本身不认领 |\n");
        sb.append("| 15006 | 购物车语义 ×5 | 拆分到 CART 域，旧码本身不认领 |\n");
        sb.append("| 10099 | 重复 ×5 | 待业务方逐接口认领 |\n");
        sb.append("| 17034 | 重复 ×4 | 待业务方逐接口认领 |\n");
        sb.append("| 18006 | 重复 ×4 | 待业务方逐接口认领 |\n");
        sb.append("| 11046 | 重复 ×3 | 待业务方逐接口认领 |\n");
        sb.append("| 17058 | 重复 ×3 | 待业务方逐接口认领 |\n");
        sb.append("| 11006 | BizCodeEnume=用户未登录 / RiderCodeEnume=只能提现到本人账户 | "
                + "<b>已按模块拆分认领</b>：主站语义 → AUTH 域 30802，骑手语义 → RIDER 域 35010；"
                + "两者均登记 legacyCodes，判定 AMBIGUOUS，<b>不做 1:1 自动映射</b> |\n");
        sb.append("| 11007 | BizCodeEnume=权限不足 / RiderCodeEnume=短信验证码错误 | "
                + "已按 BizCodeEnume 语义认领到 PERMISSION 域 31005；骑手侧语义用 AUTH 域 30006 |\n");
        sb.append("| 11003 | BizCodeEnume=账号或密码错误 / RiderCodeEnume=已审核通过 / handler 用作 syntax error | "
                + "旧码已被 SYSTEM 域 20602（SQL 语法错误）认领，认证语义请用 AUTH 域 30005 |\n");
        sb.append("| 11002 | BizCodeEnume=用户不存在 / RiderCodeEnume=正在审核 | "
                + "<b>已按模块拆分认领</b>：主站语义 → USER 域 32001，骑手语义 → USER 域 32801；"
                + "两者均登记 legacyCodes，判定 AMBIGUOUS，<b>不做 1:1 自动映射</b> |\n");
        sb.append("| 10086 | GlobalExceptionEnum=登录失败请重新登录 / BizCodeEnume=快递公司名称和首字母需小于30个字符 / "
                + "paas-userms 当作「没有权限」 | "
                + "登录语义认领到 AUTH 域 30801；另两种用法<b>已在本轮修正</b>：快递名称 → VALIDATION 域 21002"
                + "（含 BizCodeEnume 常量码值一并改 21002）、userms「没有权限」 → PERMISSION 域 31005 |\n");
        sb.append("| 15030 | BizCodeEnume=超出最大配送距离（前端 pc / uniapp 误当作「无货」） | "
                + "真身是配送距离，认领到 GOODS 域 40802；无货语义单独开 GOODS 域 40801，"
                + "前端须把无货判断从 15030 改到 40801 |\n");
        sb.append("| 10016 | BizCodeEnume.TOKEN_IS_INVALID「token失效,请重新登陆」/ activity 插件当作「未选择优惠券类型 / 未选择商品」/ "
                + "与 10012 并列进未授权数组 | "
                + "认证语义是<b>真身</b>（imchat 有 {@code code === 10016 → 跳登录} 分支），认领到 AUTH 域 30803；"
                + "activity 插件的参数校验误用<b>已在本轮修正</b>为 VALIDATION 域 21002。"
                + "<b>不因 10016 与 10012 在同一数组就跟着判 AUTH</b>：10012 是「系统异常，请联系客服」，与认证无关 |\n");
        sb.append("| 10012 | BizCodeEnume.SYSTEM_ERROR「系统异常，请联系客服」 | "
                + "与 10016 / 10086 并列进 rider / sellerapp 的未授权数组，但语义是系统异常，"
                + "<b>不认领到 AUTH</b>。<b>前端已移除</b>：rider `utils/authCode.js` 的 `AUTH_RELAUNCH_LOGIN_CODES`、"
                + "sellerapp `utils/request.js` 的 `AUTH_UNAUTHORIZED_CODES` 均已去掉 10012——"
                + "留着它一次普通系统异常就会把用户踢到登录页，移除后系统异常正常提示、不再误跳登录 |\n");
        sb.append('\n');
        return sb.toString();
    }

    /**
     * 渲染前端映射表（两端字节一致）。
     *
     * @param all 全部错误码
     * @return JS 模块文本
     */
    private static String renderFrontendMap(List<ErrorCodeDef> all) {
        StringBuilder sb = new StringBuilder();
        sb.append("// GENERATED BY ErrorCodeDocGenerator - DO NOT EDIT\n");
        sb.append("// 源：backend/paas/errorcode 的 Java 域枚举；修改请改枚举后重跑生成器\n");
        sb.append("// 生成时间：").append(LocalDateTime.now().format(FORMATTER)).append('\n');
        sb.append("// 8 端（admin / cmsadmin / cms / dcenter / tcenter / supplier / scmadmin / pc）本文件内容字节一致\n\n");
        sb.append("export const ERROR_CODE_VERSION = '").append(ErrorCodeDictionaryService.VERSION).append("'\n\n");
        sb.append("export const ERROR_CODE_MAP = {\n");
        for (ErrorCodeDef def : all) {
            sb.append("  ").append(def.code()).append(": { name: '").append(def.name())
                    .append("', userMessage: '").append(escapeJs(def.meta().userMessage()))
                    .append("', level: '").append(def.meta().level().name())
                    .append("', retryable: ").append(def.meta().retryable())
                    .append(", domain: '").append(def.domain().code())
                    .append("', uiAction: '").append(def.meta().uiAction().name())
                    .append("', category: '").append(def.meta().category().name())
                    .append("', exposeToUser: ").append(def.meta().exposeToUser())
                    .append(", legacyCodes: [").append(join(def.meta().legacyCodes()))
                    .append("] },\n");
        }
        sb.append("}\n\n");
        // 旧码 → 新码：只收录 EXACT（唯一认领者）。
        // 歧义旧码（11002 / 11006）绝不能进这张表 —— JS 对象字面量里重复 key 后者覆盖前者，
        // 曾导致 admin 端 11006 被静默判成骑手语义 35010（「只能提现到本人账户」，不跳登录），
        // 而 admin 端 11006 的真身是「用户未登录」，必须跳登录。歧义码一律走下面的 AMBIGUOUS_* 常量。
        sb.append("// 旧码 → 新码，只含 EXACT（唯一认领者）；歧义旧码见下方 AMBIGUOUS_*，"
                + "保证 JS 对象字面量不出现重复 key\n");
        sb.append("export const LEGACY_TO_NEW = {\n");
        Map<Integer, List<Integer>> exact = new LinkedHashMap<>();
        for (ErrorCodeDef def : all) {
            for (int legacy : def.meta().legacyCodes()) {
                if (LegacyCodeIndex.isAmbiguous(legacy)) {
                    continue;
                }
                exact.computeIfAbsent(legacy, k -> new ArrayList<>()).add(def.code());
            }
        }
        List<Integer> exactKeys = new ArrayList<>(exact.keySet());
        Collections.sort(exactKeys);
        for (int legacy : exactKeys) {
            sb.append("  ").append(legacy).append(": [").append(join(exact.get(legacy))).append("],\n");
        }
        sb.append("}\n\n");

        Map<Integer, List<Integer>> ambiguous = LegacyCodeIndex.ambiguousAll();
        List<Integer> ambiguousKeys = new ArrayList<>(ambiguous.keySet());
        Collections.sort(ambiguousKeys);
        sb.append("// 歧义旧码：被多个新码同时认领，禁止 1:1 自动映射，各端按自身模块取用候选码\n");
        sb.append("export const AMBIGUOUS_LEGACY_CODES = [").append(join(ambiguousKeys)).append("]\n\n");
        sb.append("// 歧义旧码 → 全部候选新码（顺序即枚举声明顺序，非优先级）\n");
        sb.append("export const AMBIGUOUS_CANDIDATES = {\n");
        for (int legacy : ambiguousKeys) {
            sb.append("  ").append(legacy).append(": [").append(join(ambiguous.get(legacy))).append("],\n");
        }
        sb.append("}\n\n");
        sb.append("// 成功码集合：成功语义与错误码严格分离，新码段 20001-99999 内永不出现成功码\n");
        sb.append("export const CANONICAL_SUCCESS_CODE = 1\n");
        sb.append("export const COMPAT_SUCCESS_CODES = [1, 200]\n");
        sb.append("export const FAIL_CODE = 0\n");
        return sb.toString();
    }

    /**
     * @param codes 旧码数组
     * @return 逗号连接字符串
     */
    private static String join(int[] codes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(codes[i]);
        }
        return sb.toString();
    }

    /**
     * @param codes 码值列表
     * @return 逗号连接字符串
     */
    private static String join(List<Integer> codes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codes.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(codes.get(i));
        }
        return sb.toString();
    }

    /**
     * Markdown 表格单元格转义。
     *
     * @param value 原始值
     * @return 转义后的值
     */
    private static String escapeCell(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "\\|").replace("\n", " ");
    }

    /**
     * JS 单引号字符串转义。
     *
     * @param value 原始值
     * @return 转义后的值
     */
    private static String escapeJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
