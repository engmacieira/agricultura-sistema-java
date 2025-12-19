import sqlite3

DATABASE = 'gestao.db'

def get_db_connection():
    conn = sqlite3.connect(DATABASE)
    conn.row_factory = sqlite3.Row 
    conn.execute("PRAGMA foreign_keys = ON;")
    
    return conn