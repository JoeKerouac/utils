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
            BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(),
                    BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics();
            g2D.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
            // 循环每一个像素点，改变像素点的Alpha值
            int alpha = 0;
            for (int j1 = bufferedImage.getMinY(); j1 < bufferedImage.getHeight() / 2; j1++) {
                for (int j2 = bufferedImage.getMinX(); j2 < bufferedImage.getWidth(); j2++) {
                    int rgb = bufferedImage.getRGB(j2, j1);
                    rgb = (alpha << 24) | (rgb & 0x00ffffff);
                    bufferedImage.setRGB(j2, j1, rgb);
                }
            }
            g2D.drawImage(bufferedImage, 0, 0, null);

            // 生成图片为PNG

            ImageIO.write(bufferedImage, "png", new File(newPath));
        } catch (Exception e) {
            logger.error("图片透明度处理失败");
        }

    }

    public static void main(String[] args) {
        setAlpha("D://2.jpg", "D://2.png");
    }

    /**
     * 获取图片的说明
     *
     * @param path 图片的路径
     * @return 图片说明
     * @throws IOException 读取图片异常时抛出
     */
    public static ImgMetadata getImgInfo(final String path) throws IOException {
        ImgMetadata imgMetadata = new ImgMetadata();
        File file = new File(path);
        BufferedImage image = ImageIO.read(file);
        imgMetadata.setHeight(image.getHeight());
        imgMetadata.setWidth(image.getWidth());
        imgMetadata.setName(file.getName());
        return imgMetadata;
    }

    @Data
    public static final class ImgMetadata {
        ImgMetadata() {
        }

        /*
         * 图片宽
         */
        private int width;
        /*
         * 图片高
         */
        private int height;
        /*
         * 图片文件名
         */
        private String name;
    }
}
