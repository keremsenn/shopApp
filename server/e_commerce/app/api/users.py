from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.service.user_service import UserService

users_bp = Blueprint('users', __name__)


@users_bp.route('', methods=['GET'])
@jwt_required()
def get_all_users():
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Admin access required'}), 403

    users = UserService.get_all_users()
    return jsonify([user.to_dict() for user in users]), 200


@users_bp.route('/<int:user_id>', methods=['GET'])
@jwt_required()
def get_user(user_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if current_user_id != user_id and (not current_user or current_user.role != 'admin'):
        return jsonify({'error': 'Access denied'}), 403

    user = UserService.get_user_by_id(user_id)
    if not user:
        return jsonify({'error': 'User not found'}), 404

    return jsonify(user.to_dict()), 200


@users_bp.route('/me', methods=['GET'])
@jwt_required()
def get_current_user():
    current_user_id = int(get_jwt_identity())
    user = UserService.get_user_by_id(current_user_id)

    if not user:
        return jsonify({'error': 'User not found'}), 404

    return jsonify(user.to_dict()), 200


@users_bp.route('/<int:user_id>', methods=['PUT'])
@jwt_required()
def update_user(user_id):
    current_user_id = int(get_jwt_identity())

    requesting_user = UserService.get_user_by_id(current_user_id)

    if not requesting_user:
        return jsonify({'error': 'Unauthorized'}), 401

    if current_user_id != user_id and requesting_user.role != 'admin':
        return jsonify({'error': 'Access denied'}), 403

    data = request.get_json()
    if not data:
        return jsonify({'error': 'No data provided'}), 400

    user, error = UserService.update_user(user_id, data, requesting_user)
    if error:
        status_code = 404 if error == "User not found" else 400
        return jsonify({'error': error}), status_code

    return jsonify({
        'message': 'User updated successfully',
        'user': user.to_dict()
    }), 200


@users_bp.route('/<int:user_id>', methods=['DELETE'])
@jwt_required()
def delete_user(user_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)
    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Admin access required'}), 403

    success, error = UserService.delete_user(user_id)

    if error:
        status_code = 404 if error == "User not found" else 400
        return jsonify({'error': error}), status_code

    return jsonify({'message': 'User deleted successfully'}), 200