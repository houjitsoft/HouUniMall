package com.houjiang.commons.util;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.*;

public class Url {

    private final static String ENCODE = "UTF-8";

    /**
     * URL 解码
     *
     * @return String
     * @author Amos
     * @date 2015-3-17 下午04:09:51
     */
    public static String getURLDecoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLDecoder.decode(str, ENCODE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * URL 转码
     *
     * @return String
     * @author Amos
     * @date 2015-3-17 下午04:10:28
     */
    public static String getURLEncoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLEncoder.encode(str, ENCODE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @return void
     * @author Amos
     * @date 2015-3-17 下午04:09:16
     */
    // public static void localMain(String[] args) {
    // String str =
    // System.out.println(getURLEncoderString(str));
    // System.out.println(getURLDecoderString(str));
    //
    // }

    /**
     * list转String
     *
     * @param mList
     * @return
     */
    /** 采用Stringbuilder.append()的方式追加 */
    public static String listToString2(List<String> mList) {
        final String SEPARATOR = ",";
        // mList = Arrays.asList("AAA", "BBB", "CCC");
        StringBuilder sb = new StringBuilder();
        String convertedListStr = "";
        if (null != mList && mList.size() > 0) {
            for (String item : mList) {
                sb.append(item);
                sb.append(SEPARATOR);
            }
            convertedListStr = sb.toString();
            convertedListStr = convertedListStr.substring(0, convertedListStr.length() - SEPARATOR.length());
            return convertedListStr;
        } else {
            return "List is null!!!";
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        final ExecutorService exec = Executors.newFixedThreadPool(1);
        Callable<String> call = new Callable<String>() {
            @Override
            public String call() throws Exception {
                // 开始执行耗时操作
                Thread.sleep(1000 * 5);
                return "执行完成!!";
            }
        };
        try {
            Future<String> future = exec.submit(call);
            // 任务处理超时时间设为1秒
            String obj = future.get(1000 * 5, TimeUnit.MILLISECONDS);
            System.out.println("任务成功返回:" + obj);
        } catch (TimeoutException ex) {
            System.out.println("处理超时啦....");
            ex.printStackTrace();
        } catch (Exception e) {
            System.out.println("处理失败.");
            e.printStackTrace();
        } finally {
            System.out.println("再次执行！");
        }
        // 关闭线程池
        exec.shutdown();
    }
}
