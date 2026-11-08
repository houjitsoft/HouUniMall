package com.houjiang.base.controller;

import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName BaseShareUrlController
 * @Description
 * @Author Amos
 * @Date 2023/8/24 9:22
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/shareUrl")
public class BaseShareUrlController extends BaseController{



    /**
     * 分享二维码生成
     * @param param
     * @return
     * @throws NotFoundException
     * @throws IOException
     * @throws WriterException
     */
    @PostMapping("/commonCreatQrCode")
    public ResponseResult commonCreatQrCode(@RequestBody  Map<String, Object> param) throws NotFoundException, IOException, WriterException {
        ResponseResult result=baseShareUrlService.commonCreatQrCode(param);
        return result;
    }

}
