package com.ktg.mes.report.controller;

import com.ktg.mes.report.utils.QRCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@RestController
public class QRCodeController {

    @Autowired
    private QRCodeUtil qrCodeUtil;

    /**
     * 生成二维码图片接口
     * @param content 二维码内容
     * @param width 图片宽度（可选，默认300）
     * @param height 图片高度（可选，默认300）
     * @param response HttpServletResponse对象，用于输出图片流
     */
    @GetMapping(value = "/api/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public void generateQRCode(
            @RequestParam String content,
            @RequestParam(defaultValue = "200") int width,
            @RequestParam(defaultValue = "200") int height,
            HttpServletResponse response) {
        try {
            // 设置响应头，确保浏览器正确识别为PNG图片
            response.setContentType("image/png");
            OutputStream os = response.getOutputStream();
            
            // 调用工具类生成二维码字节流，并直接写入响应输出流
            // 这里假设您的QRCodeUtil工具类中有一个方法可以直接返回字节数组
            byte[] qrCodeBytes = qrCodeUtil.generateQRCodeBytes(content, width, height);
            os.write(qrCodeBytes);
            os.flush();
            os.close();
        } catch (Exception e) {
            // 日志记录异常，可根据需要添加更详细的错误处理
            throw new RuntimeException("生成二维码失败: " + e.getMessage());
        }
    }
}