package com.houjiang.thirdparty.utils;

import java.io.*;
import java.util.Base64;

/**
 * @author Amos
 */
public class Base64Utils {

    /**
     * 文件转字符串
     *
     * @param file
     * @return
     */
    public static String file2String(File file) {
        BufferedReader buffer = null;
        StringBuilder sb = new StringBuilder();
        try {
            buffer = new BufferedReader(new FileReader(file));
            String temp = "";
            while ((temp = buffer.readLine()) != null) {
                sb.append(temp);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (buffer != null) {

                    buffer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解密
     *
     * @param str
     * @return
     */
    public static String getFromBase64(String str) {
        byte[] b = null;
        String result = null;
        if (str != null) {
            
            try {
                b = Base64.getDecoder().decode(str);
                result = new String(b, "utf-8");
            } catch (Exception e) {

            }
        }
        return result;
    }

    /**
     * 加密
     *
     * @param str
     * @return
     */
    public static byte[] getBase64(String str) {
        byte[] b = null;
        String s = null;
        try {
            b = str.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {

        }

        return b;
    }

    /**
     * 编码
     *
     * @param bstr
     * @return String
     */
    public static String encode(byte[] bstr) {
        return Base64.getEncoder().encodeToString(bstr);
    }

    /**
     * 解码
     *
     * @param str
     * @return string
     */
    public static byte[] decode(String str) {
        byte[] bt = null;

        bt = Base64.getDecoder().decode(str);

        return bt;
    }


}
