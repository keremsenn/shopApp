from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.service.cart_service import CartService

cart_bp = Blueprint('cart', __name__)


@cart_bp.route('', methods=['GET'])
@jwt_required()
def get_cart():
    current_user_id = int(get_jwt_identity())
    cart = CartService.get_cart(current_user_id)

    if not cart:
        return jsonify({'items': [], 'total': 0}), 200

    return jsonify(cart.to_dict()), 200


@cart_bp.route('/items', methods=['POST'])
@jwt_required()
def add_item_to_cart():
    current_user_id = int(get_jwt_identity())
    data = request.get_json()

    if not data:
        return jsonify({'error': 'No data provided'}), 400

    if 'product_id' not in data:
        return jsonify({'error': 'product_id is required'}), 400

    cart_item, error = CartService.add_item_to_cart(
        current_user_id,
        data['product_id'],
        data.get('quantity', 1)
    )

    if error:
        status_code = 404 if "not found" in error else 400
        return jsonify({'error': error}), status_code

    return jsonify({
        'message': 'Item added to cart successfully',
        'cart_item': cart_item.to_dict()
    }), 201


@cart_bp.route('/items/<int:cart_item_id>', methods=['PUT'])
@jwt_required()
def update_cart_item(cart_item_id):
    current_user_id = int(get_jwt_identity())
    data = request.get_json()

    if not data or 'quantity' not in data:
        return jsonify({'error': 'quantity is required'}), 400

    cart_item, error = CartService.update_cart_item(current_user_id, cart_item_id, data['quantity'])

    if error:
        status_code = 404 if "not found" in error else 400
        return jsonify({'error': error}), status_code

    return jsonify({
        'message': 'Cart item updated successfully',
        'cart_item': cart_item.to_dict()
    }), 200


@cart_bp.route('/items/<int:cart_item_id>', methods=['DELETE'])
@jwt_required()
def remove_item_from_cart(cart_item_id):
    current_user_id = int(get_jwt_identity())
    success, error = CartService.remove_item_from_cart(current_user_id, cart_item_id)

    if error:
        status_code = 404 if "not found" in error else 400
        return jsonify({'error': error}), status_code

    return jsonify({'message': 'Item removed from cart successfully'}), 200


@cart_bp.route('', methods=['DELETE'])
@jwt_required()
def clear_cart():
    current_user_id = int(get_jwt_identity())

    success, error = CartService.clear_cart(current_user_id)
    if error:
        return jsonify({'error': error}), 400

    return jsonify({'message': 'Cart cleared successfully'}), 200