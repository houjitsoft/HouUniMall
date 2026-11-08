package com.houjiang.plugin.update.model;

/**
 * 安装结果（{@code /base/plugin-center/update/install} 的 data）。
 *
 * @author Amos
 * @since M3
 */
public final class InstallResult {

    private final String taskId;

    private final String pluginCode;

    private final String version;

    private final String state;

    private final String step;

    private final String jarPath;

    private final long sizeBytes;

    private final String checksum;

    /**
     * 构造安装结果。
     *
     * @param taskId     任务号
     * @param pluginCode 插件编码
     * @param version    已安装版本
     * @param state      任务终态
     * @param step       最后停留的阶段
     * @param jarPath    落盘路径
     * @param sizeBytes  制品字节数
     * @param checksum   制品 md5
     */
    public InstallResult(String taskId,
                         String pluginCode,
                         String version,
                         String state,
                         String step,
                         String jarPath,
                         long sizeBytes,
                         String checksum) {
        this.taskId = taskId;
        this.pluginCode = pluginCode;
        this.version = version;
        this.state = state;
        this.step = step;
        this.jarPath = jarPath;
        this.sizeBytes = sizeBytes;
        this.checksum = checksum;
    }

    /**
     * @return 任务号
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * @return 插件编码
     */
    public String getPluginCode() {
        return pluginCode;
    }

    /**
     * @return 已安装版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return 任务终态
     */
    public String getState() {
        return state;
    }

    /**
     * @return 最后停留的阶段
     */
    public String getStep() {
        return step;
    }

    /**
     * @return 落盘路径
     */
    public String getJarPath() {
        return jarPath;
    }

    /**
     * @return 制品字节数
     */
    public long getSizeBytes() {
        return sizeBytes;
    }

    /**
     * @return 制品 md5
     */
    public String getChecksum() {
        return checksum;
    }
}
