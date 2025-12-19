from flask import Flask
import logging
import os
from logging.handlers import RotatingFileHandler

def create_app(user_data_path):
    """
    Esta é a "Application Factory" (Fábrica de Aplicação).
    Ela cria, configura e "monta" nossa aplicação Flask.
    """
    app = Flask(__name__)
    
    app.config['USER_DATA_PATH'] = user_data_path   

    if not app.debug:
        log_dir = os.path.join(user_data_path, 'logs_backend')
        if not os.path.exists(log_dir):
            os.makedirs(log_dir)

        file_handler = RotatingFileHandler(
            os.path.join(log_dir, 'backend.log'), 
            maxBytes=1024 * 1024 * 10, 
            backupCount=3
        )
        
        file_handler.setFormatter(logging.Formatter(
            '%(asctime)s %(levelname)s: %(message)s [in %(pathname)s:%(lineno)d]'
        ))
        
        app.logger.setLevel(logging.INFO)
        file_handler.setLevel(logging.INFO)
        
        app.logger.addHandler(file_handler)
        
        app.logger.info('Servidor Backend (Flask) iniciado')

    from app.routers.produtor_routes import produtor_bp
    
    from app.routers.servico_routes import servico_bp
    
    from app.routers.execucao_routes import execucao_bp
    
    from app.routers.pagamento_routes import pagamento_bp
    
    from app.routers.relatorio_routes import relatorio_bp
    
    from app.routers.admin_routes import admin_bp
    
    app.register_blueprint(produtor_bp, url_prefix='/api')   
    app.register_blueprint(servico_bp, url_prefix='/api')   
    app.register_blueprint(execucao_bp, url_prefix='/api')   
    app.register_blueprint(pagamento_bp, url_prefix='/api')  
    app.register_blueprint(relatorio_bp, url_prefix='/api')
    app.register_blueprint(admin_bp, url_prefix='/api')
    
    @app.route('/health')
    def health_check():
        """Verifica se o servidor está no ar."""
        return "Servidor Flask (Refatorado) no ar e saudável!"

    return app