from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.service.seller_request_service import SellerRequestService
from app.service.user_service import UserService

seller_req_bp = Blueprint('seller_requests', __name__)

@seller_req_bp.route('/apply', methods=['POST'])
@jwt_required()
def apply_for_seller():
    current_user_id = int(get_jwt_identity())
    data = request.get_json() or {}

    request_obj, error = SellerRequestService.create_request(
        user_id=current_user_id,
        company_name=data.get('company_name'),
        tax_number=data.get('tax_number')
    )

    if error:
        return jsonify({'error': error}), 400

    return jsonify({
        'message': 'Application submitted successfully. Waiting for admin approval.',
        'request': request_obj.to_dict()
    }), 201


@seller_req_bp.route('/pending', methods=['GET'])
@jwt_required()
def get_pending_applications():
    current_user_id = int(get_jwt_identity())
    current_user = UserService.get_user_by_id(current_user_id)

    if not current_user or current_user.role != 'admin':
        return jsonify({'error': 'Admin access required'}), 403

    requests = SellerRequestService.get_pending_requests()
    return jsonify([req.to_dict() for req in requests]), 200

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
        'request': req.to_dict()
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
        'request': req.to_dict()
    }), 200