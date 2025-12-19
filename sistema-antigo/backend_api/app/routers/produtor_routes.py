from flask import Blueprint, jsonify, request
from app.models.produtor_model import Produtor
import app.repositories.produtor_repository as produtor_repository
import math

produtor_bp = Blueprint('produtor_bp', __name__)

@produtor_bp.route('/produtores', methods=['GET'])
def get_produtores_api():
    
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 10, type=int)
    search_term = request.args.get('q', None, type=str)
    
    total_items = produtor_repository.get_produtores_count(search_term)
    if total_items == 0:
        return jsonify({
            'produtores': [],
            'total_pages': 0,
            'current_page': 1,
            'total_items': 0
        })

    total_pages = math.ceil(total_items / per_page)
    
    produtores = produtor_repository.get_all_produtores(page, per_page, search_term) 
    
    return jsonify({
        'produtores': [p.to_dict() for p in produtores], 
        'total_pages': total_pages,    
        'current_page': page,          
        'total_items': total_items     
    }), 200


@produtor_bp.route('/produtores/<int:produtor_id>', methods=['GET'])
def get_produtor_api(produtor_id):
    produtor = produtor_repository.get_produtor_by_id(produtor_id)
    if produtor:
        return jsonify(produtor.to_dict())
    else:
        return jsonify({'erro': 'Produtor não encontrado'}), 404


@produtor_bp.route('/produtores', methods=['POST'])
def create_produtor_api():
    dados = request.json
    if 'nome' not in dados or not dados['nome']:
        return jsonify({'erro': 'O campo "nome" é obrigatório'}), 400

    novo_produtor = Produtor(
        nome=dados['nome'],
        apelido=dados.get('apelido'),
        cpf=dados.get('cpf'),
        regiao=dados.get('regiao'),
        referencia=dados.get('referencia'),
        telefone=dados.get('telefone')
    )
    produtor_salvo = produtor_repository.create_produtor(novo_produtor)
    return jsonify(produtor_salvo.to_dict()), 201


@produtor_bp.route('/produtores/<int:produtor_id>', methods=['PUT'])
def update_produtor_api(produtor_id):
    if not produtor_repository.get_produtor_by_id(produtor_id):
        return jsonify({'erro': 'Produtor não encontrado'}), 404
        
    dados = request.json
    if 'nome' not in dados or not dados['nome']:
        return jsonify({'erro': 'O campo "nome" é obrigatório'}), 400

    produtor_atualizado = Produtor(
        produtor_id=produtor_id,
        nome=dados['nome'],
        apelido=dados.get('apelido'),
        cpf=dados.get('cpf'),
        regiao=dados.get('regiao'),
        referencia=dados.get('referencia'),
        telefone=dados.get('telefone')
    )
    produtor_salvo = produtor_repository.update_produtor(produtor_atualizado)
    return jsonify(produtor_salvo.to_dict()), 200


@produtor_bp.route('/produtores/<int:produtor_id>', methods=['DELETE'])
def delete_produtor_api(produtor_id):
    if not produtor_repository.get_produtor_by_id(produtor_id):
        return jsonify({'erro': 'Produtor não encontrado'}), 404
        
    sucesso = produtor_repository.delete_produtor(produtor_id)
    
    if sucesso:
        return jsonify({'mensagem': 'Produtor excluído com sucesso'}), 200
    else:
        return jsonify({'erro': 'Erro ao excluir produtor. Verifique se ele possui execuções.'}), 409