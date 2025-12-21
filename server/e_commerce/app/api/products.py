from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from marshmallow import ValidationError
from app.service.product_service import ProductService
from app.service.user_service import UserService
from app.schemas.product_schema import ProductSchema, ProductImageSchema

products_bp = Blueprint('products', __name__)

product_schema = ProductSchema()
products_schema = ProductSchema(many=True)
images_schema = ProductImageSchema(many=True)


@products_bp.route('/search', methods=['GET'])
def search_products_api():
    query = request.args.get('q', '')

    if not query:
        return jsonify([]), 200
    results = ProductService.search_in_elastic(query)
    return jsonify(results), 200

@products_bp.route('', methods=['GET'])
def get_all_products():
    products = ProductService.get_all_products()
    return jsonify(products_schema.dump(products)), 200

@products_bp.route('/<int:product_id>', methods=['GET'])
def get_product(product_id):
    product = ProductService.get_product_by_id(product_id)
    if not product:
        return jsonify({'error': 'Ürün bulunamadı'}), 404
    return jsonify(product_schema.dump(product)), 200

@products_bp.route('/', methods=['POST'])
@jwt_required()
def create_product():
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Sadece admin ürün ekleyebilir'}), 403

    json_data = request.get_json() or {}
    try:
        validated_data = product_schema.load(json_data)
    except ValidationError as err:
        return jsonify(err.messages), 422

    product, error = ProductService.create_product(validated_data, requesting_user=current_user)
    if error:
        return jsonify({'error': error}), 400

    return jsonify({'message': 'Ürün başarıyla oluşturuldu', 'product': product_schema.dump(product)}), 201

@products_bp.route('/<int:product_id>', methods=['PUT'])
@jwt_required()
def update_product(product_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Sadece admin ürün güncelleyebilir'}), 403

    json_data = request.get_json() or {}
    try:
        validated_data = product_schema.load(json_data, partial=True)
    except ValidationError as err:
        return jsonify(err.messages), 422

    product, error = ProductService.update_product(product_id, validated_data, requesting_user=current_user)
    if error:
        status_code = 404 if "bulunamadı" in error else 400
        return jsonify({'error': error}), status_code

    return jsonify({'message': 'Ürün güncellendi', 'product': product_schema.dump(product)}), 200

@products_bp.route('/<int:product_id>', methods=['DELETE'])
@jwt_required()
def delete_product(product_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Sadece admin ürün silebilir'}), 403

    success, error = ProductService.delete_product(product_id, requesting_user=current_user)
    if error:
        return jsonify({'error': error}), 404 if "bulunamadı" in error else 400

    return jsonify({'message': 'Ürün silindi'}), 200

@products_bp.route('/<int:product_id>/images', methods=['POST'])
@jwt_required()
def add_product_images(product_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Sadece admin resim ekleyebilir'}), 403

    files = request.files.getlist('file')
    if not files or files[0].filename == '':
        return jsonify({'error': 'Dosya seçilmedi'}), 400

    images, error = ProductService.add_product_images(product_id, requesting_user=current_user, files=files)
    if error:
        return jsonify({'error': error}), 400

    return jsonify({'message': f'{len(images)} resim yüklendi', 'images': images_schema.dump(images)}), 201

@products_bp.route('/images/<int:image_id>', methods=['DELETE'])
@jwt_required()
def delete_product_image(image_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Sadece admin resim silebilir'}), 403

    success, error = ProductService.delete_product_image(image_id, requesting_user=current_user)
    if error:
        return jsonify({'error': error}), 400

    return jsonify({'message': 'Resim silindi'}), 200

@products_bp.route('/category/<int:category_id>', methods=['GET'])
def get_products_by_category(category_id):
    products = ProductService.get_products_by_category(category_id)
    return jsonify(products_schema.dump(products)), 200