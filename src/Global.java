/**
 * global的具体输出信息，属性参考C0指导书
 */
public class Global {
    /* 是否为常量？非零值为真 */
    private Integer is_const;
    /* 数组的长度 */
    private Integer valueCount;
    /* 数组所有元素的无间隔排序 */
    private String valueItems;

    public Global(Integer is_const, Integer valueCount, String valueItems) {
        this.is_const = is_const;
        this.valueCount = valueCount;
        this.valueItems = valueItems;
    }

    public Global(Integer is_const) {
        this.is_const = is_const;
        this.valueCount = 0;
        this.valueItems = null;
    }

    public Integer getIs_const() {
        return is_const;
    }

    public void setIs_const(Integer is_const) {
        this.is_const = is_const;
    }

    public Integer getValueCount() {
        return valueCount;
    }

    public void setValueCount(Integer valueCount) {
        this.valueCount = valueCount;
    }

    public String getValueItems() {
        return valueItems;
    }

    public void setValueItems(String valueItems) {
        this.valueItems = valueItems;
    }
}
