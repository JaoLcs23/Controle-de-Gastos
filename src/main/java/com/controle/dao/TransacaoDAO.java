package com.controle.dao;

import com.controle.model.Categoria;
import com.controle.model.Transacao;
import com.controle.model.TipoCategoria;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransacaoDAO extends AbstractDAO<Transacao, Integer> {

    private CategoriaDAO categoriaDAO;

    public TransacaoDAO() {
        super();
        this.categoriaDAO = new CategoriaDAO();
    }

    @Override
    public void save(Transacao transacao) {
        String sql = "INSERT INTO transacoes (descricao, valor, data, tipo, categoria_id) OUTPUT INSERTED.id VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, transacao.getDescricao());
            stmt.setDouble(2, transacao.getValor());
            stmt.setDate(3, Date.valueOf(transacao.getData()));
            stmt.setString(4, transacao.getTipo().name());
            if (transacao.getCategoria() != null) {
                stmt.setInt(5, transacao.getCategoria().getId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    transacao.setId(rs.getInt(1));
                }
            }
            System.out.println("Transação '" + transacao.getDescricao() + "' salva com sucesso. ID: " + transacao.getId());
        } catch (SQLException e) {
            System.err.println("Erro ao salvar transação: " + e.getMessage());
            throw new RuntimeException("Erro ao salvar transação.", e);
        }
    }

    @Override
    public Transacao findById(Integer id) {
        String sql = "SELECT id, descricao, valor, data, tipo, categoria_id FROM transacoes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String descricao = rs.getString("descricao");
                    double valor = rs.getDouble("valor");
                    LocalDate data = rs.getDate("data").toLocalDate();
                    TipoCategoria tipo = TipoCategoria.valueOf(rs.getString("tipo"));
                    Integer categoriaId = rs.getObject("categoria_id", Integer.class);
                    Categoria categoria = null;
                    if (categoriaId != null) {
                        categoria = categoriaDAO.findById(categoriaId);
                    }
                    return new Transacao(id, descricao, valor, data, tipo, categoria);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar transação por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transação.", e);
        }
        return null;
    }

    @Override
    public List<Transacao> findAll() {
        List<Transacao> transacoes = new ArrayList<>();
        String sql = "SELECT id, descricao, valor, data, tipo, categoria_id FROM transacoes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String descricao = rs.getString("descricao");
                double valor = rs.getDouble("valor");
                LocalDate data = rs.getDate("data").toLocalDate();
                TipoCategoria tipo = TipoCategoria.valueOf(rs.getString("tipo"));
                Integer categoriaId = rs.getObject("categoria_id", Integer.class);
                Categoria categoria = null;
                if (categoriaId != null) {
                    categoria = categoriaDAO.findById(categoriaId);
                }
                transacoes.add(new Transacao(id, descricao, valor, data, tipo, categoria));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas as transações: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transações.", e);
        }
        return transacoes;
    }

    @Override
    public void update(Transacao transacao) {
        String sql = "UPDATE transacoes SET descricao = ?, valor = ?, data = ?, tipo = ?, categoria_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, transacao.getDescricao());
            stmt.setDouble(2, transacao.getValor());
            stmt.setDate(3, Date.valueOf(transacao.getData()));
            stmt.setString(4, transacao.getTipo().name());
            if (transacao.getCategoria() != null) {
                stmt.setInt(5, transacao.getCategoria().getId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setInt(6, transacao.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Transação '" + transacao.getDescricao() + "' atualizada com sucesso.");
            } else {
                System.out.println("Nenhuma transação encontrada com ID: " + transacao.getId() + " para atualizar.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar transação: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar transação.", e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM transacoes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Transação com ID " + id + " excluída com sucesso.");
            } else {
                System.out.println("Nenhuma transação encontrada com ID: " + id + " para excluir.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir transação: " + e.getMessage());
            throw new RuntimeException("Erro ao excluir transação.", e);
        }
    }

    //Busca transacoes cujas descrições contêm o termo de busca
    public List<Transacao> findAllByDescriptionLike(String termoBusca) {
        List<Transacao> transacoes = new ArrayList<>();
        String sql = "SELECT id, descricao, valor, data, tipo, categoria_id FROM transacoes WHERE descricao LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + termoBusca + "%"); // Usa % para buscar parcial
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String descricao = rs.getString("descricao");
                    double valor = rs.getDouble("valor");
                    LocalDate data = rs.getDate("data").toLocalDate();
                    TipoCategoria tipo = TipoCategoria.valueOf(rs.getString("tipo"));
                    Integer categoriaId = rs.getObject("categoria_id", Integer.class);
                    Categoria categoria = null;
                    if (categoriaId != null) {
                        categoria = categoriaDAO.findById(categoriaId);
                    }
                    transacoes.add(new Transacao(id, descricao, valor, data, tipo, categoria));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar transações por termo na descrição: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transações por termo.", e);
        }
        return transacoes;
    }
}