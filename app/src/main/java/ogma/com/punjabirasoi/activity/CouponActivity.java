package ogma.com.punjabirasoi.activity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ogma.com.punjabirasoi.R;
import ogma.com.punjabirasoi.enums.URL;
import ogma.com.punjabirasoi.model.CouponModel;
import ogma.com.punjabirasoi.network.HttpClient;
import ogma.com.punjabirasoi.network.NetworkConnection;

public class CouponActivity extends AppCompatActivity {

    public static final String TOTAL_PAY_AMOUNT = "extra_total_amount";
    public static final String PHONE_NUMBER = "extra_phone_number";

    private NetworkConnection connection;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private String cart_amount = "";
    private RecyclerAdapter recyclerAdapter;
    public static ArrayList<CouponModel> couponList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;

    private JSONArray jArr = new JSONArray();
    private String phoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);

        if (getIntent().hasExtra(PHONE_NUMBER)) {
            phoneNumber = getIntent().getStringExtra(PHONE_NUMBER);
        }

        connection = new NetworkConnection(this);
        coordinatorLayout = findViewById(R.id.coordinator_layout);

        recyclerView = findViewById(R.id.rv_coupon);
        recyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);

        if (prepareExecuteAsync())
            new CouponActivity.FetchCouponTask().execute();

        if (getIntent().getStringExtra(TOTAL_PAY_AMOUNT) != null) {
            cart_amount = getIntent().getStringExtra(TOTAL_PAY_AMOUNT);
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coupon, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {


            holder.tvCouponName.setText(jArr.optJSONObject(position).optString("name"));
            holder.tvMinAmount.setText(jArr.optJSONObject(position).optString("min_amount"));

        }


        @Override
        public int getItemCount() {

            return jArr.length();
        }

        class ViewHolder extends RecyclerView.ViewHolder {


            TextView tvCouponName;
            TextView tvMinAmount;
            LinearLayout linearLayout;


            ViewHolder(View itemView) {
                super(itemView);
                tvCouponName = itemView.findViewById(R.id.tv_coupon_name);
                tvMinAmount = itemView.findViewById(R.id.tv_min_amount);
                linearLayout = itemView.findViewById(R.id.single_coupon);

                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getAdapterPosition();
                        String promocode = jArr.optJSONObject(position).optString("id");
                        if (prepareExecuteAsync())
                            new CouponActivity.FetchApplyCouponTask().execute(promocode, cart_amount);
                    }
                });

            }
        }
    }

    private class FetchCouponTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(CouponActivity.this);
        private JSONObject response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("PLease wait..");
            mDialog.setIndeterminate(true);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("restaurant_id", '1');

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.COUPON_LIST.getURL(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    jArr = response.getJSONArray("Coupon");
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

    private class FetchApplyCouponTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(CouponActivity.this);
        private JSONObject response;
        private String payableAmount = "", discountAmount = "";
        private String promoId = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("PLease wait..");
            mDialog.setIndeterminate(true);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                promoId = params[0];
                mJsonObject.put("promo_id", params[0]);
                mJsonObject.put("cart_amount", params[1]);
                mJsonObject.put("phone_number", phoneNumber);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.APPLY_COUPON.getURL(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    payableAmount = response.getString("payable_amount");
                    discountAmount = response.getString("discount_amount");
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
                Intent intent = new Intent();
                intent.putExtra("PAYABLE_AMOUNT", payableAmount);
                intent.putExtra("DISCOUNT_AMOUNT", discountAmount);
                intent.putExtra("PROMO_ID", promoId);
                setResult(RESULT_OK, intent);
                finish();
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
