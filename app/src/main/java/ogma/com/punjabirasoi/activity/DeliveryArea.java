package ogma.com.punjabirasoi.activity;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.TextView;
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.afollestad.sectionedrecyclerview.SectionedViewHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ogma.com.punjabirasoi.R;
import ogma.com.punjabirasoi.enums.URL;
import ogma.com.punjabirasoi.network.HttpClient;
import ogma.com.punjabirasoi.network.NetworkConnection;


public class DeliveryArea extends AppCompatActivity {

    private static final String TAG = DeliveryArea.class.getSimpleName();
    private CoordinatorLayout coordinatorLayout;
    private SectionedRecyclerAdapter sectionedRecyclerAdapter;
    private JSONArray jArr = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_area);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        sectionedRecyclerAdapter = new SectionedRecyclerAdapter();
        recyclerView.setAdapter(sectionedRecyclerAdapter);

        if (prepareExecuteAsync())
            new FetchAreaTask().execute();
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

    private class SectionedRecyclerAdapter extends SectionedRecyclerViewAdapter<SectionedRecyclerAdapter.ViewHolder> {

        @Override
        public SectionedRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layout;
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    layout = R.layout.section_header;
                    break;
                case VIEW_TYPE_ITEM:
                    layout = R.layout.section_child;
                    break;
                default:
                    throw new RuntimeException("Unknown view type in RecyclerView");
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

            return new SectionedRecyclerAdapter.ViewHolder(view, viewType);
        }


        @Override
        public int getSectionCount() {
            return jArr.length();
        }

        @Override
        public int getItemCount(int section) {
            return jArr.optJSONObject(section).optJSONArray("subarea").length();
        }

        @Override
        public void onBindHeaderViewHolder(SectionedRecyclerAdapter.ViewHolder holder, int section, boolean expanded) {
            holder.tvHeader.setText(jArr.optJSONObject(section).optString("name", ""));
        }

        @Override
        public void onBindFooterViewHolder(ViewHolder holder, int section) {

        }

        @Override
        public void onBindViewHolder(SectionedRecyclerAdapter.ViewHolder holder, int section, int relativePosition, int absolutePosition) {
            holder.tvChild.setText(jArr.optJSONObject(section).optJSONArray("subarea").optJSONObject(relativePosition).optString("subarea_name", ""));
        }

        @Override
        public int getItemViewType(int section, int relativePosition, int absolutePosition) {
            return super.getItemViewType(section, relativePosition, absolutePosition);
        }

        class ViewHolder extends SectionedViewHolder implements View.OnClickListener {

            //Header view
            private TextView tvHeader;
            //Item view
            private TextView tvChild;


            private ViewHolder(View itemView, int viewType) {
                super(itemView);
                switch (viewType) {
                    case VIEW_TYPE_HEADER:
                        tvHeader = (TextView) itemView.findViewById(R.id.tv_section_title);
                        break;
                    case VIEW_TYPE_ITEM:
                        tvChild = (TextView) itemView.findViewById(R.id.tv_section_child_title);
                        itemView.setOnClickListener(this);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onClick(View view) {
                if (view.getId() == itemView.getId()) {
                    Log.e(TAG, "onClick at position: " + getRelativePosition());
                    String subArea = jArr.optJSONObject(getRelativePosition().section()).optJSONArray("subarea").optJSONObject(getRelativePosition().relativePos()).optString("subarea_name", "");
                    Log.e(TAG, "onClick at : " + subArea);
                    setResult(RESULT_OK, new Intent().putExtra("sub_area", subArea));
                    onBackPressed();
                }
            }
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

    private class FetchAreaTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(DeliveryArea.this);
        private JSONObject response;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.AREA_LIST.getURL(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    jArr = response.getJSONArray("Area");
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
                sectionedRecyclerAdapter.notifyDataSetChanged();
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
