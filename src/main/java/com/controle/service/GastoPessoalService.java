package com.controle.service;

import com.controle.dao.CategoriaDAO;
import com.controle.dao.TransacaoDAO;
import com.controle.model.Categoria;
import com.controle.model.Transacao;
import com.controle.model.TipoCategoria;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Camada de serviço para gerenciar a lógica de negócio do Sistema de Controle de Gastos Pessoais
public class GastoPessoalService {

    private CategoriaDAO categoriaDAO;
    private TransacaoDAO transacaoDAO;

    public GastoPessoalService() {
        this.categoriaDAO = new CategoriaDAO();
        this.transacaoDAO = new TransacaoDAO();
    }

    // --- Métodos para Categoria ---

    public Categoria adicionarCategoria(String nome, TipoCategoria tipo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da categoria não pode ser vazio.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da categoria não pode ser nulo.");
        }

        if (categoriaDAO.findByNome(nome.trim()) != null) {
            throw new IllegalArgumentException("Categoria com o nome '" + nome + "' já existe.");
        }

        Categoria novaCategoria = new Categoria(nome.trim(), tipo);
        categoriaDAO.save(novaCategoria);
        return novaCategoria;
    }

    public Categoria buscarCategoriaPorId(int id) {
        return categoriaDAO.findById(id);
    }

    public Categoria buscarCategoriaPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        return categoriaDAO.findByNome(nome.trim());
    }

    public List<Categoria> listarTodasCategorias() {
        return categoriaDAO.findAll();
    }

    public List<Categoria> listarCategoriasPorTermo(String termoBusca) {
        if (termoBusca == null || termoBusca.trim().isEmpty()) {
            return categoriaDAO.findAll();
        }
        return categoriaDAO.findAllByNomeLike(termoBusca.trim());
    }

    public void atualizarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getId() <= 0) {
            throw new IllegalArgumentException("Categoria inválida para atualização.");
        }
        Categoria existente = categoriaDAO.findByNome(categoria.getNome());
        if (existente != null && existente.getId() != categoria.getId()) {
            throw new IllegalArgumentException("Outra categoria já existe com o nome '" + categoria.getNome() + "'.");
        }
        categoriaDAO.update(categoria);
    }

    public void excluirCategoria(int id) {
        categoriaDAO.delete(id);
    }

    // --- Métodos para Transação ---

    public Transacao adicionarTransacao(String descricao, double valor, LocalDate data, TipoCategoria tipo, String categoriaNome) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("A descricao da transação não pode ser vazia.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser positivo.");
        }
        if (data == null || data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data da transação não pode ser nula ou futura.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da transação não pode ser nulo.");
        }

        Categoria categoria = null;
        if (categoriaNome != null && !categoriaNome.trim().isEmpty()) {
            categoria = categoriaDAO.findByNome(categoriaNome.trim());
            if (categoria == null) {
                throw new IllegalArgumentException("Categoria '" + categoriaNome + "' não encontrada. Crie-a primeiro.");
            }
            if (categoria.getTipo() != tipo) {
                throw new IllegalArgumentException("O tipo da transação (" + tipo + ") não corresponde ao tipo da categoria '" + categoriaNome + "' (" + categoria.getTipo() + ").");
            }
        }

        Transacao novaTransacao = new Transacao(descricao.trim(), valor, data, tipo, categoria);
        transacaoDAO.save(novaTransacao);
        return novaTransacao;
    }

    public Transacao buscarTransacaoPorId(int id) {
        return transacaoDAO.findById(id);
    }

    // Método existente que lista todas as transações
    public List<Transacao> listarTodasTransacoes() {
        return transacaoDAO.findAll();
    }

    //Lista transacoes filtradas por um termo de busca na descricao
    public List<Transacao> listarTransacoesPorTermo(String termoBusca) {
        if (termoBusca == null || termoBusca.trim().isEmpty()) {
            return transacaoDAO.findAll(); // Se o termo for vazio, retorna todas
        }
        return transacaoDAO.findAllByDescriptionLike(termoBusca.trim());
    }


    public List<Transacao> listarTransacoesPorTipo(TipoCategoria tipo) {
        return transacaoDAO.findAll().stream()
                .filter(t -> t.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    public void atualizarTransacao(Transacao transacao, String novaCategoriaNome) {
        if (transacao == null || transacao.getId() <= 0) {
            throw new IllegalArgumentException("Transação inválida para atualização.");
        }
        if (transacao.getValor() <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser positivo.");
        }
        if (transacao.getData() == null || transacao.getData().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data da transação não pode ser nula ou futura.");
        }
        if (transacao.getTipo() == null) {
            throw new IllegalArgumentException("O tipo da transação não pode ser nulo.");
        }

        Categoria categoriaAssociada = null;
        if (novaCategoriaNome != null && !novaCategoriaNome.trim().isEmpty()) {
            categoriaAssociada = categoriaDAO.findByNome(novaCategoriaNome.trim());
            if (categoriaAssociada == null) {
                throw new IllegalArgumentException("Categoria '" + novaCategoriaNome + "' não encontrada para a transação.");
            }
            if (categoriaAssociada.getTipo() != transacao.getTipo()) {
                throw new IllegalArgumentException("O tipo da transação (" + transacao.getTipo() + ") não corresponde ao tipo da categoria '" + categoriaAssociada.getNome() + "' (" + categoriaAssociada.getTipo() + ").");
            }
        }
        transacao.setCategoria(categoriaAssociada);

        transacaoDAO.update(transacao);
    }

    public void excluirTransacao(int id) {
        transacaoDAO.delete(id);
    }

    // --- Métodos para Relatórios ---

    public double calcularBalancoTotal(LocalDate inicio, LocalDate fim) {
        List<Transacao> transacoesNoPeriodo = transacaoDAO.findAll().stream()
                .filter(t -> !t.getData().isBefore(inicio) && !t.getData().isAfter(fim))
                .collect(Collectors.toList());

        double totalReceitas = transacoesNoPeriodo.stream()
                .filter(t -> t.getTipo() == TipoCategoria.RECEITA)
                .mapToDouble(Transacao::getValor)
                .sum();
        double totalDespesas = transacoesNoPeriodo.stream()
                .filter(t -> t.getTipo() == TipoCategoria.DESPESA)
                .mapToDouble(Transacao::getValor)
                .sum();
        return totalReceitas - totalDespesas;
    }

    public double calcularTotalReceitas(LocalDate inicio, LocalDate fim) {
        return transacaoDAO.findAll().stream()
                .filter(t -> t.getTipo() == TipoCategoria.RECEITA &&
                        !t.getData().isBefore(inicio) &&
                        !t.getData().isAfter(fim))
                .mapToDouble(Transacao::getValor)
                .sum();
    }

    public double calcularTotalDespesas(LocalDate inicio, LocalDate fim) {
        return transacaoDAO.findAll().stream()
                .filter(t -> t.getTipo() == TipoCategoria.DESPESA &&
                        !t.getData().isBefore(inicio) &&
                        !t.getData().isAfter(fim))
                .mapToDouble(Transacao::getValor)
                .sum();
    }

    public Map<String, Double> calcularDespesasPorCategoria(LocalDate inicio, LocalDate fim) {
        return transacaoDAO.findAll().stream()
                .filter(t -> t.getTipo() == TipoCategoria.DESPESA &&
                        !t.getData().isBefore(inicio) &&
                        !t.getData().isAfter(fim))
                .collect(Collectors.groupingBy(
                        t -> t.getCategoria() != null ? t.getCategoria().getNome() : "Sem Categoria",
                        Collectors.summingDouble(Transacao::getValor)
                ));
    }
}