package com.sena.qfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private List<Mensaje> mensajesPreview;  // Lista con los mensajes previos
    private List<Mensaje> mensajesActivos;   // Lista que se usará después de unirse

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

        // Mensajes previos (vista previa antes de unirse)
        mensajesPreview = new ArrayList<>();
        mensajesPreview.add(new Mensaje("Usuario 1", "Hola, ¿cómo están?", "12:00 p.m.", nombreComunidad));
        mensajesPreview.add(new Mensaje("Usuario 2", "Bien, ¿y tú?", "12:01 p.m.", nombreComunidad));
        mensajesPreview.add(new Mensaje("Usuario 1", "Todo bien, gracias", "12:02 p.m.", nombreComunidad));

        // Lista de mensajes activa al unirse (vacía al inicio)
        mensajesActivos = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_comunidad, container, false);

        // Inicialización del RecyclerView
        recyclerView = view.findViewById(R.id.recyclerChat);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Mostrar los mensajes previos antes de unirse
        mensajeAdapter = new MensajeAdapter(mensajesPreview);
        recyclerView.setAdapter(mensajeAdapter);

        // Mostrar nombre de comunidad
        TextView txtTitulo = view.findViewById(R.id.txtTitulo);
        if (nombreComunidad != null) {
            txtTitulo.setText(nombreComunidad);
        }

        // Referencias a vistas
        LinearLayout layoutAviso = view.findViewById(R.id.layoutAviso);
        LinearLayout layoutEnviarMensaje = view.findViewById(R.id.layoutEnviarMensaje);
        EditText etMensaje = view.findViewById(R.id.etMensaje);
        Button btnEnviar = view.findViewById(R.id.btnEnviar);
        Button btnUnirmeComunidad = view.findViewById(R.id.btnUnirmeComunidad);

        // Cargar mensajes si ya se unió
        mensajesActivos = obtenerMensajesDeSharedPreferences(nombreComunidad);
        if (!mensajesActivos.isEmpty()) {
            mensajeAdapter.setListaMensajes(mensajesActivos);
            mensajeAdapter.notifyDataSetChanged();
            layoutAviso.setVisibility(View.GONE);
            layoutEnviarMensaje.setVisibility(View.VISIBLE);
            btnUnirmeComunidad.setVisibility(View.GONE);
        }

        // Botón para unirse a la comunidad
        btnUnirmeComunidad.setOnClickListener(v -> {
            btnUnirmeComunidad.setVisibility(View.GONE);
            layoutAviso.setVisibility(View.GONE);
            layoutEnviarMensaje.setVisibility(View.VISIBLE);

            // Limpiar los mensajes previos y establecer la lista activa
            mensajeAdapter.setListaMensajes(mensajesActivos);
            mensajeAdapter.notifyDataSetChanged();

            // Guardar los mensajes cuando el usuario se une
            guardarMensajesEnSharedPreferences(mensajesActivos, nombreComunidad);
        });

        // Botón para enviar mensaje
        btnEnviar.setOnClickListener(v -> {
            String contenido = etMensaje.getText().toString().trim();
            if (!contenido.isEmpty()) {
                Mensaje nuevo = new Mensaje("Tú", contenido, "Ahora", nombreComunidad);
                mensajesActivos.add(nuevo);  // Agregar el nuevo mensaje a la lista activa
                mensajeAdapter.notifyItemInserted(mensajesActivos.size() - 1);  // Notificar que se insertó un nuevo mensaje
                recyclerView.scrollToPosition(mensajesActivos.size() - 1);  // Desplazar el RecyclerView hacia el último mensaje
                etMensaje.setText("");  // Limpiar el campo de mensaje

                // Guardar los mensajes después de enviar uno nuevo
                guardarMensajesEnSharedPreferences(mensajesActivos, nombreComunidad);
            }
        });

        return view;
    }

    // Guardar los mensajes en SharedPreferences
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

    // Obtener los mensajes guardados de SharedPreferences
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

    // Clase interna: Adaptador del RecyclerView
    public class MensajeAdapter extends RecyclerView.Adapter<MensajeAdapter.MensajeViewHolder> {
        private List<Mensaje> listaMensajes;

        public MensajeAdapter(List<Mensaje> listaMensajes) {
            this.listaMensajes = listaMensajes; // ¡No copiar, solo referenciar!
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

    // Clase interna: Modelo de mensaje
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
