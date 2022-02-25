package com.florencia.pedidossi.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.florencia.pedidossi.MainActivity;
import com.florencia.pedidossi.R;
import com.florencia.pedidossi.models.Empresa;

import java.util.ArrayList;
import java.util.List;

public class EmpresaAdapter extends RecyclerView.Adapter<EmpresaAdapter.EmpresaViewHolder> {
    public static final String TAG = "TAG_EMPRESAADAPTER";
    List<Empresa> empresas = new ArrayList<>();
    Activity activity;

    public EmpresaAdapter(Activity activity, List<Empresa> empresas) {
        this.empresas = empresas;
        this.activity = activity;
    }

    @NonNull
    @Override
    public EmpresaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EmpresaViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cv_empresa, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EmpresaViewHolder holder, int position) {
        holder.bindEmpresa(empresas.get(position));
    }

    @Override
    public int getItemCount() {
        return empresas.size();
    }

    class EmpresaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDireccion;
        ImageView imageView;

        EmpresaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre);
            tvDireccion = itemView.findViewById(R.id.tv_direccion);
            imageView = itemView.findViewById(R.id.image);
        }

        void bindEmpresa(final Empresa empresa) {
            tvNombre.setText(empresa.nombre.toUpperCase());
            tvDireccion.setText(empresa.direccion);
            itemView.setBackgroundResource(empresa.seleccionada ? R.drawable.bg_border_green : R.drawable.bg_white);

            if (empresa.urlimage != "") {
                Glide.with(itemView.getContext())
                        .load(empresa.urlimage)
                        .placeholder(R.drawable.circle_loading)
                        .into(imageView);
            }

            itemView.setOnClickListener(v -> {
                Empresa miC = empresas.get(getAdapterPosition());
                empresas.get(getAdapterPosition()).seleccionada = !empresas.get(getAdapterPosition()).seleccionada;

                boolean seleccionada = false;
                for (Empresa c : empresas) {
                    if (!c.rucempresa.equals(miC.rucempresa))
                        c.seleccionada = false;
                    if (c.seleccionada)
                        seleccionada = true;
                }
                notifyDataSetChanged();
                ((MainActivity) activity).btnContinuar.setVisibility(seleccionada ? View.VISIBLE : View.GONE);
            });
        }
    }
}
