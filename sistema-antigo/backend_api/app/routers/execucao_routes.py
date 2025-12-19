from flask import Blueprint, jsonify, request
import math 
from app.models.execucao_model import Execucao
import app.repositories.execucao_repository as execucao_repository
import app.repositories.produtor_repository as produtor_repository 
import app.repositories.servico_repository as servico_repository 

execucao_bp = Blueprint('execucao_bp', __name__)

@execucao_bp.route('/execucoes', methods=['GET'])
def get_execucoes_api():
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 10, type=int)
    status = request.args.get('status', 'todos', type=str)
    search_term = request.args.get('q', None, type=str)
    
    total_items = execucao_repository.get_execucoes_count(status, search_term)
    if total_items == 0:
        return jsonify({
            'execucoes': [],
            'total_pages': 0,
            'current_page': 1,
            'total_items': 0
        })

    total_pages = math.ceil(total_items / per_page)
    
    execucoes = execucao_repository.get_all_execucoes(page, per_page, status, search_term)
    
    return jsonify({
        'execucoes': execucoes, 
        'total_pages': total_pages,
        'current_page': page,
        'total_items': total_items
    }), 200

@execucao_bp.route('/execucoes/<int:execucao_id>', methods=['GET'])
def get_execucao_api(execucao_id):
    execucao = execucao_repository.get_execucao_by_id(execucao_id)
    
    if execucao:
        return jsonify(execucao.to_dict())
    else:
        return jsonify({'erro': 'Execução não encontrada'}), 404

@execucao_bp.route('/execucoes', methods=['POST'])
def create_execucao_api():
    dados = request.json
    
    campos_obrigatorios = ['produtor_id', 'servico_id', 'data_execucao']
    for campo in campos_obrigatorios:
        if campo not in dados or dados[campo] is None:
            return jsonify({'erro': f'O campo "{campo}" é obrigatório'}), 400

    nova_execucao = Execucao(
        produtor_id=dados['produtor_id'],
        servico_id=dados['servico_id'],
        data_execucao=dados['data_execucao'],
        horas_prestadas=dados.get('horas_prestadas', 0.0),
        valor_total=dados.get('valor_total', 0.0)
    )
    
    execucao_salva = execucao_repository.create_execucao(nova_execucao)

    if execucao_salva is None:
        msg = "Não foi possível criar a execução. Verifique se o 'produtor_id' e o 'servico_id' existem."
        return jsonify({'erro': msg}), 409 
        
    return jsonify(execucao_salva.to_dict()), 201 


@execucao_bp.route('/execucoes/<int:execucao_id>', methods=['PUT'])
def update_execucao_api(execucao_id):
    if not execucao_repository.get_execucao_by_id(execucao_id):
        return jsonify({'erro': 'Execução não encontrada'}), 404
        
    dados = request.json
    
    campos_obrigatorios = ['produtor_id', 'servico_id', 'data_execucao']
    for campo in campos_obrigatorios:
        if campo not in dados or dados[campo] is None:
            return jsonify({'erro': f'O campo "{campo}" é obrigatório'}), 400

    execucao_atualizada = Execucao(
        execucao_id=execucao_id, 
        produtor_id=dados['produtor_id'],
        servico_id=dados['servico_id'],
        data_execucao=dados['data_execucao'],
        horas_prestadas=dados.get('horas_prestadas', 0.0),
        valor_total=dados.get('valor_total', 0.0)
    )
    
    execucao_salva = execucao_repository.update_execucao(execucao_atualizada)

    if execucao_salva is None:
        msg = "Não foi possível atualizar a execução. Verifique se o 'produtor_id' e o 'servico_id' existem."
        return jsonify({'erro': msg}), 409 
        
    return jsonify(execucao_salva.to_dict()), 200 


@execucao_bp.route('/execucoes/<int:execucao_id>', methods=['DELETE'])
def delete_execucao_api(execucao_id):
    if not execucao_repository.get_execucao_by_id(execucao_id):
        return jsonify({'erro': 'Execução não encontrada'}), 404
        
    sucesso = execucao_repository.delete_execucao(execucao_id)
    
    if sucesso:
        return jsonify({'mensagem': 'Execução e pagamentos associados excluídos com sucesso'}), 200
    else:
        return jsonify({'erro': 'Erro ao excluir execução'}), 500