from app.core.database import get_db_connection
import logging
import sqlite3

def get_relatorio_produtor(produtor_id: int):
    sql = """
        SELECT 
            e.execucao_id, e.data_execucao, e.horas_prestadas, e.valor_total,
            s.nome as servico_nome,
            COALESCE(SUM(p.valor_pago), 0.0) AS total_pago,
            (e.valor_total - COALESCE(SUM(p.valor_pago), 0.0)) AS saldo_devedor
        FROM 
            execucoes AS e
        INNER JOIN 
            servicos AS s ON e.servico_id = s.servico_id
        LEFT JOIN 
            pagamentos AS p ON e.execucao_id = p.execucao_id
        WHERE 
            e.produtor_id = ? AND e.deletado_em IS NULL
        GROUP BY 
            e.execucao_id
        ORDER BY 
            e.data_execucao DESC;
    """
    
    data = [] # Inicializa
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A CONSULTA E BUSCAR
        cursor = conn.cursor()
        cursor.execute(sql, (produtor_id,))
        data = cursor.fetchall() # Traz tudo para a memória
            
    except sqlite3.Error as e:
        logging.error(f"Erro ao buscar relatório para o produtor ID {produtor_id}: {e}")
        return {} # Retorna um dicionário vazio em caso de erro
    except Exception as e:
        logging.error(f"Erro inesperado em get_relatorio_produtor: {e}")
        return {}
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()

    # 4. PROCESSAR (Com a conexão já fechada)
    # (Este processamento já existia no seu código original)
    data_dict = {}
    for row in data:
        exec_id = row['execucao_id']
        data_dict[exec_id] = dict(row)
        
    return data_dict

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def get_relatorio_geral():
    
    # 1. PREPARAR (Sem conexão)
    sql = """
        SELECT 
            pr.produtor_id, pr.nome AS produtor_nome, pr.apelido AS produtor_apelido, pr.cpf,
            COUNT(DISTINCT e.execucao_id) AS total_servicos,
            SUM(e.valor_total) AS valor_total_devido,
            COALESCE(SUM(p.valor_pago), 0.0) AS valor_total_pago,
            (COALESCE(SUM(e.valor_total), 0) - COALESCE(SUM(p.valor_pago), 0.0)) AS saldo_devedor
        FROM 
            produtores AS pr
        LEFT JOIN 
            execucoes AS e ON pr.produtor_id = e.produtor_id AND e.deletado_em IS NULL
        LEFT JOIN 
            pagamentos AS p ON e.execucao_id = p.execucao_id AND p.deletado_em IS NULL
        WHERE
            pr.deletado_em IS NULL
        GROUP BY 
            pr.produtor_id, pr.nome, pr.apelido, pr.cpf
        ORDER BY 
            saldo_devedor DESC, produtor_nome ASC;
    """
    
    data = []
    conn = get_db_connection() # 2. ABRIR A CONEXÃO
    
    try:
        # 3. EXECUTAR A CONSULTA E BUSCAR
        cursor = conn.cursor()
        cursor.execute(sql)
        data = cursor.fetchall() # Traz tudo para a memória
            
    except sqlite3.Error as e:
        logging.error(f"Erro ao buscar relatório geral: {e}")
        return [] # Retorna lista vazia em caso de erro
    except Exception as e:
        logging.error(f"Erro inesperado em get_relatorio_geral: {e}")
        return []
    finally:
        # 4. GARANTIR O FECHAMENTO
        if conn:
            conn.close()

    # 5. PROCESSAR (Com a conexão já fechada)
    # (Este processamento já existia no seu código original)
    data_dict = []
    for row in data:
        data_dict.append(dict(row))
        
    return data_dict

# CÓDIGO CORRIGIDO (Garantindo o conn.close() e com exceções robustas)

def purge_deleted_records():
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    tables = ['produtores', 'servicos', 'execucoes', 'pagamentos']
    total_deleted = 0
    
    try:
        # 2. EXECUTAR A TRANSAÇÃO
        cursor = conn.cursor()
        for table in tables:
            sql = f"DELETE FROM {table} WHERE deletado_em IS NOT NULL"
            cursor.execute(sql)
            total_deleted += cursor.rowcount
        
        conn.commit()
        logging.info(f"Registros 'deletados' purgados: {total_deleted} entradas removidas.")
        return True, total_deleted
        
    except sqlite3.Error as e: # Captura Genérica (CORRIGIDA)
        logging.error(f"Erro ao purgar registros: {e}")
        if conn: # Tenta o rollback
            conn.rollback()
        return False, 0
    except Exception as e:
         logging.error(f"Erro inesperado em purge_deleted_records: {e}")
         if conn:
            conn.rollback()
         return False, 0
    finally:
        # 3. GARANTIR O FECHAMENTO (Isto já estava correto!)
        if conn:
            conn.close()