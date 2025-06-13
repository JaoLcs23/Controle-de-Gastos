package com.controle.dao;

import com.controle.util.DatabaseConnection; // Importa a classe de utilitário
import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDAO<T, ID> implements GenericDAO<T, ID> {
    protected Connection connection; // Conexão com o banco de dados

    public AbstractDAO() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Erro ao obter conexão com o banco de dados: " + e.getMessage());
            throw new RuntimeException("Falha ao inicializar DAO: Não foi possível conectar ao banco de dados.", e);
        }
    }
}