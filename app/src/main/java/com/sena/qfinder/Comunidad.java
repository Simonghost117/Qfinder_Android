package com.sena.qfinder;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class Comunidad extends Fragment {

    private RecyclerView recyclerView;
    private ComunidadAdapter adapter;
    private List<ComunidadModelo> listaComunidades;
    private ComunidadDB db;

    public Comunidad() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comunidad, container, false);

        recyclerView = view.findViewById(R.id.recyclerComunidades);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = new ComunidadDB(getContext());
        listaComunidades = db.obtenerComunidades();

        adapter = new ComunidadAdapter(getContext(), listaComunidades, getParentFragmentManager());
        recyclerView.setAdapter(adapter);

        // Configurar el buscador
        EditText buscador = view.findViewById(R.id.buscar_comunidad);
        buscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Ocultar el teclado al tocar fuera del EditText
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        // Botón flotante para agregar nueva comunidad
        FloatingActionButton fab = view.findViewById(R.id.fabAddComunidad);
        fab.setOnClickListener(v -> mostrarDialogoCrearComunidad());

        return view;
    }

    private void mostrarDialogoCrearComunidad() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Nueva comunidad");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_crear_comunidad, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombreComunidad);
        EditText etMiembros = dialogView.findViewById(R.id.etMiembrosComunidad);
        builder.setView(dialogView);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String nombre = etNombre.getText().toString().trim();
            String miembros = etMiembros.getText().toString().trim();

            if (!nombre.isEmpty() && !miembros.isEmpty()) {
                db.agregarComunidad(nombre, miembros);

                listaComunidades.clear();
                listaComunidades.addAll(db.obtenerComunidades());
                adapter.notifyDataSetChanged();

                // Abrir PerfilComunidad inmediatamente
                PerfilComunidad pf = PerfilComunidad.newInstance(nombre, miembros);
                FragmentTransaction transactionPF = requireActivity().getSupportFragmentManager().beginTransaction();
                transactionPF.replace(R.id.fragment_container, pf);
                transactionPF.addToBackStack(null);
                transactionPF.commit();

            } else {
                Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // ========================== ADAPTER ===============================
    private class ComunidadAdapter extends RecyclerView.Adapter<ComunidadAdapter.ViewHolder> {

        private Context context;
        private List<ComunidadModelo> comunidades;
        private List<ComunidadModelo> comunidadesOriginal;
        private FragmentManager fragmentManager;

        public ComunidadAdapter(Context context, List<ComunidadModelo> comunidades, FragmentManager fragmentManager) {
            this.context = context;
            this.comunidades = new ArrayList<>(comunidades);
            this.comunidadesOriginal = new ArrayList<>(comunidades);
            this.fragmentManager = fragmentManager;
        }

        public void filtrar(String texto) {
            comunidades.clear();
            if (texto.isEmpty()) {
                comunidades.addAll(comunidadesOriginal);
            } else {
                String textoFiltrado = texto.toLowerCase();
                for (ComunidadModelo item : comunidadesOriginal) {
                    if (item.getNombre().toLowerCase().contains(textoFiltrado)) {
                        comunidades.add(item);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_comunidad, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ComunidadModelo comunidad = comunidades.get(position);
            holder.nombre.setText(comunidad.getNombre());
            holder.miembros.setText(comunidad.getMiembros());

            holder.imgComunidad.setOnClickListener(view -> {
                try {
                    PerfilComunidad pf = PerfilComunidad.newInstance(comunidad.getNombre(), comunidad.getMiembros());
                    FragmentTransaction transactionPF = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
                    transactionPF.replace(R.id.fragment_container, pf);
                    transactionPF.addToBackStack(null);
                    transactionPF.commit();
                } catch (Exception e) {
                    Toast.makeText(context, "Error al abrir el perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            holder.btnUnirme1.setOnClickListener(v -> {
                try {
                    ChatComunidad chatFragment = ChatComunidad.newInstance(comunidad.getNombre());
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragment_container, chatFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } catch (Exception e) {
                    Toast.makeText(context, "Error al abrir chat: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            holder.btnOpciones.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.btnOpciones);
                popup.getMenuInflater().inflate(R.menu.menu_comunidad_item, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_editar) {
                        mostrarDialogoEditar(position);
                        return true;
                    } else if (itemId == R.id.menu_eliminar) {
                        db.eliminarComunidad(comunidad.getId());
                        comunidades.remove(position);
                        notifyItemRemoved(position);
                        return true;
                    }
                    return false;
                });

                popup.show();
            });
        }

        @Override
        public int getItemCount() {
            return comunidades.size();
        }

        private void mostrarDialogoEditar(int pos) {
            ComunidadModelo comunidad = comunidades.get(pos);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Editar comunidad");

            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_crear_comunidad, null);
            EditText etNombre = dialogView.findViewById(R.id.etNombreComunidad);
            EditText etMiembros = dialogView.findViewById(R.id.etMiembrosComunidad);

            etNombre.setText(comunidad.getNombre());
            etMiembros.setText(comunidad.getMiembros());
            builder.setView(dialogView);

            builder.setPositiveButton("Guardar", (dialog, which) -> {
                comunidad.setNombre(etNombre.getText().toString().trim());
                comunidad.setMiembros(etMiembros.getText().toString().trim());
                db.editarComunidad(comunidad.getId(), comunidad.getNombre(), comunidad.getMiembros());
                notifyItemChanged(pos);
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nombre, miembros;
            ImageView imgComunidad, btnOpciones;
            Button btnUnirme1;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nombre = itemView.findViewById(R.id.nombre_comunidad);
                miembros = itemView.findViewById(R.id.miembros_comunidad);
                imgComunidad = itemView.findViewById(R.id.imgComunidad);
                btnUnirme1 = itemView.findViewById(R.id.btnUnirme1);
                btnOpciones = itemView.findViewById(R.id.btnOpciones);
            }
        }
    }

    // ========================== MODELO ===============================
    public class ComunidadModelo {
        private int id;
        private String nombre;
        private String miembros;

        public ComunidadModelo(int id, String nombre, String miembros) {
            this.id = id;
            this.nombre = nombre;
            this.miembros = miembros;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getMiembros() { return miembros; }
        public void setMiembros(String miembros) { this.miembros = miembros; }
    }

    // ========================== BASE DE DATOS ===============================
    public class ComunidadDB extends SQLiteOpenHelper {

        private static final String DB_NAME = "QFinder.db";
        private static final int DB_VERSION = 1;

        public ComunidadDB(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS comunidades (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, miembros TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS comunidades");
            onCreate(db);
        }

        public void agregarComunidad(String nombre, String miembros) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nombre", nombre);
            values.put("miembros", miembros);
            db.insert("comunidades", null, values);
        }

        public List<ComunidadModelo> obtenerComunidades() {
            List<ComunidadModelo> lista = new ArrayList<>();
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM comunidades", null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String nombre = cursor.getString(1);
                String miembros = cursor.getString(2);
                lista.add(new ComunidadModelo(id, nombre, miembros));
            }
            cursor.close();
            return lista;
        }

        public void editarComunidad(int id, String nombre, String miembros) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nombre", nombre);
            values.put("miembros", miembros);
            db.update("comunidades", values, "id=?", new String[]{String.valueOf(id)});
        }

        public void eliminarComunidad(int id) {
            SQLiteDatabase db = getWritableDatabase();
            db.delete("comunidades", "id=?", new String[]{String.valueOf(id)});
        }
    }
}
