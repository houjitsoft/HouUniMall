package com.houjiang.commons.util.wx;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Amos
 */
public class UploadWxUtil {
    private static final String UPLOAD_URL = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";

    public static NewWxMaterialResult uploadFile(String filePath, String accessToken) throws Exception {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("文件不存在！");
        }
        Integer integer = ImageUtils.judgeIsThumbnail(filePath);
        String url = null;
        if (integer == 1) {
            url = UPLOAD_URL.replace("ACCESS_TOKEN", accessToken).replace("TYPE", "image");
        } else {
            url = UPLOAD_URL.replace("ACCESS_TOKEN", accessToken).replace("TYPE", "thumb");
        }

        URL urlObj = new URL(url);

        //连接
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setConnectTimeout(1000);
        //请求头
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");
        //conn.setRequestProperty("Content-Type","multipart/form-data;");

        //设置边界
        String BOUNDARY = "----------" + System.currentTimeMillis();
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);

        StringBuilder sb = new StringBuilder();
        sb.append("--");
        sb.append(BOUNDARY);
        sb.append("\r\n");
        sb.append("Content-Disposition:form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
        sb.append("Content-Type:application/octet-stream\r\n\r\n");

        byte[] head = sb.toString().getBytes("utf-8");

        //输出流
        OutputStream out = new DataOutputStream(conn.getOutputStream());

        out.write(head);

        //文件正文部分
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        int bytes = 0;
        byte[] bufferOut = new byte[1024];
        while ((bytes = in.read(bufferOut)) != -1) {
            out.write(bufferOut, 0, bytes);
        }
        in.close();

        //结尾
        byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");
        out.write(foot);
        out.flush();
        out.close();

        //获取响应
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        String result = null;

        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        if (result == null) {
            result = buffer.toString();
        }
        reader.close();

        System.out.println(result);
//        //需要添加json-lib  jar包
//        String s = JSONUtils.toJSONString(result);

//        System.out.println(s);

//        s.trim();
        NewWxMaterialResult materialResult = JSONObject.parseObject(result, NewWxMaterialResult.class);
        return materialResult;

    }

/**
 * public static void localMain(String[] args) throws Exception {
 * File file = new File("C:\\Users\\slj\\Pictures\\Camera Roll\\1.jpg");
 * if (!file.exists() || !file.isFile()) {
 * throw new IOException("文件不存在！");
 * }
 * 
 * String url = UPLOAD_URL.replace("ACCESS_TOKEN", "48_zlJCL-pCU6TjP_mu08LnS69wqYad7riEwCR-ZSLzQPT80TS5ATlTYKD2MLhXpIw7r7PGh9_Nef8Uif0a-DfFZDcC05VX0mOYr8-gsfzrzqH89atYy7cdiSnUP_rvqo6pEt32jYz-ILHyn37xYIMhACAHRV").replace("TYPE", "image");
 * URL urlObj = new URL(url);
 * 
 //连接
 *
 * HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
 * 
 * conn.setRequestMethod("POST");
 * conn.setDoInput(true);
 * conn.setDoOutput(true);
 * conn.setUseCaches(false);
 * 
 //请求头
 *
 * conn.setRequestProperty("Connection", "Keep-Alive");
 * conn.setRequestProperty("Charset", "UTF-8");
 //conn.setRequestProperty("Content-Type","multipart/form-data;");
 *
 * 
 //设置边界
 *
 * String BOUNDARY = "----------" + System.currentTimeMillis();
 * conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
 * 
 * StringBuilder sb = new StringBuilder();
 * sb.append("--");
 * sb.append(BOUNDARY);
 * sb.append("\r\n");
 * sb.append("Content-Disposition:form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
 * sb.append("Content-Type:application/octet-stream\r\n\r\n");
 * 
 * byte[] head = sb.toString().getBytes("utf-8");
 * 
 //输出流
 *
 * OutputStream out = new DataOutputStream(conn.getOutputStream());
 * 
 * out.write(head);
 * 
 //文件正文部分
 *
 * DataInputStream in = new DataInputStream(new FileInputStream(file));
 * int bytes = 0;
 * byte[] bufferOut = new byte[1024];
 * while ((bytes = in.read(bufferOut)) != -1) {
 * out.write(bufferOut, 0, bytes);
 * }
 * in.close();
 * 
 //结尾
 *
 * byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");
 * out.write(foot);
 * out.flush();
 * out.close();
 * 
 //获取响应
 *
 * StringBuffer buffer = new StringBuffer();
 * BufferedReader reader = null;
 * String result = null;
 * 
 * reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 * String line = null;
 * while ((line = reader.readLine()) != null) {
 * buffer.append(line);
 * }
 * if (result == null) {
 * result = buffer.toString();
 * }
 * reader.close();
 * 
 //需要添加json-lib  jar包
 *
 * String s = JSONUtils.toJSONString(result);
 //        JSONObject json = JSONObject.fromObject(result);
 *
 * System.out.println(s);
 //        String mediaId = s.getString("media_id");
 *
 //        System.out.println();
/**
 *
 * }
 */


    public static void download(String urlString, String filename, String savePath) throws Exception {
        // 构造URL
        URL url = new URL(urlString);
        // 打开连接
        URLConnection con = url.openConnection();
        //设置请求超时为5s
        con.setConnectTimeout(5 * 1000);
        // 输入流
        InputStream is = con.getInputStream();

        // 1K的数据缓冲
        byte[] bs = new byte[1024];
        // 读取到的数据长度
        int len;
        // 输出的文件流
        File sf = new File(savePath);
        if (!sf.exists()) {
            sf.mkdirs();
        }
        // 获取图片的扩展名
        String extensionName = filename.substring(filename.lastIndexOf(".") + 1);
        // 新的图片文件名 = 编号 +"."图片扩展名
//        AlipayFinanceZcbEndowmentorderDetailQueryModel goods = null;
        String newFileName = filename + "." + extensionName;
        OutputStream os = new FileOutputStream(sf.getPath() + "\\" + filename);
        // 开始读取
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        // 完毕，关闭所有链接
        os.close();
        is.close();


    }
}
