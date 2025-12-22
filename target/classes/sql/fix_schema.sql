-- 1. Desligar verificação de chaves estrangeiras temporariamente
PRAGMA foreign_keys=off;

-- 2. Iniciar transação para segurança
BEGIN TRANSACTION;

-- 3. Renomear a tabela problemática atual
ALTER TABLE execucoes RENAME TO execucoes_old;

-- 4. Criar a nova tabela com a tipagem CORRETA (DATE para data_execucao)
CREATE TABLE execucoes (
    execucao_id INTEGER PRIMARY KEY AUTOINCREMENT,
    produtor_id INTEGER NOT NULL,
    servico_id INTEGER NOT NULL,
    data_execucao DATE NOT NULL,      -- <--- AQUI ESTÁ A MÁGICA: Tipo DATE
    horas_prestadas DECIMAL(10,2),
    valor_total DECIMAL(10,2),
    deletado_em DATETIME,             -- Este continua como DATETIME
    FOREIGN KEY(produtor_id) REFERENCES produtores(produtor_id),
    FOREIGN KEY(servico_id) REFERENCES servicos(servico_id)
);

-- 5. Copiar os dados da tabela antiga para a nova
INSERT INTO execucoes (execucao_id, produtor_id, servico_id, data_execucao, horas_prestadas, valor_total, deletado_em)
SELECT execucao_id, produtor_id, servico_id, data_execucao, horas_prestadas, valor_total, deletado_em
FROM execucoes_old;

-- 6. Remover a tabela antiga
DROP TABLE execucoes_old;

-- 7. Efetivar as mudanças
COMMIT;

-- 8. Religar chaves estrangeiras
PRAGMA foreign_keys=on;