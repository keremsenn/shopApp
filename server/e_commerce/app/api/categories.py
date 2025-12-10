from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.service.category_service import CategoryService
from app.service.user_service import UserService

categories_bp = Blueprint('categories', __name__)

@categories_bp.route('', methods=['GET'])
def get_all_categories():
    categories = CategoryService.get_all_categories()
    return jsonify([category.to_dict() for category in categories]), 200

@categories_bp.route('/roots', methods=['GET'])
def get_root_categories():
    categories = CategoryService.get_root_categories()
    return jsonify([category.to_dict() for category in categories]), 200

@categories_bp.route('/<int:category_id>', methods=['GET'])
def get_category(category_id):
    category = CategoryService.get_category_by_id(category_id)
    if not category:
        return jsonify({'error': 'Category not found'}), 404
    
    return jsonify(category.to_dict()), 200

@categories_bp.route('/parent/<int:parent_id>', methods=['GET'])
def get_child_categories(parent_id):
    categories = CategoryService.get_child_categories(parent_id)
    return jsonify([category.to_dict() for category in categories]), 200

@categories_bp.route('', methods=['POST'])
@jwt_required()
def create_category():
    current_user_id = get_jwt_identity()
    current_user = UserService.get_user_by_id(current_user_id)
    
    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Admin access required'}), 403
    
    data = request.get_json()
    if not data:
        return jsonify({'error': 'No data provided'}), 400
    
    if 'name' not in data:
        return jsonify({'error': 'name is required'}), 400
    
    category, error = CategoryService.create_category(
        name=data['name'],
        parent_id=data.get('parent_id')
    )
    
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({
        'message': 'Category created successfully',
        'category': category.to_dict()
    }), 201

@categories_bp.route('/<int:category_id>', methods=['PUT'])
@jwt_required()
def update_category(category_id):
    current_user_id = get_jwt_identity()
    current_user = UserService.get_user_by_id(current_user_id)
    
    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Admin access required'}), 403
    
    data = request.get_json()
    if not data:
        return jsonify({'error': 'No data provided'}), 400
    
    category, error = CategoryService.update_category(category_id, **data)
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({
        'message': 'Category updated successfully',
        'category': category.to_dict()
    }), 200

@categories_bp.route('/<int:category_id>', methods=['DELETE'])
@jwt_required()
def delete_category(category_id):
    current_user_id = get_jwt_identity()
    current_user = UserService.get_user_by_id(current_user_id)
    
    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Admin access required'}), 403
    
    success, error = CategoryService.delete_category(category_id)
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({'message': 'Category deleted successfully'}), 200


