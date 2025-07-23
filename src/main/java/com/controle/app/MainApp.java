package com.controle.app;

import com.controle.model.Categoria;
import com.controle.model.TipoCategoria;
import com.controle.model.Transacao;
import com.controle.service.GastoPessoalService;
import com.controle.util.DatabaseConnection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MainApp {

    public static void main(String[] args) {
        System.out.println("Iniciando o Sistema de Controle de Gastos Pessoais...");

        //Criar as tabelas no banco de dados (se nao existirem)
        try {
            DatabaseConnection.createTables();
            System.out.println("Tabelas verificadas/criadas no SQL Server.");
        } catch (Exception e) {
            System.err.println("Erro ao criar/verificar tabelas: " + e.getMessage());
            return; // Interrompe a execucao se nao puder criar tabelas
        }

        //Instanciar o servico principal
        GastoPessoalService service = new GastoPessoalService();
        System.out.println("\nServiço de Gastos Pessoais inicializado.");

        //Demonstração das funcionalidades

        //Adicionar Categorias
        System.out.println("\n--- Adicionando Categorias ---");
        Categoria salario = null;
        Categoria alimentacao = null;
        Categoria transporte = null;
        Categoria lazer = null;
        Categoria moradia = null;

        try {
            salario = service.adicionarCategoria("Salário", TipoCategoria.RECEITA);
            alimentacao = service.adicionarCategoria("Alimentação", TipoCategoria.DESPESA);
            transporte = service.adicionarCategoria("Transporte", TipoCategoria.DESPESA);
            lazer = service.adicionarCategoria("Lazer", TipoCategoria.DESPESA);
            moradia = service.adicionarCategoria("Moradia", TipoCategoria.DESPESA);
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao adicionar categoria: " + e.getMessage());
            // Tenta buscar se ja existe para continuar a demonstração
            salario = service.buscarCategoriaPorNome("Salário");
            alimentacao = service.buscarCategoriaPorNome("Alimentação");
            transporte = service.buscarCategoriaPorNome("Transporte");
            lazer = service.buscarCategoriaPorNome("Lazer");
            moradia = service.buscarCategoriaPorNome("Moradia");
        }

        //Listar todas as categorias
        System.out.println("\n--- Categorias Cadastradas ---");
        List<Categoria> categorias = service.listarTodasCategorias();
        categorias.forEach(System.out::println);

        //Adicionar Transações
        System.out.println("\n--- Adicionando Transações ---");
        try {
            service.adicionarTransacao("Salário Mensal", 3000.00, LocalDate.of(2025, 5, 30), TipoCategoria.RECEITA, "Salário");
            service.adicionarTransacao("Almoço no restaurante", 45.50, LocalDate.of(2025, 6, 1), TipoCategoria.DESPESA, "Alimentação");
            service.adicionarTransacao("Gasolina do carro", 80.00, LocalDate.of(2025, 6, 2), TipoCategoria.DESPESA, "Transporte");
            service.adicionarTransacao("Cinema com a namorada", 55.00, LocalDate.of(2025, 6, 2), TipoCategoria.DESPESA, "Lazer");
            service.adicionarTransacao("Conta de luz", 120.00, LocalDate.of(2025, 6, 3), TipoCategoria.DESPESA, "Moradia");
            service.adicionarTransacao("Freelance Marketing", 700.00, LocalDate.of(2025, 6, 7), TipoCategoria.RECEITA, "Salário");
            service.adicionarTransacao("Mercado da semana", 150.00, LocalDate.of(2025, 6, 8), TipoCategoria.DESPESA, "Alimentação");
            service.adicionarTransacao("Uber para trabalho", 25.00, LocalDate.of(2025, 6, 8), TipoCategoria.DESPESA, "Transporte");
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao adicionar transação: " + e.getMessage());
        }

        //Listar todas as transações
        System.out.println("\n--- Todas as Transações ---");
        List<Transacao> todasTransacoes = service.listarTodasTransacoes();
        todasTransacoes.forEach(System.out::println);

        // Definindo um periodo muito abrangente para o balanco geral do console
        LocalDate inicioGeral = LocalDate.of(1900, 1, 1); // Uma data bem antiga
        LocalDate fimGeral = LocalDate.now(); // Até a data atual

        //Calcular Balanço Total (primeira chamada)
        System.out.println("\n--- Balanço Total ---");
        double balanco = service.calcularBalancoTotal(inicioGeral, fimGeral);
        System.out.printf("Balanço Total: R$ %.2f%n", balanco);

        //Calcular Despesas por Categoria em um período
        System.out.println("\n--- Despesas por Categoria (Maio-Junho 2025) ---");
        LocalDate inicioPeriodo = LocalDate.of(2025, 5, 1);
        LocalDate fimPeriodo = LocalDate.of(2025, 6, 30);
        Map<String, Double> despesasPorCategoria = service.calcularDespesasPorCategoria(inicioPeriodo, fimPeriodo);
        despesasPorCategoria.forEach((cat, total) -> System.out.printf("%s: R$ %.2f%n", cat, total));

        //Atualizar uma transação
        System.out.println("\n--- Atualizando uma Transação ---");

        Transacao transacaoParaAtualizar = service.buscarTransacaoPorId(2); // Atualizando "Almoço no restaurante"
        if (transacaoParaAtualizar != null) {
            System.out.println("Transação antes da atualização: " + transacaoParaAtualizar);
            transacaoParaAtualizar.setDescricao("Almoço com cliente");
            transacaoParaAtualizar.setValor(60.00);
            try {
                service.atualizarTransacao(transacaoParaAtualizar, "Alimentação");
                System.out.println("Transação após atualização: " + service.buscarTransacaoPorId(2));
            } catch (IllegalArgumentException e) {
                System.err.println("Erro ao atualizar transação: " + e.getMessage());
            }
        } else {
            System.out.println("Transação com ID 2 não encontrada para atualizar.");
        }

        //Excluir uma transacao
        System.out.println("\n--- Excluindo uma Transacao ---");
        int idParaExcluir = 4; // Excluir "Cinema com amigos"
        service.excluirTransacao(idParaExcluir);
        Transacao transacaoExcluida = service.buscarTransacaoPorId(idParaExcluir);
        if (transacaoExcluida == null) {
            System.out.println("Transacao com ID " + idParaExcluir + " foi realmente excluída.");
        } else {
            System.out.println("Erro: Transação com ID " + idParaExcluir + " ainda existe.");
        }

        //Verificar balanço após exclusão
        System.out.println("\n--- Balanço Total Após Exclusão ---");
        balanco = service.calcularBalancoTotal(inicioGeral, fimGeral);
        System.out.printf("Novo Balanço Total: R$ %.2f%n", balanco);

        System.out.println("\nSistema de Controle de Gastos Pessoais finalizado.");
    }
}