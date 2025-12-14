from flask import Blueprint, request, jsonify
from marshmallow import ValidationError
from app.service.auth_service import AuthService
from app.schemas.auth_schema import RegisterSchema, LoginSchema
from app.schemas.user_schema import UserSchema

auth_bp = Blueprint('auth', __name__)

register_schema = RegisterSchema()
login_schema = LoginSchema()
user_schema = UserSchema()

@auth_bp.route('/register', methods=['POST'])
def register():
    json_data = request.get_json()
    if not json_data:
        return jsonify({'error': 'No data provided'}), 400
    try:
        validated_data = register_schema.load(json_data)
    except ValidationError as err:
        return jsonify(err.messages), 422

    user, error = AuthService.register_user(validated_data)

    if error:
        return jsonify({'error': error}), 400

    return jsonify({
        'message': 'User registered successfully',
        'user': user_schema.dump(user)
    }), 201


@auth_bp.route('/login', methods=['POST'])
def login():
    json_data = request.get_json()
    if not json_data:
        return jsonify({'error': 'No data provided'}), 400
    try:
        validated_data = login_schema.load(json_data)
    except ValidationError as err:
        return jsonify(err.messages), 422
    result, error = AuthService.login_user(validated_data)

    if error:
        return jsonify({'error': error}), 401

    return jsonify({
        'user': user_schema.dump(result['user']),
        'access_token': result['access_token']
    }), 200