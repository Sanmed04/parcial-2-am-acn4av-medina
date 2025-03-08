package com.example.gastosya;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.GastoViewHolder> {

    private List<Gasto> gastos;
    private final Map<String, Integer> categoriaIconoMap;
    private final OnItemClickListener onItemClickListener;

    public GastoAdapter(List<Gasto> gastos, OnItemClickListener onItemClickListener) {
        this.gastos = gastos;
        this.onItemClickListener = onItemClickListener;

        categoriaIconoMap = new HashMap<>();
        categoriaIconoMap.put("Servicio", R.drawable.baseline_home_repair_service_24);
        categoriaIconoMap.put("Compra", R.drawable.baseline_shopping_bag_24);
        categoriaIconoMap.put("Transaccion", R.drawable.baseline_handshake_24);
        categoriaIconoMap.put("Alimentacion", R.drawable.baseline_fastfood_24);
        categoriaIconoMap.put("Salud", R.drawable.baseline_local_hospital_24);
        categoriaIconoMap.put("Entretenimiento", R.drawable.baseline_live_tv_24);
        categoriaIconoMap.put("Transporte", R.drawable.baseline_directions_transit_24);
        categoriaIconoMap.put("Vivienda", R.drawable.baseline_house_24);
        categoriaIconoMap.put("Educacion", R.drawable.baseline_school_24);
    }

    public void updateData(List<Gasto> nuevosGastos) {
        this.gastos = nuevosGastos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gasto, parent, false);
        return new GastoViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        Gasto gasto = gastos.get(position);
        holder.tvNombreGasto.setText(gasto.getNombre());
        holder.tvCantidadGasto.setText(String.format("$%.2f", gasto.getCantidad()));

        Integer icono = categoriaIconoMap.get(gasto.getCategoria());
        if (icono != null) {
            holder.imgCategoriaGasto.setImageResource(icono);
        } else {
            holder.imgCategoriaGasto.setImageResource(R.drawable.predeterminado);
        }

        holder.btnEliminarGasto.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onEliminarClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gastos.size();
    }

    public static class GastoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreGasto, tvCantidadGasto;
        ImageView imgCategoriaGasto;
        ImageButton btnEliminarGasto;

        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreGasto = itemView.findViewById(R.id.tvNombreGasto);
            tvCantidadGasto = itemView.findViewById(R.id.tvCantidadGasto);
            imgCategoriaGasto = itemView.findViewById(R.id.imgCategoriaGasto);
            btnEliminarGasto = itemView.findViewById(R.id.btnEliminarGasto);
        }
    }

    public interface OnItemClickListener {
        void onEliminarClick(int position);
    }
}
