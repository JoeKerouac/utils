package com.joe.utils.img;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * 图片工具类
 *
 * @author joe
 */
@Slf4j
public class ImgUtil {
    private ImgUtil() {
    }

    /**
     * 更改图片alpha值
     *
     * @param oldPath 图片本地路径
     * @param newPath 更改alpha值后的图片保存路径
     * @param alpha   要设置的alpha值
     * @throws IOException IOException
     */
    public static void changeAlpha(String oldPath, String newPath, byte alpha) throws IOException {
        changeAlpha(new FileInputStream(oldPath), new FileOutputStream(newPath), alpha);
    }

    /**
     * 更改图片alpha值
     *
     * @param oldPath 图片本地路径
     * @param newPath 更改alpha值后的图片保存路径
     * @param alpha   要设置的alpha值
     * @param filter  alpha filter
     * @throws IOException IOException
     */
    public static void changeAlpha(String oldPath, String newPath, byte alpha, AlphaFilter filter) throws IOException {
        changeAlpha(new FileInputStream(oldPath), new FileOutputStream(newPath), alpha, filter);
    }

    /**
     * 更改图片的alpha值
     *
     * @param srcInput   图像输入
     * @param destOutput 图像输出
     * @param alpha      要设置的alpha值
     * @throws IOException IOException
     */
    public static void changeAlpha(InputStream srcInput, OutputStream destOutput, byte alpha) throws IOException {
        changeAlpha(srcInput, destOutput, alpha, null);
    }

    /**
     * 更改alpha值
     *
     * @param srcInput   图像输入
     * @param destOutput 图像输出
     * @param alpha      要设置的alpha值
     * @param filter     alpha filter
     * @throws IOException IOException
     */
    public static void changeAlpha(InputStream srcInput, OutputStream destOutput, byte alpha, AlphaFilter filter)
            throws IOException {
        //加载图片
        log.debug("开始加载图片");
        BufferedImage old = ImageIO.read(srcInput);
        log.debug("图片加载完毕，开始改变图片的alpha值");

        //使用32位深带alpha通道模式
        BufferedImage bufferedImage = new BufferedImage(old.getWidth(), old.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        if (filter == null) {
            filter = (x, y, rgb) -> true;
        }

        // 循环每一个像素点，改变像素点的Alpha值
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                int rgb = old.getRGB(x, y);
                if (filter.filter(x, y, rgb)) {
                    //设置alpha值
                    rgb = (alpha << 24) | (rgb & 0x00ffffff);
                }
                bufferedImage.setRGB(x, y, rgb);
            }
        }
        log.debug("图片aphasia值更改完毕");
        // 生成图片为PNG
        ImageIO.write(bufferedImage, "png", destOutput);
    }

//    public static BufferedImage spin(BufferedImage image, double angle) {
//        int width = image.getWidth();
//        int height = image.getHeight();
//        BufferedImage bufferedImage = new BufferedImage(height, width, image.getType());
//
//        int x = width >> 1;
//        int y = height >> 1;
//
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                int rgb = image.getRGB(i, j);
//                Point point = MathUtil.spin(new Point(i - x, j - y), angle);
//                int newX = (int)Math.round(point.getX()) + x;
//                int newY = (int) Math.round(point.getY()) + y;
//                try {
//
//                    bufferedImage.setRGB(newX, newY, rgb);
//                } catch (Exception e) {
//
//                }
//            }
//        }
//
//        return bufferedImage;
//    }

//    public static void main(String[] args) throws IOException{
//        BufferedImage old = ImageIO.read(new FileInputStream("D:\\2.jpg"));
//        BufferedImage image = spin(old, MathUtil.ANGLE_90);
//        ImageIO.write(image, "jpg", new FileOutputStream("D:\\3.jpg"));
//    }

    /**
     * 获取图片的说明
     *
     * @param path 图片的路径
     * @return 图片说明
     * @throws IOException IOException
     */
    public static ImgMetadata getImgInfo(final String path) throws IOException {
        return getImgInfo(ImageIO.read(new File(path)));
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
        imgMetadata.setAlpha(image.getColorModel().hasAlpha());
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

        BufferedImage dest = new BufferedImage(width, height, src.getType());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = src.getRGB(x * widthScale, y * heightScale);
                dest.setRGB(x, y, rgb);
            }
        }

        return dest;
    }

    /**
     * 复制image
     *
     * @param image image源
     * @return 复制后的image，与原图一模一样
     */
    public static BufferedImage copy(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        return copy(image, newImage);
    }

    /**
     * 将一个image信息复制到另一个image中
     *
     * @param src  源
     * @param dest 目标
     * @return dest
     */
    public static BufferedImage copy(BufferedImage src, BufferedImage dest) {
        dest.getGraphics().drawImage(src, 0, 0, null);
        return dest;
    }

    /**
     * alpha filter，设置alpha值的时候使用
     */
    public interface AlphaFilter {
        /**
         * 根据结果判断更改alpha值
         *
         * @param x   像素x坐标
         * @param y   像素y坐标
         * @param rgb x、y坐标对应的rgb值
         * @return 返回true表示需要更改，返回false表示不需要更改
         */
        boolean filter(int x, int y, int rgb);
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
        /**
         * 是否有alpha通道
         */
        private boolean alpha;
    }
}
