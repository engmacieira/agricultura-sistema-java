from flask import Blueprint, jsonify, request
from app.models.servico_model import Servico
import math
import app.repositories.servico_repository as servico_repository

servico_bp = Blueprint('servico_bp', __name__)

@servico_bp.route('/servicos', methods=['GET'])
def get_servicos_api():
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 10, type=int)
    
    total_items = servico_repository.get_servicos_count()
    if total_items == 0:
        return jsonify({
            'servicos': [],
            'total_pages': 0,
            'current_page': 1,
            'total_items': 0
        })

    total_pages = math.ceil(total_items / per_page)
    
    servicos = servico_repository.get_all_servicos(page, per_page)
    
    return jsonify({
        'servicos': [s.to_dict() for s in servicos],
        'total_pages': total_pages,
        'current_page': page,
        'total_items': total_items
    }), 200


@servico_bp.route('/servicos/<int:servico_id>', methods=['GET'])
def get_servico_api(servico_id):
    servico = servico_repository.get_servico_by_id(servico_id)
    if servico:
        return jsonify(servico.to_dict())
    else:
        return jsonify({'erro': 'Serviço não encontrado'}), 404

@servico_bp.route('/servicos', methods=['POST'])
def create_servico_api():
    dados = request.json
    
    if 'nome' not in dados or not dados['nome']:
        return jsonify({'erro': 'O campo "nome" é obrigatório'}), 400
    if 'valor_unitario' not in dados:
        return jsonify({'erro': 'O campo "valor_unitario" é obrigatório'}), 400

    novo_servico = Servico(
        nome=dados['nome'],
        valor_unitario=float(dados['valor_unitario']) 
    )
    
    servico_salvo = servico_repository.create_servico(novo_servico)

    if servico_salvo is None:
        return jsonify({'erro': f"Serviço com nome '{novo_servico.nome}' já existe."}), 409 
        
    return jsonify(servico_salvo.to_dict()), 201 

@servico_bp.route('/servicos/<int:servico_id>', methods=['PUT'])
def update_servico_api(servico_id):
    
    if not servico_repository.get_servico_by_id(servico_id):
        return jsonify({'erro': 'Serviço não encontrado'}), 404    
    dados = request.json
    
    if 'nome' not in dados or not dados['nome']:
        return jsonify({'erro': 'O campo "nome" é obrigatório'}), 400
    if 'valor_unitario' not in dados:
        return jsonify({'erro': 'O campo "valor_unitario" é obrigatório'}), 400

    servico_atualizado = Servico(
        servico_id=servico_id, 
        nome=dados['nome'],
        valor_unitario=float(dados['valor_unitario'])
    )
    
    servico_salvo = servico_repository.update_servico(servico_atualizado)

    if servico_salvo is None:
        return jsonify({'erro': f"Serviço com nome '{servico_atualizado.nome}' já existe."}), 409
        
    return jsonify(servico_salvo.to_dict()), 200 

@servico_bp.route('/servicos/<int:servico_id>', methods=['DELETE'])
def delete_servico_api(servico_id):
    
    if not servico_repository.get_servico_by_id(servico_id):
        return jsonify({'erro': 'Serviço não encontrado'}), 404
        
    sucesso = servico_repository.delete_servico(servico_id)
    
    if sucesso:
        return jsonify({'mensagem': 'Serviço excluído com sucesso'}), 200
    else:
        return jsonify({'erro': 'Este serviço não pode ser excluído pois está em uso por uma ou mais execuções.'}), 409 