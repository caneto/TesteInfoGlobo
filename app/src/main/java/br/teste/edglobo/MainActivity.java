package br.teste.edglobo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import br.teste.edglobo.adapters.CarouselAdapter;
import br.teste.edglobo.adapters.CarouselCatAdapter;
import br.teste.edglobo.adapters.ListAdapter;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Context context = this;
    DBHelper myDB;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
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
    ImageView userImage;
    TextView userName, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_main);

        myDB = new DBHelper(context);

        sharedpreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        /*Navigation Drawer*/
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        /*Navigation Drawer*/

        swipeContainer = findViewById(R.id.swipeContainer);
        scrollView = findViewById(R.id.scrollView);
        componentContainer = findViewById(R.id.componentContainer);
        loaderView = findViewById(R.id.shimmer_layout);
        errorView = findViewById(R.id.error);
        errorTxt = findViewById(R.id.errorTxt);
        emptyView = findViewById(R.id.empty);
        emptyTxt = findViewById(R.id.emptyTxt);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Extras.isConnected(context)) {
                    new fetchJsonData().execute();
                } else {
                    showError(getString(R.string.dashboard_no_internet_text));
                }
            }
        });

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
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.nav_categories:
                startActivity(new Intent(context, CategoriesActivity.class));
                break;
            case R.id.nav_bookmarks:
                startActivity(new Intent(context, BookmarksActivity.class));
                break;
            default:
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void searchView(View v){
        startActivity(new Intent(context, SearchActivity.class));
    }

    public void showError(String msg) {
        swipeContainer.setRefreshing(false);
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }

    public void showEmpty(String msg) {
        swipeContainer.setRefreshing(false);
        emptyView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        emptyTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }

    public void hideError() {
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
                JSONObject jsonObj = new JSONObject(jsonAarry.getString(0)); //getString("conteudos"));
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

    public void populatePagesIntoMenu(JSONArray pages)
    {
        Menu menu = navView.getMenu();
        menu.add(0, 1, 1, getString(R.string.category_toolbar_text))//"Categories")
                .setIcon(R.drawable.ic_category)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        startActivity(new Intent(context, CategoriesActivity.class));
                        return false;
                    }
                });

        menu.add(0, 2, 2, getString(R.string.favoritos)) //"Bookmarks")
                .setIcon(R.drawable.ic_bookmarks)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        startActivity(new Intent(context, BookmarksActivity.class));
                        return false;
                    }
                });
        try {
            for (int i = 0; i < pages.length(); i++) {
                final JSONObject pageObject = pages.getJSONObject(i);
                menu.add(1, i+3, i+3, pageObject.optString("page_title"))
                        .setIcon(R.drawable.ic_pages)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent myIntent = new Intent(context, PageActivity.class);
                                myIntent.putExtra("page_id", pageObject.optString("page_id"));
                                myIntent.putExtra("page_title", pageObject.optString("page_title"));
                                startActivity(myIntent);
                                return false;
                            }
                        });
            }
        }catch (Exception e){}
    }

    public void processCarousel(ArrayList<HashMap<String, String>> data, String headline) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.component_carousel, null);
        TextView headlineTextView = view.findViewById(R.id.headline);
        if (headline.trim().isEmpty()) {
            headlineTextView.setVisibility(View.GONE);
        } else {
            headlineTextView.setText(headline);
        }
        RecyclerView listView = view.findViewById(R.id.listView);
        componentContainer.addView(view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        listView.setLayoutManager(layoutManager);
        CarouselAdapter adapter = new CarouselAdapter(context, myDB, data);
        listView.setAdapter(adapter);
        onPostProcess();
    }

    public void processCat(ArrayList<HashMap<String, String>> data, String headline) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.component_cat, null);
        TextView headlineTextView = view.findViewById(R.id.headline);
        if (headline.trim().isEmpty()) {
            headlineTextView.setVisibility(View.GONE);
        } else {
            headlineTextView.setText(headline);
        }
        RecyclerView listView = view.findViewById(R.id.listView);
        componentContainer.addView(view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        listView.setLayoutManager(layoutManager);
        CarouselCatAdapter adapter = new CarouselCatAdapter(context, data);
        listView.setAdapter(adapter);
        onPostProcess();
    }

    public void processCustomHTML(String data, String headline) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.component_customhtml, null);
        TextView headlineTextView = view.findViewById(R.id.headline);
        if (headline.trim().isEmpty()) {
            headlineTextView.setVisibility(View.GONE);
        } else {
            headlineTextView.setText(headline);
        }
        WebView webView = view.findViewById(R.id.webView);
        componentContainer.addView(view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setVerticalScrollBarEnabled(false);
        webView.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
        onPostProcess();
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
                myIntent.putExtra("post_id", data.get(+position).get("post_id"));
                startActivity(myIntent);
            }
        });
        onPostProcess();
    }

    public void onPostProcess() {
        swipeContainer.setRefreshing(false);
        loaderView.stopShimmerAnimation();
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

}