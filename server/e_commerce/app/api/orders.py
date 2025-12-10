from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.service.order_service import OrderService
from app.service.user_service import UserService

orders_bp = Blueprint('orders', __name__)

@orders_bp.route('', methods=['GET'])
@jwt_required()
def get_all_orders():
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)
    
    if current_user and current_user.role == 'admin':
        orders = OrderService.get_all_orders()
    else:
        orders = OrderService.get_orders_by_user(current_user_id)
    
    return jsonify([order.to_dict() for order in orders]), 200

@orders_bp.route('/<int:order_id>', methods=['GET'])
@jwt_required()
def get_order(order_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)
    
    order = OrderService.get_order_by_id(order_id)
    if not order:
        return jsonify({'error': 'Order not found'}), 404

    if order.user_id != current_user_id and (not current_user or current_user.role != 'admin'):
        return jsonify({'error': 'Access denied'}), 403
    
    return jsonify(order.to_dict()), 200

@orders_bp.route('', methods=['POST'])
@jwt_required()
def create_order():
    current_user_id = get_jwt_identity()
    data = request.get_json()

    if data and 'items' in data:
        order, error = OrderService.create_order(current_user_id, data['items'])
    else:
        order, error = OrderService.create_order_from_cart(current_user_id)
    
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({
        'message': 'Order created successfully',
        'order': order.to_dict()
    }), 201

@orders_bp.route('/<int:order_id>/status', methods=['PUT'])
@jwt_required()
def update_order_status(order_id):
    current_user_id = get_jwt_identity()
    current_user = UserService.get_user_by_id(current_user_id)
    
    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Admin access required'}), 403
    
    data = request.get_json()
    if not data or 'status' not in data:
        return jsonify({'error': 'status is required'}), 400
    
    order, error = OrderService.update_order_status(order_id, data['status'])
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({
        'message': 'Order status updated successfully',
        'order': order.to_dict()
    }), 200

@orders_bp.route('/<int:order_id>/cancel', methods=['POST'])
@jwt_required()
def cancel_order(order_id):
    current_user_id = get_jwt_identity()
    current_user = UserService.get_user_by_id(current_user_id)
    
    order = OrderService.get_order_by_id(order_id)
    if not order:
        return jsonify({'error': 'Order not found'}), 404

    if order.user_id != current_user_id and (not current_user or current_user.role != 'admin'):
        return jsonify({'error': 'Access denied'}), 403
    
    success, error = OrderService.cancel_order(order_id)
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({'message': 'Order cancelled successfully'}), 200


