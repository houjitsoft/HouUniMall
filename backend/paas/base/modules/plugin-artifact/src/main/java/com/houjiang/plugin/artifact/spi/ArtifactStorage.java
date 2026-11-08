package com.houjiang.plugin.artifact.spi;

import com.houjiang.plugin.artifact.model.ArtifactStream;
import com.houjiang.plugin.artifact.model.StoredObject;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

/**
 * 插件制品存储 SPI。
 *
 * <p>存在的意义：把「制品放哪儿」与「发布/分发业务逻辑」解耦。
 * P0 用本地磁盘（{@code LocalDiskArtifactStorage}），生产切 MinIO / COS
 * （{@code MinioArtifactStorage} / {@code CosArtifactStorage}）时只需切换
 * {@code houcloud.artifact.type=minio|cos}，业务代码零改动。
 *
 * <p>三条硬约束（实作者必须遵守）：
 * <ol>
 *   <li><b>绝不覆盖</b>：{@link #putIfAbsent} 在目标 key 已存在时必须抛冲突异常。
 *       已 published 的制品一旦被覆盖，会出现「同一版本号两个 jar」的灾难性不一致，
 *       且客户端已下载的 md5 与线上签名永久对不上。</li>
 *   <li><b>先写临时文件再原子落位</b>：避免上传中断在目标路径留下半截文件，
 *       被后续下载当成合法制品。</li>
 *   <li><b>支持 Range</b>：断点续传是 64MB 制品在弱网下的刚需，{@link #open} 必须支持区间读取。</li>
 * </ol>
 *
 * @author Amos
 * @since M0
 */
public interface ArtifactStorage {

    /**
     * 写入制品；<b>目标 key 已存在时抛冲突异常，绝不覆盖</b>。
     *
     * @param key          存储键，契约见 {@code ArtifactKeys}：{@code {code}/{version}/{code}-{version}.jar}
     * @param in           制品字节流（方法内部不负责关闭由调用方传入的流之外的资源）
     * @param declaredSize 声明的字节数，用于写入完成后做一次完整性校验；&lt;=0 表示不校验
     * @return 落位后的制品元信息（含 md5 / sha256 / size）
     * @throws com.houjiang.plugin.artifact.exception.ArtifactConflictException 目标 key 已存在
     * @throws com.houjiang.plugin.artifact.exception.ArtifactIntegrityException 实际写入字节数与 declaredSize 不一致
     * @throws IOException                                                       读写失败
     */
    StoredObject putIfAbsent(String key, InputStream in, long declaredSize) throws IOException;

    /**
     * 打开制品流，支持 HTTP Range 区间读取。
     *
     * @param key        存储键
     * @param rangeStart 区间起点（含），为 null 表示从头
     * @param rangeEnd   区间终点（含），为 null 表示到文件末尾
     * @return 制品流；调用方必须关闭以释放底层资源
     * @throws com.houjiang.plugin.artifact.exception.ArtifactNotFoundException 制品不存在
     * @throws IOException                                                      打开失败
     */
    ArtifactStream open(String key, Long rangeStart, Long rangeEnd) throws IOException;

    /**
     * 删除某个插件版本的制品目录（整目录，含同版本下的附属文件）。
     *
     * @param pluginCode 插件编码
     * @param version    插件版本
     */
    void deleteVersion(String pluginCode, String version);

    /**
     * 复制制品（用于「重新发布同一版本」时先复制一份再原子替换，或跨存储搬迁）。
     *
     * @param fromKey 源存储键
     * @param toKey   目标存储键，已存在时抛冲突异常
     */
    void copyVersion(String fromKey, String toKey);

    /**
     * 签发内部直链。
     *
     * <p><b>仅云存储实现（COS/OSS）有意义</b>。本地磁盘实现没有独立的下载端点，
     * 必须由本进程回源流式输出，因此本地实现固定抛 {@link UnsupportedOperationException}，
     * 上层需降级为「经 /base/plugin-center/artifact/raw 回源」。
     *
     * @param key 存储键
     * @param ttl 链接有效期
     * @return 带签名的绝对 URL
     */
    String signedInternalUrl(String key, Duration ttl);
}
