package br.teste.edglobo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class ArticleActivity  extends AppCompatActivity {

    Context context = this;
    String Id = "";
    TextView toolbarTitle;
    SwipeRefreshLayout swipeContainer;
    NestedScrollView scrollView;
    ShimmerLayout loaderView;
    LinearLayout errorView, emptyView;
    TextView errorTxt, emptyTxt;
    ImageView articleThumbnail;
    CardView thumbnailContainer;
    TextView articleTitulo, articleAutor, articleData, articleSecaoNome, articleTexto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_article);

        toolbarTitle = findViewById(R.id.toolbar_title);

        Intent intent = getIntent();
        if (intent.hasExtra("id")) {
            Id = intent.getStringExtra("id");
        }else{
            finish();
        }

        swipeContainer = findViewById(R.id.swipeContainer);
        scrollView = findViewById(R.id.scrollView);
        loaderView = findViewById(R.id.shimmer_layout);
        errorView = findViewById(R.id.error);
        errorTxt = findViewById(R.id.errorTxt);
        emptyView = findViewById(R.id.empty);
        emptyTxt = findViewById(R.id.emptyTxt);
        articleThumbnail = findViewById(R.id.article_thumbnail);
        thumbnailContainer = findViewById(R.id.thumbnail_container);
        articleTexto = findViewById(R.id.article_texto);
        articleTitulo = findViewById(R.id.article_titulo);
        articleAutor = findViewById(R.id.article_autor);
        articleData = findViewById(R.id.article_data);
        articleSecaoNome = findViewById(R.id.article_secao_nome);

        if(Extras.isConnected(this)){
            new fetchJsonData().execute();
        }else{
            showError(getString(R.string.article_no_internet_text));
        }

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Extras.isConnected(context)) {
                    new fetchJsonData().execute();
                } else {
                    showError(getString(R.string.article_no_internet_text));
                }
            }
        });

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

                for (int i = 0; i < conteudos.length(); i++) {
                    JSONObject componentObject = conteudos.getJSONObject(i);

                    if(componentObject.optString("id").contentEquals(Id)) {

                    if(componentObject.length() == 14) {

                        if (componentObject.has("autores")) {
                            if (!componentObject.getJSONArray("autores").isNull(0)) {
                                String autores = componentObject.getJSONArray("autores").getString(0);
                                articleAutor.setText(autores);
                            }
                        }

                        String texto = componentObject.optString("texto");
                        articleTexto.setText(texto);

                        String subTitulo = componentObject.optString("subTitulo");

                        String titulo = componentObject.optString("titulo");
                        articleTitulo.setText(titulo);

                        String url = componentObject.optString("url");
                        String urlOriginal = componentObject.optString("urlOriginal");

                        // Abre o Objeto de Secão
                        JSONObject secaodados = componentObject.getJSONObject("secao");
                        String secao_nome = secaodados.optString("nome");
                        articleSecaoNome.setText(secao_nome);

                        String secao_url = secaodados.optString("url");

                        if (componentObject.has("imagens")) {
                            if (componentObject.getJSONArray("imagens").length() != 0) {
                                JSONObject imagensdados = componentObject.getJSONArray("imagens").getJSONObject(0);
                                if (!imagensdados.isNull("autor")) {
                                    String imagens_autor = imagensdados.optString("autor");
                                    String imagens_fonte = imagensdados.optString("fonte");
                                    String imagens_legenda = imagensdados.optString("legenda");
                                    //String imagens_url = imagensdados.optString("url");

                                    try{
                                        Picasso.get()
                                                .load(imagensdados.optString("url"))
                                                .resize(500, 300)
                                                .centerCrop()
                                                .into(articleThumbnail, new Callback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        thumbnailContainer.setVisibility(View.VISIBLE);
                                                    }
                                                    @Override
                                                    public void onError(Exception ex) {
                                                        thumbnailContainer.setVisibility(View.GONE);
                                                    }
                                                });
                                    }catch (Exception e){
                                        thumbnailContainer.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }
                        onPostProcess();

                    }
                    }



                }

            } catch (Exception e) {
                e.printStackTrace();
                showError(getString(R.string.dashboard_other_error_text));
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
