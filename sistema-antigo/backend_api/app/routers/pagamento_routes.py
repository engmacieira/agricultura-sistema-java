from flask import Blueprint, jsonify, request
from app.models.pagamento_model import Pagamento
import app.repositories.pagamento_repository as pagamento_repository
import app.repositories.execucao_repository as execucao_repository 

pagamento_bp = Blueprint('pagamento_bp', __name__)

@pagamento_bp.route('/execucoes/<int:execucao_id>/pagamentos', methods=['GET'])
def get_pagamentos_por_execucao_api(execucao_id):
    
    if not execucao_repository.get_execucao_by_id(execucao_id):
        return jsonify({'erro': 'Execução não encontrada'}), 404
        
    pagamentos = pagamento_repository.get_pagamentos_by_execucao_id(execucao_id)
    return jsonify([p.to_dict() for p in pagamentos]), 200


@pagamento_bp.route('/execucoes/<int:execucao_id>/pagamentos', methods=['POST'])
def create_pagamento_api(execucao_id):
    
    if not execucao_repository.get_execucao_by_id(execucao_id):
        return jsonify({'erro': 'Execução não encontrada'}), 404
        
    dados = request.json
    
    if 'valor_pago' not in dados or 'data_pagamento' not in dados:
        msg = 'Os campos "valor_pago" e "data_pagamento" são obrigatórios'
        return jsonify({'erro': msg}), 400

    novo_pagamento = Pagamento(
        execucao_id=execucao_id, 
        valor_pago=dados['valor_pago'],
        data_pagamento=dados['data_pagamento']
    )
    
    pagamento_salvo = pagamento_repository.create_pagamento(novo_pagamento)
        
    return jsonify(pagamento_salvo.to_dict()), 201 

@pagamento_bp.route('/pagamentos/<int:pagamento_id>', methods=['GET'])
def get_pagamento_api(pagamento_id):
    pagamento = pagamento_repository.get_pagamento_by_id(pagamento_id)
    if pagamento:
        return jsonify(pagamento.to_dict())
    else:
        return jsonify({'erro': 'Pagamento não encontrado'}), 404

@pagamento_bp.route('/pagamentos/<int:pagamento_id>', methods=['PUT'])
def update_pagamento_api(pagamento_id):
    if not pagamento_repository.get_pagamento_by_id(pagamento_id):
        return jsonify({'erro': 'Pagamento não encontrado'}), 404
        
    dados = request.json
    
    campos_obrigatorios = ['execucao_id', 'valor_pago', 'data_pagamento']
    for campo in campos_obrigatorios:
        if campo not in dados:
            return jsonify({'erro': f'O campo "{campo}" é obrigatório'}), 400

    pagamento_atualizado = Pagamento(
        pagamento_id=pagamento_id, 
        execucao_id=dados['execucao_id'],
        valor_pago=dados['valor_pago'],
        data_pagamento=dados['data_pagamento']
    )
    
    pagamento_salvo = pagamento_repository.update_pagamento(pagamento_atualizado)
    
    if pagamento_salvo is None:
        return jsonify({'erro': 'Falha ao atualizar. Verifique se o "execucao_id" existe.'}), 409
        
    return jsonify(pagamento_salvo.to_dict()), 200 

@pagamento_bp.route('/pagamentos/<int:pagamento_id>', methods=['DELETE'])
def delete_pagamento_api(pagamento_id):
    if not pagamento_repository.get_pagamento_by_id(pagamento_id):
        return jsonify({'erro': 'Pagamento não encontrado'}), 404
        
    sucesso = pagamento_repository.delete_pagamento(pagamento_id)
    
    if sucesso:
        return jsonify({'mensagem': 'Pagamento excluído com sucesso'}), 200
    else:
        return jsonify({'erro': 'Erro ao excluir pagamento'}), 500