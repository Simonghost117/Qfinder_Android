package com.sena.qfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ChatComunidad extends Fragment {

    private static final String ARG_NOMBRE_COMUNIDAD = "nombre_comunidad";
    private String nombreComunidad;

    private RecyclerView recyclerView;
    private MensajeAdapter mensajeAdapter;
    private List<Mensaje> mensajesPreview;
    private List<Mensaje> mensajesActivos;

    public ChatComunidad() {}

    public static ChatComunidad newInstance(String nombreComunidad) {
        ChatComunidad fragment = new ChatComunidad();
        Bundle args = new Bundle();
        args.putString(ARG_NOMBRE_COMUNIDAD, nombreComunidad);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nombreComunidad = getArguments().getString(ARG_NOMBRE_COMUNIDAD);
        }

        mensajesPreview = new ArrayList<>();
        mensajesPreview.add(new Mensaje("Usuario 1", "Hola, ¿cómo están?", "12:00 p.m.", nombreComunidad));
        mensajesPreview.add(new Mensaje("Usuario 2", "Bien, ¿y tú?", "12:01 p.m.", nombreComunidad));
        mensajesPreview.add(new Mensaje("Usuario 1", "Todo bien, gracias", "12:02 p.m.", nombreComunidad));

        mensajesActivos = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_comunidad, container, false);

        recyclerView = view.findViewById(R.id.recyclerChat);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mensajeAdapter = new MensajeAdapter(mensajesPreview);
        recyclerView.setAdapter(mensajeAdapter);

        TextView txtTitulo = view.findViewById(R.id.txtTitulo);
        if (nombreComunidad != null) {
            txtTitulo.setText(nombreComunidad);
        }

        LinearLayout layoutAviso = view.findViewById(R.id.layoutAviso);
        LinearLayout layoutEnviarMensaje = view.findViewById(R.id.layoutEnviarMensaje);
        EditText etMensaje = view.findViewById(R.id.etMensaje);
        Button btnEnviar = view.findViewById(R.id.btnEnviar);
        Button btnUnirmeComunidad = view.findViewById(R.id.btnUnirmeComunidad);
        Button btnInfo = view.findViewById((R.id.btnInfo));

        mensajesActivos = obtenerMensajesDeSharedPreferences(nombreComunidad);
        if (!mensajesActivos.isEmpty()) {
            mensajeAdapter.setListaMensajes(mensajesActivos);
            mensajeAdapter.notifyDataSetChanged();
            layoutAviso.setVisibility(View.GONE);
            layoutEnviarMensaje.setVisibility(View.VISIBLE);
            btnUnirmeComunidad.setVisibility(View.GONE);
        }

        btnUnirmeComunidad.setOnClickListener(v -> {
            btnUnirmeComunidad.setVisibility(View.GONE);
            layoutAviso.setVisibility(View.GONE);
            layoutEnviarMensaje.setVisibility(View.VISIBLE);

            mensajeAdapter.setListaMensajes(mensajesActivos);
            mensajeAdapter.notifyDataSetChanged();

            guardarMensajesEnSharedPreferences(mensajesActivos, nombreComunidad);

            // Incrementar miembros en SharedPreferences
            int miembrosActuales = obtenerNumeroMiembros(nombreComunidad);
            guardarNumeroMiembros(nombreComunidad, miembrosActuales + 1);
        });

        btnEnviar.setOnClickListener(v -> {
            String contenido = etMensaje.getText().toString().trim();
            if (!contenido.isEmpty()) {
                Mensaje nuevo = new Mensaje("Tú", contenido, "Ahora", nombreComunidad);
                mensajesActivos.add(nuevo);
                mensajeAdapter.notifyItemInserted(mensajesActivos.size() - 1);
                recyclerView.scrollToPosition(mensajesActivos.size() - 1);
                etMensaje.setText("");
                guardarMensajesEnSharedPreferences(mensajesActivos, nombreComunidad);
            }
        });

        // Botón Info – abre el perfil con número real de miembros
        btnInfo.setOnClickListener(v -> {
            int miembros = obtenerNumeroMiembros(nombreComunidad);
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment perfilFragment = PerfilComunidad.newInstance(nombreComunidad, String.valueOf(miembros));
            transaction.replace(R.id.fragment_container, perfilFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    private void guardarMensajesEnSharedPreferences(List<Mensaje> mensajes, String nombreComunidad) {
        JSONArray jsonArray = new JSONArray();
        for (Mensaje mensaje : mensajes) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("nombreUsuario", mensaje.getNombreUsuario());
                obj.put("contenido", mensaje.getContenido());
                obj.put("hora", mensaje.getHora());
                obj.put("comunidad", mensaje.getComunidad());
                jsonArray.put(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("MensajesComunidad", Context.MODE_PRIVATE);
        prefs.edit().putString(nombreComunidad, jsonArray.toString()).apply();
    }

    private List<Mensaje> obtenerMensajesDeSharedPreferences(String nombreComunidad) {
        SharedPreferences prefs = requireContext().getSharedPreferences("MensajesComunidad", Context.MODE_PRIVATE);
        String json = prefs.getString(nombreComunidad, null);
        List<Mensaje> mensajes = new ArrayList<>();

        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    mensajes.add(new Mensaje(
                            obj.getString("nombreUsuario"),
                            obj.getString("contenido"),
                            obj.getString("hora"),
                            obj.getString("comunidad")
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mensajes;
    }

    private void guardarNumeroMiembros(String nombreComunidad, int miembros) {
        SharedPreferences prefs = requireContext().getSharedPreferences("MiembrosComunidad", Context.MODE_PRIVATE);
        prefs.edit().putInt(nombreComunidad, miembros).apply();
    }

    private int obtenerNumeroMiembros(String nombreComunidad) {
        SharedPreferences prefs = requireContext().getSharedPreferences("MiembrosComunidad", Context.MODE_PRIVATE);
        return prefs.getInt(nombreComunidad, 1); // por defecto 1
    }

    public class MensajeAdapter extends RecyclerView.Adapter<MensajeAdapter.MensajeViewHolder> {
        private List<Mensaje> listaMensajes;

        public MensajeAdapter(List<Mensaje> listaMensajes) {
            this.listaMensajes = listaMensajes;
        }

        @Override
        public MensajeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mensaje, parent, false);
            return new MensajeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MensajeViewHolder holder, int position) {
            Mensaje mensaje = listaMensajes.get(position);
            holder.txtNombreUsuario.setText(mensaje.getNombreUsuario());
            holder.txtContenidoMensaje.setText(mensaje.getContenido());
            holder.txtHoraMensaje.setText(mensaje.getHora());
        }

        @Override
        public int getItemCount() {
            return listaMensajes.size();
        }

        public void setListaMensajes(List<Mensaje> nuevaLista) {
            this.listaMensajes = nuevaLista;
        }

        public class MensajeViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombreUsuario, txtContenidoMensaje, txtHoraMensaje;

            public MensajeViewHolder(View itemView) {
                super(itemView);
                txtNombreUsuario = itemView.findViewById(R.id.txtNombreUsuario);
                txtContenidoMensaje = itemView.findViewById(R.id.txtContenidoMensaje);
                txtHoraMensaje = itemView.findViewById(R.id.txtHoraMensaje);
            }
        }
    }

    public class Mensaje {
        private final String nombreUsuario;
        private final String contenido;
        private final String hora;
        private final String comunidad;

        public Mensaje(String nombreUsuario, String contenido, String hora, String comunidad) {
            this.nombreUsuario = nombreUsuario;
            this.contenido = contenido;
            this.hora = hora;
            this.comunidad = comunidad;
        }

        public String getNombreUsuario() {
            return nombreUsuario;
        }

        public String getContenido() {
            return contenido;
        }

        public String getHora() {
            return hora;
        }

        public String getComunidad() {
            return comunidad;
        }
    }
}
