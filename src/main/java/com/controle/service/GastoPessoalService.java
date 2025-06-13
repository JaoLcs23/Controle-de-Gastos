package com.controle.service;

import com.controle.dao.CategoriaDAO;
import com.controle.dao.TransacaoDAO;
import com.controle.model.Categoria;
import com.controle.model.Transacao;
import com.controle.model.TipoCategoria;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors; // Para operar em colecoes de dados

public class GastoPessoalService {

    private CategoriaDAO categoriaDAO;
    private TransacaoDAO transacaoDAO;

    //Construtor da classe de servico
    public GastoPessoalService() {
        this.categoriaDAO = new CategoriaDAO();
        this.transacaoDAO = new TransacaoDAO();
    }

    //Metodos para Categoria
    public Categoria adicionarCategoria(String nome, TipoCategoria tipo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da categoria não pode ser vazio.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da categoria não pode ser nulo.");
        }

        // Verificar se a categoria ja existe pelo nome
        if (categoriaDAO.findByNome(nome.trim()) != null) {
            throw new IllegalArgumentException("Categoria com o nome '" + nome + "' ja existe.");
        }

        Categoria novaCategoria = new Categoria(nome.trim(), tipo);
        categoriaDAO.save(novaCategoria); // Salva no banco de dados, o ID sera atualizado no objeto
        return novaCategoria;
    }

    //Busca uma categoria pelo seu ID
    public Categoria buscarCategoriaPorId(int id) {
        return categoriaDAO.findById(id);
    }

    //Busca uma categoria pelo seu nome
    public Categoria buscarCategoriaPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        return categoriaDAO.findByNome(nome.trim());
    }

    //Lista todas as categorias cadastradas no sistema
    public List<Categoria> listarTodasCategorias() {
        return categoriaDAO.findAll();
    }

    //Atualiza os dados de uma categoria existente
    public void atualizarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getId() <= 0) {
            throw new IllegalArgumentException("Categoria invalida para atualizacao.");
        }
        // Verificar se o novo nome ja existe para outra categoria
        Categoria existente = categoriaDAO.findByNome(categoria.getNome());
        if (existente != null && existente.getId() != categoria.getId()) {
            throw new IllegalArgumentException("Outra categoria ja existe com o nome '" + categoria.getNome() + "'.");
        }
        categoriaDAO.update(categoria);
    }

    //Exclui uma categoria pelo seu ID
    public void excluirCategoria(int id) {
        categoriaDAO.delete(id);
    }

    //Metodos para Transacao

    //Adiciona uma nova transacao (gasto ou receita) ao sistema
    public Transacao adicionarTransacao(String descricao, double valor, LocalDate data, TipoCategoria tipo, String categoriaNome) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("A descricao da transacao nao pode ser vazia.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor da transacao deve ser positivo.");
        }
        if (data == null || data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data da transacao nao pode ser nula ou futura.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da transacao nao pode ser nulo.");
        }

        Categoria categoria = null;
        if (categoriaNome != null && !categoriaNome.trim().isEmpty()) {
            categoria = categoriaDAO.findByNome(categoriaNome.trim());
            if (categoria == null) {
                throw new IllegalArgumentException("Categoria '" + categoriaNome + "' nao encontrada. Crie-a primeiro.");
            }
            // Garante que o tipo da transacao corresponda ao tipo da categoria
            if (categoria.getTipo() != tipo) {
                throw new IllegalArgumentException("O tipo da transacao (" + tipo + ") nao corresponde ao tipo da categoria '" + categoriaNome + "' (" + categoria.getTipo() + ").");
            }
        }

        Transacao novaTransacao = new Transacao(descricao.trim(), valor, data, tipo, categoria);
        transacaoDAO.save(novaTransacao);
        return novaTransacao;
    }

    //Busca uma transacao pelo seu ID
    public Transacao buscarTransacaoPorId(int id) {
        return transacaoDAO.findById(id);
    }

    //Lista todas as transacoes cadastradas no sistema
    public List<Transacao> listarTodasTransacoes() {
        return transacaoDAO.findAll();
    }

    //Lista as transacoes de um determinado tipo (RECEITA ou DESPESA)
    public List<Transacao> listarTransacoesPorTipo(TipoCategoria tipo) {
        return transacaoDAO.findAll().stream()
                .filter(t -> t.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    //Calcula o balanco total do usuario (Receitas - Despesas)
    public double calcularBalancoTotal() {
        List<Transacao> todasTransacoes = transacaoDAO.findAll();
        double totalReceitas = todasTransacoes.stream()
                .filter(t -> t.getTipo() == TipoCategoria.RECEITA)
                .mapToDouble(Transacao::getValor)
                .sum();
        double totalDespesas = todasTransacoes.stream()
                .filter(t -> t.getTipo() == TipoCategoria.DESPESA)
                .mapToDouble(Transacao::getValor)
                .sum();
        return totalReceitas - totalDespesas;
    }

    //Calcula o total de despesas por categoria em um periodo
    public java.util.Map<String, Double> calcularDespesasPorCategoria(LocalDate inicio, LocalDate fim) {
        return transacaoDAO.findAll().stream()
                .filter(t -> t.getTipo() == TipoCategoria.DESPESA &&
                        !t.getData().isBefore(inicio) &&
                        !t.getData().isAfter(fim))
                .collect(Collectors.groupingBy(
                        t -> t.getCategoria() != null ? t.getCategoria().getNome() : "Sem Categoria",
                        Collectors.summingDouble(Transacao::getValor)
                ));
    }


    //Atualiza os dados de uma transacao existente
    public void atualizarTransacao(Transacao transacao, String novaCategoriaNome) {
        if (transacao == null || transacao.getId() <= 0) {
            throw new IllegalArgumentException("Transacao invalida para atualizacao.");
        }
        if (transacao.getValor() <= 0) {
            throw new IllegalArgumentException("O valor da transacao deve ser positivo.");
        }
        if (transacao.getData() == null || transacao.getData().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data da transacao nao pode ser nula ou futura.");
        }
        if (transacao.getTipo() == null) {
            throw new IllegalArgumentException("O tipo da transacao nao pode ser nulo.");
        }

        Categoria categoriaAssociada = null;
        if (novaCategoriaNome != null && !novaCategoriaNome.trim().isEmpty()) {
            categoriaAssociada = categoriaDAO.findByNome(novaCategoriaNome.trim());
            if (categoriaAssociada == null) {
                throw new IllegalArgumentException("Categoria '" + novaCategoriaNome + "' nao encontrada para a transacao.");
            }
            // Logica de negocio: Garante que o tipo da transacao corresponda ao tipo da categoria
            if (categoriaAssociada.getTipo() != transacao.getTipo()) {
                throw new IllegalArgumentException("O tipo da transacao (" + transacao.getTipo() + ") nao corresponde ao tipo da categoria '" + novaCategoriaNome + "' (" + categoriaAssociada.getTipo() + ").");
            }
        }
        transacao.setCategoria(categoriaAssociada); // Define a categoria no objeto transacao

        transacaoDAO.update(transacao);
    }

    //Exclui uma transacao pelo seu ID
    public void excluirTransacao(int id) {
        transacaoDAO.delete(id);
    }
}