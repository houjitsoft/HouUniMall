package com.houjiang.base.service;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 小匠帮你传 - 文件智能识别
 * @author Amos
 */
public interface FileRecognizeService {

    /**
     * TODO 补充方法说明
     * @param file TODO
     * @param description TODO
     * @param token TODO
     * @return TODO
     */
    ResponseResult recognize(MultipartFile file, String description, String token);

    /** 受控目录模式的智能入库识别。 */
    ResponseResult recognizeForImport(MultipartFile file, String systemKey, String description, String token);

    /**
     * TODO 补充方法说明
     * @param params TODO
     * @return TODO
     */
    ResponseResult confirm(Map<String, Object> params);
}
