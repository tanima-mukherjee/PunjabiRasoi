package ogma.com.punjabirasoi.model;

/**
 * Created by pc on 4/16/2018.
 */

public class CouponModel {
    private String couponId;
    private String couponName;
    private int minAmount;


    public CouponModel(String couponId, String couponName, int minAmount) {
        this.couponId = couponId;
        this.couponName = couponName;
        this.minAmount = minAmount;

    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }
}
