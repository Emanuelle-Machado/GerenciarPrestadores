package com.example.gerenciarprestadores.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Objects;

@Entity(tableName = "agendamento",
        indices = {@Index(value = {"data"})})
public class Agendamento {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String nomeCliente;
    public String endereco;
    public String telefone;
    public Date data;

    public Agendamento() {
    }

    public Agendamento(String nomeCliente, String endereco, String telefone, Date data) {
        this.nomeCliente = nomeCliente;
        this.endereco = endereco;
        this.telefone = telefone;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Agendamento)) return false;
        Agendamento that = (Agendamento) o;
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

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }
}