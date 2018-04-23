package ogma.com.punjabirasoi.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.mukesh.OtpView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import ogma.com.punjabirasoi.R;
import ogma.com.punjabirasoi.enums.URL;
import ogma.com.punjabirasoi.model.CartItem;
import ogma.com.punjabirasoi.network.HttpClient;
import ogma.com.punjabirasoi.network.NetworkConnection;
import ogma.com.punjabirasoi.utility.UniversalImageLoaderFactory;


public class CartActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "category_id";
    public static final String EXTRA_NAME = "category_name";
    public static final int COUPON_REQUEST_ID = 425;
    private static final String TAG = Menu.class.getName();
    private android.view.Menu menu;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerAdapter recyclerAdapter;
    private CoordinatorLayout coordinatorLayout;
    private NetworkConnection connection;
    private String totalAmountToPay = " ", categoryName = "";
    private JSONArray jArr = new JSONArray();
    private ImageLoader imageLoader;
    private TextView totalItem;
    private TextView totalItemPrice;
    private TextView packingCharges;
    private TextView deliveryCharges;
    private TextView gst;
    private TextView toPay;
    private TextView discountAmount;
    private Button btnContinue;
    private Button btnApplyCoupon;
    private ViewSwitcher viewSwitcher;
    private String phoneNumber = "", promoId = "";
    public static ArrayList<CartItem> cartList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("Cart");
        }


        connection = new NetworkConnection(this);
        imageLoader = new UniversalImageLoaderFactory.Builder(this).getInstance().initForAdapter().build();

        btnContinue = findViewById(R.id.btn_continue);
        btnApplyCoupon = findViewById(R.id.btn_apply_coupon);
        totalItem = findViewById(R.id.tv_total_items);
        totalItemPrice = findViewById(R.id.tv_total_item_price);
        packingCharges = findViewById(R.id.tv_packing_charges);
        deliveryCharges = findViewById(R.id.tv_delivery_charge);
        gst = findViewById(R.id.tv_gst_charge);
        toPay = findViewById(R.id.tv_to_pay);
        discountAmount = findViewById(R.id.tv_discount_amount);
        viewSwitcher = findViewById(R.id.viewSwitcher);


        coordinatorLayout = findViewById(R.id.coordinator_layout);

        recyclerView = findViewById(R.id.rv_menu_list);

        recyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);

        if (cartList.size() > 0) {
            viewSwitcher.setDisplayedChild(1);
        } else {
            viewSwitcher.setDisplayedChild(0);
        }

        int totalQuantity = 0;
        int totalCartPrice = 0;

        for (int i = 0; i < cartList.size(); i++) {
            totalQuantity = totalQuantity + cartList.get(i).getQuantity();
            totalCartPrice = totalCartPrice + cartList.get(i).getTotalPrice();
        }

        Resources res = getResources();
        String totalCost = String.valueOf(totalCartPrice);
        String totalCartAmount = res.getString(R.string.rs, totalCost);
        totalItemPrice.setText(totalCartAmount);
        String items = "Item Total : " + totalQuantity;
        totalItem.setText(items);

        int parcelCharge = 0;
        parcelCharge = parcelCharge + (totalQuantity * 5);
        String parcelChargeStr = String.valueOf(parcelCharge);
        packingCharges.setText(res.getString(R.string.rs, parcelChargeStr));

        int total_gst = 0;
        total_gst = (totalCartPrice * 18) / 100;
        String gst_total = String.valueOf(total_gst);
        gst.setText(res.getString(R.string.rs, gst_total));

        int deliveryCharge = 20;
        String amountDelivery = String.valueOf(deliveryCharge);
        deliveryCharges.setText(res.getString(R.string.rs, amountDelivery));

        int grandTotal = totalCartPrice + parcelCharge + deliveryCharge + total_gst;
        totalAmountToPay = String.valueOf(grandTotal);
        toPay.setText(res.getString(R.string.rs, totalAmountToPay));


    }


    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.menu_action_empty_cart) {
            //Todo: Empty cart
            if (cartList.size() > 0)
                promptCart();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void promptCart() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Empty Cart");
        adb.setMessage("Do you want to empty your cart?");
        adb.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cartList.clear();
                setResult(RESULT_OK);
                viewSwitcher.setDisplayedChild(0);
                dialog.dismiss();
            }
        });
        adb.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.setCancelable(false);
        adb.show();
    }


    public void onClick(View view) {
        if (view.getId() == btnContinue.getId()) {
            Intent i = new Intent(CartActivity.this, OrderActivity.class);
            i.putExtra(OrderActivity.TOTAL_PAY_AMOUNT, totalAmountToPay);
            i.putExtra(OrderActivity.PROMO_ID, promoId);
            i.putExtra(OrderActivity.PHONE_NUMBER, phoneNumber);
            startActivity(i);
        }
        if (view.getId() == btnApplyCoupon.getId()) {
            showChangePhoneNumberDialog();
        }
    }

    public void showChangePhoneNumberDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog_phone_number, null);
        dialogBuilder.setView(dialogView);

        TextInputLayout tilPhone = dialogView.findViewById(R.id.til_phone);
        final EditText etPhone = dialogView.findViewById(R.id.et_phone);

        dialogBuilder.setTitle("Phone Number dialog");
        dialogBuilder.setMessage("Enter phone number below");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                if (etPhone.getText().toString().isEmpty() || etPhone.getText().toString().length() != 10) {
                    Toast.makeText(CartActivity.this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                } else if (prepareExecuteAsync())
                    new CheckPhoneNumberTask().execute(etPhone.getText().toString());
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showOtpDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog_otp, null);
        dialogBuilder.setView(dialogView);

        final OtpView otpView = dialogView.findViewById(R.id.otp_view);
        //tilPhone = dialogView.findViewById(R.id.til_phone);
        //etPhone =  dialogView.findViewById(R.id.et_phone);

        dialogBuilder.setTitle("OTP Dialog");
        dialogBuilder.setMessage("Enter OTP below");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (otpView.hasValidOTP() && prepareExecuteAsync())
                    new CheckOTPTask().execute(otpView.getOTP());
                else
                    Toast.makeText(CartActivity.this, "Enter valid otp", Toast.LENGTH_SHORT).show();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
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

    private class CheckPhoneNumberTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(CartActivity.this);
        private JSONObject response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Submitting phone number...");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("phone_number", params[0]);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.VERIFY_PHONE_NUMBER.getURL(), mJsonObject);

                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    phoneNumber = response.getString("phone");
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
                showOtpDialog();
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

    private class CheckOTPTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(CartActivity.this);
        private JSONObject response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Submitting phone number...");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("otp", params[0]);
                mJsonObject.put("phone_number", phoneNumber);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.VERIFY_OTP.getURL(), mJsonObject);

                boolean status = response != null && response.getInt("is_error") == 0;

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
                Intent j = new Intent(CartActivity.this, CouponActivity.class);
                j.putExtra(CouponActivity.TOTAL_PAY_AMOUNT, totalAmountToPay);
                j.putExtra(CouponActivity.PHONE_NUMBER, phoneNumber);
                startActivityForResult(j, COUPON_REQUEST_ID);
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


    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            holder.tvMenuName.setText(cartList.get(position).getMenuName());
            holder.tvCategoryName.setText(cartList.get(position).getCategoryName());
            holder.tvQuantity.setText(String.format(Locale.US, "%d", cartList.get(position).getQuantity()));
            Resources res = getResources();
//            holder.tvTotalPrice.setText(String.format(Locale.US,"%d", cartList.get(position).getTotalPrice()));
            holder.tvTotalPrice.setText(res.getString(R.string.rs, cartList.get(position).getTotalPrice() + ""));
            // holder.tvTotalPrice.setText(Menu.cartList.get(position).getTotalPrice() + "");
        }


        @Override
        public int getItemCount() {
            //return jArr.length();
            return cartList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView ivMenuIcon;
            TextView tvCategoryName;
            TextView tvMenuName;
            TextView tvQuantity;
            TextView tvTotalPrice;

            //  TextView tvMenuCategoryName;

            ViewHolder(View itemView) {
                super(itemView);
                ivMenuIcon = itemView.findViewById(R.id.iv_item_menu);
                tvMenuName = itemView.findViewById(R.id.tv_menu_name);
                tvCategoryName = itemView.findViewById(R.id.tv_category_name);
                tvQuantity = itemView.findViewById(R.id.tv_menu_quantity);
                tvTotalPrice = itemView.findViewById(R.id.tv_menu_price);

                //  tvMenuCategoryName = itemView.findViewById(R.id.tv_menu_category);


            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == COUPON_REQUEST_ID) {

            if (resultCode == RESULT_OK) {

                Resources res = getResources();
                String payable_amount = data.getStringExtra("PAYABLE_AMOUNT");
                String discount_amount = data.getStringExtra("DISCOUNT_AMOUNT");
                promoId = data.getStringExtra("PROMO_ID");

                discountAmount.setText(res.getString(R.string.rs, discount_amount));
                toPay.setText(res.getString(R.string.rs, payable_amount));
                totalAmountToPay = payable_amount;
                btnApplyCoupon.setEnabled(false);

            }
        }
    }


}



