package ogma.com.punjabirasoi.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import ogma.com.punjabirasoi.R;
import ogma.com.punjabirasoi.enums.URL;
import ogma.com.punjabirasoi.model.CartItem;
import ogma.com.punjabirasoi.network.HttpClient;
import ogma.com.punjabirasoi.network.NetworkConnection;
import ogma.com.punjabirasoi.utility.UniversalImageLoaderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Menu extends AppCompatActivity {

    public static final String EXTRA_ID = "category_id";
    public static final String EXTRA_NAME = "category_name";
    private static final String TAG = Menu.class.getName();
    public static ArrayList<CartItem> cartList = new ArrayList<>();
    public static ArrayList<String> cartQuantities = new ArrayList<>();
    //private App app;
    private android.view.Menu menu;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private RecyclerAdapter recyclerAdapter;
    private CoordinatorLayout coordinatorLayout;
    private NetworkConnection connection;
    private String categoryId = "", categoryName = "";
    private JSONArray jArr = new JSONArray();
    private ImageLoader imageLoader;
    TextView tvMenuCategoryName;
    private Button addMenuBtn;
    TextView tvTotalItems;
    TextView tvTotalPrice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("Menu");
        }

        connection = new NetworkConnection(this);
        imageLoader = new UniversalImageLoaderFactory.Builder(this).getInstance().initForAdapter().build();

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        tvMenuCategoryName = findViewById(R.id.tv_menu_category);

        recyclerView = findViewById(R.id.rv_menu);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);

        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvTotalItems = findViewById(R.id.tv_total_items);

        if (getIntent().getStringExtra(EXTRA_ID) != null && getIntent().getStringExtra(EXTRA_NAME) != null) {
            categoryId = getIntent().getStringExtra(EXTRA_ID);
            categoryName = getIntent().getStringExtra(EXTRA_NAME);
            tvMenuCategoryName.setText(categoryName);
            if (prepareExecuteAsync())
                new FetchMenuTask().execute();
        } else {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "An internal error occurred", Snackbar.LENGTH_SHORT);
            snackbar.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    super.onDismissed(snackbar, event);
                    finish();
                }

                @Override
                public void onShown(Snackbar snackbar) {
                    super.onShown(snackbar);
                }
            });
            snackbar.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        totalCartItem();
        recyclerAdapter.notifyDataSetChanged();
    }

    public void showMenuQuantityDialog(final int position) {
        final NumberPicker numberPicker = new NumberPicker(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(numberPicker);
        numberPicker.setMaxValue(10);
        numberPicker.setMinValue(1);

        builder.setTitle("Quantity of food");
        builder.setMessage("Choose the no. of items");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                addCartItem(position, String.valueOf(numberPicker.getValue()));

                //  new ChangeOrderItemStatusTask().execute(orderId, orderStatus, String.valueOf(numberPicker.getValue()));
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_cart:
                startActivity(new Intent(Menu.this, CartActivity.class));
                break;
            default:
                break;
        }
    }


    private void addCartItem(int position, String quantity) {
        String menuId = jArr.optJSONObject(position).optString("id");
        String price = jArr.optJSONObject(position).optString("price");
        String menuName = jArr.optJSONObject(position).optString("name");
        String menuCategoryName = categoryName;

        cartQuantities.set(position, quantity);


        int index = checkMenuItemExists(menuId);
        if (index >= 0) {
            if (quantity.equals("0")) {
                cartList.remove(index);
            } else {
                int totalPrice = (int) Float.parseFloat(price) * Integer.parseInt(quantity);
//                cartList.get(index).setQuantity(quantity);
                cartList.get(index).setTotalPrice(totalPrice);
            }

        } else {
            int totalPrice = (int) Float.parseFloat(price) * Integer.parseInt(quantity);
//            CartItem cartItem = new CartItem(categoryId, menuId, menuName, menuCategoryName, quantity, price, totalPrice);
//            cartList.add(cartItem);
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

    private void totalCartItem() {
        int totalQuantity = 0;
        int totalCartPrice = 0;

        for (int i = 0; i < cartList.size(); i++) {
//            totalQuantity = totalQuantity + Integer.parseInt(cartList.get(i).getQuantity());
            totalCartPrice = totalCartPrice + cartList.get(i).getTotalPrice();
        }

        Resources res = getResources();
        String totalCost = String.valueOf(totalCartPrice);
        String totalCartAmount = res.getString(R.string.rs, totalCost);
        tvTotalPrice.setText(totalCartAmount);
        String items = totalQuantity + " items";
        tvTotalItems.setText(items);
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

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.menu_action_show_as_grid) {
            item.setVisible(false);
            menu.findItem(R.id.menu_action_show_as_list).setVisible(true);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(recyclerAdapter);
            return true;
        }

        if (item.getItemId() == R.id.menu_action_show_as_list) {
            item.setVisible(false);
            menu.findItem(R.id.menu_action_show_as_grid).setVisible(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(recyclerAdapter);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            /*try {
                imageLoader.displayImage(jArr.getJSONObject(position).getString("photo"),
              holder.ivMenuIcon,
              UniversalImageLoaderFactory.getDefaultOptions(R.drawable.loader));
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
            Resources res = getResources();
            String amount = res.getString(R.string.rs, jArr.optJSONObject(position).optString("price"));
            holder.tvMenuTitle.setText(jArr.optJSONObject(position).optString("name"));
            holder.tvMenuPrice.setText(amount);
            holder.tvCategoryName.setText(categoryName);
            int quantity = Integer.parseInt(cartQuantities.get(position));
            holder.btnAddItem.setText(quantity > 0 ? String.valueOf(quantity) : "ADD");
            holder.btnAddItem.setClickable(quantity == 0);
            holder.btnAddQuantity.setVisibility(quantity > 0 ? View.VISIBLE : View.GONE);
            holder.btnRemoveQuantity.setVisibility(quantity > 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public int getItemCount() {
            return jArr.length();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView ivMenuIcon;
            TextView tvMenuTitle;
            TextView tvMenuPrice;
            TextView tvCategoryName;
            Button btnAddItem, btnAddQuantity, btnRemoveQuantity;
            //  TextView tvMenuCategoryName;

            ViewHolder(View itemView) {
                super(itemView);
                ivMenuIcon = itemView.findViewById(R.id.iv_item_menu);
                tvMenuTitle = itemView.findViewById(R.id.tv_menu_item);
                tvMenuPrice = itemView.findViewById(R.id.tv_item_price);
                tvCategoryName = itemView.findViewById(R.id.tv_category_item);
                btnAddItem = itemView.findViewById(R.id.btn_add_menu);
                btnAddQuantity = itemView.findViewById(R.id.btn_add_quantity);
                btnRemoveQuantity = itemView.findViewById(R.id.btn_remove_quantity);
                //  tvMenuCategoryName = itemView.findViewById(R.id.tv_menu_category);
               /* itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getAdapterPosition();
                        Log.e(TAG, "onClick at position: " + position);
                        startActivity(new Intent(Menu.this, MenuDetails.class));

                    }
                });*/

                btnAddItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        showMenuQuantityDialog(getAdapterPosition());
                        int quantity = Integer.parseInt(cartQuantities.get(getAdapterPosition()));
                        quantity++;
                        addCartItem(getAdapterPosition(), String.valueOf(quantity));
                        notifyItemChanged(getAdapterPosition());
                    }
                });

                btnAddQuantity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int quantity = Integer.parseInt(cartQuantities.get(getAdapterPosition()));
                        quantity++;
                        addCartItem(getAdapterPosition(), String.valueOf(quantity));
                        notifyItemChanged(getAdapterPosition());
                    }
                });

                btnRemoveQuantity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int quantity = Integer.parseInt(cartQuantities.get(getAdapterPosition()));
                        quantity--;
                        if (quantity <= 0) {
                            quantity = 0;
                        }
                        addCartItem(getAdapterPosition(), String.valueOf(quantity));
                        notifyItemChanged(getAdapterPosition());
                    }
                });
            }
        }
    }

    private class FetchMenuTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(Menu.this);
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

                mJsonObject.put("parent_category_id", categoryId);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.MENU_LIST.getURL(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    jArr = response.getJSONArray("Menu");
                    cartQuantities.clear();
                    for (int i = 0; i < jArr.length(); i++) {
                        cartQuantities.add("0");
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
                recyclerAdapter.notifyDataSetChanged();
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


