package com.houjiang.commons.util.secverifyapiutil;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64Util
 * Date: 2019/6/24
 * Time: 15:15
 *
 * @author Amos
 */
public abstract class Base64Utils {
    private static final Charset DEFAULT_CHARSET;

    public Base64Utils() {
    }

    public static byte[] encode(byte[] src) {
        return src.length == 0 ? src : Base64.getEncoder().encode(src);
    }

    public static byte[] decode(byte[] src) {
        return src.length == 0 ? src : Base64.getDecoder().decode(src);
    }

    public static byte[] encodeUrlSafe(byte[] src) {
        return src.length == 0 ? src : Base64.getUrlEncoder().encode(src);
    }

    public static byte[] decodeUrlSafe(byte[] src) {
        return src.length == 0 ? src : Base64.getUrlDecoder().decode(src);
    }

    public static String encodeToString(byte[] src) {
        return src.length == 0 ? "" : new String(encode(src), DEFAULT_CHARSET);
    }

    public static byte[] decodeFromString(String src) {
        return src.isEmpty() ? new byte[0] : decode(src.getBytes(DEFAULT_CHARSET));
    }

    public static String encodeToUrlSafeString(byte[] src) {
        return new String(encodeUrlSafe(src), DEFAULT_CHARSET);
    }

    public static byte[] decodeFromUrlSafeString(String src) {
        return decodeUrlSafe(src.getBytes(DEFAULT_CHARSET));
    }

    /**
     * 编码
     *
     * @param bstr
     * @return String
     */
    public static String encode2(byte[] bstr) {
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

    /**
     * @param file
     * @return String
     * @description 将文件转base64字符串
     * @date 2018年3月20日
     * @author Amos
     * File转成编码成BASE64
     */

    public static String fileToBase64(File file) {
        String base64 = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            in.read(bytes);
            base64 = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return base64;
    }

    static {
        DEFAULT_CHARSET = StandardCharsets.UTF_8;
    }

    public static double base64FileSize(MultipartFile file) throws IOException {
        // 解码为字节数组
        byte[] bytes2 = decode(new String(file.getBytes()));
        // 获取文件大小
        int fileSize = bytes2.length;
        return (double) fileSize / (1024 * 1024);
    }

    /**
     * 方法名:          PDFToBase64
     * 方法功能描述:     将pdf文件转换为Base64编码
     *
     * @param: String sourceFile:源文件路径
     * @return: String  base64字符串
     */
    public static String PDFToBase64(String sourceFile) {

        FileInputStream fin = null;
        BufferedInputStream bin = null;
        ByteArrayOutputStream baos = null;
        BufferedOutputStream bout = null;
        File file = new File(sourceFile);
        try {
            fin = new FileInputStream(file);
            bin = new BufferedInputStream(fin);
            baos = new ByteArrayOutputStream();
            bout = new BufferedOutputStream(baos);
            byte[] buffer = new byte[1024];
            int len = bin.read(buffer);
            while (len != -1) {
                bout.write(buffer, 0, len);
                len = bin.read(buffer);
            }
            //刷新此输出流并强制写出所有缓冲的输出字节
            bout.flush();
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes).trim();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fin.close();
                bin.close();
                bout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /** 解密 */
    public static String getFromBase64(String str) {
        byte[] b = null;
        String result = null;
        if (str != null) {
            
            try {
                b = Base64.getDecoder().decode(str);
                result = new String(b, StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
