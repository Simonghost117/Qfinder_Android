package com.sena.qfinder.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sena.qfinder.R;
import com.sena.qfinder.data.api.AuthService;
import com.sena.qfinder.data.api.ApiClient;
import com.sena.qfinder.data.models.RedListResponse;
import com.sena.qfinder.data.models.RedRequest;
import com.sena.qfinder.data.models.RedResponse;
import com.sena.qfinder.ui.chat.ChatComunidad;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Comunidad extends Fragment {

    private static final String TAG = "ComunidadFragment";
    private RecyclerView recyclerView;
    private ComunidadAdapter adapter;
    private List<RedResponse> listaRedes;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;

    public Comunidad() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inicializando fragmento");
        View view = inflater.inflate(R.layout.fragment_comunidad, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        Log.d(TAG, "SharedPreferences inicializado");

        recyclerView = view.findViewById(R.id.recyclerComunidades);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = view.findViewById(R.id.progressBar);
        Log.d(TAG, "Vistas inicializadas");

        listaRedes = new ArrayList<>();
        adapter = new ComunidadAdapter(getContext(), listaRedes, getParentFragmentManager());
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "Adapter configurado");

        EditText buscador = view.findViewById(R.id.buscar_comunidad);
        buscador.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "Texto buscado: " + s.toString());
                adapter.filtrar(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        cargarRedes();

        return view;
    }

    private void cargarRedes() {
        Log.d(TAG, "Iniciando carga de redes");
        String token = sharedPreferences.getString("token", null);
        if (token == null) {
            Log.e(TAG, "Token no encontrado en SharedPreferences");
            Toast.makeText(getContext(), "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Mostrando ProgressBar");

        AuthService authService = ApiClient.getClient().create(AuthService.class);
        Call<RedListResponse> call = authService.listarRedes("Bearer " + token);
        Log.d(TAG, "Realizando llamada a listarRedes");

        call.enqueue(new Callback<RedListResponse>() {
            @Override
            public void onResponse(Call<RedListResponse> call, Response<RedListResponse> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Respuesta recibida. Código: " + response.code());

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d(TAG, "Respuesta completa: " + response.body().toString());

                        if (response.body().isSuccess()) {
                            Log.d(TAG, "Respuesta exitosa. Redes recibidas: " + response.body().getData().size());
                            listaRedes.clear();
                            listaRedes.addAll(response.body().getData());
                            adapter.actualizarListaCompleta(listaRedes);
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "Datos actualizados en el adapter");
                        } else {
                            Log.e(TAG, "Success: false en la respuesta");
                            Log.e(TAG, "Mensaje del servidor: " + response.body().getMessage());
                            Toast.makeText(getContext(), "Error: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Cuerpo de respuesta vacío");
                        try {
                            if (response.errorBody() != null) {
                                Log.e(TAG, "Error body: " + response.errorBody().string());
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error al leer errorBody", e);
                        }
                        Toast.makeText(getContext(), "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Respuesta no exitosa");
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error al leer errorBody", e);
                    }
                    Toast.makeText(getContext(), "Error al cargar redes. Código: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RedListResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error en la llamada: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ComunidadAdapter extends RecyclerView.Adapter<ComunidadAdapter.ViewHolder> {

        private Context context;
        private List<RedResponse> redes;
        private List<RedResponse> redesOriginal;
        private FragmentManager fragmentManager;

        public ComunidadAdapter(Context context, List<RedResponse> redes, FragmentManager fragmentManager) {
            this.context = context;
            this.redes = new ArrayList<>(redes);
            this.redesOriginal = new ArrayList<>(redes);
            this.fragmentManager = fragmentManager;
            Log.d(TAG, "Adapter inicializado con " + redes.size() + " redes");
        }

        public void actualizarListaCompleta(List<RedResponse> nuevaLista) {
            Log.d(TAG, "Actualizando lista completa. Nuevo tamaño: " + nuevaLista.size());
            this.redesOriginal = new ArrayList<>(nuevaLista);
            this.redes = new ArrayList<>(nuevaLista);
        }

        public void filtrar(String texto) {
            Log.d(TAG, "Filtrando por: " + texto);
            redes.clear();
            if (texto.isEmpty()) {
                redes.addAll(redesOriginal);
            } else {
                String textoFiltrado = texto.toLowerCase();
                for (RedResponse item : redesOriginal) {
                    if (item.getNombre_red().toLowerCase().contains(textoFiltrado)) {
                        redes.add(item);
                    }
                }
            }
            Log.d(TAG, "Resultados del filtro: " + redes.size());
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d(TAG, "Creando nuevo ViewHolder");
            View view = LayoutInflater.from(context).inflate(R.layout.item_comunidad, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RedResponse red = redes.get(position);
            Log.d(TAG, "Enlazando red: " + red.getNombre_red());

            holder.nombre.setText(red.getNombre_red());

            // Cargar imagen con Glide
            if (red.getImagen_red() != null && !red.getImagen_red().isEmpty()) {
                Glide.with(context)
                        .load(red.getImagen_red())
                        .placeholder(R.drawable.imgcomunidad)
                        .error(R.drawable.imgcomunidad)
                        .circleCrop()
                        .into(holder.imgComunidad);
            } else {
                holder.imgComunidad.setImageResource(R.drawable.imgcomunidad);
            }

            boolean unido = obtenerEstadoUnion(red.getNombre_red());

            if (unido) {
                holder.btnUnirme.setVisibility(View.GONE);
            } else {
                holder.btnUnirme.setVisibility(View.VISIBLE);
                holder.btnUnirme.setText("Unirme");
                holder.btnUnirme.setBackgroundColor(context.getResources().getColor(R.color.azul_link));
            }

            holder.btnUnirme.setOnClickListener(v -> {
                Log.d(TAG, "Uniendose a la red: " + red.getNombre_red());
                guardarEstadoUnion(red.getNombre_red(), true);
                notifyItemChanged(position);
                Toast.makeText(context, "Te has unido a " + red.getNombre_red(), Toast.LENGTH_SHORT).show();
                abrirChatComunidad(red);
            });

            holder.imgComunidad.setOnClickListener(view -> {
                Log.d(TAG, "Abriendo perfil de la red: " + red.getNombre_red());
                PerfilComunidad pf = PerfilComunidad.newInstance(
                        red.getNombre_red()
                        ,red.getDescripcion_red(),
                        // Descripción vacía
                        red.getImagen_red()
                );
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, pf);
                transaction.addToBackStack(null);
                transaction.commit();
            });
        }

        private void abrirChatComunidad(RedResponse red) {
            boolean unido = obtenerEstadoUnion(red.getNombre_red());
            if (unido) {
                ChatComunidad chat = ChatComunidad.newInstance(red.getNombre_red(), red.getImagen_red());
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, chat);
                transaction.addToBackStack(null);
                transaction.commit();
            } else {
                new AlertDialog.Builder(context)
                        .setTitle("Unirse a la comunidad")
                        .setMessage("Debes unirte a " + red.getNombre_red() + " para acceder al chat")
                        .setPositiveButton("Unirme", (dialog, which) -> {
                            for (int i = 0; i < redes.size(); i++) {
                                if (redes.get(i).getId_red() == red.getId_red()) {
                                    ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                                    if (holder != null) {
                                        holder.btnUnirme.performClick();
                                    }
                                    break;
                                }
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        }

        @Override
        public int getItemCount() {
            return redes.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nombre;
            ImageView imgComunidad;
            Button btnUnirme;
            CardView cardComunidad;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nombre = itemView.findViewById(R.id.nombre_comunidad);
                imgComunidad = itemView.findViewById(R.id.imgComunidad);
                btnUnirme = itemView.findViewById(R.id.btnUnirme1);
                cardComunidad = itemView.findViewById(R.id.cardComunidad);

                cardComunidad.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        abrirChatComunidad(redes.get(position));
                    }
                });
            }
        }

        private void guardarEstadoUnion(String nombreRed, boolean unido) {
            Log.d(TAG, "Guardando estado de unión. Red: " + nombreRed + ", Estado: " + unido);
            SharedPreferences prefs = requireContext().getSharedPreferences("UnionComunidad", Context.MODE_PRIVATE);
            prefs.edit().putBoolean(nombreRed, unido).apply();
        }

        private boolean obtenerEstadoUnion(String nombreRed) {
            SharedPreferences prefs = requireContext().getSharedPreferences("UnionComunidad", Context.MODE_PRIVATE);
            boolean estado = prefs.getBoolean(nombreRed, false);
            Log.d(TAG, "Obteniendo estado de unión. Red: " + nombreRed + ", Estado: " + estado);
            return estado;
        }
    }
}