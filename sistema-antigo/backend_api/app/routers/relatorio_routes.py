from flask import Blueprint, jsonify, request
import app.repositories.relatorio_repository as relatorio_repository
import app.repositories.produtor_repository as produtor_repository 
import logging
import math

relatorio_bp = Blueprint('relatorio_bp', __name__)

@relatorio_bp.route('/relatorios/produtor/<int:produtor_id>/dividas', methods=['GET'])
def get_relatorio_dividas_produtor_api(produtor_id):
    
    produtor = produtor_repository.get_produtor_by_id(produtor_id)
    if not produtor:
        return jsonify({'erro': 'Produtor não encontrado'}), 404
        
    try:
        relatorio_dividas = relatorio_repository.get_dividas_por_produtor(produtor_id)
        
        return jsonify(relatorio_dividas), 200
        
    except Exception as e:
        print(f"Erro ao gerar relatório de dívidas: {e}")
        return jsonify({'erro': 'Erro interno ao processar o relatório'}), 500
    
@relatorio_bp.route('/admin/purge', methods=['POST'])
def purge_old_data_api():
    try:
        logging.info("Recebida requisição de Purge...")
        resultado = relatorio_repository.purge_deleted_records()
        
        if resultado["sucesso"]:
            return jsonify({
                "mensagem": f"Limpeza concluída. Total de {resultado['total_excluido']} registros excluídos."
            }), 200
        else:
            return jsonify({"erro": resultado["erro"]}), 500
            
    except Exception as e:
        logging.error(f"Erro inesperado na rota /admin/purge: {e}")
        return jsonify({'erro': 'Erro interno grave ao processar a limpeza'}), 500