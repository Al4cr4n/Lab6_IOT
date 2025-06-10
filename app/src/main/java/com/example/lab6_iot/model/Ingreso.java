package com.example.lab6_iot.model;

import com.google.firebase.Timestamp;

public class Ingreso {
    private String id;
    private String titulo;
    private double monto;
    private String descripcion;
    private Timestamp fecha;

    // Constructor vacío para Firestore
    public Ingreso() { }

    // Constructor con campos
    public Ingreso(String titulo, double monto, String descripcion, Timestamp fecha) {
        this.titulo = titulo;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    // Getter y setter para el ID
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    // Estos son los getters que necesitas:
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public double getMonto() {
        return monto;
    }
    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Timestamp getFecha() {
        return fecha;
    }
    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }
}
