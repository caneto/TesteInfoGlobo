package br.teste.edglobo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import br.teste.edglobo.adapters.ListAdapter;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Context context = this;
    DBHelper myDB;
    SwipeRefreshLayout swipeContainer;
    NestedScrollView scrollView;
    ShimmerLayout loaderView;
    LinearLayout componentContainer, errorView, emptyView;
    TextView errorTxt, emptyTxt;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navView;
    View navHeaderLayout;
    LinearLayout defaultNavView, loginNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_main);

        myDB = new DBHelper(context);

        /*Navigation Drawer*/
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        /*Navigation Drawer*/

        scrollView = findViewById(R.id.scrollView);
        componentContainer = findViewById(R.id.componentContainer);
        loaderView = findViewById(R.id.shimmer_layout);
        errorView = findViewById(R.id.error);
        errorTxt = findViewById(R.id.errorTxt);
        emptyView = findViewById(R.id.empty);
        emptyTxt = findViewById(R.id.emptyTxt);

        if (Extras.isConnected(this)) {
            new fetchJsonData().execute();
        } else {
            showError(getString(R.string.dashboard_no_internet_text));
        }

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showError(String msg) {
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }

    public void showEmpty(String msg) {
        emptyView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        emptyTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }

    public void hideError() {
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
            componentContainer.removeAllViews();
            hideError();
        }
        protected String doInBackground(String... args) {
            String response = Extras.excuteGet(Extras.URL());
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonAarry = new JSONArray(result);
                JSONObject jsonObj = new JSONObject(jsonAarry.getString(0));
                JSONArray conteudos = jsonObj.getJSONArray("conteudos");
                ArrayList dataList = new ArrayList<HashMap<String, String>>();

                for (int i = 0; i < conteudos.length(); i++) {
                    JSONObject componentObject = conteudos.getJSONObject(i);

                    if(componentObject.length() == 14) {
                        HashMap<String, String> map = new HashMap<String, String>();


                        if (componentObject.has("autores")) {
                            if(!componentObject.getJSONArray("autores").isNull(0)) {
                                map.put("autores", componentObject.getJSONArray("autores").getString(0));
                            }
                        }

                        map.put("texto", componentObject.optString("texto"));
                        map.put("subTitulo", componentObject.optString("subTitulo"));
                        map.put("atualizadoEm", componentObject.optString("atualizadoEm"));
                        map.put("id", componentObject.optString("id"));
                        map.put("publicadoEm", componentObject.optString("publicadoEm"));
                        map.put("tipo", componentObject.optString("tipo"));
                        map.put("titulo", componentObject.optString("titulo"));
                        map.put("url", componentObject.optString("url"));
                        map.put("urlOriginal", componentObject.optString("urlOriginal"));

                        // Abre o Objeto de Secão
                        JSONObject secaodados = componentObject.getJSONObject("secao");
                        map.put("secao_nome", secaodados.optString("nome"));
                        map.put("secao_url", secaodados.optString("url"));

                        if (componentObject.has("imagens")) {
                            if(componentObject.getJSONArray("imagens").length()!=0) {
                               JSONObject imagensdados = componentObject.getJSONArray("imagens").getJSONObject(0);
                               if (!imagensdados.isNull("autor")) {
                                  map.put("imagens_autor", imagensdados.optString("autor"));
                                  map.put("imagens_fonte", imagensdados.optString("fonte"));
                                  map.put("imagens_legenda", imagensdados.optString("legenda"));
                                  map.put("imagens_url", imagensdados.optString("url"));
                               }
                            }
                        }

                        dataList.add(map);
                    }


                }

                processList(dataList, "Noticias");

            } catch (Exception e) {
                e.printStackTrace();
                showError(getString(R.string.dashboard_other_error_text));
            }
        }
    }

    public void processList(final ArrayList<HashMap<String, String>> data, String headline) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.component_list, null);
        TextView headlineTextView = view.findViewById(R.id.headline);
        if (headline.trim().isEmpty()) {
            headlineTextView.setVisibility(View.GONE);
        } else {
            headlineTextView.setText(headline);
        }
        ListView listView = view.findViewById(R.id.listView);
        componentContainer.addView(view);

        ListAdapter adapter = new ListAdapter(context, myDB, data);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myIntent = new Intent(context, ArticleActivity.class);
                myIntent.putExtra("id", data.get(+position).get("id"));
                startActivity(myIntent);
            }
        });
        onPostProcess();
    }

    public void onPostProcess() {
        loaderView.stopShimmerAnimation();
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

}