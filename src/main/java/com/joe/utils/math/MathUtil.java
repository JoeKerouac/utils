package com.joe.utils.math;

import java.util.*;

/**
 * @author joe
 * @version 2018.06.04 18:25
 */
public class MathUtil {
    private MathUtil() {

    }

    /**
     * 30度角的近似弧度值
     */
    public static final double ANGLE_30 = Math.PI / 6;
    /**
     * 45度角的近似弧度值
     */
    public static final double ANGLE_45 = Math.PI / 4;
    /**
     * 60度角的近似弧度值
     */
    public static final double ANGLE_60 = Math.PI / 3;
    /**
     * 90度角的近似弧度值
     */
    public static final double ANGLE_90 = Math.PI / 2;
    /**
     * 120度角的近似弧度值
     */
    public static final double ANGLE_120 = 2.0 / 3 * Math.PI;
    /**
     * 135度角的近似弧度值
     */
    public static final double ANGLE_135 = 3.0 / 4 * Math.PI;
    /**
     * 150度角的近似弧度值
     */
    public static final double ANGLE_150 = 5.0 / 6 * Math.PI;
    /**
     * 180度角的近似弧度值
     */
    public static final double ANGLE_180 = Math.PI;
    /**
     * 270度角的近似弧度值
     */
    public static final double ANGLE_270 = 3.0 / 2 * Math.PI;
    /**
     * 360度角的近似弧度值
     */
    public static final double ANGLE_360 = 2 * Math.PI;

    /**
     * 30度角的近似sin值
     */
    public static final double SIN_30 = 0.5;
    /**
     * 45度角的近似sin值
     */
    public static final double SIN_45 = Math.sqrt(2) / 2;
    /**
     * 60度角的近似sin值
     */
    public static final double SIN_60 = Math.sqrt(3) / 2;
    /**
     * 90度角的近似sin值
     */
    public static final double SIN_90 = 1;
    /**
     * 120度角的近似sin值
     */
    public static final double SIN_120 = SIN_60;
    /**
     * 135度角的近似sin值
     */
    public static final double SIN_135 = SIN_45;
    /**
     * 150度角的近似sin值
     */
    public static final double SIN_150 = SIN_30;
    /**
     * 180度角的近似sin值
     */
    public static final double SIN_180 = 0;
    /**
     * 270度角的近似sin值
     */
    public static final double SIN_270 = -1;
    /**
     * 360度角的近似sin值
     */
    public static final double SIN_360 = SIN_180;

    /**
     * 30度角的近似COS值
     */
    public static final double COS_30 = Math.sqrt(3) / 2;
    /**
     * 45度角的近似COS值
     */
    public static final double COS_45 = Math.sqrt(2) / 2;
    /**
     * 60度角的近似COS值
     */
    public static final double COS_60 = 1 / 2;
    /**
     * 90度角的近似COS值
     */
    public static final double COS_90 = 0;
    /**
     * 120度角的近似COS值
     */
    public static final double COS_120 = -COS_60;
    /**
     * 135度角的近似COS值
     */
    public static final double COS_135 = -COS_45;
    /**
     * 150度角的近似COS值
     */
    public static final double COS_150 = -COS_30;
    /**
     * 180度角的近似COS值
     */
    public static final double COS_180 = -1;
    /**
     * 270度角的近似COS值
     */
    public static final double COS_270 = 0;
    /**
     * 360度角的近似COS值
     */
    public static final double COS_360 = -COS_180;

    /**
     * 将坐标以原点为中心旋转
     *
     * @param point 坐标
     * @param angle 旋转弧度（注意：不是度数，是弧度），为正时表示逆时针旋转，为负时表示顺时针旋转，顺时针旋转不精确，尽量使用逆时针旋转，可以使用内置的弧度ANGLE_N。
     * @return 旋转后的坐标，不精确（弧度对应的角度是90、180、270、360时是精确的，尽量使用这些值）
     */
    public static Point spin(Point point, double angle) {
        return spin(Collections.singletonList(point), angle).get(0);
    }

    /**
     * 将坐标集合中的坐标以原点为中心旋转
     *
     * @param points 坐标集合
     * @param angle  旋转弧度（注意：不是度数，是弧度），为正时表示逆时针旋转，为负时表示顺时针旋转，顺时针旋转不精确，尽量使用逆时针旋转，可以使用内置的弧度ANGLE_N。
     * @return 旋转后的坐标，不精确（弧度对应的角度是90、180、270、360时是精确的，尽量使用这些值）
     */
    public static List<Point> spin(Point[] points, double angle) {
        return spin(Arrays.asList(points), angle);
    }

    /**
     * 将坐标集合中的坐标以原点为中心旋转
     *
     * @param points 坐标集合
     * @param angle  旋转弧度（注意：不是度数，是弧度），为正时表示逆时针旋转，为负时表示顺时针旋转，顺时针旋转不精确，尽量使用逆时针旋转，可以使用内置的弧度ANGLE_N。
     * @return 旋转后的坐标，不精确（弧度对应的角度是90、180、270、360时是精确的，尽量使用这些值）
     */
    public static List<Point> spin(Collection<Point> points, double angle) {
        double SIN, COS;

        //判断用户的值是否是内置的几个特殊角度对应的弧度值
        if (angle == ANGLE_30) {
            SIN = SIN_30;
            COS = COS_30;
        } else if (angle == ANGLE_45) {
            SIN = SIN_45;
            COS = COS_45;
        } else if (angle == ANGLE_60) {
            SIN = SIN_60;
            COS = COS_60;
        } else if (angle == ANGLE_90) {
            SIN = SIN_90;
            COS = COS_90;
        } else if (angle == ANGLE_120) {
            SIN = SIN_120;
            COS = COS_120;
        } else if (angle == ANGLE_135) {
            SIN = SIN_135;
            COS = COS_135;
        } else if (angle == ANGLE_150) {
            SIN = SIN_150;
            COS = COS_150;
        } else if (angle == ANGLE_180) {
            SIN = SIN_180;
            COS = COS_180;
        } else if (angle == ANGLE_270) {
            SIN = SIN_270;
            COS = COS_270;
        } else if (angle == ANGLE_360) {
            SIN = SIN_360;
            COS = COS_360;
        } else {
            SIN = Math.sin(angle);
            COS = Math.cos(angle);
        }

        List<Point> result = new ArrayList<>(points.size());
        points.parallelStream().forEach(point -> {
            double x = point.getX();
            double y = point.getY();
            //计算旋转后的新坐标
            double newX = x * COS - y * SIN;
            double newY = x * SIN - y * COS;
            result.add(new Point(newX, newY));
        });

        return result;
    }

    /**
     * 计算阶乘
     *
     * @param arg 要计算的参数，必须大于0
     * @return 阶乘计算结果（如果结果大于long可以表示的最大值将会出现不确定结果）
     */
    public static long factorial(int arg) {
        if (arg <= 0) {
            throw new IllegalArgumentException("参数必须大于0");
        }
        long result = 1;
        for (int i = arg; i > 0; i--) {
            result *= i;
        }

        return result;
    }
}
