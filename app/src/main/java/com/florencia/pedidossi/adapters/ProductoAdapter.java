package com.florencia.pedidossi.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.florencia.pedidossi.R;
import com.florencia.pedidossi.activities.HomeActivity;
import com.florencia.pedidossi.activities.ProductoActivity;
import com.florencia.pedidossi.models.Carrito;
import com.florencia.pedidossi.models.DetalleCarrito;
import com.florencia.pedidossi.models.Producto;
import com.florencia.pedidossi.utils.Global;
import com.florencia.pedidossi.utils.MyToast;
import com.florencia.pedidossi.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {
    public static final String TAG = "TAG_ProductoADAPTER";
    List<Producto> productos = new ArrayList<>();
    HomeActivity activity;

    public ProductoAdapter(HomeActivity activity, List<Producto> productos) {
        this.productos = productos;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductoViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cv_producto, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        holder.bindProducto(productos.get(position));
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCodigo, tvCategoria, tvPrecio;
        ImageButton btnAdd;
        ImageView imageView;

        ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre);
            tvCodigo = itemView.findViewById(R.id.tv_codigo);
            tvCategoria = itemView.findViewById(R.id.tv_categoria);
            tvPrecio = itemView.findViewById(R.id.tv_precio);
            btnAdd = itemView.findViewById(R.id.btnAddCart);
            imageView = itemView.findViewById(R.id.image);
        }

        void bindProducto(final Producto producto) {
            tvNombre.setText(producto.nombreproducto.toUpperCase());
            tvCodigo.setText(producto.codigo);
            tvCategoria.setText(producto.categoria);
            tvPrecio.setText(Utils.FormatoMoneda(producto.Precio(), 2) + "/" + producto.abreunidad);

            String image = producto.image_default;
            if(producto.images != null && producto.images.size()>0)
                image = producto.images.get(0).url;
            Glide.with(itemView.getContext())
                    .load(image)
                    .centerCrop()
                    .placeholder(R.drawable.circle_loading)
                    .into(imageView);

            itemView.setOnClickListener(v -> {
                Intent i = new Intent(itemView.getContext(), ProductoActivity.class);
                i.putExtra("idproducto", productos.get(getAdapterPosition()).idproducto);
                i.putExtra("rucempresa", productos.get(getAdapterPosition()).rucempresa);
                itemView.getContext().startActivity(i);
            });
            btnAdd.setOnClickListener(v -> AddItemCart());
        }

        void AddItemCart() {
            try {
                if (Global.carrito == null)
                    Global.carrito = new Carrito();
                for (DetalleCarrito item : Global.carrito.detalle) {
                    if (item.producto.idproducto == productos.get(getAdapterPosition()).idproducto) {
                        MyToast.show(activity, R.string.item_added, MyToast.SHORT, MyToast.WARNING);
                        return;
                    }
                }
                DetalleCarrito newItem = new DetalleCarrito();
                newItem.producto = productos.get(getAdapterPosition());
                newItem.cantidad = 1d;
                newItem.precio = productos.get(getAdapterPosition()).Precio();
                newItem.Total();
                Global.carrito.detalle.add(newItem);
                MyToast.show(activity, R.string.item_add, MyToast.SHORT, MyToast.INFO);
                activity.lblCantCarrito.setText("" + Global.carrito.detalle.size());
            } catch (Exception e) {
                Log.d("TAGACT", e.getMessage());
            }
        }
    }
}

