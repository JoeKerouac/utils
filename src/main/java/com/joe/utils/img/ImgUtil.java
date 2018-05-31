package com.joe.utils.img;

import com.joe.utils.common.IOUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImgUtil {
    private static final Logger logger = LoggerFactory.getLogger(ImgUtil.class);

    public static void setAlpha(String oldPath, String newPath) {
        /*
          增加测试项 读取图片，绘制成半透明
         */
        try {
            logger.debug("开始读取图片：{}", oldPath);
            byte[] data = IOUtils.read(new FileInputStream(oldPath));
            logger.debug("图片加载完毕，开始改变图片的alpha值");

            ImageIcon imageIcon = new ImageIcon(data);
            //创建一个新模式的图片（防止原图片没有alpha通道，强制生成一个带alpha通道的图片）
            BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(),
                    BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics();
            g2D.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
            // 循环每一个像素点，改变像素点的Alpha值
            int alpha = 20;
            for (int j1 = bufferedImage.getMinY(); j1 < bufferedImage.getHeight() / 2; j1++) {
                for (int j2 = bufferedImage.getMinX(); j2 < bufferedImage.getWidth(); j2++) {
                    int rgb = bufferedImage.getRGB(j2, j1);
                    rgb = (alpha << 24) | (rgb & 0x00ffffff);
                    bufferedImage.setRGB(j2, j1, rgb);
                }
            }

            // 生成图片为PNG
            ImageIO.write(bufferedImage, "png", new File(newPath));
        } catch (Exception e) {
            logger.error("图片透明度处理失败");
        }

    }

    public static void test(String oldPath, String newPath) {
        /*
          增加测试项 读取图片，绘制成半透明
         */
        try {
            BufferedImage image = ImageIO.read(new FileInputStream(oldPath));

        } catch (Exception e) {
            logger.error("图片透明度处理失败");
        }

    }

    public static void main(String[] args) throws IOException {
//        String path = "D://2.jpg";
//        BufferedImage image = ImageIO.read(new FileInputStream(path));
//        System.out.println("status 是：" + image.getColorModel().getPixelSize());
        BufferedImage image = compression(ImageIO.read(new FileInputStream("D://2.jpg")), 3 , 1);
        ImageIO.write(image, "jpg", new File("D://3.jpg"));
//        setAlpha("D://2.jpg", "D://2.png");
    }


    /**
     * 获取图片的说明
     *
     * @param path 图片的路径
     * @return 图片说明
     * @throws IOException 读取图片异常时抛出
     */
    public static ImgMetadata getImgInfo(final String path) throws IOException {
        File file = new File(path);
        BufferedImage image = ImageIO.read(file);
        ImgMetadata imgMetadata = getImgInfo(image);
        imgMetadata.setName(file.getName());
        return imgMetadata;
    }

    /**
     * 获取图片的说明
     *
     * @param image 图片的数据
     * @return 图片说明（不包含文件名，获取不到文件名）
     */
    public static ImgMetadata getImgInfo(BufferedImage image) {
        ImgMetadata imgMetadata = new ImgMetadata();
        imgMetadata.setHeight(image.getHeight());
        imgMetadata.setWidth(image.getWidth());
        imgMetadata.setPixelSize(image.getColorModel().getPixelSize());
        return imgMetadata;
    }

    /**
     * 缩放图片（将图片宽高同时变小，图片将会变模糊同时变小）
     *
     * @param src   原图片数据
     * @param scale 缩放比列，宽高同时缩放
     * @return 缩放后的图片（会变模糊）
     */
    public static BufferedImage compression(BufferedImage src, int scale) {
        return compression(src, scale, scale);
    }

    /**
     * 缩放图片
     *
     * @param src         图片源
     * @param widthScale  宽度缩放比例
     * @param heightScale 高度缩放比列
     * @return 缩放后的图片（会变模糊）
     */
    public static BufferedImage compression(BufferedImage src, int widthScale, int heightScale) {
        int oldWidth = src.getWidth();
        int oldHeight = src.getHeight();
        int width = oldWidth / widthScale + ((oldWidth % widthScale) > 0 ? 1 : 0);
        int height = oldHeight / heightScale + ((oldHeight % heightScale) > 0 ? 1 : 0);

        Graphics2D g2D = (Graphics2D) src.getGraphics();
        g2D.drawImage(src, 0, 0, null);

        BufferedImage dest = new BufferedImage(width, height, src.getType());

        for (int x = src.getMinX(); x < width; x++) {
            for (int y = src.getMinY(); y < height; y++) {
                int rgb = src.getRGB(x * widthScale, y * heightScale);
                dest.setRGB(x, y, rgb);
            }
        }

        return dest;
    }


    @Data
    public static final class ImgMetadata {
        ImgMetadata() {
        }

        /**
         * 图片宽
         */
        private int width;
        /**
         * 图片高
         */
        private int height;
        /**
         * 图片文件名
         */
        private String name;
        /**
         * 图片位深
         */
        private int pixelSize;
    }
}
