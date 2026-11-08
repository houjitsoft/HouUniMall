package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.houjiang.base.entity.BaseShareUrlEntity;
import com.houjiang.commons.util.ResponseResult;

import java.io.IOException;
import java.util.Map;

/**
 * @author Amos
 */
public interface BaseShareUrlService extends IService<BaseShareUrlEntity> {


    /**
     * 分享二维码生成
     * @param param
     * @return
     * @throws NotFoundException
     * @throws IOException
     * @throws WriterException
     */
  ResponseResult commonCreatQrCode(Map<String, Object> param) throws NotFoundException, IOException, WriterException;
}
