from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from marshmallow import ValidationError
from app.service.address_service import AddressService
from app.schemas.address_schema import AddressSchema

addresses_bp = Blueprint('addresses', __name__)

address_schema = AddressSchema()
addresses_schema = AddressSchema(many=True)


@addresses_bp.route('', methods=['GET'])
@jwt_required()
def get_user_addresses():
    current_user_id = int(get_jwt_identity())
    addresses = AddressService.get_user_addresses(current_user_id)
    return jsonify(addresses_schema.dump(addresses)), 200


@addresses_bp.route('/<int:address_id>', methods=['GET'])
@jwt_required()
def get_address(address_id):
    current_user_id = int(get_jwt_identity())
    address = AddressService.get_address_by_id(address_id, current_user_id)

    if not address:
        return jsonify({'error': 'Address not found'}), 404
    return jsonify(address_schema.dump(address)), 200


@addresses_bp.route('', methods=['POST'])
@jwt_required()
def create_address():
    current_user_id = int(get_jwt_identity())
    json_data = request.get_json()

    if not json_data:
        return jsonify({'error': 'No data provided'}), 400
    try:
        validated_data = address_schema.load(json_data)
    except ValidationError as err:
        return jsonify(err.messages), 422

    address, error = AddressService.create_address(
        user_id=current_user_id,
        data=validated_data
    )

    if error:
        return jsonify({'error': error}), 400

    return jsonify({
        'message': 'Address created successfully',
        'address': address_schema.dump(address)
    }), 201


@addresses_bp.route('/<int:address_id>', methods=['PUT'])
@jwt_required()
def update_address(address_id):
    current_user_id = int(get_jwt_identity())
    json_data = request.get_json()

    if not json_data:
        return jsonify({'error': 'No data provided'}), 400
    try:
        validated_data = address_schema.load(json_data, partial=True)
    except ValidationError as err:
        return jsonify(err.messages), 422

    address, error = AddressService.update_address(address_id, current_user_id, validated_data)

    if error:
        status_code = 404 if "not found" in error else 400
        return jsonify({'error': error}), status_code

    return jsonify({
        'message': 'Address updated successfully',
        'address': address_schema.dump(address)
    }), 200


@addresses_bp.route('/<int:address_id>', methods=['DELETE'])
@jwt_required()
def delete_address(address_id):
    current_user_id = int(get_jwt_identity())

    success, error = AddressService.delete_address(address_id, current_user_id)

    if error:
        status_code = 404 if "not found" in error else 400
        return jsonify({'error': error}), status_code

    return jsonify({'message': 'Address deleted successfully'}), 200