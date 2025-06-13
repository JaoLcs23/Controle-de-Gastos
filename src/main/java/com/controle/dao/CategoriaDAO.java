package com.controle.dao;

import com.controle.model.Categoria;
import com.controle.model.TipoCategoria;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO extends AbstractDAO<Categoria, Integer> {

    public CategoriaDAO() {
        super(); // Chama o construtor da classe abstrata para obter a conexão
    }

    @Override
    public void save(Categoria categoria) {
        String sql = "INSERT INTO categorias (nome, tipo) OUTPUT INSERTED.id VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getTipo().name()); // Salva o nome do enum ("RECEITA" ou "DESPESA")

            // Executa a inserção e obtém o ResultSet com o ID gerado
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    categoria.setId(rs.getInt(1));
                }
            }
            System.out.println("Categoria '" + categoria.getNome() + "' salva com sucesso. ID: " + categoria.getId());
        } catch (SQLException e) {
            System.err.println("Erro ao salvar categoria: " + e.getMessage());
            throw new RuntimeException("Erro ao salvar categoria.", e);
        }
    }

    @Override
    public Categoria findById(Integer id) {
        String sql = "SELECT id, nome, tipo FROM categorias WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("nome");
                    // Converte a string do banco de dados de volta para o enum TipoCategoria
                    TipoCategoria tipo = TipoCategoria.valueOf(rs.getString("tipo"));
                    return new Categoria(id, nome, tipo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar categoria por ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Erro ao buscar categoria.", e);
        }
        return null; // Retorna null se a categoria não for encontrada
    }

    @Override
    public List<Categoria> findAll() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id, nome, tipo FROM categorias";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                TipoCategoria tipo = TipoCategoria.valueOf(rs.getString("tipo"));
                categorias.add(new Categoria(id, nome, tipo));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas as categorias: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar categorias.", e);
        }
        return categorias;
    }

    @Override
    public void update(Categoria categoria) {
        String sql = "UPDATE categorias SET nome = ?, tipo = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getTipo().name());
            stmt.setInt(3, categoria.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Categoria '" + categoria.getNome() + "' atualizada com sucesso.");
            } else {
                System.out.println("Nenhuma categoria encontrada com ID: " + categoria.getId() + " para atualizar.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar categoria: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar categoria.", e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Categoria com ID " + id + " excluída com sucesso.");
            } else {
                System.out.println("Nenhuma categoria encontrada com ID: " + id + " para excluir.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir categoria: " + e.getMessage());
            throw new RuntimeException("Erro ao excluir categoria.", e);
        }
    }

    //Método para buscar uma categoria pelo nome
    public Categoria findByNome(String nome) {
        String sql = "SELECT id, nome, tipo FROM categorias WHERE nome = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    TipoCategoria tipo = TipoCategoria.valueOf(rs.getString("tipo"));
                    return new Categoria(id, nome, tipo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar categoria por nome '" + nome + "': " + e.getMessage());
            throw new RuntimeException("Erro ao buscar categoria por nome.", e);
        }
        return null;
    }
}