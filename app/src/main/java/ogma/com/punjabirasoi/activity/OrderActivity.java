package ogma.com.punjabirasoi.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ogma.com.punjabirasoi.R;
import ogma.com.punjabirasoi.enums.URL;
import ogma.com.punjabirasoi.network.HttpClient;
import ogma.com.punjabirasoi.network.NetworkConnection;


public class OrderActivity extends AppCompatActivity {


    public static final String TOTAL_PAY_AMOUNT = "extra_total_amount";
    private static final int REQUEST_CODE_SUB_AREA = 130;

    private String totalAmount = "";

    private CoordinatorLayout coordinatorLayout;

    private TextInputLayout tilFirstName;
    private TextInputEditText etFirstName;

    private TextInputLayout tilLastName;
    private TextInputEditText etLastName;

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;

    private TextInputLayout tilPhone;
    private TextInputEditText etPhone;

    private TextInputLayout tilAddress;
    private TextInputEditText etAddress;

    private TextInputLayout tilArea;
    private TextInputEditText etArea;

    private TextInputLayout tilOrderNote;
    private TextInputEditText etOrderNote;

    private TextInputLayout tilPayment;
    private RadioGroup radioGroup;

    int radiochecked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        totalAmount = getIntent().getExtras().getString(TOTAL_PAY_AMOUNT);

       // Log.e("onCreate: ", list.size() + " " + totalAmount);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        tilFirstName = findViewById(R.id.til_first_name);
        etFirstName =  findViewById(R.id.et_first_name);

        tilLastName =  findViewById(R.id.til_last_name);
        etLastName =  findViewById(R.id.et_last_name);

        tilEmail = findViewById(R.id.til_email);
        etEmail =  findViewById(R.id.et_email);

        tilPhone = findViewById(R.id.til_phone);
        etPhone =  findViewById(R.id.et_phone);

        tilAddress = findViewById(R.id.til_address);
        etAddress =  findViewById(R.id.et_address);

        tilArea = findViewById(R.id.til_area);
        etArea = findViewById(R.id.et_area);

        tilOrderNote = findViewById(R.id.til_order_note);
        etOrderNote = findViewById(R.id.et_order_note);

        tilPayment = findViewById(R.id.til_payment);
        radioGroup = findViewById(R.id.rgrp_payment);


    }

    public void onClick(View view) {
        if (view.getId() == R.id.et_area) {
            startActivityForResult(new Intent(this, DeliveryArea.class), REQUEST_CODE_SUB_AREA);
        }else if (view.getId() == R.id.btn_place_order) {
            if (validate() && prepareExecuteAsync()) {
                new PlaceOrderTask().execute(totalAmount,
                        etFirstName.getText().toString().trim(),
                        etLastName.getText().toString().trim(),
                        etEmail.getText().toString().trim(),
                        etPhone.getText().toString().trim(),
                        etAddress.getText().toString().trim(),
                        etArea.getText().toString().trim(),
                        etOrderNote.getText().toString().trim()
                );
            }
        }
    }



    private boolean validate() {
        if (etFirstName.getText().toString().trim().length() == 0) {
            tilFirstName.setError("Please enter your first name");
            Snackbar.make(coordinatorLayout, "Please enter your first name", Snackbar.LENGTH_SHORT).show();
            return false;
        } else {
            tilFirstName.setErrorEnabled(false);
        }

        if (etLastName.getText().toString().trim().length() == 0) {
            tilLastName.setError("Please enter your name");
            Snackbar.make(coordinatorLayout, "Please enter your name", Snackbar.LENGTH_SHORT).show();
            return false;
        } else {
            tilLastName.setErrorEnabled(false);
        }

        if (etEmail.getText().toString().trim().length() == 0) {
            tilEmail.setError("Please enter your email");
            Snackbar.make(coordinatorLayout, "Please enter your email", Snackbar.LENGTH_SHORT).show();
            return false;
        } else if (!isValidEmail(etEmail.getText().toString())) {
            tilEmail.setError("Please enter valid email");
            Snackbar.make(coordinatorLayout, "Please enter valid email", Snackbar.LENGTH_SHORT).show();
            return false;
        } else {
            tilEmail.setErrorEnabled(false);
        }

        if (etPhone.getText().toString().trim().length() != 10) {
            tilPhone.setError("Please enter a valid phone number");
            Snackbar.make(coordinatorLayout, "Please enter a valid phone number", Snackbar.LENGTH_SHORT).show();
            return false;
        } else {
            tilPhone.setErrorEnabled(false);
        }

        if (etAddress.getText().toString().trim().length() == 0) {
            tilAddress.setError("Please enter a address");
            Snackbar.make(coordinatorLayout, "Please enter a address", Snackbar.LENGTH_SHORT).show();
            return false;
        } else {
            tilAddress.setErrorEnabled(false);
        }

        if (etArea.getText().toString().trim().length() == 0) {
            tilArea.setError("Please enter a area");
            Snackbar.make(coordinatorLayout, "Please enter a area", Snackbar.LENGTH_SHORT).show();
            return false;
        } else {
            tilArea.setErrorEnabled(false);
        }

        if (radioGroup.getCheckedRadioButtonId() == R.id.rbtn_paytm) {
            tilPayment.setError("Please select COD");
            Snackbar.make(coordinatorLayout, "Please select COD", Snackbar.LENGTH_SHORT).show();
            return false;
        } else {
            tilPayment.setErrorEnabled(false);

        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SUB_AREA && resultCode == RESULT_OK) {
            String subArea = data.getStringExtra("sub_area");
            if (subArea != null)
                etArea.setText(subArea);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean prepareExecuteAsync() {
        NetworkConnection connection = new NetworkConnection(this);
        if (connection.isNetworkConnected()) {
            return true;
        } else if (connection.isNetworkConnectingOrConnected()) {
            Snackbar.make(coordinatorLayout, "Connection temporarily unavailable", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(coordinatorLayout, "You're offline", Snackbar.LENGTH_SHORT).show();
        }
        return false;
    }

   /* private void emptyCart(String tableId) {
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        int orderId = databaseHandler.getOrderIdIfExists(tableId);
        if (orderId > 0) {
            databaseHandler.deleteOrder(orderId);
            databaseHandler.closeDB();
        }
    }*/

    private class PlaceOrderTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(OrderActivity.this);
        private JSONObject response;
        private String orderId = "", serverOrderId = "";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Placing order...");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("total_amount", params[0]);

                JSONObject jsonObjectForm = new JSONObject();
                jsonObjectForm.put("first_name", params[1]);
                jsonObjectForm.put("last_name", params[2]);
                jsonObjectForm.put("email", params[3]);
                jsonObjectForm.put("phone", params[4]);
                jsonObjectForm.put("address", params[5]);
                jsonObjectForm.put("area", params[6]);
                jsonObjectForm.put("order_note", params[7]);

                mJsonObject.put("address_details", jsonObjectForm);

                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < Menu.cartList.size(); i++) {
                    JSONObject object = new JSONObject();
                    object.put("menu_id", Menu.cartList.get(i).getMenuId());
                    object.put("menu_name", Menu.cartList.get(i).getMenuName());
                    object.put("menu_price", Menu.cartList.get(i).getPrice());
                    object.put("menu_quantity", Menu.cartList.get(i).getQuantity());
                    jsonArray.put(object);
                }

                mJsonObject.put("cart_list", jsonArray);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.PLACE_ORDER.getURL(), mJsonObject);

                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    serverOrderId = response.getString("order_id");
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

                new AlertDialog.Builder(OrderActivity.this)
                        .setView(R.layout.layout_order_placed_dialog)
                        .setPositiveButton("Home", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(OrderActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .setCancelable(false)
                        .show();
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

