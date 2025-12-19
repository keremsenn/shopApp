from flask import Flask, send_from_directory, current_app
from flask_sqlalchemy import SQLAlchemy
from flask_jwt_extended import JWTManager
from flask_cors import CORS
from flask_migrate import Migrate
from config import Config
import os

db = SQLAlchemy()
jwt = JWTManager()
migrate = Migrate()


def create_app(config_class=Config):
    app = Flask(__name__)
    app.config.from_object(config_class)

    db.init_app(app)
    jwt.init_app(app)
    migrate.init_app(app, db)
    CORS(app)

    os.makedirs(Config.UPLOAD_FOLDER, exist_ok=True)

    @app.route('/uploads/<path:filename>')
    def serve_uploads(filename):
        upload_folder = current_app.config['UPLOAD_FOLDER']
        return send_from_directory(upload_folder, filename)

    from app.api.auth import auth_bp
    from app.api.users import users_bp
    from app.api.products import products_bp
    from app.api.categories import categories_bp
    from app.api.cart import cart_bp
    from app.api.orders import orders_bp
    from app.api.addresses import addresses_bp
    from app.api.seller_request_routes import seller_req_bp
    from app.api.favorite import favorites_bp


    app.register_blueprint(auth_bp, url_prefix='/api/auth')
    app.register_blueprint(users_bp, url_prefix='/api/users')
    app.register_blueprint(products_bp, url_prefix='/api/products')
    app.register_blueprint(categories_bp, url_prefix='/api/categories')
    app.register_blueprint(cart_bp, url_prefix='/api/cart')
    app.register_blueprint(orders_bp, url_prefix='/api/orders')
    app.register_blueprint(addresses_bp, url_prefix='/api/addresses')
    app.register_blueprint(seller_req_bp, url_prefix='/api/seller-requests')
    app.register_blueprint(favorites_bp, url_prefix='/api/favorites')

    return app