package com.houjiang.commons.util;

import javax.net.ssl.*;
import java.io.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;

/**
 * @program: simple_tools
 * @description:
 * @author Amos
 * @create: 2019-10-17 15:50
 **/
public class HttpsUtil {

    /**
     * 功能描述:
     * 〈发送get请求,请求参数格式是 name1=value1&name2=value2 的形式。〉
     *
     * @params : [url, param]
     * @return : java.lang.String
     * @author Amos
     * @date : 2019/10/17 15:53
     */
    public static String sendGet(String url, String param) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            //String urlNameString = url + "?" + param;
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (ConnectException e) {
            //此处转成自己的日志记录
            System.out.println("调用HttpUtils.sendGet ConnectException, url=" + url + ",param=" + param);
        } catch (SocketTimeoutException e) {
            //此处转成自己的日志记录
            System.out.println("调用HttpUtils.sendGet SocketTimeoutException, url=" + url + ",param=" + param);
        } catch (IOException e) {
            //此处转成自己的日志记录
            System.out.println("调用HttpUtils.sendGet IOException, url=" + url + ",param=" + param);
        } catch (Exception e) {
            //此处转成自己的日志记录
            System.out.println("调用HttpsUtil.sendGet Exception, url=" + url + ",param=" + param);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                //此处转成自己的日志记录
                System.out.println("调用in.close Exception, url=" + url + ",param=" + param);
            }
        }
        return result.toString();
    }

    /**
     * 功能描述:
     * 〈发送post请求,请求参数格式是 name1=value1&name2=value2 的形式。〉
     *
     * @params : [url, param]
     * @return : java.lang.String
     * @author Amos
     * @date : 2019/10/17 15:54
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            //String urlNameString = url + "?" + param;
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (ConnectException e) {
            //此处转成自己的日志记录
            System.out.println("调用HttpUtils.sendPost ConnectException, url=" + url + ",param=" + param);
        } catch (SocketTimeoutException e) {
            //此处转成自己的日志记录
            System.out.println("调用HttpUtils.sendPost SocketTimeoutException, url=" + url + ",param=");
        } catch (IOException e) {
            //此处转成自己的日志记录
            System.out.println("调用HttpUtils.sendPost IOException, url=" + url + ",param=" + param);
        } catch (Exception e) {
            //此处转成自己的日志记录
            System.out.println("调用HttpsUtil.sendPost Exception, url=" + url + ",param=" + param);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                //此处转成自己的日志记录
                System.out.println("调用in.close Exception, url=" + url + ",param=" + param);
            }
        }
        return result.toString();
    }

    /**
     * 功能描述:
     * 〈发送SSL方式的Post方法 请求参数格式是 name1=value1&name2=value2 的形式〉
     *
     * @params : [url, param]
     * @return : java.lang.String
     * @author Amos
     * @date : 2019/10/17 16:00
     */
    public static String sendSSLPost(String url, String param) {
        StringBuilder result = new StringBuilder();
        String urlNameString = url + "?" + param;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
            URL console = new URL(urlNameString);
            HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String ret = "";
            while ((ret = br.readLine()) != null) {
                if (ret != null && !ret.trim().equals("")) {
                    result.append(new String(ret.getBytes("ISO-8859-1"), "utf-8"));
                }
            }
            conn.disconnect();
            br.close();
        } catch (ConnectException e) {
            System.out.println("调用HttpUtils.sendSSLPost ConnectException, url=" + url + ",param=" + param);
        } catch (SocketTimeoutException e) {
            System.out.println("调用HttpUtils.sendSSLPost SocketTimeoutException, url=" + url + ",param=" + param);
        } catch (IOException e) {
            System.out.println("调用HttpUtils.sendSSLPost IOException, url=" + url + ",param=" + param);
        } catch (Exception e) {
            System.out.println("调用HttpsUtil.sendSSLPost Exception, url=" + url + ",param=" + param);
        }
        return result.toString();
    }

    private static class TrustAnyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}