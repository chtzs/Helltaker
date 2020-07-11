package cc.chenhaotian.helltaker.list;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import cc.chenhaotian.helltaker.HelltakerLoader;
import cc.chenhaotian.helltaker.R;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.MyViewHolder>{
    private String[] data;
    public interface OnAddListener{
        void OnAdd(String name);
    }
    private OnAddListener onAddListener;
    public CardListAdapter(String[] data){
        this.data = data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setData(data[position]);
    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    public void setOnAddListener(OnAddListener onAddListener){
        this.onAddListener = onAddListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        LinearLayout linearLayout;
        TextView textView;
        ImageView imageView;
        HelltakerLoader loader;
        String data = "Lucy";

        MyViewHolder(View itemView){
            super(itemView);
            cardView = itemView.findViewById(R.id.card);
            linearLayout = new LinearLayout(itemView.getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER);
            imageView = new ImageView(itemView.getContext());
            textView = new TextView(itemView.getContext());
            linearLayout.addView(imageView);
            linearLayout.addView(textView);
            cardView.addView(linearLayout);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onAddListener != null){
                        onAddListener.OnAdd(data);
                    }
                }
            });
        }

        /**
         * 设置妹子信息
         * @param data 名字
         */
        private void setData(String data){
            this.data = data;
            loader = new HelltakerLoader(data, itemView.getContext());
            loader.loadBitmap();
            imageView.setImageBitmap(loader.next());
            imageView.getLayoutParams().width = 200;
            imageView.getLayoutParams().height = 200;
            imageView.requestLayout();
            textView.setText(data);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
    }
}
