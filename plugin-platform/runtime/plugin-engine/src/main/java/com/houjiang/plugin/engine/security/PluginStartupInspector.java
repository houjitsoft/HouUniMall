package com.houjiang.plugin.engine.security;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 启动期合规静态扫描（方案 10.3）。
 *
 * <p>对不可信第三方插件，在加载前扫描字节码，命中规则即在装载前拒绝，避免「先装载再报错」
 * 造成的半生效状态。命中结果写入安装/升级任务的 {@code error_code}，供前端展示。
 *
 * <p><b>实现方式</b>：直接解析 class 文件常量池提取方法引用，不引入 ASM 依赖——
 * 与 {@code PluginRouteScanner} 的字节码扫描路线保持一致。代价是只做「引用级」检测：
 * 能看到插件引用了 {@code Runtime.exec}，看不到反射拼出来的类名。
 * 这个强度对「防误用 + 提高恶意成本」是够的，真正的强隔离要靠 standalone 进程形态。
 *
 * <p><b>规则与级别</b>：
 * <ul>
 *   <li>{@link Severity#BLOCK} —— 拒绝装载：停 JVM、拉起子进程、突破模块封装</li>
 *   <li>{@link Severity#WARN} —— 只告警不阻断：动态类加载、关闭钩子等存在正当用途</li>
 * </ul>
 *
 * @author HouCloud
 */
public class PluginStartupInspector {

    /** 拦截级别。 */
    public enum Severity {
        /** 阻断装载。 */
        BLOCK,
        /** 仅告警，不阻断。 */
        WARN
    }

    /** 阻断级规则：常量池 {@code owner#method}。 */
    private static final Set<String> BLOCK_RULES = Set.of(
            "java/lang/System#exit",
            "java/lang/Runtime#exec",
            "java/lang/Runtime#getRuntime",
            "java/lang/ProcessBuilder#start",
            "java/lang/ProcessBuilder#<init>",
            "java/lang/Thread#stop",
            "java/lang/Thread#suspend",
            "java/lang/Thread#resume",
            "sun/misc/Unsafe#*",
            "jdk/internal/misc/Unsafe#*"
    );

    /** 告警级规则。 */
    private static final Set<String> WARN_RULES = Set.of(
            "java/lang/Class#forName",
            "java/lang/ClassLoader#loadClass",
            "java/lang/ClassLoader#defineClass",
            "java/lang/reflect/AccessibleObject#setAccessible",
            "java/lang/Runtime#addShutdownHook",
            "java/lang/Runtime#removeShutdownHook",
            "java/lang/System#setProperty",
            "java/lang/System#setSecurityManager"
    );

    /** 单个 jar 最多扫描的类数，防止超大包把装载流程拖死。 */
    private static final int MAX_CLASSES_PER_JAR = 20_000;

    private static final int CONSTANT_UTF8 = 1;
    private static final int CONSTANT_LONG = 5;
    private static final int CONSTANT_DOUBLE = 6;
    private static final int CONSTANT_CLASS = 7;
    private static final int CONSTANT_STRING = 8;
    private static final int CONSTANT_FIELDREF = 9;
    private static final int CONSTANT_METHODREF = 10;
    private static final int CONSTANT_INTERFACE_METHODREF = 11;
    private static final int CONSTANT_NAME_AND_TYPE = 12;
    private static final int CONSTANT_METHOD_HANDLE = 15;
    private static final int CONSTANT_METHOD_TYPE = 16;
    private static final int CONSTANT_DYNAMIC = 17;
    private static final int CONSTANT_INVOKE_DYNAMIC = 18;
    private static final int CONSTANT_MODULE = 19;
    private static final int CONSTANT_PACKAGE = 20;

    /**
     * 扫描插件主类（兼容历史签名）。
     *
     * @return 违规列表；无违规返回空列表
     */
    public List<Violation> scan(Class<?> pluginClass) {
        if (pluginClass == null) {
            return Collections.emptyList();
        }
        String resource = pluginClass.getName().replace('.', '/') + ".class";
        ClassLoader loader = pluginClass.getClassLoader();
        try (InputStream in = loader == null
                ? ClassLoader.getSystemResourceAsStream(resource)
                : loader.getResourceAsStream(resource)) {
            if (in == null) {
                List<Violation> result = new ArrayList<>();
                result.add(new Violation("SCAN_FAILED", "无法定位类字节码：" + pluginClass.getName(), Severity.WARN));
                return result;
            }
            return scanClassBytes(readAll(in), pluginClass.getName());
        } catch (IOException e) {
            List<Violation> result = new ArrayList<>();
            result.add(new Violation("SCAN_FAILED", "无法读取类字节码：" + pluginClass.getName(), Severity.WARN));
            return result;
        }
    }

    /**
     * 扫描整个插件 jar 内的所有 class。
     *
     * @param jar 插件安装包；不存在或不是普通文件时返回单条 {@code SCAN_FAILED} 告警
     */
    public List<Violation> scan(Path jar) {
        if (jar == null) {
            throw new IllegalArgumentException("安装包路径不能为空");
        }
        List<Violation> violations = new ArrayList<>();
        if (!Files.isRegularFile(jar)) {
            violations.add(new Violation("SCAN_FAILED", "安装包不存在：" + jar, Severity.WARN));
            return violations;
        }
        int scanned = 0;
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue;
                }
                if (scanned++ >= MAX_CLASSES_PER_JAR) {
                    violations.add(new Violation("SCAN_TRUNCATED",
                            "类数量超过上限 " + MAX_CLASSES_PER_JAR + "，仅扫描前 " + MAX_CLASSES_PER_JAR + " 个",
                            Severity.WARN));
                    break;
                }
                try (InputStream in = zip.getInputStream(entry)) {
                    violations.addAll(scanClassBytes(readAll(in), entry.getName()));
                } catch (IOException e) {
                    violations.add(new Violation("SCAN_FAILED", "无法读取：" + entry.getName(), Severity.WARN));
                }
            }
        } catch (IOException e) {
            violations.add(new Violation("SCAN_FAILED", "无法打开安装包：" + jar, Severity.WARN));
        }
        return violations;
    }

    /**
     * 扫描单个 class 的字节码。
     *
     * @param classBytes 类文件内容
     * @param source     来源标识（类名或 jar 内路径），用于违规定位
     */
    public List<Violation> scanClassBytes(byte[] classBytes, String source) {
        if (classBytes == null || classBytes.length == 0) {
            return Collections.emptyList();
        }
        List<String> refs;
        try {
            refs = parseMethodReferences(classBytes);
        } catch (IOException | IllegalArgumentException e) {
            List<Violation> result = new ArrayList<>();
            result.add(new Violation("SCAN_FAILED", "字节码解析失败：" + source, Severity.WARN));
            return result;
        }
        List<Violation> violations = new ArrayList<>();
        Set<String> reported = new HashSet<>();
        for (String ref : refs) {
            int sharp = ref.indexOf('#');
            if (sharp <= 0) {
                continue;
            }
            String owner = ref.substring(0, sharp);
            String method = ref.substring(sharp + 1);
            Severity severity = matchSeverity(owner, method);
            if (severity == null) {
                continue;
            }
            String rule = owner + "#" + method;
            if (!reported.add(rule)) {
                continue;
            }
            violations.add(new Violation(
                    severity == Severity.BLOCK ? "DANGEROUS_API" : "SENSITIVE_API",
                    "命中规则 " + rule + "（来源：" + source + "）",
                    severity));
        }
        return violations;
    }

    /** @return 列表中是否存在 {@link Severity#BLOCK} 级违规 */
    public boolean hasBlockingViolation(List<Violation> violations) {
        if (violations == null) {
            return false;
        }
        for (Violation v : violations) {
            if (v.severity == Severity.BLOCK) {
                return true;
            }
        }
        return false;
    }

    private static Severity matchSeverity(String owner, String method) {
        if (BLOCK_RULES.contains(owner + "#" + method) || BLOCK_RULES.contains(owner + "#*")) {
            return Severity.BLOCK;
        }
        if (WARN_RULES.contains(owner + "#" + method) || WARN_RULES.contains(owner + "#*")) {
            return Severity.WARN;
        }
        return null;
    }

    /**
     * 解析 class 常量池，提取全部方法引用（{@code owner#name}）。
     *
     * <p>只取 Methodref / InterfaceMethodref 的 owner 与方法名，不做控制流分析：
     * 引用即视为可能调用，宁可误报也不漏报（误报可通过 WARN 级别与人工复核消化）。
     */
    private static List<String> parseMethodReferences(byte[] classBytes) throws IOException {
        List<String> refs = new ArrayList<>();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(classBytes));
        try {
            if (in.readInt() != 0xCAFEBABE) {
                throw new IllegalArgumentException("不是合法的 class 文件");
            }
            in.readUnsignedShort(); // minor_version
            in.readUnsignedShort(); // major_version
            int cpCount = in.readUnsignedShort();
            String[] utf8 = new String[cpCount];
            int[] classNameIndex = new int[cpCount];
            int[] nameAndTypeName = new int[cpCount];
            int[] methodClass = new int[cpCount];
            int[] methodNameAndType = new int[cpCount];
            List<Integer> methodRefs = new ArrayList<>();

            for (int i = 1; i < cpCount; i++) {
                int tag = in.readUnsignedByte();
                switch (tag) {
                    case CONSTANT_UTF8:
                        utf8[i] = in.readUTF();
                        break;
                    case CONSTANT_CLASS:
                    case CONSTANT_STRING:
                    case CONSTANT_METHOD_TYPE:
                    case CONSTANT_MODULE:
                    case CONSTANT_PACKAGE:
                        classNameIndex[i] = in.readUnsignedShort();
                        break;
                    case CONSTANT_NAME_AND_TYPE:
                        nameAndTypeName[i] = in.readUnsignedShort();
                        in.readUnsignedShort(); // descriptor，引用级扫描不需要
                        break;
                    case CONSTANT_METHODREF:
                    case CONSTANT_INTERFACE_METHODREF:
                        methodClass[i] = in.readUnsignedShort();
                        methodNameAndType[i] = in.readUnsignedShort();
                        methodRefs.add(i);
                        break;
                    case CONSTANT_FIELDREF:
                    case CONSTANT_DYNAMIC:
                    case CONSTANT_INVOKE_DYNAMIC:
                        in.readInt(); // 两个 u2
                        break;
                    case CONSTANT_METHOD_HANDLE:
                        in.readUnsignedByte();
                        in.readUnsignedShort();
                        break;
                    case CONSTANT_LONG:
                    case CONSTANT_DOUBLE:
                        in.readLong(); // 8 字节常量
                        i++;           // 占两个常量池槽位
                        break;
                    case 3: // Integer
                    case 4: // Float
                        in.readInt();
                        break;
                    default:
                        throw new IllegalArgumentException("未知常量池标签：" + tag);
                }
            }
            for (Integer idx : methodRefs) {
                String owner = utf8At(utf8, classNameIndex[methodClass[idx]]);
                int nt = methodNameAndType[idx];
                String name = utf8At(utf8, nameAndTypeName[nt]);
                if (owner != null && name != null) {
                    refs.add(owner + "#" + name);
                }
            }
        } finally {
            in.close();
        }
        return refs;
    }

    private static String utf8At(String[] utf8, int index) {
        if (index <= 0 || index >= utf8.length) {
            return null;
        }
        return utf8[index];
    }

    private static byte[] readAll(InputStream in) throws IOException {
        return in.readAllBytes();
    }

    /** 扫描违规项。 */
    public static class Violation {
        /** 规则编码：{@code DANGEROUS_API} / {@code SENSITIVE_API} / {@code SCAN_FAILED} / {@code SCAN_TRUNCATED}。 */
        public final String rule;
        /** 可读描述，含来源定位。 */
        public final String detail;
        /** 级别；历史两参构造默认 {@link Severity#BLOCK}。 */
        public final Severity severity;

        public Violation(String rule, String detail) {
            this(rule, detail, Severity.BLOCK);
        }

        public Violation(String rule, String detail, Severity severity) {
            this.rule = rule;
            this.detail = detail;
            this.severity = severity == null ? Severity.BLOCK : severity;
        }

        @Override
        public String toString() {
            return severity + " " + rule + ": " + detail;
        }
    }
}
