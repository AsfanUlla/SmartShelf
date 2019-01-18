package ga.asfanulla.smartshelf;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by asfan on 1/29/18.
 */

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<HashMap<String, String>> itemList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView weight, timer;

        public MyViewHolder(View view) {
            super(view);
            weight = view.findViewById(R.id.t1);
            timer = view.findViewById(R.id.t2);
        }

    }


    public ItemAdapter(Context context, ArrayList<HashMap<String,String>> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final HashMap<String, String> map = itemList.get(position);
        holder.weight.setText(map.get("weight"));
        holder.timer.setText(map.get("stamp"));
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
