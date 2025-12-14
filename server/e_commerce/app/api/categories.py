from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from marshmallow import ValidationError
from app.service.category_service import CategoryService
from app.service.user_service import UserService
from app.schemas.category_schema import CategorySchema

categories_bp = Blueprint('categories', __name__)

category_schema = CategorySchema()
categories_schema = CategorySchema(many=True)


@categories_bp.route('', methods=['GET'])
def get_all_categories():
    categories = CategoryService.get_all_categories()
    return jsonify(categories_schema.dump(categories)), 200


@categories_bp.route('/roots', methods=['GET'])
def get_root_categories():
    categories = CategoryService.get_root_categories()
    return jsonify(categories_schema.dump(categories)), 200


@categories_bp.route('/<int:category_id>', methods=['GET'])
def get_category(category_id):
    category = CategoryService.get_category_by_id(category_id)
    if not category:
        return jsonify({'error': 'Category not found'}), 404

    return jsonify(category_schema.dump(category)), 200


@categories_bp.route('/parent/<int:parent_id>', methods=['GET'])
def get_child_categories(parent_id):
    categories = CategoryService.get_child_categories(parent_id)
    return jsonify(categories_schema.dump(categories)), 200


@categories_bp.route('', methods=['POST'])
@jwt_required()
def create_category():
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user:
        return jsonify({'error': 'User not found'}), 401

    json_data = request.get_json()
    if not json_data:
        return jsonify({'error': 'No data provided'}), 400

    try:
        validated_data = category_schema.load(json_data)
    except ValidationError as err:
        return jsonify(err.messages), 422

    category, error = CategoryService.create_category(validated_data, requesting_user=current_user)

    if error:
        status_code = 403 if "Access denied" in error else 400
        return jsonify({'error': error}), status_code

    return jsonify({
        'message': 'Category created successfully',
        'category': category_schema.dump(category)
    }), 201


@categories_bp.route('/<int:category_id>', methods=['PUT'])
@jwt_required()
def update_category(category_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user:
        return jsonify({'error': 'User not found'}), 401

    json_data = request.get_json()
    if not json_data:
        return jsonify({'error': 'No data provided'}), 400
    try:
        validated_data = category_schema.load(json_data, partial=True)
    except ValidationError as err:
        return jsonify(err.messages), 422

    category, error = CategoryService.update_category(category_id, validated_data, requesting_user=current_user)

    if error:
        status_code = 403 if "Access denied" in error else 400
        if "not found" in error: status_code = 404
        return jsonify({'error': error}), status_code

    return jsonify({
        'message': 'Category updated successfully',
        'category': category_schema.dump(category)
    }), 200


@categories_bp.route('/<int:category_id>', methods=['DELETE'])
@jwt_required()
def delete_category(category_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user:
        return jsonify({'error': 'User not found'}), 401

    success, error = CategoryService.delete_category(category_id, requesting_user=current_user)

    if error:
        status_code = 403 if "Access denied" in error else 400
        if "not found" in error: status_code = 404
        return jsonify({'error': error}), status_code

    return jsonify({'message': 'Category deleted successfully'}), 200