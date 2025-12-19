class Produtor:
    
    def __init__(self, nome, apelido=None, cpf=None, regiao=None, referencia=None, 
                 telefone=None, produtor_id=None):
        self.produtor_id = produtor_id
        self.nome = nome 
        self.apelido = apelido
        self.cpf = cpf
        self.regiao = regiao
        self.referencia = referencia
        self.telefone = telefone

    def __repr__(self):
        return f"<Produtor {self.produtor_id}: {self.nome}>"

    def to_dict(self):
        return {
            'id': self.produtor_id,
            'nome': self.nome,
            'apelido': self.apelido,
            'cpf': self.cpf,
            'regiao': self.regiao,
            'referencia': self.referencia,
            'telefone': self.telefone
        }