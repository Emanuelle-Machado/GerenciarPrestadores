package com.example.gerenciarprestadores.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "tipo_servico")
public class TipoServico {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String nome;
    public double valorPorUnidade;
    public String unidadeMedida;

    public TipoServico() {
    }

    public TipoServico(String nome, double valorPorUnidade, String unidadeMedida) {
        this.nome = nome;
        this.valorPorUnidade = valorPorUnidade;
        this.unidadeMedida = unidadeMedida;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TipoServico)) return false;
        TipoServico that = (TipoServico) o;
        return id == that.id;
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getValorPorUnidade() {
        return valorPorUnidade;
    }

    public void setValorPorUnidade(double valorPorUnidade) {
        this.valorPorUnidade = valorPorUnidade;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }
}