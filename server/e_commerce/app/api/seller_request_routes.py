from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from marshmallow import ValidationError
from app.service.seller_request_service import SellerRequestService
from app.service.user_service import UserService
from app.schemas.seller_request_schema import SellerRequestSchema, SellerRequestApplySchema

seller_req_bp = Blueprint('seller_requests', __name__)

apply_schema = SellerRequestApplySchema()
request_schema = SellerRequestSchema()
requests_schema = SellerRequestSchema(many=True)


@seller_req_bp.route('/apply', methods=['POST'])
@jwt_required()
def apply_for_seller():
    current_user_id = int(get_jwt_identity())
    json_data = request.get_json() or {}

    try:
        validated_data = apply_schema.load(json_data)
    except ValidationError as err:
        return jsonify(err.messages), 422

    request_obj, error = SellerRequestService.create_request(
        user_id=current_user_id,
        data=validated_data
    )

    if error:
        return jsonify({'error': error}), 400

    return jsonify({
        'message': 'Application submitted successfully. Waiting for admin approval.',
        'request': request_schema.dump(request_obj)
    }), 201


@seller_req_bp.route('/pending', methods=['GET'])
@jwt_required()
def get_pending_applications():
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Admin access required'}), 403

    requests = SellerRequestService.get_pending_requests()
    return jsonify(requests_schema.dump(requests)), 200


@seller_req_bp.route('/<int:request_id>/approve', methods=['POST'])
@jwt_required()
def approve_application(request_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Admin access required'}), 403

    req, error = SellerRequestService.approve_request(request_id, requesting_admin=current_user)

    if error:
        return jsonify({'error': error}), 400

    return jsonify({
        'message': 'User upgraded to SELLER successfully',
        'request': request_schema.dump(req)
    }), 200


@seller_req_bp.route('/<int:request_id>/reject', methods=['POST'])
@jwt_required()
def reject_application(request_id):
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Admin access required'}), 403

    req, error = SellerRequestService.reject_request(request_id, requesting_admin=current_user)

    if error:
        return jsonify({'error': error}), 400

    return jsonify({
        'message': 'Application rejected',
        'request': request_schema.dump(req)
    }), 200