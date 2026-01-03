package com.ktg.mes.report.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class QRCodeUtil {
    
    // 默认二维码尺寸
    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 200;
    
    // 默认图片格式
    private static final String DEFAULT_FORMAT = "PNG";
    
    // 默认二维码颜色配置
    private static final int ON_COLOR = 0xFF000000; // 黑色
    private static final int OFF_COLOR = 0xFFFFFFFF; // 白色
    
    /**
     * 生成二维码的Base64编码（不带Data URL前缀）
     * @param content 条码内容
     * @return Base64字符串
     */
    public String generateQRCodeBase64(String content) {
        return generateQRCodeBase64(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    /**
     * 生成二维码的Base64编码（不带Data URL前缀）
     * @param content 条码内容
     * @param width 宽度
     * @param height 高度
     * @return Base64字符串
     */
    public String generateQRCodeBase64(String content, int width, int height) {
        try {
            byte[] qrCodeBytes = generateQRCodeBytes(content, width, height);
            return Base64Utils.encodeToString(qrCodeBytes);
        } catch (Exception e) {
            // 如果二维码生成失败，返回一个空白的Base64占位符
            return generateErrorPlaceholderBase64(width, height);
        }
    }
    
    /**
     * 生成二维码的Data URL（带前缀）
     * @param content 条码内容
     * @return Data URL字符串
     */
    public String generateQRCodeDataURL(String content) {
        return generateQRCodeDataURL(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    /**
     * 生成二维码的Data URL（带前缀）
     * @param content 条码内容
     * @param width 宽度
     * @param height 高度
     * @return Data URL字符串
     */
    public String generateQRCodeDataURL(String content, int width, int height) {
        String base64 = generateQRCodeBase64(content, width, height);
        return "data:image/png;base64," + base64;
    }
    
    /**
     * 生成二维码图片字节数组
     * @param content 条码内容
     * @param width 宽度
     * @param height 高度
     * @return 图片字节数组
     */
    public byte[] generateQRCodeBytes(String content, int width, int height) {
        try {
            // 设置二维码参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 高容错率
            hints.put(EncodeHintType.MARGIN, 1); // 设置边距
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // 字符编码
            
            // 创建QRCodeWriter对象
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            
            // 编码内容
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            
            // 转换为BufferedImage
            MatrixToImageConfig config = new MatrixToImageConfig(ON_COLOR, OFF_COLOR);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);
            
            // 转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, DEFAULT_FORMAT, baos);
            
            return baos.toByteArray();
        } catch (Exception e) {
            // 如果生成失败，返回错误占位图片
            return generateErrorPlaceholderBytes(width, height);
        }
    }
    
    /**
     * 生成二维码的字节数组（默认尺寸）
     * @param content 条码内容
     * @return 图片字节数组
     */
    public byte[] generateQRCodeBytes(String content) {
        return generateQRCodeBytes(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    /**
     * 生成错误占位符的Base64
     */
    private String generateErrorPlaceholderBase64(int width, int height) {
        try {
            byte[] bytes = generateErrorPlaceholderBytes(width, height);
            return Base64Utils.encodeToString(bytes);
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * 生成错误占位符图片
     */
    private byte[] generateErrorPlaceholderBytes(int width, int height) {
        try {
            // 创建一个简单的错误提示图片
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = image.createGraphics();
            
            // 设置白色背景
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            
            // 绘制红色X
            g2d.setColor(java.awt.Color.RED);
            g2d.setStroke(new java.awt.BasicStroke(3));
            g2d.drawLine(10, 10, width - 10, height - 10);
            g2d.drawLine(width - 10, 10, 10, height - 10);
            
            g2d.dispose();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, DEFAULT_FORMAT, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}