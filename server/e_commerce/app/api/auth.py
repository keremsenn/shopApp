from flask import Blueprint, request, jsonify
from app.service.auth_service import AuthService

auth_bp = Blueprint('auth', __name__)


@auth_bp.route('/register', methods=['POST'])
def register():
    data = request.get_json()

    if not data:
        return jsonify({'error': 'No data provided'}), 400

    required_fields = ['fullname', 'email', 'password']
    for field in required_fields:
        if field not in data:
            return jsonify({'error': f'{field} is required'}), 400

    user, error = AuthService.register_user(
        fullname=data['fullname'],
        email=data['email'],
        password=data['password'],
        phone=data.get('phone'),
        role='customer'
    )

    if error:
        return jsonify({'error': error}), 400

    return jsonify({
        'message': 'User registered successfully',
        'user': user.to_dict()
    }), 201


@auth_bp.route('/login', methods=['POST'])
def login():
    data = request.get_json()

    if not data:
        return jsonify({'error': 'No data provided'}), 400

    if 'email' not in data or 'password' not in data:
        return jsonify({'error': 'Email and password are required'}), 400

    result, error = AuthService.login_user(data['email'], data['password'])

    if error:
        return jsonify({'error': error}), 401

    return jsonify(result), 200