from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.service.order_service import OrderService
from app.service.user_service import UserService

orders_bp = Blueprint('orders', __name__)


@orders_bp.route('', methods=['GET'])
@jwt_required()
def get_orders():
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user:
        return jsonify({'error': 'User not found'}), 404

    if current_user.role == 'admin':
        orders = OrderService.get_all_orders()
    else:
        orders = OrderService.get_orders_by_user(current_user_id)

    return jsonify([order.to_dict() for order in orders]), 200


@orders_bp.route('/<int:order_id>', methods=['GET'])
@jwt_required()
def get_order_detail(order_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    order = OrderService.get_order_by_id(order_id)
    if not order:
        return jsonify({'error': 'Order not found'}), 404

    # Yetki Kontrolü: Admin değilse ve sipariş kullanıcının değilse hata ver
    if current_user.role != 'admin' and order.user_id != current_user_id:
        return jsonify({'error': 'Access denied'}), 403

    return jsonify(order.to_dict()), 200


@orders_bp.route('', methods=['POST'])
@jwt_required()
def create_order():

    current_user_id = int(get_jwt_identity())
    data = request.get_json() or {}

    address_id = data.get('address_id')
    if not address_id:
        return jsonify({'error': 'address_id is required'}), 400

    if 'items' in data and data['items']:
        order, error = OrderService.create_order_direct(current_user_id, address_id, data['items'])
    else:
        order, error = OrderService.create_order_from_cart(current_user_id, address_id)

    if error:
        return jsonify({'error': error}), 400

    return jsonify({
        'message': 'Order created successfully',
        'order': order.to_dict()
    }), 201


@orders_bp.route('/<int:order_id>/status', methods=['PUT'])
@jwt_required()
def update_order_status(order_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Admin access required'}), 403

    data = request.get_json()
    status = data.get('status')

    if not status:
        return jsonify({'error': 'status is required'}), 400

    order, error = OrderService.update_order_status(order_id, status)
    if error:
        return jsonify({'error': error}), 400

    return jsonify({
        'message': 'Order status updated successfully',
        'order': order.to_dict()
    }), 200


@orders_bp.route('/<int:order_id>/cancel', methods=['POST'])
@jwt_required()
def cancel_order(order_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    is_admin = (current_user.role == 'admin') if current_user else False

    success, error = OrderService.cancel_order(order_id, current_user_id, is_admin)

    if error:
        status_code = 404 if error == "Order not found" else 400
        if error == "Access denied": status_code = 403
        return jsonify({'error': error}), status_code

    return jsonify({'message': 'Order cancelled successfully'}), 200
