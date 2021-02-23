package br.teste.edglobo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import br.teste.edglobo.adapters.CategoriesAdapter;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class CategoriesActivity extends AppCompatActivity {

    Context context = this;
    ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
    SwipeRefreshLayout swipeContainer;
    NestedScrollView scrollView;
    ShimmerLayout loaderView;
    ListView listView;
    LinearLayout errorView, emptyView;
    TextView errorTxt, emptyTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_categories);

        swipeContainer = findViewById(R.id.swipeContainer);
        scrollView = findViewById(R.id.scrollView);
        listView = findViewById(R.id.listView);
        loaderView = findViewById(R.id.shimmer_layout);
        errorView = findViewById(R.id.error);
        errorTxt = findViewById(R.id.errorTxt);
        emptyView = findViewById(R.id.empty);
        emptyTxt = findViewById(R.id.emptyTxt);

        if(Extras.isConnected(this)){
            new fetchJsonData().execute();
        }else{
            showError(getString(R.string.category_no_internet_text));
        }

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Extras.isConnected(context)) {
                    new fetchJsonData().execute();
                } else {
                    showError(getString(R.string.category_no_internet_text));
                }
            }
        });

    }

    public void finishView(View v){
        finish();
    }

    public void showError(String msg)
    {
        swipeContainer.setRefreshing(false);
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }
    public void showEmpty(String msg)
    {
        swipeContainer.setRefreshing(false);
        emptyView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        emptyTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }
    public void hideError()
    {
        swipeContainer.setRefreshing(false);
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        loaderView.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        loaderView.startShimmerAnimation();
    }

    class fetchJsonData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideError();
            dataList.clear();
        }

        protected String doInBackground(String... args) {
            String response = Extras.excuteGet(Extras.URL());
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");
                if (status.contentEquals("false")){
                    showError(getString(R.string.category_other_error_text));
                } else if (status.contentEquals("empty")) {
                    showEmpty(getString(R.string.category_nothing_found_text));
                } else if (status.contentEquals("true")) {

                    JSONArray categories = jsonObj.getJSONArray("categories");
                    int color_pos = 0;
                    for (int i = 0; i < categories.length(); i++) {
                        JSONObject jsonObject = categories.getJSONObject(i);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("color_pos", color_pos+"");
                        map.put("cat_id", jsonObject.optString("cat_id"));
                        map.put("cat_name", jsonObject.optString("cat_name"));
                        dataList.add(map);
                        color_pos++;
                        if(color_pos > 5)
                        {
                            color_pos = 0;
                        }
                    }
                    CategoriesAdapter adapter = new CategoriesAdapter(context, dataList);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent i = new Intent(context, PostActivity.class);
                            i.putExtra("cat_id", dataList.get(+position).get("cat_id"));
                            i.putExtra("cat_name", dataList.get(+position).get("cat_name"));
                            startActivity(i);
                        }
                    });
                    onPostProcess();
                }else{
                    showError(getString(R.string.category_other_error_text));
                }
            }catch (Exception e){
                e.printStackTrace();
                showError(getString(R.string.category_other_error_text));
            }
        }
    }

    public void onPostProcess() {
        swipeContainer.setRefreshing(false);
        loaderView.stopShimmerAnimation();
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }
}
