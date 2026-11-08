package com.houjiang.merchant.common.plugin.storage.engine;

import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.util.YmlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 本地存储
 */
@Slf4j
public class LocalStorage {

    /**
     * 本地上传
     *
     * @author Amos
     * @param multipartFile 上传对象
     * @param key 文件名称 20220331/122.jpg
     * @param folder 文件夹
     */
    public void upload(MultipartFile multipartFile, String key, String folder) {
        // 映射目录
        String directory = YmlUtils.get("merchant.upload-directory");
        if (directory == null || directory.equals("")) {
            throw new OperateException("请配置上传目录merchant.upload-directory");
        }

        // 文件信息
        String saveName = key.split("/")[1];
        String savePath = (directory + folder + "/" + key.split("/")[0]).replace("\\", "/");

        // 创建目录
        File fileExist = new File(savePath);
        if (!fileExist.exists()) {
            if (!fileExist.mkdirs()) {
                throw new OperateException("创建上传目录失败");
            }
        }

        // 保存文件
        try {
            File dest = new File(savePath, saveName);
            multipartFile.transferTo(dest);
        } catch (Exception e) {
            log.error("本地文件上传失败", e);
            throw new OperateException("本地文件上传失败，请稍后重试");
        }

    }

    public void delete(String filePath) {
        String directory = YmlUtils.get("merchant.upload-directory");
        File file = new File(directory + filePath);
        file.delete();
    }

}
