package com.houjiang.commons.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.util.secverifyapiutil.Base64Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 二维码工具类
 *
 * @author Amos
 */
@Component
@RefreshScope
public class QrCodeUtil {

    private static String domain;

    @Value("${domainName}")
    private  String domainName;

    @PostConstruct
    public void getDomainName() {
        domain = this.domainName;
    }

    /** base64图片固定前缀 */
    public static final String BASE_PREFIX = "data:image/png;base64,";

    /**
     * 生成一个二维码图片
     *
     * @param width
     * @param height
     * @param content
     * @return
     * @throws WriterException
     * @throws IOException
     */

    public static byte[] createQRCode(int width, int height, String content){
        ByteArrayOutputStream os = null;

        try {
            // 二维码基本参数设置

            Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();

            // 设置编码字符集utf-8
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

            // 设置纠错等级L/M/Q/H,纠错等级越高越不易识别，当前设置等级为最高等级H
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

            // 可设置范围为0-10，但仅四个变化0 1(2) 3(4 5 6) 7(8 9 10)
            hints.put(EncodeHintType.MARGIN, 0);

            // 生成图片类型为QRCode

            BarcodeFormat format = BarcodeFormat.QR_CODE;

            // 创建位矩阵对象

            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, format, width, height, hints);

            // 设置位矩阵转图片的参数

            //  MatrixToImageConfig config = new MatrixToImageConfig(Color.black.getRGB(), Color.white.getRGB());

            // 位矩阵对象转流对象

            os = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(bitMatrix, "png", os);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return os.toByteArray();

    }

    /** 对字节数组字符串进行Base64解码并生成图片 */
    public static String GenerateImage(String imgStr, String imgFilePath) {
        // 图像数据为空
        if (imgStr == null)
        {
            return null;
        }
        try {
            // Base64解码
            byte[] bytes = Base64.getDecoder().decode(imgStr);
            for (int i = 0; i < bytes.length; ++i) {
                // 调整异常数据
                if (bytes[i] < 0) {
                    bytes[i] += 256;
                }
            }
            Date currentTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateString = formatter.format(currentTime);
            String pathUrl = ResourceUtils.getURL("classpath:").getPath();
            String path1 = pathUrl+"/static/images/"+ dateString+".jpg";
            // 生成jpeg图片
            OutputStream out = new FileOutputStream(path1);
            out.write(bytes);
            out.flush();
            out.close();
            return dateString+".jpg";
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 生成二维码上传oss
     */
    public static ResponseResult  GenerateQRCode(String code){
            byte[] b = createQRCode(150, 150, code);
            //上传oss
            String encode = Base64Utils.encode2(b);
            OssUploadUpdateParamDTO param = new OssUploadUpdateParamDTO();
            param.setBase64Str(encode);
            param.setStreamName(".png");
            param.setFileUrl("file/upload/shop/settled/");
            return ResponseResult.success(param);
    }
    /**
     * 生成商品二维码返回base64格式
     * @param path
     * @return
     */
    public static String getBase64URLString(String path) {
        String domainUrl = domain + path;
        try {
            byte[] b = createQRCode(150, 150, domainUrl);
            //base64
            return BASE_PREFIX + Base64.getEncoder().encodeToString(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
