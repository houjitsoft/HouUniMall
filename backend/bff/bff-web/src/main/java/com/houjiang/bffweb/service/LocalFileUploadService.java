package com.houjiang.bffweb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 本机磁盘上传（供商城素材库等使用，不依赖 thirdparty OSS）。
/**
 * @author Amos
 */
@Slf4j
@Service
public class LocalFileUploadService {

    @Value("${local.upload.path:}")
    private String configuredUploadPath;

    /** 写入 public 下目录时，对外 URL 前缀（Nginx 需能访问 /uploads/...） */
    @Value("${mall.upload.url-prefix:/uploads/mall}")
    private String publicUrlPrefix;

    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String newName = System.currentTimeMillis() + "_" + System.nanoTime() + ext;

        File dir = new File(resolveUploadDir());
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("创建上传目录失败: " + dir.getAbsolutePath());
        }
        File dest = new File(dir, newName);
        file.transferTo(dest);

        String prefix = publicUrlPrefix.endsWith("/")
                ? publicUrlPrefix.substring(0, publicUrlPrefix.length() - 1)
                : publicUrlPrefix;
        String url = prefix + "/" + newName;
        log.info("本地上传成功: {} -> {}", original, url);
        return url;
    }

    private String resolveUploadDir() {
        String raw = configuredUploadPath == null ? "" : configuredUploadPath.trim();
        if (!raw.isEmpty()) {
            return isAbsolutePath(raw) ? raw : new File(System.getProperty("user.dir"), raw).getAbsolutePath();
        }
        boolean win = System.getProperty("os.name", "").toLowerCase().contains("win");
        return win
                ? "D:/wwwroot/houcloud/public/uploads/mall"
                : "/www/wwwroot/houcloud/public/uploads/mall";
    }

    private static boolean isAbsolutePath(String path) {
        if (path.startsWith("/")) {
            return true;
        }
        return path.length() >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':';
    }
}
