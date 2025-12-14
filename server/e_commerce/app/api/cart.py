from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from marshmallow import ValidationError
from app.service.cart_service import CartService
from app.schemas.cart_schema import CartSchema, CartItemSchema

cart_bp = Blueprint('cart', __name__)
cart_schema = CartSchema()
cart_item_schema = CartItemSchema()


@cart_bp.route('', methods=['GET'])
@jwt_required()
def get_cart():
    current_user_id = int(get_jwt_identity())
    cart = CartService.get_cart(current_user_id)

    if not cart:
        return jsonify({'items': [], 'total_price': 0.0}), 200

    return jsonify(cart_schema.dump(cart)), 200


@cart_bp.route('/items', methods=['POST'])
@jwt_required()
def add_item_to_cart():
    current_user_id = int(get_jwt_identity())
    json_data = request.get_json() or {}
    try:
        validated_data = cart_item_schema.load(json_data)
    except ValidationError as err:
        return jsonify(err.messages), 422

    cart_item, error = CartService.add_item_to_cart(current_user_id, validated_data)

    if error:
        status_code = 404 if "not found" in error else 400
        return jsonify({'error': error}), status_code

    return jsonify({
        'message': 'Item added to cart successfully',
        'cart_item': cart_item_schema.dump(cart_item)
    }), 201


@cart_bp.route('/items/<int:cart_item_id>', methods=['PUT'])
@jwt_required()
def update_cart_item(cart_item_id):
    current_user_id = int(get_jwt_identity())
    json_data = request.get_json() or {}

    if 'quantity' not in json_data:
        return jsonify({'error': 'quantity is required'}), 400

    try:
        CartItemSchema(only=("quantity",)).load(json_data)
    except ValidationError as err:
        return jsonify(err.messages), 422

    cart_item, error = CartService.update_cart_item(
        current_user_id,
        cart_item_id,
        json_data['quantity']
    )

    if error:
        status_code = 404 if "not found" in error else 400
        return jsonify({'error': error}), status_code

    return jsonify({
        'message': 'Cart item updated successfully',
        'cart_item': cart_item_schema.dump(cart_item)
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