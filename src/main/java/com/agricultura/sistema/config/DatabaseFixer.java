package com.agricultura.sistema.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;

//@Component
public class DatabaseFixer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        System.out.println("=============================================");
        System.out.println(">>> [DB FIXER] INICIANDO MIGRAÇÃO FORÇADA <<<");
        System.out.println("=============================================");

        try {
            // 1. Limpeza preventiva: Se a tabela temporária já existir (de um erro
            // anterior), apaga.
            try {
                jdbcTemplate.execute("DROP TABLE IF EXISTS execucoes_old");
            } catch (Exception e) {
                // Ignora erro se tabela não existir
            }

            // 2. Desliga chaves estrangeiras e inicia transação
            jdbcTemplate.execute("PRAGMA foreign_keys=off");
            jdbcTemplate.execute("BEGIN TRANSACTION");

            // 3. Renomeia a tabela atual problemática
            System.out.println(">>> Renomeando tabela antiga...");
            jdbcTemplate.execute("ALTER TABLE execucoes RENAME TO execucoes_old");

            // 4. Cria a nova tabela com o tipo DATE (A Correção Real)
            System.out.println(">>> Criando nova tabela correta...");
            String createSql = """
                        CREATE TABLE execucoes (
                            execucao_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            produtor_id INTEGER NOT NULL,
                            servico_id INTEGER NOT NULL,
                            data_execucao DATE NOT NULL,
                            horas_prestadas DECIMAL(10,2),
                            valor_total DECIMAL(10,2),
                            deletado_em DATETIME,
                            FOREIGN KEY(produtor_id) REFERENCES produtores(produtor_id),
                            FOREIGN KEY(servico_id) REFERENCES servicos(servico_id)
                        )
                    """;
            jdbcTemplate.execute(createSql);

            // 5. Copia os dados (Explicitando colunas para evitar erro de ordem)
            System.out.println(">>> Migrando dados...");
            String copySql = """
                        INSERT INTO execucoes
                        (execucao_id, produtor_id, servico_id, data_execucao, horas_prestadas, valor_total, deletado_em)
                        SELECT
                        execucao_id, produtor_id, servico_id, data_execucao, horas_prestadas, valor_total, deletado_em
                        FROM execucoes_old
                    """;
            jdbcTemplate.execute(copySql);

            // 6. Limpa a bagunça e finaliza
            jdbcTemplate.execute("DROP TABLE execucoes_old");
            jdbcTemplate.execute("COMMIT");
            jdbcTemplate.execute("PRAGMA foreign_keys=on");

            System.out.println("==================================================");
            System.out.println(">>> [SUCESSO] TABELA EXECUCOES CORRIGIDA!      <<<");
            System.out.println(">>> AGORA PODE COMENTAR O @Component DESTE ARQUIVO <<<");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println(">>> [ERRO CRÍTICO] FALHA NA MIGRAÇÃO: " + e.getMessage());
            e.printStackTrace();
            try {
                jdbcTemplate.execute("ROLLBACK");
            } catch (Exception ex) {
            }
        }
    }
}