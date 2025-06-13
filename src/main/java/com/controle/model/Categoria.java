package com.controle.model;

public class Categoria {
    private int id;
    private String nome;
    private TipoCategoria tipo;

    // Construtor para categorias existentes no banco de dados
    public Categoria(int id, String nome, TipoCategoria tipo) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo; // Atribuição correta
    }

    // Construtor para novas categorias
    public Categoria(String nome, TipoCategoria tipo) {
        this.nome = nome; // Atribui o nome
        this.tipo = tipo; // Atribui o tipo
    }

    // Getters e Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public TipoCategoria getTipo() {
        return tipo;
    }
    public void setTipo(TipoCategoria tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Categoria{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", tipo=" + tipo +
                '}';
    }
}