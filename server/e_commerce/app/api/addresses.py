from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.service.address_service import AddressService

addresses_bp = Blueprint('addresses', __name__)

@addresses_bp.route('', methods=['GET'])
@jwt_required()
def get_user_addresses():
    current_user_id = get_jwt_identity()
    addresses = AddressService.get_user_addresses(current_user_id)
    return jsonify([address.to_dict() for address in addresses]), 200

@addresses_bp.route('/<int:address_id>', methods=['GET'])
@jwt_required()
def get_address(address_id):
    current_user_id = int(get_jwt_identity())
    address = AddressService.get_address_by_id(address_id)
    
    if not address:
        return jsonify({'error': 'Address not found'}), 404

    if address.user_id != current_user_id:
        return jsonify({'error': 'Access denied'}), 403
    
    return jsonify(address.to_dict()), 200

@addresses_bp.route('', methods=['POST'])
@jwt_required()
def create_address():
    current_user_id = get_jwt_identity()
    data = request.get_json()
    
    if not data:
        return jsonify({'error': 'No data provided'}), 400
    
    required_fields = ['title', 'city', 'district', 'detail']
    for field in required_fields:
        if field not in data:
            return jsonify({'error': f'{field} is required'}), 400
    
    address, error = AddressService.create_address(
        user_id=current_user_id,
        title=data['title'],
        city=data['city'],
        district=data['district'],
        detail=data['detail']
    )
    
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({
        'message': 'Address created successfully',
        'address': address.to_dict()
    }), 201

@addresses_bp.route('/<int:address_id>', methods=['PUT'])
@jwt_required()
def update_address(address_id):
    current_user_id = int(get_jwt_identity())
    address = AddressService.get_address_by_id(address_id)
    
    if not address:
        return jsonify({'error': 'Address not found'}), 404

    if address.user_id != current_user_id:
        return jsonify({'error': 'Access denied'}), 403
    
    data = request.get_json()
    if not data:
        return jsonify({'error': 'No data provided'}), 400
    
    address, error = AddressService.update_address(address_id, **data)
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({
        'message': 'Address updated successfully',
        'address': address.to_dict()
    }), 200

@addresses_bp.route('/<int:address_id>', methods=['DELETE'])
@jwt_required()
def delete_address(address_id):
    current_user_id = int(get_jwt_identity())
    address = AddressService.get_address_by_id(address_id)
    
    if not address:
        return jsonify({'error': 'Address not found'}), 404

    if address.user_id != current_user_id:
        return jsonify({'error': 'Access denied'}), 403
    
    success, error = AddressService.delete_address(address_id)
    if error:
        return jsonify({'error': error}), 400
    
    return jsonify({'message': 'Address deleted successfully'}), 200


