package com.houjiang.commons.util.secverifyapiutil;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * @author Amos
 */
public class QRCodeUtil {

    /** 二维码图片宽度 */
    private static int width = 300;
    /** 二维码图片高度 */
    private static int height = 300;
    /** 二维码的图片格式 */
    private static String format = "jpg";

    /** 解析base64编码的二维码中内容 */
    public static Result Base64Str2Result(String base64Str) throws IOException, NotFoundException {
        MultiFormatReader formatReader = new MultiFormatReader();
        InputStream input = QRCodeUtil.base64Str2Stream(base64Str);
        BufferedImage image = ImageIO.read(input);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        // 定义二维码的参数
        HashMap hints = new HashMap();
        // 编码格式
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        // 纠错等级
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

        Result result = formatReader.decode(binaryBitmap, hints);
        return result;
    }

    /**
     * 二维码默认宽高300，utf-8编码,jpg格式
     *
     * @param text 二维码内容
     * @return
     * @throws IOException
     * @throws NotFoundException
     * @throws WriterException
     */
    public static String text2Base64Str(String text) throws IOException, NotFoundException, WriterException {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        // 内容所使用字符集编码
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, format, outputStream);
        String base64Str = QRCodeUtil.stream2Base64(outputStream);
        return base64Str;
    }

    /** 将二维码转为 base64 */
    private static String stream2Base64(ByteArrayOutputStream outputStream) {
        return Base64.encodeBase64String(outputStream.toByteArray());
    }

    private static InputStream base64Str2Stream(String base64Str) throws IOException {
//		@SuppressWarnings("restriction")
//		
        // 解密
        byte[] b = Base64.decodeBase64(base64Str);
        // 处理数据
        for (int i = 0; i < b.length; ++i) {
            if (b[i] < 0) {
                b[i] += 256;
            }
        }
        // 对图片进行命名
        InputStream input = new ByteArrayInputStream(b);
        // BufferedImage image = ImageIO.read(input);
        return input;
    }

    public static void localMain(String[] args) throws Exception {

    }


}
