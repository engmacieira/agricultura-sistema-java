from app.core.database import get_db_connection
from app.models.pagamento_model import Pagamento
import logging
import sqlite3

def get_pagamento_by_id(pagamento_id: int):
    sql = "SELECT * FROM pagamentos WHERE pagamento_id = ? AND deletado_em IS NULL"
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A CONSULTA
        cursor = conn.cursor()
        cursor.execute(sql, (pagamento_id,))
        data = cursor.fetchone()
        
        if data is None:
            return None
            
        return Pagamento(
            pagamento_id=data['pagamento_id'],
            execucao_id=data['execucao_id'],
            valor_pago=data['valor_pago'],
            data_pagamento=data['data_pagamento']
        )

    except sqlite3.Error as e:
        logging.error(f"Erro ao buscar pagamento ID {pagamento_id}: {e}")
        return None
    except Exception as e:
        logging.error(f"Erro inesperado em get_pagamento_by_id: {e}")
        return None
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def get_pagamentos_by_execucao_id(execucao_id: int):
    
    # 1. PREPARAR (Sem conexão)
    sql = "SELECT * FROM pagamentos WHERE execucao_id = ? AND deletado_em IS NULL ORDER BY data_pagamento"
    
    rows = []
    conn = get_db_connection() # 2. ABRIR A CONEXÃO
    
    try:
        # 3. EXECUTAR A CONSULTA E BUSCAR
        cursor = conn.cursor()
        cursor.execute(sql, (execucao_id,))
        rows = cursor.fetchall() # Traz tudo para a memória
            
    except sqlite3.Error as e:
        logging.error(f"Erro ao buscar pagamentos para a execução ID {execucao_id}: {e}")
        return [] # Retorna lista vazia em caso de erro
    except Exception as e:
        logging.error(f"Erro inesperado em get_pagamentos_by_execucao_id: {e}")
        return []
    finally:
        # 4. GARANTIR O FECHAMENTO
        if conn:
            conn.close()

    # 5. PROCESSAR (Com a conexão já fechada)
    lista_pagamentos = []
    for row in rows:
        lista_pagamentos.append(
            Pagamento(
                pagamento_id=row['pagamento_id'],
                execucao_id=row['execucao_id'],
                valor_pago=row['valor_pago'],
                data_pagamento=row['data_pagamento']
            )
        )
        
    return lista_pagamentos

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def create_pagamento(pagamento: Pagamento):
    sql = """INSERT INTO pagamentos (execucao_id, valor_pago, data_pagamento) 
             VALUES (?, ?, ?)"""
    params = (pagamento.execucao_id, pagamento.valor_pago, pagamento.data_pagamento)
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A TRANSAÇÃO
        cursor = conn.cursor()
        cursor.execute(sql, params)
        pagamento.pagamento_id = cursor.lastrowid
        conn.commit()
        
        return pagamento
        
    except sqlite3.IntegrityError as e: # Captura Específica (FK constraint)
        logging.error(f"Erro de integridade ao criar pagamento: {e}")
        logging.error("Verifique se o 'execucao_id' existe.")
        return None
    except sqlite3.Error as e: # Captura Genérica
        logging.error(f"Erro no banco ao criar pagamento: {e}")
        return None
    except Exception as e:
         logging.error(f"Erro inesperado em create_pagamento: {e}")
         return None
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()

# CÓDIGO CORRIGIDO (Garantindo o conn.close() e com exceções robustas)

def update_pagamento(pagamento: Pagamento):
    sql = """
        UPDATE pagamentos 
        SET execucao_id = ?, valor_pago = ?, data_pagamento = ?
        WHERE pagamento_id = ?
    """
    params = (
        pagamento.execucao_id,
        pagamento.valor_pago,
        pagamento.data_pagamento,
        pagamento.pagamento_id 
    )
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A TRANSAÇÃO
        cursor = conn.cursor()
        cursor.execute(sql, params)
        conn.commit()
        
        logging.info(f"Pagamento atualizado! ID: {pagamento.pagamento_id}")
        return pagamento   
        
    except sqlite3.IntegrityError as e: # Captura Específica (CORRIGIDA)
        logging.error(f"Erro de integridade ao atualizar pagamento: {e}")
        logging.error("Verifique se o 'execucao_id' existe.")
        return None 
    except sqlite3.Error as e: # Captura Genérica (para "database locked", etc.)
        logging.error(f"Erro no banco ao atualizar pagamento ID {pagamento.pagamento_id}: {e}")
        return None
    except Exception as e:
         logging.error(f"Erro inesperado em update_pagamento: {e}")
         return None
    finally:
        # 3. GARANTIR O FECHAMENTO (Isto já estava correto!)
        if conn:
            conn.close()

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def delete_pagamento(pagamento_id: int):
    sql = "UPDATE pagamentos SET deletado_em = CURRENT_TIMESTAMP WHERE pagamento_id = ?"
    updated = False # Inicializa fora
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A TRANSAÇÃO
        cursor = conn.cursor()
        cursor.execute(sql, (pagamento_id,))
        conn.commit()
        updated = cursor.rowcount > 0 # Atualiza a variável
            
    except sqlite3.Error as e: # Captura Genérica (corrigido de conn.Error)
        logging.error(f"Erro ao tentar fazer soft delete do Pagamento ID {pagamento_id}: {e}")
        return False # Retorna False em caso de erro no banco
    except Exception as e:
        logging.error(f"Erro inesperado em delete_pagamento: {e}")
        return False
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()
            
    # 4. LÓGICA DE LOG (Fora do try, com a conexão já fechada)
    if updated:
        logging.info(f"Soft delete do Pagamento ID: {pagamento_id} realizado com sucesso.")
    else:
        logging.warning(f"Tentativa de soft delete do Pagamento ID: {pagamento_id} falhou (ID não encontrado).")
    
    return updated