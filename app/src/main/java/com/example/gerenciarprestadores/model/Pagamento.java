package com.example.gerenciarprestadores.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Objects;

@Entity(tableName = "pagamento",
        foreignKeys = {
                @ForeignKey(entity = Servico.class,
                        parentColumns = "id",
                        childColumns = "servicoId",
                        onDelete = ForeignKey.CASCADE)
        })
public class Pagamento {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long servicoId;
    public double valorPago;
    public Date dataPagamento;

    public Pagamento() {
    }

    public Pagamento(long servicoId, double valorPago, Date dataPagamento) {
        this.servicoId = servicoId;
        this.valorPago = valorPago;
        this.dataPagamento = dataPagamento;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pagamento)) return false;
        Pagamento pagamento = (Pagamento) o;
        return id == pagamento.id;
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

    public long getServicoId() {
        return servicoId;
    }

    public void setServicoId(long servicoId) {
        this.servicoId = servicoId;
    }

    public double getValorPago() {
        return valorPago;
    }

    public void setValorPago(double valorPago) {
        this.valorPago = valorPago;
    }

    public Date getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }
}