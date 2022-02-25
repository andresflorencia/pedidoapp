package com.florencia.pedidossi.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.florencia.pedidossi.R;
import com.florencia.pedidossi.activities.CarritoActivity;
import com.florencia.pedidossi.activities.ProductoActivity;
import com.florencia.pedidossi.models.DetalleCarrito;
import com.florencia.pedidossi.models.Producto;
import com.florencia.pedidossi.utils.MyToast;
import com.florencia.pedidossi.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.internal.Util;

public class DetalleCarritoAdapter extends RecyclerView.Adapter<DetalleCarritoAdapter.DetalleViewHolder> {
    public static final String TAG = "TAG_ProductoADAPTER";
    List<DetalleCarrito> productos;
    CarritoActivity activity;

    public DetalleCarritoAdapter(CarritoActivity activity, List<DetalleCarrito> productos) {
        this.productos = productos;
        this.activity = activity;
    }

    @NonNull
    @Override
    public DetalleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DetalleViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cv_itemcarrito, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DetalleViewHolder holder, int position) {
        holder.bindProducto(productos.get(position));
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    class DetalleViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvInfo, tvPrecio, tvTotal, tvCantidad;
        ImageButton btnDelete;
        ImageView imageView;
        Button btnMas, btnMenos;

        DetalleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre);
            tvInfo = itemView.findViewById(R.id.tv_info);
            tvPrecio = itemView.findViewById(R.id.tv_precio);
            tvCantidad = itemView.findViewById(R.id.tv_cantidad);
            tvTotal = itemView.findViewById(R.id.tv_total);
            btnMas = itemView.findViewById(R.id.btnMas);
            btnMenos = itemView.findViewById(R.id.btnMenos);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            imageView = itemView.findViewById(R.id.image);
        }

        void bindProducto(final DetalleCarrito item) {
            tvNombre.setText(item.producto.nombreproducto.toUpperCase());
            tvInfo.setText("Info adicional");
            tvPrecio.setText(Utils.FormatoMoneda(item.precio, 2));
            tvCantidad.setText(item.cantidad.toString());
            tvTotal.setText(Utils.FormatoMoneda(item.total, 2));

            String image = item.producto.image_default;
            if (item.producto.images != null && item.producto.images.size() > 0)
                image = item.producto.images.get(0).url;

            Glide.with(itemView.getContext())
                    .load(image)
                    .centerCrop()
                    .placeholder(R.drawable.circle_loading)
                    .into(imageView);

            btnDelete.setOnClickListener(v -> {
                productos.remove(getAdapterPosition());
                notifyDataSetChanged();
                activity.Totales();
                MyToast.show(activity, R.string.item_delete, MyToast.SHORT, MyToast.ERROR);
            });

            btnMenos.setOnClickListener(v -> CambiaCantidad(false));

            btnMas.setOnClickListener(v -> CambiaCantidad(true));
        }

        void CambiaCantidad(boolean incrementar) {
            if (incrementar)
                productos.get(getAdapterPosition()).cantidad++;
            else if (productos.get(getAdapterPosition()).cantidad > 1)
                productos.get(getAdapterPosition()).cantidad--;
            CalculaTotal();
        }

        void CalculaTotal() {
            try {
                Double precio = productos.get(getAdapterPosition()).precio;
                Double cantidad = productos.get(getAdapterPosition()).cantidad;
                productos.get(getAdapterPosition()).total = precio * cantidad;
                notifyDataSetChanged();
                activity.Totales();
            } catch (Exception e) {

            }
        }
    }
}

