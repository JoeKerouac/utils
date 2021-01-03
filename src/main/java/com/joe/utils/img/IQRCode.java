package com.joe.utils.img;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import net.glxn.qrgen.QRCode;

/**
 * 二维码工具类
 *
 * @author joe
 */
public class IQRCode {
    private static final Logger logger = LoggerFactory.getLogger(IQRCode.class);

    private IQRCode() {

    }

    /**
     * 将指定数据生成二维码并保存至指定文件
     *
     * @param data
     *            二维码数据
     * @param fileName
     *            文件名（全名，包含路径）
     * @param width
     *            图片的宽
     * @param height
     *            图片的高
     * @throws IOException
     *             找不到指定文件
     */
    public static void create(String data, String fileName, int width, int height) throws IOException {
        create(data, new FileOutputStream(fileName), width, height);
        logger.debug("二维码保存位置为：{}", fileName);
    }

    /**
     * 将指定数据生成二维码并写入指定输出流
     *
     * @param data
     *            二维码数据
     * @param out
     *            输出流
     * @param width
     *            图片的宽
     * @param height
     *            图片的高
     * @throws IOException
     *             IO异常
     */
    public static void create(String data, OutputStream out, int width, int height) throws IOException {
        logger.debug("生成二维码，要生成的图片的宽为{}，高为{}", width, height);
        QRCode code = createQRCode(data, width, height);
        code.writeTo(out);
        out.close();
        logger.debug("二维码生成成功");
    }

    /**
     * 将指定数据生成二维码图片的BufferedImage
     *
     * @param data
     *            二维码数据
     * @param width
     *            图片的宽
     * @param height
     *            图片的高
     * @return 二维码图片对应的BufferedImage
     * @throws IOException
     *             IO异常
     */
    public static BufferedImage createImg(String data, int width, int height) throws IOException {
        logger.debug("生成二维码，要生成的图片的宽为{}，高为{}", width, height);
        QRCode code = createQRCode(data, width, height);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(code.stream().toByteArray()));
        logger.debug("二维码生成成功");
        return image;
    }

    /**
     * 从本地文件读取二维码内容
     *
     * @param filePath
     *            文件路径
     * @return 二维码中的信息
     * @throws IOException
     *             IO异常
     * @throws NotFoundException
     *             当图像文件中没有二维码信息（图像）时抛出该异常
     */
    public static String read(String filePath) throws IOException, NotFoundException {
        BufferedImage image = ImageIO.read(new File(filePath));
        return read(image);
    }

    /**
     * 从图片中读取二维码
     *
     * @param url
     *            图片的URL
     * @return 二维码包含的文本信息
     * @throws IOException
     *             IO异常
     * @throws NotFoundException
     *             当图像文件中没有二维码信息（图像）时抛出该异常
     */
    public static String read(URL url) throws IOException, NotFoundException {
        BufferedImage image = ImageIO.read(url);
        return read(image);
    }

    /**
     * 从流中读取二维码内容
     *
     * @param input
     *            二维码的输入流
     * @return 二维码中的信息
     * @throws IOException
     *             IO异常
     * @throws NotFoundException
     *             当图像文件中没有二维码信息（图像）时抛出该异常
     */
    public static String read(InputStream input) throws IOException, NotFoundException {
        BufferedImage image = ImageIO.read(input);
        return read(image);
    }

    /**
     * 将指定数据生成二维码
     *
     * @param data
     *            二维码数据
     * @param width
     *            图片的宽
     * @param height
     *            图片的高
     * @return QRCode QRCode
     */
    private static QRCode createQRCode(String data, int width, int height) {
        logger.debug("生成二维码，要生成的图片的宽为{}，高为{}", width, height);
        QRCode code = QRCode.from(data);
        code.withCharset("UTF8");
        code.withSize(width, height);
        logger.debug("二维码生成成功");
        return code;
    }

    /**
     * 从二维码图片读取二维码信息
     *
     * @param image
     *            图片信息
     * @return 二维码文本信息
     * @throws NotFoundException
     *             当图像文件中没有二维码信息（图像）时抛出该异常
     */
    private static String read(BufferedImage image) throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        Binarizer binarizer = new HybridBinarizer(source);
        BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
        Result result = new MultiFormatReader().decode(binaryBitmap);
        return result.getText();
    }
}
