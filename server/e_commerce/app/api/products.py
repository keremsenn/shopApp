from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.service.product_service import ProductService
from app.service.user_service import UserService

products_bp = Blueprint('products', __name__)

@products_bp.route('', methods=['GET'])
def get_all_products():
    products = ProductService.get_all_products()
    return jsonify([product.to_dict() for product in products]), 200

@products_bp.route('/<int:product_id>', methods=['GET'])
def get_product(product_id):
    product = ProductService.get_product_by_id(product_id)
    if not product:
        return jsonify({'error': 'Product not found'}), 404
    
    return jsonify(product.to_dict()), 200

@products_bp.route('', methods=['POST'])
@jwt_required()
def create_product():
    current_user_id = get_jwt_identity()
    current_user = UserService.get_user_by_id(current_user_id)
    
    if not current_user or current_user.role not in ['seller', 'admin']:
        return jsonify({'error': 'Seller or admin access required'}), 403
    
    data = request.get_json()
    if not data:
        return jsonify({'error': 'No data provided'}), 400
    
    required_fields = ['name', 'price', 'category_id']
    for field in required_fields:
        if field not in data:
            return jsonify({'error': f'{field} is required'}), 400
    
    product, error = ProductService.create_product(
        seller_id=current_user_id,
        category_id=data['category_id'],
        name=data['name'],
        description=data.get('description'),
        price=data['price'],
        stock=data.get('stock', 0),
        rating=data.get('rating', 0.0)
    )
    
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({
        'message': 'Product created successfully',
        'product': product.to_dict()
    }), 201

@products_bp.route('/<int:product_id>', methods=['PUT'])
@jwt_required()
def update_product(product_id):
    current_user_id = get_jwt_identity()
    current_user = UserService.get_user_by_id(current_user_id)
    
    product = ProductService.get_product_by_id(product_id)
    if not product:
        return jsonify({'error': 'Product not found'}), 404

    if product.seller_id != current_user_id and (not current_user or current_user.role not in ['admin', 'seller']):
        return jsonify({'error': 'Access denied'}), 403
    
    data = request.get_json()
    if not data:
        return jsonify({'error': 'No data provided'}), 400
    
    product, error = ProductService.update_product(product_id, **data)
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({
        'message': 'Product updated successfully',
        'product': product.to_dict()
    }), 200

@products_bp.route('/<int:product_id>', methods=['DELETE'])
@jwt_required()
def delete_product(product_id):
    current_user_id = get_jwt_identity()
    current_user = UserService.get_user_by_id(current_user_id)
    
    product = ProductService.get_product_by_id(product_id)
    if not product:
        return jsonify({'error': 'Product not found'}), 404

    if product.seller_id != current_user_id and (not current_user or current_user.role not in ['admin','seller']):
        return jsonify({'error': 'Access denied'}), 403
    
    success, error = ProductService.delete_product(product_id)
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({'message': 'Product deleted successfully'}), 200

@products_bp.route('/seller/<int:seller_id>', methods=['GET'])
def get_products_by_seller(seller_id):
    products = ProductService.get_products_by_seller(seller_id)
    return jsonify([product.to_dict() for product in products]), 200

@products_bp.route('/category/<int:category_id>', methods=['GET'])
def get_products_by_category(category_id):
    products = ProductService.get_products_by_category(category_id)
    return jsonify([product.to_dict() for product in products]), 200

@products_bp.route('/<int:product_id>/images', methods=['POST'])
@jwt_required()
def add_product_image(product_id):
    current_user_id = get_jwt_identity()
    current_user = UserService.get_user_by_id(current_user_id)
    
    product = ProductService.get_product_by_id(product_id)
    if not product:
        return jsonify({'error': 'Product not found'}), 404

    if product.seller_id != current_user_id and (not current_user or current_user.role != 'seller'):
        return jsonify({'error': 'Access denied'}), 403

    if 'file' in request.files:
        file = request.files['file']
        if file.filename == '':
            return jsonify({'error': 'No file selected'}), 400
        image, error = ProductService.add_product_image(product_id, file=file)
    elif request.is_json and 'url' in request.get_json():
        data = request.get_json()
        image, error = ProductService.add_product_image(product_id, url=data['url'])
    else:
        return jsonify({'error': 'Either file or url must be provided'}), 400
    
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({
        'message': 'Image added successfully',
        'image': image.to_dict()
    }), 201

@products_bp.route('/images/<int:image_id>', methods=['DELETE'])
@jwt_required()
def delete_product_image(image_id):
    current_user_id = get_jwt_identity()
    current_user = UserService.get_user_by_id(current_user_id)

    from app.models.product_image import ProductImage
    image = ProductImage.query.get(image_id)
    if not image:
        return jsonify({'error': 'Image not found'}), 404
    
    product = ProductService.get_product_by_id(image.product_id)
    if not product:
        return jsonify({'error': 'Product not found'}), 404

    if product.seller_id != current_user_id and (not current_user or current_user.role != 'seller'):
        return jsonify({'error': 'Access denied'}), 403
    
    success, error = ProductService.delete_product_image(image_id)
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({'message': 'Image deleted successfully'}), 200


