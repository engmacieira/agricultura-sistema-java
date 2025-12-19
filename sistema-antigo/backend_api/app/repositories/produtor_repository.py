from app.core.database import get_db_connection
from app.models.produtor_model import Produtor
import logging
import sqlite3

# CÓDIGO CORRIGIDO (Garantindo o conn.close())
def get_produtor_by_id(produtor_id: int):
    sql = "SELECT * FROM produtores WHERE produtor_id = ? AND deletado_em IS NULL"
    conn = get_db_connection() # Abre a conexão
    
    try:
        cursor = conn.cursor()
        cursor.execute(sql, (produtor_id,))
        data = cursor.fetchone() 
            
        if data is None:
            return None 
            
        return Produtor(
            produtor_id=data['produtor_id'],
            nome=data['nome'],
            apelido=data['apelido'],
            cpf=data['cpf'],
            regiao=data['regiao'],
            referencia=data['referencia'],
            telefone=data['telefone']
        )

    except sqlite3.Error as e:
        logging.error(f"Erro ao buscar produtor ID {produtor_id}: {e}")
        return None
    except Exception as e:
        logging.error(f"Erro inesperado em get_produtor_by_id: {e}")
        return None
        
    finally:
        # Esta é a parte mais importante
        if conn:
            conn.close()
            
# CÓDIGO NOVO (Refatorado com 'with' e melhorias)

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def get_all_produtores(page: int, per_page: int, search_term: str = None):
    
    # 1. PREPARAR (Sem conexão)
    offset = (page - 1) * per_page
    params = []
    where_clause = "WHERE deletado_em IS NULL"
    
    if search_term:
        where_clause += " AND (nome LIKE ? OR apelido LIKE ? OR cpf LIKE ?)"
        search_like = f"%{search_term}%"
        params.extend([search_like, search_like, search_like])
        
    sql = f"SELECT * FROM produtores {where_clause} ORDER BY nome LIMIT ? OFFSET ?;"
    params.extend([per_page, offset])
    
    rows = []
    conn = get_db_connection() # 2. ABRIR A CONEXÃO
    
    try:
        # 3. EXECUTAR A CONSULTA
        cursor = conn.cursor()
        cursor.execute(sql, params)
        rows = cursor.fetchall() # Traz tudo para a memória
            
    except sqlite3.Error as e:
        logging.error(f"Erro ao buscar todos os produtores: {e}")
        return [] # Retorna lista vazia em caso de erro
    except Exception as e:
        logging.error(f"Erro inesperado em get_all_produtores: {e}")
        return []
    finally:
        # 4. GARANTIR O FECHAMENTO
        if conn:
            conn.close()

    # 5. PROCESSAR (Com a conexão já fechada)
    lista_produtores = []
    for row in rows:
        lista_produtores.append(
            Produtor(
                produtor_id=row['produtor_id'],
                nome=row['nome'],
                apelido=row['apelido'],
                cpf=row['cpf'],
                regiao=row['regiao'],
                referencia=row['referencia'],
                telefone=row['telefone']
            )
        )
        
    return lista_produtores

# CÓDIGO NOVO (Refatorado com 'with' e melhorias)

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def get_produtores_count(search_term: str = None):
    
    # 1. PREPARAR (Sem conexão)
    params = []
    where_clause = "WHERE deletado_em IS NULL"
    
    if search_term:
        where_clause += " AND (nome LIKE ? OR apelido LIKE ? OR cpf LIKE ?)"
        search_like = f"%{search_term}%"
        params.extend([search_like, search_like, search_like])
        
    sql = f"SELECT COUNT(*) FROM produtores {where_clause};"
    
    conn = get_db_connection() # 2. ABRIR A CONEXÃO
    
    try:
        # 3. EXECUTAR A CONSULTA
        cursor = conn.cursor()
        cursor.execute(sql, params)
        total = cursor.fetchone()[0] # Pega o primeiro valor da primeira linha
        
        return total

    except sqlite3.Error as e:
        logging.error(f"Erro ao contar produtores: {e}")
        return 0 # Retorna 0 em caso de erro
    except Exception as e:
        logging.error(f"Erro inesperado em get_produtores_count: {e}")
        return 0
    finally:
        # 4. GARANTIR O FECHAMENTO
        if conn:
            conn.close()
            
# CÓDIGO NOVO (Refatorado com 'with' e correção de bugs)

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def create_produtor(produtor: Produtor):
    sql = """
        INSERT INTO produtores (nome, apelido, cpf, regiao, referencia, telefone)
        VALUES (?, ?, ?, ?, ?, ?)
    """
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A TRANSAÇÃO
        cursor = conn.cursor()
        cursor.execute(sql, (produtor.nome, produtor.apelido, produtor.cpf, 
                              produtor.regiao, produtor.referencia, produtor.telefone))
        conn.commit() # Salva as mudanças
        produtor.produtor_id = cursor.lastrowid
        
        return produtor
        
    except sqlite3.IntegrityError as e: # Captura Específica (a do log!)
        logging.error(f"Erro ao criar produtor: {e}")
        # Este log era útil, vamos mantê-lo:
        logging.warning(f"Provável CPF duplicado: {produtor.cpf}") 
        return None
    except sqlite3.Error as e: # Captura Genérica para outros erros de banco
        logging.error(f"Erro no banco ao criar produtor: {e}")
        return None
    except Exception as e:
         logging.error(f"Erro inesperado em create_produtor: {e}")
         return None
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()
            
# CÓDIGO NOVO (Refatorado com 'with' e correção de bugs)

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def update_produtor(produtor: Produtor):
    sql = """
        UPDATE produtores 
        SET nome = ?, apelido = ?, cpf = ?, regiao = ?, referencia = ?, telefone = ?
        WHERE produtor_id = ?
    """
    params = (
        produtor.nome,
        produtor.apelido, 
        produtor.cpf, 
        produtor.regiao, 
        produtor.referencia, 
        produtor.telefone,
        produtor.produtor_id 
    )
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A TRANSAÇÃO
        cursor = conn.cursor()
        cursor.execute(sql, params)
        conn.commit() # Salva as mudanças
        
        logging.info(f"Produtor atualizado! ID: {produtor.produtor_id}")
        return produtor
        
    except sqlite3.IntegrityError as e: # Captura Específica (ex: CPF duplicado)
        logging.error(f"Erro de integridade ao atualizar produtor ID {produtor.produtor_id}: {e}")
        return None
    except sqlite3.Error as e: # Captura Genérica (ex: "database locked")
        logging.error(f"Erro no banco ao atualizar produtor ID {produtor.produtor_id}: {e}")
        return None
    except Exception as e:
         logging.error(f"Erro inesperado em update_produtor: {e}")
         return None
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()
            
# CÓDIGO NOVO (Refatorado com 'with' e correção de bug)

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def delete_produtor(produtor_id: int):
    sql = "UPDATE produtores SET deletado_em = CURRENT_TIMESTAMP WHERE produtor_id = ?"
    updated = False # Inicializa fora
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A TRANSAÇÃO
        cursor = conn.cursor()
        cursor.execute(sql, (produtor_id,))
        conn.commit()
        updated = cursor.rowcount > 0 # Atualiza a variável
            
    except sqlite3.Error as e: # Captura Genérica (corrigido de conn.Error)
        logging.error(f"Erro ao tentar fazer soft delete do Produtor ID {produtor_id}: {e}")
        return False # Retorna False em caso de erro no banco
    except Exception as e:
        logging.error(f"Erro inesperado em delete_produtor: {e}")
        return False
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()
            
    # 4. LÓGICA DE LOG (Fora do try, com a conexão já fechada)
    if updated:
        logging.info(f"Soft delete do Produtor ID: {produtor_id} realizado com sucesso.")
    else:
        logging.warning(f"Tentativa de soft delete do Produtor ID: {produtor_id} falhou (ID não encontrado).")
    
    return updated
    
# CÓDIGO NOVO (Refatorado com 'with' e melhorias)

# CÓDIGO CORRIGIDO (Garantindo o conn.close())

def get_produtor_by_nome(nome: str):
    sql = "SELECT * FROM produtores WHERE nome = ? AND deletado_em IS NULL"
    
    conn = get_db_connection() # 1. ABRIR A CONEXÃO
    
    try:
        # 2. EXECUTAR A CONSULTA
        cursor = conn.cursor()
        cursor.execute(sql, (nome,))
        data = cursor.fetchone()
            
        if data is None:
            return None 
            
        return Produtor(
            produtor_id=data['produtor_id'],
            nome=data['nome'],
            apelido=data['apelido'],
            cpf=data['cpf'],
            regiao=data['regiao'],
            referencia=data['referencia'],
            telefone=data['telefone']
        )

    except sqlite3.Error as e: # Captura genérica do sqlite3
        logging.error(f"Erro ao buscar produtor pelo nome '{nome}': {e}")
        return None
    except Exception as e: # Captura para qualquer outro erro
        logging.error(f"Erro inesperado em get_produtor_by_nome: {e}")
        return None
    finally:
        # 3. GARANTIR O FECHAMENTO
        if conn:
            conn.close()