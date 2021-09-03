package com.example.cft_test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ValuteListAdapter extends RecyclerView.Adapter<ValuteListAdapter.ViewHolder> {

    private final List<String> valutes;
    private ItemClickListener itemClickListener;

    private final LayoutInflater inflater;
    public int selectedPos = RecyclerView.NO_POSITION;

    public ValuteListAdapter(Context context, List<String> valutes) {
        this.valutes = valutes;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ValuteListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.item_valute, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ValuteListAdapter.ViewHolder holder, int position) {
        holder.valuteTV.setText(valutes.get(position));

        holder.itemView.setSelected(selectedPos == position);
    }

    @Override
    public int getItemCount() {
        return valutes.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView valuteTV;

        public ViewHolder(View itemView) {
            super(itemView);
            valuteTV = itemView.findViewById(R.id.valute_tv);
            valuteTV.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null)
                itemClickListener.onItemClick(v, getAbsoluteAdapterPosition());
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
