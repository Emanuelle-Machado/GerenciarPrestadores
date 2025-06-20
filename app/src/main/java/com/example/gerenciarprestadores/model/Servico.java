package com.example.gerenciarprestadores.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "servico",
        foreignKeys = {
                @ForeignKey(entity = Agendamento.class,
                        parentColumns = "id",
                        childColumns = "agendamentoId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = TipoServico.class,
                        parentColumns = "id",
                        childColumns = "tipoServicoId")
        })
public class Servico {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long agendamentoId;
    public long tipoServicoId;
    public double quantidade;
    public String status; // "Pendente", "Executado", "Recebido", "Parcialmente Recebido"
    public double valorTotal;

    public Servico() {
    }

    public Servico(long agendamentoId, long tipoServicoId, double quantidade, String status, double valorTotal) {
        this.agendamentoId = agendamentoId;
        this.tipoServicoId = tipoServicoId;
        this.quantidade = quantidade;
        this.status = status;
        this.valorTotal = valorTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Servico)) return false;
        Servico servico = (Servico) o;
        return id == servico.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAgendamentoId() {
        return agendamentoId;
    }

    public void setAgendamentoId(long agendamentoId) {
        this.agendamentoId = agendamentoId;
    }

    public long getTipoServicoId() {
        return tipoServicoId;
    }

    public void setTipoServicoId(long tipoServicoId) {
        this.tipoServicoId = tipoServicoId;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }
}