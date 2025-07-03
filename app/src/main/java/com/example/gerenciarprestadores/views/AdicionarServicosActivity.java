package com.example.gerenciarprestadores.views;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gerenciarprestadores.DAO.ServicoDao;
import com.example.gerenciarprestadores.DAO.TipoServicoDao;
import com.example.gerenciarprestadores.Database.AppDatabase;
import com.example.gerenciarprestadores.R;
import com.example.gerenciarprestadores.model.Servico;
import com.example.gerenciarprestadores.model.TipoServico;

import java.util.ArrayList;
import java.util.List;

public class AdicionarServicosActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ServicoAdapter adapter;
    private List<Servico> servicoList;
    private AppDatabase db;
    private ServicoDao servicoDao;
    private TipoServicoDao tipoServicoDao;
    private long agendamentoId;
    private Button btnAdicionarServico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_servicos);

        recyclerView = findViewById(R.id.recyclerViewServicos);
        btnAdicionarServico = findViewById(R.id.btnAdicionarServico);
        servicoList = new ArrayList<>();
        adapter = new ServicoAdapter(servicoList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = AppDatabase.getInstance(this);
        servicoDao = db.servicoDao();
        tipoServicoDao = db.tipoServicoDao();

        agendamentoId = getIntent().getLongExtra("agendamentoId", -1);
        if (agendamentoId == -1) {
            Toast.makeText(this, "Erro ao obter agendamento", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadServicos();
        btnAdicionarServico.setOnClickListener(v -> showAdicionarServicoDialog());
    }

    private void loadServicos() {
        new Thread(() -> {
            servicoList.clear();
            servicoList.addAll(servicoDao.getByAgendamentoId(agendamentoId));
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }).start();
    }

    private void showAdicionarServicoDialog() {
        new Thread(() -> {
            List<TipoServico> tipos = tipoServicoDao.getAll();
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_adicionar_servico, null);
                builder.setView(dialogView);

                Spinner spinnerTipos = dialogView.findViewById(R.id.spinnerTipos);
                EditText etQuantidade = dialogView.findViewById(R.id.etQuantidade);
                Button btnSalvar = dialogView.findViewById(R.id.btnSalvar);
                Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

                ArrayAdapter<TipoServico> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTipos.setAdapter(adapter);

                AlertDialog dialog = builder.create();

                btnSalvar.setOnClickListener(v -> {
                    TipoServico tipoSelecionado = (TipoServico) spinnerTipos.getSelectedItem();
                    String quantidadeStr = etQuantidade.getText().toString();

                    if (tipoSelecionado == null || quantidadeStr.isEmpty()) {
                        Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double quantidade;
                    try {
                        quantidade = Double.parseDouble(quantidadeStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Quantidade inválida", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Servico servico = new Servico();
                    servico.agendamentoId = agendamentoId;
                    servico.tipoServicoId = tipoSelecionado.id;
                    servico.quantidade = quantidade;
                    servico.valorTotal = quantidade * tipoSelecionado.valorPorUnidade;
                    servico.status = "Pendente";

                    new Thread(() -> {
                        servicoDao.insert(servico);
                        runOnUiThread(() -> {
                            loadServicos();
                            dialog.dismiss();
                            Toast.makeText(this, "Serviço adicionado com sucesso", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                });

                btnCancelar.setOnClickListener(v -> dialog.dismiss());
                dialog.show();
            });
        }).start();
    }

    private class ServicoAdapter extends RecyclerView.Adapter<ServicoAdapter.ViewHolder> {
        private List<Servico> servicos;

        public ServicoAdapter(List<Servico> servicos) {
            this.servicos = servicos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_servico, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Servico servico = servicos.get(position);
            new Thread(() -> {
                TipoServico tipo = tipoServicoDao.getById(servico.tipoServicoId);
                runOnUiThread(() -> {
                    holder.tvDescricao.setText(String.format("%s - %.2f %s", tipo.nome, servico.quantidade, tipo.unidadeMedida));
                    holder.tvValor.setText(String.format("R$ %.2f", servico.valorTotal));
                    holder.tvStatus.setText(servico.status);
                });
            }).start();
        }

        @Override
        public int getItemCount() {
            return servicos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDescricao, tvValor, tvStatus;

            public ViewHolder(View itemView) {
                super(itemView);
                tvDescricao = itemView.findViewById(R.id.tvDescricao);
                tvValor = itemView.findViewById(R.id.tvValor);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }
}