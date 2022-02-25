package com.florencia.pedidossi.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.florencia.pedidossi.R;
import com.florencia.pedidossi.activities.ProfileActivity;
import com.florencia.pedidossi.interfaces.IUsuario;
import com.florencia.pedidossi.models.Direccion;
import com.florencia.pedidossi.utils.Global;
import com.florencia.pedidossi.utils.MyToast;
import com.florencia.pedidossi.utils.Utils;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.florencia.pedidossi.utils.MyToast.SHORT;

public class DireccionAdapter extends RecyclerView.Adapter<DireccionAdapter.DireccionViewHolder> {
    public static final String TAG = "TAG_EMPRESAADAPTER";
    List<Direccion> direcciones = new ArrayList<>();
    Activity activity;
    Retrofit retrofit;

    public DireccionAdapter(Activity activity, List<Direccion> direcciones, Retrofit retrofit) {
        this.direcciones = direcciones;
        this.activity = activity;
        this.retrofit = retrofit;
    }

    @NonNull
    @Override
    public DireccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DireccionViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cv_direccion, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DireccionViewHolder holder, int position) {
        holder.bind(direcciones.get(position));
    }

    @Override
    public int getItemCount() {
        return direcciones.size();
    }

    class DireccionViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumero, tvDireccion;
        ImageButton btnFavorita, btnDelete;

        DireccionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumero = itemView.findViewById(R.id.tv_numero);
            tvDireccion = itemView.findViewById(R.id.tv_direccion);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnFavorita = itemView.findViewById(R.id.btnFavorito);
        }

        void bind(final Direccion direccion) {
            tvNumero.setText("Dirección: " + (getAdapterPosition() + 1));
            tvDireccion.setText(direccion.calle_principal + ", " + direccion.calle_secundaria + " - " + direccion.ciudad);

            btnFavorita.setImageResource(direccion.favorita ? R.drawable.ic_star : R.drawable.ic_star_border);

            btnFavorita.setOnClickListener(v -> {
                if (!direcciones.get(getAdapterPosition()).favorita)
                    ChangeFavorite();
            });

            btnDelete.setOnClickListener(v -> DeleteDirection());

            itemView.setOnClickListener(v -> ((ProfileActivity) activity).newDireccion(getAdapterPosition()));
        }

        void ChangeFavorite() {
            IUsuario iUsuario = retrofit.create(IUsuario.class);
            Call<JsonObject> call = iUsuario.setDirectionFavorite(direcciones.get(getAdapterPosition()).iddireccion);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!response.isSuccessful()) {
                        Utils.showMessage(activity, "Error: " + response.code());
                        return;
                    }
                    try {
                        if (response.body() != null) {
                            JsonObject obj = response.body();
                            if (obj.get("error").getAsBoolean())
                                MyToast.show(activity, obj.get("message").getAsString(), MyToast.LONG, MyToast.ERROR);
                            else {
                                boolean res = obj.get("data").getAsBoolean();
                                if (res) {
                                    for (Direccion c : direcciones)
                                        c.favorita = false;
                                    direcciones.get(getAdapterPosition()).favorita = true;
                                    notifyDataSetChanged();
                                    Global.usuario.GuardarSesionLocal(activity);
                                    MyToast.show(activity, "Dirección marcada como favorita.", SHORT, MyToast.SUCCESS);
                                } else
                                    MyToast.show(activity, "Error al establecer la dirección.", MyToast.LONG, MyToast.ERROR);
                            }
                        } else
                            Utils.showMessage(activity, "el body es null");
                    } catch (Exception ex) {
                        Log.d("TAGACTI", ex.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("TAGACT", t.getMessage());
                }
            });
        }

        void DeleteDirection() {
            IUsuario iUsuario = retrofit.create(IUsuario.class);
            Call<JsonObject> call = iUsuario.DeleteDirection(direcciones.get(getAdapterPosition()).iddireccion);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!response.isSuccessful()) {
                        Utils.showMessage(activity, "Error: " + response.code());
                        return;
                    }
                    try {
                        if (response.body() != null) {
                            JsonObject obj = response.body();
                            if (obj.get("error").getAsBoolean())
                                MyToast.show(activity, obj.get("message").getAsString(), MyToast.LONG, MyToast.ERROR);
                            else {
                                boolean res = obj.get("data").getAsBoolean();
                                if (res) {
                                    direcciones.remove(getAdapterPosition());
                                    notifyDataSetChanged();
                                    Global.usuario.GuardarSesionLocal(activity);
                                    MyToast.show(activity, "Dirección eliminada correctamente.", SHORT, MyToast.SUCCESS);
                                } else
                                    MyToast.show(activity, "Error al eliminar la direccion.", SHORT, MyToast.ERROR);
                            }
                        } else
                            Utils.showMessage(activity, "el body es null");
                    } catch (Exception ex) {
                        Log.d("TAGACTI", ex.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("TAGACT", t.getMessage());
                }
            });
        }
    }
}
