package br.teste.edglobo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import br.teste.edglobo.ArticleActivity;
import br.teste.edglobo.DBHelper;
import br.teste.edglobo.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;

public class ListAdapter extends BaseAdapter {
    private Context context;
    DBHelper mydb;
    private ArrayList<HashMap<String, String>> dataList;

    public ListAdapter(Context c, DBHelper db, ArrayList<HashMap<String, String>> d) {
        context = c;
        dataList = d;
        mydb = db;
    }

    public int getCount() {
        return dataList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ListViewHolder holder = null;
        if (convertView == null) {
            holder = new ListViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_list, parent, false);
            holder.contentContainer = convertView.findViewById(R.id.contentContainer);
            holder.rowSecao = convertView.findViewById(R.id.row_secao);
            holder.rowTitulo = convertView.findViewById(R.id.row_titulo);
            holder.rowImagem = convertView.findViewById(R.id.row_imagem);
            convertView.setTag(holder);
        } else {
            holder = (ListViewHolder) convertView.getTag();
        }

        final HashMap<String, String> singleTask = dataList.get(position);
        holder.contentContainer.setId(position);
        holder.rowSecao.setId(position);
        holder.rowTitulo.setId(position);
        holder.rowImagem.setId(position);

        holder.contentContainer.setVisibility(View.VISIBLE);
        try {
            holder.rowSecao.setText(singleTask.get("secao_nome"));
            holder.rowTitulo.setText(singleTask.get("titulo"));

          //  holder.contentContainer.setOnClickListener(new View.OnClickListener() {
         //       @Override
         //       public void onClick(View v) {
        //            Intent myIntent = new Intent(context, ArticleActivity.class);
      //              myIntent.putExtra("id", dataList.get(position).get("id"));
    //                startActivity(myIntent);
  //              }
//            });

            Picasso.get()
                    .load(singleTask.get("imagens_url"))
                    .resize(300, 200)
                    .centerCrop()
                    .error(R.drawable.no_image)
                    .into(holder.rowImagem);
        } catch (Exception e) {}

        return convertView;
    }
}

class ListViewHolder {
    LinearLayout contentContainer;
    TextView rowSecao, rowTitulo;
    ImageView rowImagem;
}