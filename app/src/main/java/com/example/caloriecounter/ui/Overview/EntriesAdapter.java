package com.example.caloriecounter.ui.Overview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecounter.R;
import com.example.caloriecounter.data.models.CalorieEntry;

import java.util.Locale;

public class EntriesAdapter extends ListAdapter<CalorieEntry, EntriesAdapter.VH> {

    public EntriesAdapter() {
        super(DIFF_CALLBACK);
    }


    private static final DiffUtil.ItemCallback<CalorieEntry> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CalorieEntry>() {
                @Override
                public boolean areItemsTheSame(@NonNull CalorieEntry oldItem, @NonNull CalorieEntry newItem) {
                    return oldItem.getName().equals(newItem.getName());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CalorieEntry oldItem, @NonNull CalorieEntry newItem) {
                    return oldItem.getQuantity() == newItem.getQuantity() &&
                            oldItem.getCaloriesPerUnit() == newItem.getCaloriesPerUnit();
                }
            };

    public static class VH extends RecyclerView.ViewHolder {
        final TextView name, qty, perUnit, total;

        public VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvEntryName);
            qty = itemView.findViewById(R.id.tvQuantity);
            perUnit = itemView.findViewById(R.id.tvCaloriesPerUnit);
            total = itemView.findViewById(R.id.tvTotalForItem);
        }
    }

    private String formatNumber(double value) {
        if (value == (long) value)
            return String.format(Locale.getDefault(), "%d", (long) value);
        else
            return String.format(Locale.getDefault(), "%.2f", value);
    }
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CalorieEntry e = getItem(position);
        Context context = holder.itemView.getContext();

        holder.qty.setText(context.getString(R.string.entry_quantity,
                formatNumber(e.getQuantity())));

        holder.perUnit.setText(context.getString(R.string.entry_calories_per_unit,
                formatNumber(e.getCaloriesPerUnit())));

        holder.total.setText(context.getString(R.string.entry_total,
                formatNumber(e.getTotalCalories())));
        holder.name.setText(e.getName());
    }
}