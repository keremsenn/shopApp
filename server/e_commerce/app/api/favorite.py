# app/api/favorites.py

from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.service.favorite_service import FavoriteService
from app.schemas.favorite_schema import FavoriteSchema

favorites_bp = Blueprint('favorites', __name__)
favorite_schema = FavoriteSchema()
favorites_schema = FavoriteSchema(many=True)


@favorites_bp.route('/', methods=['POST'])
@jwt_required()
def add_favorite():
    user_id = get_jwt_identity()
    data = request.get_json()

    product_id = data.get('product_id')

    if not product_id:
        return jsonify({"message": "product_id zorunludur"}), 400

    fav, error = FavoriteService.add_favorite(int(user_id), product_id)

    if error and not fav:
        return jsonify({"message": error}), 400

    if error and fav:
        return jsonify({
            "message": error,
            "favorite": favorite_schema.dump(fav)
        }), 200

    return jsonify({
        "message": "Favorilere eklendi",
        "favorite": favorite_schema.dump(fav)
    }), 201


@favorites_bp.route('/', methods=['GET'])
@jwt_required()
def get_favorites():
    user_id = get_jwt_identity()

    favorites = FavoriteService.get_user_favorites(int(user_id))

    return jsonify(favorites_schema.dump(favorites)), 200


@favorites_bp.route('/<int:product_id>', methods=['DELETE'])
@jwt_required()
def remove_favorite(product_id):
    user_id = get_jwt_identity()

    success, error = FavoriteService.remove_favorite(int(user_id), product_id)

    if success:
        return jsonify({"message": "Favorilerden kaldırıldı"}), 200
    else:
        return jsonify({"message": error or "İşlem başarısız"}), 404