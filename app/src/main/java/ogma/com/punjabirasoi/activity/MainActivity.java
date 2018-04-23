package ogma.com.punjabirasoi.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.afollestad.sectionedrecyclerview.SectionedViewHolder;

import ogma.com.punjabirasoi.R;
import ogma.com.punjabirasoi.enums.URL;
import ogma.com.punjabirasoi.model.CartItem;
import ogma.com.punjabirasoi.model.CategoryModel;
import ogma.com.punjabirasoi.network.HttpClient;
import ogma.com.punjabirasoi.network.NetworkConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static ogma.com.punjabirasoi.activity.CartActivity.cartList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final int REQ_CART = 121;

    //private App app;
    private android.view.Menu menu;
    private LinearLayoutManager linearLayoutManager;
    //    private RecyclerAdapter recyclerAdapter;
    private SectionedRecyclerAdapter sectionedRecyclerAdapter;
    private CoordinatorLayout coordinatorLayout;
    private NetworkConnection connection;
    private TextView tvTotalPrice;
    private TextView tvTotalItems;
    private View ViewCart;
//    private ImageLoader imageLoader;
//    private ArrayList<CategoryModel> categoryModels = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setTitle("Punjabi Rasoi");
        }


        connection = new NetworkConnection(this);
//        imageLoader = new UniversalImageLoaderFactory.Builder(this).getInstance().initForAdapter().build();

        coordinatorLayout = findViewById(R.id.coordinator_layout);

        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvTotalItems = findViewById(R.id.tv_total_items);
        ViewCart = findViewById(R.id.view_cart);

        RecyclerView recyclerView = findViewById(R.id.rv_category);

        recyclerView.setHasFixedSize(true);


//        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

//        recyclerAdapter = new RecyclerAdapter();
        sectionedRecyclerAdapter = new SectionedRecyclerAdapter();
        recyclerView.setAdapter(sectionedRecyclerAdapter);

        if (prepareExecuteAsync())
            new FetchCategoryTask().execute();

    }

    private boolean prepareExecuteAsync() {
        if (connection.isNetworkConnected()) {
            return true;
        } else if (connection.isNetworkConnectingOrConnected()) {
            Snackbar.make(coordinatorLayout, "Connection temporarily unavailable", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(coordinatorLayout, "You're offline", Snackbar.LENGTH_SHORT).show();
        }
        return false;
    }

    /*@Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_cart:
                startActivityForResult(new Intent(this, CartActivity.class),REQ_CART);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CART){
            if (resultCode == RESULT_OK){
                for (int i = 0; i < sectionedRecyclerAdapter.getArrayList().size(); i++) {
                    for (int j = 0; j < sectionedRecyclerAdapter.getArrayList().get(i).getMenuLists().size(); j++) {
                        sectionedRecyclerAdapter.getArrayList().get(i).getMenuLists().get(j).setQuantity(0);
                    }
                }
                sectionedRecyclerAdapter.notifyDataSetChanged();
                totalCartItem();
            }

        }
    }

    private class SectionedRecyclerAdapter extends SectionedRecyclerViewAdapter<SectionedRecyclerAdapter.ViewHolder> {


        private ArrayList<CategoryModel> arrayList = new ArrayList<>();

        private void addItem(CategoryModel categoryModel) {
            arrayList.add(categoryModel);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        private void removeItem(int position) {
            arrayList.remove(position);
            notifyItemRemoved(position);
            notifyItemChanged(position);
        }

        private CategoryModel getItem(int position) {
            return arrayList.get(position);
        }

        private ArrayList<CategoryModel> getArrayList() {
            return arrayList;
        }

        @Override
        public SectionedRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layout;
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    layout = R.layout.category_item;
                    break;
                case VIEW_TYPE_ITEM:
                    layout = R.layout.menu_item;
                    break;
                default:
                    throw new RuntimeException("Unknown view type in RecyclerView");
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

            return new SectionedRecyclerAdapter.ViewHolder(view, viewType);
        }

        @Override
        public int getSectionCount() {
            return arrayList.size();
        }

        @Override
        public int getItemCount(int section) {
            return arrayList.get(section).getMenuLists().size();
        }

        @Override
        public boolean showFooters() {
            return false;
        }

        @Override
        public void onBindHeaderViewHolder(ViewHolder holder, int section, boolean expanded) {
            holder.tvSectionTitle.setText(getItem(section).getCategoryName());
            holder.tvSectionSubTitle.setText(getItem(section).getCategoryDescription());

        }

        @Override
        public void onBindFooterViewHolder(ViewHolder holder, int section) {

        }

        @Override
        public void onBindViewHolder(SectionedRecyclerAdapter.ViewHolder holder, int section, int relativePosition, int absolutePosition) {

            holder.tvItemTitle.setText(getItem(section).getMenuLists().get(relativePosition).getMenuName());
            Resources res = getResources();
            String totalCartAmount = res.getString(R.string.rs, getItem(section).getMenuLists().get(relativePosition).getMenuPrice());
            holder.tvItemPrice.setText(totalCartAmount);

            int quantity = getItem(section).getMenuLists().get(relativePosition).getQuantity();
            holder.btnAddItem.setText(quantity > 0 ? String.valueOf(quantity) : "ADD");
            holder.btnAddItem.setClickable(quantity == 0);
            holder.btnAddQuantity.setVisibility(quantity > 0 ? View.VISIBLE : View.GONE);
            holder.btnRemoveQuantity.setVisibility(quantity > 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public int getItemViewType(int section, int relativePosition, int absolutePosition) {
            return super.getItemViewType(section, relativePosition, absolutePosition);
        }

        class ViewHolder extends SectionedViewHolder implements View.OnClickListener {

            //Header view
            private TextView tvSectionTitle;
            private TextView tvSectionSubTitle;
            private View layoutHeader;
            //Item view
            private TextView tvItemTitle;
            private TextView tvItemPrice;
            Button btnAddItem, btnAddQuantity, btnRemoveQuantity;


            private ViewHolder(View itemView, int viewType) {
                super(itemView);
                switch (viewType) {
                    case VIEW_TYPE_HEADER:
                        tvSectionTitle = itemView.findViewById(R.id.tv_category_name);
                        tvSectionSubTitle = itemView.findViewById(R.id.tv_category_description);
                        layoutHeader = itemView.findViewById(R.id.layout_header);
                        layoutHeader.setOnClickListener(this);
                        break;
                    case VIEW_TYPE_FOOTER:
                        break;
                    case VIEW_TYPE_ITEM:
                        tvItemTitle = itemView.findViewById(R.id.tv_menu_name);
                        tvItemPrice = itemView.findViewById(R.id.tv_menu_price);
                        btnAddItem = itemView.findViewById(R.id.btn_add_menu);
                        btnAddQuantity = itemView.findViewById(R.id.btn_add_quantity);
                        btnRemoveQuantity = itemView.findViewById(R.id.btn_remove_quantity);

                        btnAddItem.setOnClickListener(this);
                        btnAddQuantity.setOnClickListener(this);
                        btnRemoveQuantity.setOnClickListener(this);

                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onClick(View view) {
                switch (getItemViewType()) {
                    case VIEW_TYPE_HEADER:
                        if (view.getId() == layoutHeader.getId()) {
                            Log.e(TAG, "onClick at position: " + getAdapterPosition());
                            if (isSectionExpanded(getRelativePosition().section())) {
                                collapseSection(getRelativePosition().section());
                            } else {
                                expandSection(getRelativePosition().section());
                            }
                        }
                        break;
                    case VIEW_TYPE_FOOTER:
                        break;
                    case VIEW_TYPE_ITEM:
                        if (view.getId() == tvItemTitle.getId()) {
                            Log.e(TAG, "onClick at position: " + getAdapterPosition());
                        } else if (view.getId() == btnAddItem.getId()) {
                            int quantity = getItem(getRelativePosition().section()).getMenuLists().get(getRelativePosition().relativePos()).getQuantity();
                            quantity++;
                            getItem(getRelativePosition().section()).getMenuLists().get(getRelativePosition().relativePos()).setQuantity(quantity);
                            notifyItemChanged(getAdapterPosition());
                            addCartItem();
                        } else if (view.getId() == btnAddQuantity.getId()) {
                            int quantity = getItem(getRelativePosition().section()).getMenuLists().get(getRelativePosition().relativePos()).getQuantity();
                            quantity++;
                            getItem(getRelativePosition().section()).getMenuLists().get(getRelativePosition().relativePos()).setQuantity(quantity);
                            notifyItemChanged(getAdapterPosition());
                            addCartItem();
                        } else if (view.getId() == btnRemoveQuantity.getId()) {
                            int quantity = getItem(getRelativePosition().section()).getMenuLists().get(getRelativePosition().relativePos()).getQuantity();
                            quantity--;
                            if (quantity <= 0) {
                                quantity = 0;
                            }
                            getItem(getRelativePosition().section()).getMenuLists().get(getRelativePosition().relativePos()).setQuantity(quantity);
                            notifyItemChanged(getAdapterPosition());
                            addCartItem();
                        }

                        break;
                    default:
                        break;
                }
            }

            private void addCartItem() {
                String categoryId = getItem(getRelativePosition().section()).getCategoryId();
                String categoryName = getItem(getRelativePosition().section()).getCategoryName();

                String menuId = getItem(getRelativePosition().section()).getMenuLists().get(getRelativePosition().relativePos()).getMenuId();
                String menuName = getItem(getRelativePosition().section()).getMenuLists().get(getRelativePosition().relativePos()).getMenuName();
                String menuPrice = getItem(getRelativePosition().section()).getMenuLists().get(getRelativePosition().relativePos()).getMenuPrice();
                int menuQuantity = getItem(getRelativePosition().section()).getMenuLists().get(getRelativePosition().relativePos()).getQuantity();

                if(ViewCart.getVisibility() == View.GONE)
                {
                    ViewCart.setVisibility(View.VISIBLE);
                }
                int index = checkMenuItemExists(menuId);
                if (index >= 0) {
                    if (menuQuantity == 0) {
                        cartList.remove(index);
                    } else {
                        int totalPrice = (int) Float.parseFloat(menuPrice) * menuQuantity;
                        cartList.get(index).setQuantity(menuQuantity);
                        cartList.get(index).setTotalPrice(totalPrice);
                    }

                } else {
                    int totalPrice = (int) Float.parseFloat(menuPrice) * menuQuantity;
                    CartItem cartItem = new CartItem(categoryId, categoryName, menuId, menuName, menuQuantity, (int) Float.parseFloat(menuPrice), totalPrice);
                    cartList.add(cartItem);
                }
                totalCartItem();
            }

            private int checkMenuItemExists(String menuId) {
                for (int i = 0; i < cartList.size(); i++) {
                    if (cartList.get(i).getMenuId().equals(menuId)) {
                        return i;
                    }
                }
                return -1;
            }

        }



    }

    private void totalCartItem() {
        int totalQuantity = 0;
        int totalCartPrice = 0;

        for (int i = 0; i < cartList.size(); i++) {
            totalQuantity = totalQuantity + cartList.get(i).getQuantity();
            totalCartPrice = totalCartPrice + cartList.get(i).getTotalPrice();
        }

        Resources res = getResources();
        String totalCost = String.valueOf(totalCartPrice);
        String totalCartAmount = res.getString(R.string.rs, totalCost);
        tvTotalPrice.setText(totalCartAmount);
        String items = totalQuantity + " items";
        tvTotalItems.setText(items);
    }


    private class FetchCategoryTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
        private JSONObject response;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please wait..");
            mDialog.setIndeterminate(true);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("restaurant_id", '1');

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.CATEGORY_LIST.getURL(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {

                    JSONArray jArr = response.getJSONArray("Category");
                    for (int i = 0; i < jArr.length(); i++) {
                        String categoryId = jArr.getJSONObject(i).getString("id");
                        String categoryName = jArr.getJSONObject(i).getString("name");
                        String categoryDesc = jArr.getJSONObject(i).getString("description");
                        ArrayList<CategoryModel.MenuModel> menuModels = new ArrayList<>();

                        JSONArray menuJsonArr = jArr.getJSONObject(i).getJSONArray("Menu");
                        for (int j = 0; j < menuJsonArr.length(); j++) {
                            String menuId = menuJsonArr.getJSONObject(j).getString("id");
                            String menuName = menuJsonArr.getJSONObject(j).getString("name");
                            String menuPrice = menuJsonArr.getJSONObject(j).getString("price");
                            int menuQuantity = 0;
                            menuModels.add(new CategoryModel.MenuModel(menuId, menuName, menuPrice, menuQuantity));

                        }

                        sectionedRecyclerAdapter.addItem(new CategoryModel(categoryId, categoryName, categoryDesc, menuModels));

                    }
                }
                return status;
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                mDialog.dismiss();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            mDialog.dismiss();
            if (status) {
                Log.e(TAG, "onPostExecute: categories added");
                sectionedRecyclerAdapter.collapseAllSections();
            } else {
                try {
                    Snackbar.make(coordinatorLayout, response.getString("err_msg"), Snackbar.LENGTH_LONG).show();
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                    Snackbar.make(coordinatorLayout, error_msg, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }
}

