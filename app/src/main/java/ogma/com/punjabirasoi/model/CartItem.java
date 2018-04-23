package ogma.com.punjabirasoi.model;

/**
 * Created by User on 14-02-2017.
 */

public class CartItem {


    private String categoryId;
    private String categoryName;
    private String menuId;
    private String menuName;
    private int quantity;
    private int price;
    private int totalPrice;

    public CartItem(String categoryId, String categoryName, String menuId, String menuName, int quantity, int price, int totalPrice) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.menuId = menuId;
        this.menuName = menuName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}

