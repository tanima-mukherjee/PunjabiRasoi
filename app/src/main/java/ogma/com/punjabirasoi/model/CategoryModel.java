package ogma.com.punjabirasoi.model;

import java.util.ArrayList;


/**
 * Created by lolo on 7/2/18.
 */

public class CategoryModel {
    private String categoryId;
    private String categoryName;
    private String categoryDescription;
    private ArrayList<MenuModel> menuLists;

    public CategoryModel(String categoryId, String categoryName, String categoryDescription, ArrayList<MenuModel> menuLists) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
        this.menuLists = menuLists;
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

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }

    public ArrayList<MenuModel> getMenuLists() {
        return menuLists;
    }

    public void setMenuLists(ArrayList<MenuModel> menuLists) {
        this.menuLists = menuLists;
    }


    public static class MenuModel {
        private String menuId;
        private String menuName;
        private String menuPrice;
        private int quantity;

        public MenuModel(String menuId, String menuName, String menuPrice, int quantity) {
            this.menuId = menuId;
            this.menuName = menuName;
            this.menuPrice = menuPrice;
            this.quantity = quantity;
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

        public String getMenuPrice() {
            return menuPrice;
        }

        public void setMenuPrice(String menuPrice) {
            this.menuPrice = menuPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }


}





