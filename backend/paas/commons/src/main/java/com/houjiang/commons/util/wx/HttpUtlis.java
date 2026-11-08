package com.houjiang.commons.util.wx;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Amos
 */
@Component
public class HttpUtlis {
    /** 声明连接池 */
    private PoolingHttpClientConnectionManager cm;

    public HttpUtlis() {
        this.cm = new PoolingHttpClientConnectionManager();
        //设置最大连接数
        this.cm.setMaxTotal(100);
        //设置每个主机的最大连接数
        this.cm.setDefaultMaxPerRoute(10);
    }

    /** 判断文件夹是否存在 */
    public static void judeDirExists(File file) throws FileNotFoundException {
        File pathurl = new File(ResourceUtils.getURL("classpath:").getPath());
        String path = pathurl + "/static/wximage/";
        File file1 = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                System.out.println("dir exists");
            } else {
                System.out.println("the same name file exists, can not create dir");
            }
            System.out.println("dir not exists, create it ...");
            file.mkdir();
        }

    }

    /** 根据请求地址下载页面数据 */
    public String doGetHtml(String url) {
        //获取HttpClient对象
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(this.cm).build();
        //创建httpGet请求对象，设置url地址
        HttpGet httpGet = new HttpGet(url);
        //设置请求信息
        httpGet.setConfig(this.getConfig());
        //使用HttpClient发起请求，获取响应
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            //解析响应，返回结果
            if (response.getStatusLine().getStatusCode() == 200) {
                //判断响应体是否不为空，如果不为空就可以使用EntityUtils
                if (response.getEntity() != null) {
                    String content = EntityUtils.toString(response.getEntity(), "utf8");
                    System.out.println(content + "content");
                    return content;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭response
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //没有数据,返回空串
        return "";
    }

    /** 下载图片 */
    public Map<String, String> doGetImages(String url, Integer size,String domain) {
        Map<String, String> map = new HashMap<>();
        //获取HttpClient对象
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(this.cm).build();
        //创建httpGet请求对象，设置url地址
        HttpGet httpGet = new HttpGet(url);
        //设置请求信息
        httpGet.setConfig(this.getConfig());
        //使用HttpClient发起请求，获取响应
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            //解析响应，返回结果
            if (response.getStatusLine().getStatusCode() == 200) {
                //判断响应体是否不为空，如果不为空就可以使用EntityUtils
                if (response.getEntity() != null) {
                    //下载图片
                    //获取图片的后缀
//                    String exName= url.substring(url.lastIndexOf("."));
                    //创建图片名，重命名图片
                    long l = System.currentTimeMillis();
                    String picName = UUID.randomUUID().toString().indexOf(0) + l + ".png";
                    System.out.println(picName);
                    //下载图片
                    //声明OutPutStream
                    String pathUrl = domain + "/liveImg/";
                    File folder = new File(pathUrl);
                    if (!folder.exists() || !folder.isDirectory()) {
                        folder.mkdirs();
                    }
                    String path = domain + "/liveImg/" + picName;
                    String pathUrl1 = domain + "/liveCoverImg/";
                    File folder1 = new File(pathUrl1);
                    if (!folder1.exists() || !folder1.isDirectory()) {
                        folder1.mkdirs();
                    }
                    String path1 = domain + "/liveCoverImg/" + picName;
                    OutputStream outputStream = new FileOutputStream(new File(path));
                    response.getEntity().writeTo(outputStream);
                    //返回图片名称
                    Integer integer = ImageUtils.judgeIsThumbnail(path);
                    if (integer == 2) {
                        map.put("integer", "false");
                        return map;
                    }
                    if (size == 1) {
                        ImageUtils.resizeImage(path, path1, 300, 300, true);
                    } else if (size == 2) {
                        ImageUtils.resizeImage(path, path1, 1080, 1920, true);
                    } else if (size == 3) {
                        ImageUtils.resizeImage(path, path1, 800, 640, true);
                    } else {
                        ImageUtils.resizeImage(path, path1, 800, 800, true);
                    }


                    map.put("pathName", path1);
                    map.put("picName", picName);
                    return map;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭response
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //如果下载失败,返回空串
        return null;
    }

    /** 设置请求的信息 */
    private RequestConfig getConfig() {
        RequestConfig config = RequestConfig.custom()
                //创建连接的最长时间
                .setConnectionRequestTimeout(600000)
                //获取连接的最长的最长时间
                .setConnectTimeout(100000)
                //数据传输的最长时间
                .setSocketTimeout(6000000)
                .build();
        return config;
    }

}
