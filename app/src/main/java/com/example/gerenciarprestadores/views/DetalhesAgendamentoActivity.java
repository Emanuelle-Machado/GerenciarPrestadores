package com.example.gerenciarprestadores.views;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import com.example.gerenciarprestadores.DAO.AgendamentoDao;
import com.example.gerenciarprestadores.DAO.PagamentoDao;
import com.example.gerenciarprestadores.DAO.ServicoDao;
import com.example.gerenciarprestadores.DAO.TipoServicoDao;
import com.example.gerenciarprestadores.Database.AppDatabase;
import com.example.gerenciarprestadores.R;
import com.example.gerenciarprestadores.model.Agendamento;
import com.example.gerenciarprestadores.model.Pagamento;
import com.example.gerenciarprestadores.model.Servico;
import com.example.gerenciarprestadores.model.TipoServico;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetalhesAgendamentoActivity extends AppCompatActivity {
    private TextView tvNomeCliente, tvEndereco, tvTelefone, tvData;
    private RecyclerView recyclerViewServicos;
    private ServicoAdapter adapter;
    private List<Servico> servicoList;
    private Button btnLancarRecebimento, btnRenegociar, btnCancelar, btnAbrirMapa, btnLigar;
    private AppDatabase db;
    private AgendamentoDao agendamentoDao;
    private ServicoDao servicoDao;
    private TipoServicoDao tipoServicoDao;
    private PagamentoDao pagamentoDao;
    private long agendamentoId;
    private Agendamento agendamento;
    private static final int REQUEST_CALL_PHONE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_agendamento);

        tvNomeCliente = findViewById(R.id.tvNomeCliente);
        tvEndereco = findViewById(R.id.tvEndereco);
        tvTelefone = findViewById(R.id.tvTelefone);
        tvData = findViewById(R.id.tvData);
        recyclerViewServicos = findViewById(R.id.recyclerViewServicos);
        btnLancarRecebimento = findViewById(R.id.btnLancarRecebimento);
        btnRenegociar = findViewById(R.id.btnRenegociar);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnAbrirMapa = findViewById(R.id.btnAbrirMapa);
        btnLigar = findViewById(R.id.btnLigar);

        servicoList = new ArrayList<>();
        adapter = new ServicoAdapter(servicoList);
        recyclerViewServicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewServicos.setAdapter(adapter);

        db = AppDatabase.getInstance(this);
        agendamentoDao = db.agendamentoDao();
        servicoDao = db.servicoDao();
        tipoServicoDao = db.tipoServicoDao();
        pagamentoDao = db.pagamentoDao();

        agendamentoId = getIntent().getLongExtra("agendamentoId", -1);
        if (agendamentoId == -1) {
            Toast.makeText(this, "Erro ao obter agendamento", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadAgendamento();
        loadServicos();

        btnAbrirMapa.setOnClickListener(v -> abrirGoogleMaps());
        btnLigar.setOnLongClickListener(v -> {
            iniciarLigacao();
            return true;
        });
        btnLancarRecebimento.setOnClickListener(v -> showLancarRecebimentoDialog());
        btnRenegociar.setOnClickListener(v -> renegociarServicos());
        btnCancelar.setOnClickListener(v -> confirmarCancelamento());
    }

    private void loadAgendamento() {
        new Thread(() -> {
            agendamento = agendamentoDao.getById(agendamentoId);
            runOnUiThread(() -> {
                if (agendamento != null) {
                    tvNomeCliente.setText(agendamento.nomeCliente);
                    tvEndereco = findViewById(R.id.tvEndereco);
                    tvTelefone = findViewById(R.id.tvTelefone);
                    tvData = findViewById(R.id.tvData);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    tvData.setText(dateFormat.format(agendamento.data));
                } else {
                    Toast.makeText(this, "Agendamento não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }).start();
    }

    private void loadServicos() {
        new Thread(() -> {
            servicoList.clear();
            servicoList.addAll(servicoDao.getByAgendamentoId(agendamentoId));
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }).start();
    }

    private void abrirGoogleMaps() {
        if (agendamento != null) {
            String endereco = Uri.encode(agendamento.endereco);
            // Tentar com geo:0,0?q= primeiro
            Uri geoUri = Uri.parse("geo:0,0?q=" + endereco);
            Intent geoIntent = new Intent(Intent.ACTION_VIEW, geoUri);
            Log.d("DetalhesAgendamento", "Tentando abrir URI: " + geoUri);

            // Logar aplicativos disponíveis para o Intent geo
            List<ResolveInfo> geoActivities = getPackageManager().queryIntentActivities(geoIntent, 0);
            Log.d("DetalhesAgendamento", "Aplicativos disponíveis para geo URI: " + geoActivities.size());
            for (ResolveInfo info : geoActivities) {
                Log.d("DetalhesAgendamento", "App: " + info.activityInfo.packageName);
            }

            if (geoIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(geoIntent);
                Log.d("DetalhesAgendamento", "Iniciando aplicativo de mapas com geo URI: " + geoUri);
            } else {
                // Tentar com google.navigation como fallback
                Uri navUri = Uri.parse("google.navigation:q=" + endereco);
                Intent navIntent = new Intent(Intent.ACTION_VIEW, navUri);
                navIntent.setPackage("com.google.android.apps.maps");
                Log.d("DetalhesAgendamento", "Tentando fallback URI: " + navUri);

                // Logar aplicativos disponíveis para o Intent google.navigation
                List<ResolveInfo> navActivities = getPackageManager().queryIntentActivities(navIntent, 0);
                Log.d("DetalhesAgendamento", "Aplicativos disponíveis para navigation URI: " + navActivities.size());
                for (ResolveInfo info : navActivities) {
                    Log.d("DetalhesAgendamento", "App: " + info.activityInfo.packageName);
                }

                if (navIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(navIntent);
                    Log.d("DetalhesAgendamento", "Iniciando Google Maps com navigation URI: " + navUri);
                } else {
                    // Último fallback: abrir no navegador
                    Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + endereco);
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                    Log.d("DetalhesAgendamento", "Tentando abrir no navegador: " + webUri);

                    if (webIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(webIntent);
                        Log.d("DetalhesAgendamento", "Iniciando navegador com URI: " + webUri);
                    } else {
                        Toast.makeText(this, "Nenhum aplicativo ou navegador disponível para abrir mapas", Toast.LENGTH_SHORT).show();
                        Log.e("DetalhesAgendamento", "Nenhum aplicativo encontrado para URIs: geo, google.navigation, web");
                    }
                }
            }
        }
    }

    private void iniciarLigacao() {
        if (agendamento != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                ligarParaCliente();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
                Log.d("DetalhesAgendamento", "Solicitando permissão CALL_PHONE");
            }
        }
    }

    private void ligarParaCliente() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + agendamento.telefone));
        Log.d("DetalhesAgendamento", "Iniciando Intent ACTION_CALL para: " + agendamento.telefone);

        try {
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                Log.d("DetalhesAgendamento", "Ligação iniciada com sucesso");
            } else {
                Toast.makeText(this, "Nenhum aplicativo de telefone disponível", Toast.LENGTH_SHORT).show();
                Log.e("DetalhesAgendamento", "Nenhum aplicativo de telefone encontrado");
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Permissão para ligação não concedida", Toast.LENGTH_SHORT).show();
            Log.e("DetalhesAgendamento", "SecurityException ao iniciar ligação: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ligarParaCliente();
                Log.d("DetalhesAgendamento", "Permissão CALL_PHONE concedida");
            } else {
                Toast.makeText(this, "Permissão para ligação negada", Toast.LENGTH_SHORT).show();
                Log.w("DetalhesAgendamento", "Permissão CALL_PHONE negada");
            }
        }
    }

    private void showLancarRecebimentoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_lancar_recebimento, null);
        builder.setView(dialogView);

        Spinner spinnerServicos = dialogView.findViewById(R.id.spinnerServicos);
        EditText etValorPago = dialogView.findViewById(R.id.etValorPago);
        TextView tvValorTotal = dialogView.findViewById(R.id.tvValorTotal);
        TextView tvTotalPago = dialogView.findViewById(R.id.tvTotalPago);
        Button btnSalvar = dialogView.findViewById(R.id.btnSalvar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        // Pré-carregar nomes dos tipos de serviço
        Map<Long, String> tipoServicoNomes = new HashMap<>();
        new Thread(() -> {
            for (Servico servico : servicoList) {
                TipoServico tipo = tipoServicoDao.getById(servico.tipoServicoId);
                tipoServicoNomes.put(servico.id, tipo != null ? tipo.nome : "Serviço Desconhecido");
            }
            runOnUiThread(() -> {
                // Criar ArrayAdapter personalizado para exibir apenas o nome do tipo de serviço
                ArrayAdapter<Servico> adapter = new ArrayAdapter<Servico>(this, android.R.layout.simple_spinner_item, servicoList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        Servico servico = getItem(position);
                        if (servico != null) {
                            view.setText(tipoServicoNomes.getOrDefault(servico.id, "Serviço Desconhecido"));
                        }
                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                        Servico servico = getItem(position);
                        if (servico != null) {
                            view.setText(tipoServicoNomes.getOrDefault(servico.id, "Serviço Desconhecido"));
                        }
                        return view;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerServicos.setAdapter(adapter);

                // Definir valores iniciais para o primeiro serviço, se houver
                if (!servicoList.isEmpty()) {
                    spinnerServicos.setSelection(0);
                }
            });
        }).start();

        // Atualizar valores totais quando um serviço é selecionado
        spinnerServicos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Servico servicoSelecionado = (Servico) parent.getSelectedItem();
                new Thread(() -> {
                    double totalPago = pagamentoDao.getTotalPagoByServicoId(servicoSelecionado.id);
                    runOnUiThread(() -> {
                        tvValorTotal.setText(String.format("Valor Total: R$ %.2f", servicoSelecionado.valorTotal));
                        tvTotalPago.setText(String.format("Total Pago: R$ %.2f", totalPago));
                    });
                }).start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvValorTotal.setText("Valor Total: R$ 0,00");
                tvTotalPago.setText("Total Pago: R$ 0,00");
            }
        });

        AlertDialog dialog = builder.create();

        btnSalvar.setOnClickListener(v -> {
            Servico servicoSelecionado = (Servico) spinnerServicos.getSelectedItem();
            String valorPagoStr = etValorPago.getText().toString();

            if (servicoSelecionado == null || valorPagoStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double valorPago;
            try {
                valorPago = Double.parseDouble(valorPagoStr);
                if (valorPago <= 0) {
                    Toast.makeText(this, "Valor pago deve ser maior que zero", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar se o valor pago não excede o valor restante
            new Thread(() -> {
                double totalPago = pagamentoDao.getTotalPagoByServicoId(servicoSelecionado.id);
                double valorRestante = servicoSelecionado.valorTotal - totalPago;

                if (valorPago > valorRestante) {
                    runOnUiThread(() -> Toast.makeText(this, String.format("Valor pago excede o restante devido (R$ %.2f)", valorRestante), Toast.LENGTH_SHORT).show());
                    return;
                }

                Pagamento pagamento = new Pagamento();
                pagamento.servicoId = servicoSelecionado.id;
                pagamento.valorPago = valorPago;
                pagamento.dataPagamento = new Date();
                pagamentoDao.insert(pagamento);

                totalPago += valorPago;
                String novoStatus;
                if (totalPago >= servicoSelecionado.valorTotal) {
                    novoStatus = "Recebido";
                } else if (totalPago > 0) {
                    novoStatus = "Parcialmente Recebido";
                } else {
                    novoStatus = "A Receber";
                }

                servicoSelecionado.status = novoStatus;
                servicoDao.update(servicoSelecionado);

                runOnUiThread(() -> {
                    loadServicos();
                    dialog.dismiss();
                    Toast.makeText(this, "Pagamento lançado com sucesso", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void renegociarServicos() {
        Intent intent = new Intent(this, AdicionarServicosActivity.class);
        intent.putExtra("agendamentoId", agendamentoId);
        startActivity(intent);
    }

    private void confirmarCancelamento() {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar Agendamento")
                .setMessage("Deseja cancelar este agendamento? Isso excluirá todos os serviços associados.")
                .setPositiveButton("Sim", (dialog, which) -> {
                    new Thread(() -> {
                        agendamentoDao.delete(agendamento);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Agendamento cancelado", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }).start();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private class ServicoAdapter extends RecyclerView.Adapter<ServicoAdapter.ViewHolder> {
        private List<Servico> servicos;

        public ServicoAdapter(List<Servico> servicos) {
            this.servicos = servicos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_servico_detalhes, parent, false);
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

                    if (!servico.status.equals("Executado") && !servico.status.equals("Recebido") && !servico.status.equals("Parcialmente Recebido")) {
                        holder.btnMarcarExecutado.setVisibility(View.VISIBLE);
                        holder.btnMarcarExecutado.setOnClickListener(v -> marcarComoExecutado(servico));
                    } else {
                        holder.btnMarcarExecutado.setVisibility(View.GONE);
                    }
                });
            }).start();
        }

        @Override
        public int getItemCount() {
            return servicos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDescricao, tvValor, tvStatus;
            Button btnMarcarExecutado;

            public ViewHolder(View itemView) {
                super(itemView);
                tvDescricao = itemView.findViewById(R.id.tvDescricao);
                tvValor = itemView.findViewById(R.id.tvValor);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnMarcarExecutado = itemView.findViewById(R.id.btnMarcarExecutado);
            }
        }

        private void marcarComoExecutado(Servico servico) {
            new Thread(() -> {
                servico.status = "Executado";
                servicoDao.update(servico);
                runOnUiThread(() -> {
                    loadServicos();
                    Toast.makeText(DetalhesAgendamentoActivity.this, "Serviço marcado como executado", Toast.LENGTH_SHORT).show();
                });
            }).start();
        }
    }
}