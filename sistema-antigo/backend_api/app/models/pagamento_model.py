class Pagamento:
    
    def __init__(self, execucao_id: int, valor_pago: float, 
                 data_pagamento: str, pagamento_id: int = None):
        self.pagamento_id = pagamento_id
        self.execucao_id = execucao_id  
        self.valor_pago = valor_pago    
        self.data_pagamento = data_pagamento 

    def __repr__(self):
        return (f"<Pagamento {self.pagamento_id}: "
                f"R$ {self.valor_pago} para Exec. {self.execucao_id}>")

    def to_dict(self):
        return {
            'id': self.pagamento_id,
            'execucao_id': self.execucao_id,
            'valor_pago': self.valor_pago,
            'data_pagamento': self.data_pagamento
        }