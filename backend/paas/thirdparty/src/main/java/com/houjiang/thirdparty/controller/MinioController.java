package com.houjiang.thirdparty.controller;

import com.houjiang.thirdparty.utils.MinioUtils;
import io.minio.errors.MinioException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

/**
 * minio
 *
 * @Description: minio
 * @Author Amos
 * @CreateTime: 2023-07-17  13:52
 * @Version: 4.1
 * @author Amos
 */
@RestController
@RequestMapping("/minio")
public class MinioController {
    @Resource
    private MinioUtils minioUtils;
    @PostMapping("/upload")
    public String upload(MultipartFile file)  throws MinioException {
        System.out.println(file);
        minioUtils.upload(file);
        return "Hello";
    }
}