package ogma.com.punjabirasoi.enums;

/**
 * Created by sajjad on 11/08/15.
 */
public enum URL {


    CATEGORY_LIST("category_list"),
    MENU_LIST("menu_list"),
    PLACE_ORDER("add_order"),
    AREA_LIST("area"),
    APPLY_COUPON("apply_promo"),
    COUPON_LIST("coupon_list"),
    VERIFY_OTP("verify_otp"),
    PHONE_VERIFY("phone_number_verify");


    public String BASE_URL = "http://thepunjabirasoi.com/api/";


    public String mURL;

    URL(String mURL) {
        this.mURL = this.BASE_URL + mURL;
    }

    public String getURL() {
        return mURL;
    }

}
