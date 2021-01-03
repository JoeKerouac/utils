package com.joe.utils.common.unit;

import java.io.Serializable;
import java.math.BigDecimal;

import com.joe.utils.common.Assert;

import lombok.Getter;

/**
 * 带单位的值
 * <p>
 * <p>
 * 注意:如果对于大小相等，单位不等的两个值，需要用{@link #compareTo(ValueWithUnit)}比较而不能使用{@link #equals(Object)}比较，使用{@link #equals(Object)}比较将会返回false
 * </p>
 * <p>
 * <p>
 * 可以通过更改{@link #scale}和{@link #roundMode}来更改计算精度
 * </p>
 * <p>
 *
 * @author JoeKerouac
 * @version 2019年10月10日 10:11
 */
@Getter
public abstract class ValueWithUnit<UNIT extends UnitDefinition>
    implements Comparable<ValueWithUnit<UNIT>>, Serializable {

    private static final long serialVersionUID = -1840063225509183843L;

    /**
     * 值
     */
    protected BigDecimal value;

    /**
     * 单位
     */
    protected UNIT unit;

    /**
     * 计算浮点值时的精度
     */
    protected int scale = 30;

    /**
     * 计算浮点值的时候计算方式
     */
    protected int roundMode = BigDecimal.ROUND_HALF_EVEN;

    public ValueWithUnit(int value, UNIT unit) {
        this(new BigDecimal(value), unit);
    }

    public ValueWithUnit(long value, UNIT unit) {
        this(new BigDecimal(value), unit);
    }

    public ValueWithUnit(String value, UNIT unit) {
        this(new BigDecimal(value), unit);
    }

    public ValueWithUnit(BigDecimal value, UNIT unit) {
        Assert.notNull(unit);
        this.value = value == null ? BigDecimal.ZERO : value;
        this.unit = unit;
        this.value = this.value.setScale(scale, roundMode);
    }

    /**
     * 增加指定值，最终单位以当前值的单位一致
     * 
     * @param unitValue
     *            要增加的值
     * @param <T>
     *            值类型，必须与当前类型一致
     */
    public <T extends ValueWithUnit<UNIT>> void add(T unitValue) {
        Assert.notNull(unitValue);
        // 算出进制，最大支持小数点后50位，超过该值不支持
        BigDecimal radix =
            new BigDecimal(unitValue.unit.getRadix()).divide(new BigDecimal(this.unit.getRadix()), scale, roundMode);
        this.value = this.value.add(unitValue.value.multiply(radix)).setScale(scale, roundMode);
    }

    /**
     * 更改单位
     * 
     * @param unit
     *            要更改的单位
     */
    public void changeUnit(UNIT unit) {
        Assert.notNull(unit);
        // 单位一致无需更改
        if (unit.equals(this.unit)) {
            return;
        }
        BigDecimal radix =
            new BigDecimal(this.unit.getRadix()).divide(new BigDecimal(unit.getRadix()), scale, roundMode);
        this.value = this.value.multiply(radix).setScale(scale, roundMode);
        this.unit = unit;
    }

    /**
     * 获取intValue值，有可能不准确
     * 
     * @return int value
     */
    public int intValue() {
        return value.intValue();
    }

    /**
     * 获取longValue值，有可能不准确
     * 
     * @return long value
     */
    public long longValue() {
        return value.longValue();
    }

    /**
     * 获取doubleValue值，有可能不准确
     * 
     * @return double value
     */
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public String toString() {
        return "[" + value.stripTrailingZeros().toPlainString() + "," + unit.getCode() + "]";
    }

    @Override
    public int compareTo(ValueWithUnit<UNIT> o) {
        Assert.notNull(o);

        // 相等的直接返回
        if (o.equals(this)) {
            return 0;
        }

        // 取小
        UNIT unit = this.unit.getRadix() > o.unit.getRadix() ? o.unit : this.unit;
        // 换算为统一单位值比较
        BigDecimal thisValue = this.value.multiply(new BigDecimal(this.unit.getRadix() / unit.getRadix()));
        BigDecimal oValue = o.value.multiply(new BigDecimal(o.unit.getRadix() / unit.getRadix()));
        return thisValue.compareTo(oValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ValueWithUnit<?> that = (ValueWithUnit<?>)o;

        if (scale != that.scale)
            return false;
        if (roundMode != that.roundMode)
            return false;
        if (!value.equals(that.value))
            return false;
        return unit.equals(that.unit);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + unit.hashCode();
        result = 31 * result + scale;
        result = 31 * result + roundMode;
        return result;
    }
}
