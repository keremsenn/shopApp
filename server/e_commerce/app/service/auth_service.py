from app import db
from app.models.user import User
from flask_jwt_extended import create_access_token
from datetime import datetime


class AuthService:
    @staticmethod
    def register_user(fullname, email, password, phone=None, role='customer'):
        if User.query.filter_by(email=email).first():
            return None, "Email already registered"

        user = User(
            fullname=fullname,
            email=email,
            phone=phone,
            role=role
        )
        user.set_password(password)

        try:
            db.session.add(user)
            db.session.commit()
            return user, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def login_user(email, password):
        user = User.query.filter_by(email=email).first()

        if not user or not user.check_password(password):
            return None, "Invalid email or password"

        access_token = create_access_token(identity=str(user.id))
        return {
            'user': user.to_dict(),
            'access_token': access_token
        }, None

    @staticmethod
    def get_user_by_id(user_id):
        return User.query.get(user_id)

