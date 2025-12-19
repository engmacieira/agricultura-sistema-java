class Execucao:
    
    def __init__(self, produtor_id: int, servico_id: int, data_execucao: str,
                 horas_prestadas: float = 0.0, valor_total: float = 0.0, 
                 execucao_id: int = None):
        self.execucao_id = execucao_id
        self.produtor_id = produtor_id   
        self.servico_id = servico_id    
        self.data_execucao = data_execucao 
        self.horas_prestadas = horas_prestadas
        self.valor_total = valor_total

    def __repr__(self):
        return (f"<Execucao {self.execucao_id}: "
                f"Prod. {self.produtor_id} -> Serv. {self.servico_id} "
                f"em {self.data_execucao}>")

    def to_dict(self):
        return {
            'id': self.execucao_id,
            'produtor_id': self.produtor_id,
            'servico_id': self.servico_id,
            'data_execucao': self.data_execucao,
            'horas_prestadas': self.horas_prestadas,
            'valor_total': self.valor_total
        }