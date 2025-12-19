class Servico:
    
    def __init__(self, nome, valor_unitario=0.0, servico_id=None):
        """
        Construtor baseado no schema 'servicos' (database_setup.py).
        """
        self.servico_id = servico_id
        self.nome = nome 
        self.valor_unitario = valor_unitario 

    def __repr__(self):
        return f"<Servico {self.servico_id}: {self.nome} (R$ {self.valor_unitario})>"

    def to_dict(self):
        return {
            'id': self.servico_id,
            'nome': self.nome,
            'valor_unitario': self.valor_unitario
        }