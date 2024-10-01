package com.example.gastosya;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.GastoViewHolder> {

    private List<Gasto> gastos;

    public GastoAdapter(List<Gasto> gastos) {
        this.gastos = gastos;
    }

    @NonNull
    @Override
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gasto, parent, false);
        return new GastoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        Gasto gasto = gastos.get(position);
        holder.tvNombreGasto.setText(gasto.getNombre());
        holder.tvCantidadGasto.setText(String.format("$%.2f", gasto.getCantidad()));
    }

    @Override
    public int getItemCount() {
        return gastos.size();
    }

    public class GastoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreGasto, tvCantidadGasto;

        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreGasto = itemView.findViewById(R.id.tvNombreGasto);
            tvCantidadGasto = itemView.findViewById(R.id.tvCantidadGasto);
        }
    }
}
