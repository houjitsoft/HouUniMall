package com.houjiang.commons.util;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.exception.BizCodeEnume;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Amos
 */
public class ResponseUtil {
    public static void returnJson(HttpServletResponse response, Integer code, String message){
        PrintWriter writer = null;
        response.setContentType("application/json;charset=UTF-8");
        try {
            ResponseResult result = ResponseResult.fail(code,message);
            writer = response.getWriter();
            writer.print(JSON.toJSONString(result));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
    public static void returnJson(HttpServletResponse response, BizCodeEnume bizCodeEnume){
        PrintWriter writer = null;
        response.setContentType("application/json;charset=UTF-8");
        try {
            ResponseResult result = ResponseResult.fail(bizCodeEnume);
            writer = response.getWriter();
            writer.print(JSON.toJSONString(result));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
