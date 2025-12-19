from app.core.database import get_db_connection
from app.models.servico_model import Servico
import logging
import sqlite3

def get_servico_by_id(servico_id: int):
    sql = "SELECT * FROM servicos WHERE servico_id = ? AND deletado_em IS NULL"
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A CONSULTA
        cursor = conn.cursor()
        cursor.execute(sql, (servico_id,))
        data = cursor.fetchone()     
            
        if data is None:
            return None 
            
        return Servico(
            servico_id=data['servico_id'],
            nome=data['nome'],
            valor_unitario=data['valor_unitario']
        )

    except sqlite3.Error as e:
        logging.error(f"Erro ao buscar serviço ID {servico_id}: {e}")
        return None
    except Exception as e:
        logging.error(f"Erro inesperado em get_servico_by_id: {e}")
        return None
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def get_all_servicos(page: int, per_page: int):
    
    # 1. PREPARAR (Sem conexão)
    offset = (page - 1) * per_page
    sql = "SELECT * FROM servicos WHERE deletado_em IS NULL ORDER BY nome LIMIT ? OFFSET ?"
    
    rows = []
    conn = get_db_connection() # 2. ABRIR A CONEXÃO
    
    try:
        # 3. EXECUTAR A CONSULTA E BUSCAR
        cursor = conn.cursor()
        cursor.execute(sql, (per_page, offset))
        rows = cursor.fetchall() # Traz tudo para a memória
            
    except sqlite3.Error as e:
        logging.error(f"Erro ao buscar todos os serviços: {e}")
        return [] # Retorna lista vazia em caso de erro
    except Exception as e:
        logging.error(f"Erro inesperado em get_all_servicos: {e}")
        return []
    finally:
        # 4. GARANTIR O FECHAMENTO
        if conn:
            conn.close()

    # 5. PROCESSAR (Com a conexão já fechada)
    lista_servicos = []
    for row in rows:
        lista_servicos.append(
            Servico(
                servico_id=row['servico_id'],
                nome=row['nome'],
                valor_unitario=row['valor_unitario']
            )
        )
        
    return lista_servicos

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def get_servicos_count():
    sql = "SELECT COUNT(*) FROM servicos WHERE deletado_em IS NULL"
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A CONSULTA
        cursor = conn.cursor()
        cursor.execute(sql)
        total = cursor.fetchone()[0]
        
        return total

    except sqlite3.Error as e:
        logging.error(f"Erro ao contar serviços: {e}")
        return 0 # Retorna 0 em caso de erro
    except Exception as e:
        logging.error(f"Erro inesperado em get_servicos_count: {e}")
        return 0
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()
            
# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def create_servico(servico: Servico):
    sql = """INSERT INTO servicos (nome, valor_unitario) VALUES (?, ?)"""
    params = (servico.nome, servico.valor_unitario)
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A TRANSAÇÃO
        cursor = conn.cursor()
        cursor.execute(sql, params)
        servico.servico_id = cursor.lastrowid
        conn.commit()
        
        return servico
        
    except sqlite3.IntegrityError as e: # Captura Específica (a do log!)
        # Este log era o do erro que vimos:
        logging.error(f"Erro: O nome de serviço '{servico.nome}' já existe. {e}") 
        return None
    except sqlite3.Error as e: # Captura Genérica
        logging.error(f"Erro no banco ao criar serviço: {e}")
        return None
    except Exception as e:
         logging.error(f"Erro inesperado em create_servico: {e}")
         return None
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()
            
# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def update_servico(servico: Servico):
    sql = """
        UPDATE servicos 
        SET nome = ?, valor_unitario = ?
        WHERE servico_id = ?
    """
    params = (
        servico.nome, 
        servico.valor_unitario,
        servico.servico_id
    )
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A TRANSAÇÃO
        cursor = conn.cursor()
        cursor.execute(sql, params)
        conn.commit()
        
        logging.info(f"Serviço atualizado! ID: {servico.servico_id}")
        return servico   
        
    except sqlite3.IntegrityError as e: # Captura Específica (CORRIGIDA)
        logging.error(f"Erro: O nome de serviço '{servico.nome}' já existe. {e}")
        return None 
    except sqlite3.Error as e: # Captura Genérica (para "database locked", etc.)
        logging.error(f"Erro no banco ao atualizar serviço ID {servico.servico_id}: {e}")
        return None
    except Exception as e:
         logging.error(f"Erro inesperado em update_servico: {e}")
         return None
    finally:
        # 3. GARANTIR O FECHAMENTO (Isto já estava correto!)
        if conn:
            conn.close()

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def delete_servico(servico_id: int):
    sql = "UPDATE servicos SET deletado_em = CURRENT_TIMESTAMP WHERE servico_id = ?"
    updated = False # Inicializa fora
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A TRANSAÇÃO
        cursor = conn.cursor()
        cursor.execute(sql, (servico_id,))
        conn.commit()
        updated = cursor.rowcount > 0 # Atualiza a variável
            
    except sqlite3.Error as e: # Captura Genérica (corrigido de conn.Error)
        logging.error(f"Erro ao tentar fazer soft delete do Servico ID {servico_id}: {e}")
        return False # Retorna False em caso de erro no banco
    except Exception as e:
        logging.error(f"Erro inesperado em delete_servico: {e}")
        return False
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()
            
    # 4. LÓGICA DE LOG (Fora do try, com a conexão já fechada)
    if updated:
        logging.info(f"Soft delete do Servico ID: {servico_id} realizado com sucesso.")
    else:
        logging.warning(f"Tentativa de soft delete do Servico ID: {servico_id} falhou (ID não encontrado).")
    
    return updated

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def get_servico_by_nome(nome: str):
    sql = "SELECT * FROM servicos WHERE nome = ? AND deletado_em IS NULL"
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A CONSULTA
        cursor = conn.cursor()
        cursor.execute(sql, (nome,))
        data = cursor.fetchone()
            
        if data is None:
            return None
            
        return Servico(
            servico_id=data['servico_id'],
            nome=data['nome'],
            valor_unitario=data['valor_unitario']
        )

    except sqlite3.Error as e: # Captura genérica do sqlite3
        logging.error(f"Erro ao buscar serviço pelo nome '{nome}': {e}")
        return None
    except Exception as e: # Captura para qualquer outro erro
        logging.error(f"Erro inesperado em get_servico_by_nome: {e}")
        return None
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()