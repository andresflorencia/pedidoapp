package com.florencia.pedidossi.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.florencia.pedidossi.R;
import com.florencia.pedidossi.activities.HistoryActivity;
import com.florencia.pedidossi.activities.InfoHistoryActivity;
import com.florencia.pedidossi.models.PedidoHistory;
import com.florencia.pedidossi.utils.Utils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    public static final String TAG = "TAG_EMPRESAADAPTER";
    List<PedidoHistory> pedidos = new ArrayList<>();
    Activity activity;

    public HistoryAdapter(Activity activity, List<PedidoHistory> pedidos) {
        this.pedidos = pedidos;
        this.activity = activity;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cv_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bindHistory(pedidos.get(position));
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmpresa, tvFechaPedido, tvNumItems, tvTotal, tvDireccion;
        Button btnView;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmpresa = itemView.findViewById(R.id.tvEmpresa);
            tvFechaPedido = itemView.findViewById(R.id.tvFechaPedido);
            tvNumItems = itemView.findViewById(R.id.tvNumItems);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            btnView = itemView.findViewById(R.id.btnView);
        }

        void bindHistory(final PedidoHistory pedido) {
            tvEmpresa.setText(pedido.aliasempresa.equals("") ? pedido.nombreempresa : pedido.aliasempresa);
            Utils.FormatHtml(tvFechaPedido, "<strong>Pedido: </strong> " + pedido.fechapedido);
            Utils.FormatHtml(tvNumItems, pedido.detalle.size() + (pedido.detalle.size() > 1 ? " ítems" : "ítem"));
            Utils.FormatHtml(tvTotal, "<strong>Total: </strong> " + Utils.FormatoMoneda(pedido.total, 2));
            Utils.FormatHtml(tvDireccion, "<strong>Entrega: </strong> " + pedido.direccion.toString());

            btnView.setOnClickListener(v -> {
                Intent i = new Intent(activity, InfoHistoryActivity.class);
                i.putExtra("pedido", new Gson().toJson(pedidos.get(getAdapterPosition()), PedidoHistory.class));
                activity.startActivity(i);
            });
        }
    }
}
