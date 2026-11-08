package com.houjiang.plugin.api.ext;

import java.util.regex.Pattern;

/**
 * 二开持久化表名前缀强校验器（R2.1）。
 *
 * <p>二开模块与宿主共用同一个 {@code DataSource}，因此必须靠<b>命名前缀</b>把二开数据与
 * 宿主/官方表、其它二开表物理隔离。约定：二开只能操作
 * {@value #PREFIX} 前缀下的表；任何不以该前缀开头的表名一律视为越权。
 *
 * <p>本校验器是「{@code hou_ext_} 前缀」这一约定的<b>唯一事实来源</b>：{@code ExtensionDao}、
 * classpath 版迁移执行器 {@code ExtensionSchemaMigrationRunner} 均复用 {@link #validate(String)}，
 * 从而做到「非前缀表名在启动期直接失败」。
 *
 * <p>校验规则（严格、大小写敏感）：
 * <ul>
 *   <li>表名非空，且恰好以 {@value #PREFIX} 开头（前缀本身已含结尾下划线）；</li>
 *   <li>前缀后必须还有至少一个字符（裸 {@value #PREFIX} 无意义，判为非法）；</li>
 *   <li>后缀只允许小写字母、数字、下划线，且首字符为字母或数字（形如 {@code hou_ext_order_item}）。</li>
 * </ul>
 *
 * <p>放在 {@code plugin-api} 而非 {@code plugin-sdk} 的理由：{@code plugin-api} 是二开模块以
 * {@code provided} 依赖直接面向的<b>契约面</b>，校验器随契约发布、无需额外依赖即可被二开在启动期调用；
 * {@code plugin-sdk} 位于 {@code plugin-platform/**}，属运行时引擎侧、且不在本次改动归属内。
 *
 * @author Amos
 */
public final class ExtensionTableNaming {

    /** 二开专属表前缀。二开表名必须以它开头，后缀为业务名（如 {@code hou_ext_order}）。 */
    public static final String PREFIX = "hou_ext_";

    /** 合法二开表名：{@code hou_ext_} + 标识符后缀（小写字母/数字/下划线，首字符为字母或数字）。 */
    private static final Pattern VALID_TABLE = Pattern.compile("^" + PREFIX + "[a-z0-9][a-z0-9_]*$");

    private ExtensionTableNaming() {
        // 工具类，禁止实例化
    }

    /**
     * 校验表名是否满足二开命名约定，不满足则<b>立即失败</b>（fail-fast）。
     *
     * @param tableName 待校验的表名（原始名，不含反引号、不含库名限定）
     * @throws IllegalStateException 表名为空或不以 {@value #PREFIX} 开头
     */
    public static void validate(String tableName) {
        if (!isValid(tableName)) {
            throw new IllegalStateException(
                    "二开表名必须形如 '" + PREFIX + "<业务名>'{hou_ext_ 前缀 + 小写标识符}，实际为 "
                            + describe(tableName) + "；二开不得触碰宿主表或其它二开表。");
        }
    }

    /**
     * 非抛出版本的前缀校验。
     *
     * @param tableName 待校验的表名
     * @return 合法返回 {@code true}，否则 {@code false}
     */
    public static boolean isValid(String tableName) {
        return tableName != null && VALID_TABLE.matcher(tableName).matches();
    }

    /**
     * 按约定拼装并校验一个二开表名。
     *
     * @param suffix 业务后缀（如 {@code order_item}）
     * @return 拼装后的合法表名，如 {@code hou_ext_order_item}
     * @throws IllegalStateException 后缀非法，拼出的表名不符合约定
     */
    public static String of(String suffix) {
        String candidate = suffix == null ? null : PREFIX + suffix;
        validate(candidate);
        return candidate;
    }

    /** 生成可读的错误描述，避免日志里把 {@code null} 与空串混为一谈。 */
    private static String describe(String tableName) {
        if (tableName == null) {
            return "null";
        }
        if (tableName.isBlank()) {
            return "空白(\"" + tableName + "\")";
        }
        return "'" + tableName + "'";
    }
}
