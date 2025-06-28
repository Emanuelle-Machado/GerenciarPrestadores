package com.example.gerenciarprestadores.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gerenciarprestadores.DAO.TipoServicoDao;
import com.example.gerenciarprestadores.Database.AppDatabase;
import com.example.gerenciarprestadores.R;
import com.example.gerenciarprestadores.model.TipoServico;

import java.util.ArrayList;
import java.util.List;

public class TipoServicoActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TipoServicoAdapter adapter;
    private List<TipoServico> tipoServicoList;
    private AppDatabase db;
    private TipoServicoDao tipoServicoDao;
    private Button btnAdicionar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipo_servico);

        recyclerView = findViewById(R.id.recyclerViewTiposServico);
        btnAdicionar = findViewById(R.id.btnAdicionarTipoServico);
        tipoServicoList = new ArrayList<>();
        adapter = new TipoServicoAdapter(tipoServicoList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = AppDatabase.getInstance(this);
        tipoServicoDao = db.tipoServicoDao();

        loadTiposServico();

        btnAdicionar.setOnClickListener(v -> showTipoServicoDialog(null));
    }

    private void loadTiposServico() {
        new Thread(() -> {
            tipoServicoList.clear();
            tipoServicoList.addAll(tipoServicoDao.getAll());
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }).start();
    }

    private void showTipoServicoDialog(TipoServico tipoServico) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tipo_servico, null);
        builder.setView(dialogView);

        EditText etNome = dialogView.findViewById(R.id.etNome);
        EditText etValor = dialogView.findViewById(R.id.etValor);
        EditText etUnidade = dialogView.findViewById(R.id.etUnidade);
        Button btnSalvar = dialogView.findViewById(R.id.btnSalvar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        if (tipoServico != null) {
            etNome.setText(tipoServico.nome);
            etValor.setText(String.valueOf(tipoServico.valorPorUnidade));
            etUnidade.setText(tipoServico.unidadeMedida);
        }

        AlertDialog dialog = builder.create();

        btnSalvar.setOnClickListener(v -> {
            String nome = etNome.getText().toString();
            String valorStr = etValor.getText().toString();
            String unidade = etUnidade.getText().toString();

            if (nome.isEmpty() || valorStr.isEmpty() || unidade.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double valor;
            try {
                valor = Double.parseDouble(valorStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                if (tipoServico == null) {
                    TipoServico novo = new TipoServico();
                    novo.nome = nome;
                    novo.valorPorUnidade = valor;
                    novo.unidadeMedida = unidade;
                    tipoServicoDao.insert(novo);
                } else {
                    tipoServico.id = tipoServico.id;
                    tipoServico.nome = nome;
                    tipoServico.valorPorUnidade = valor;
                    tipoServico.unidadeMedida = unidade;
                    tipoServicoDao.update(tipoServico);
                }
                runOnUiThread(() -> {
                    loadTiposServico();
                    dialog.dismiss();
                    Toast.makeText(this, "Salvo com sucesso", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private class TipoServicoAdapter extends RecyclerView.Adapter<TipoServicoAdapter.ViewHolder> {
        private List<TipoServico> tipos;

        public TipoServicoAdapter(List<TipoServico> tipos) {
            this.tipos = tipos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_tipo_servico, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            TipoServico tipo = tipos.get(position);
            holder.tvNome.setText(tipo.nome);
            holder.tvValor.setText(String.format("R$ %.2f / %s", tipo.valorPorUnidade, tipo.unidadeMedida));

            holder.itemView.setOnClickListener(v -> showTipoServicoDialog(tipo));
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(TipoServicoActivity.this)
                        .setTitle("Excluir Tipo de Serviço")
                        .setMessage("Deseja excluir " + tipo.nome + "?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            new Thread(() -> {
                                tipoServicoDao.delete(tipo);
                                runOnUiThread(() -> {
                                    loadTiposServico();
                                    Toast.makeText(TipoServicoActivity.this, "Excluído com sucesso", Toast.LENGTH_SHORT).show();
                                });
                            }).start();
                        })
                        .setNegativeButton("Não", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return tipos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNome, tvValor;

            public ViewHolder(View itemView) {
                super(itemView);
                tvNome = itemView.findViewById(R.id.tvNome);
                tvValor = itemView.findViewById(R.id.tvValor);
            }
        }
    }
}