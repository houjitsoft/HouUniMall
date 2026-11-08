package com.houjiang.commons.util.wx;

import net.coobird.thumbnailator.Thumbnailator;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName ImageUtils.java
 * @Description ImageUtils
 * @createTime 2021年08月19日 14:39:00
 */
public class ImageUtils {

    /**
     * 重新生成图片宽、高
     *
     * @param srcPath   图片路径
     * @param destPath  新生成的图片路径
     * @param newWith   新的宽度
     * @param newHeight 新的高度
     * @param forceSize 是否强制使用指定宽、高,false:会保持原图片宽高比例约束
     * @return
     * @throws IOException
     */
    public static boolean resizeImage(String srcPath, String destPath, int newWith, int newHeight, boolean forceSize) throws IOException {
        if (forceSize) {
            Thumbnails.of(srcPath).forceSize(newWith, newHeight).toFile(destPath);
//            Thumbnails.of("这个放图片的路径").size(width, height).toFile(savePath+fileName);
        } else {
            Thumbnails.of(srcPath).width(newWith).height(newHeight).toFile(destPath);
        }
        return true;
    }

    /**
     * @throws
     * @title 判断是否是缩略图
     * @description
     * @author Amos
     * @updateTime 2021/8/19 15:55
     */
    public static Integer judgeIsThumbnail(String srcPath) throws IOException {
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(new File(
                    srcPath));
            Iterator iter = ImageIO.getImageReaders(iis);
            if (!iter.hasNext()) {
                System.out.println("null");
                return 2;
            } else {
                ImageReader reader = (ImageReader) iter.next();
                System.out.println(reader.getFormatName());
                return 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void localMain(String[] args) {
        //缩略图
        int width = 80;
        int height = 60;
        File in = new File("F:\\HOU_BankFont\\mallplatformmanager\\src\\main\\resources\\image\\1629359957797.png");
        File out = new File("F:\\HOU_BankFont\\mallplatformmanager\\src\\main\\resources\\ys\\1629359957797.png");
        try {
            //我设置了生成小图片的大小，并不一定会按照我设置的大小去生成图片，内部有一个比例的。
            //Thumbnailator类有很多方法，根据参数去传入参数
            Thumbnailator.createThumbnail(in, out, 300, 300);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
