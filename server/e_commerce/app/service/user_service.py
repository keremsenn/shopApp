from flask_jwt_extended import get_jwt_identity

from app import db
from app.models.user import User


class UserService:
    @staticmethod
    def get_all_users():
        return User.query.all()

    @staticmethod
    def get_user_by_id(user_id):
        return User.query.get(user_id)

    @staticmethod
    def get_user_by_email(email):
        return User.query.filter_by(email=email).first()

    @staticmethod
    def update_user(user_id, **kwargs):
        user = User.query.get(user_id)
        if not user:
            return None, "User not found"

        allowed_fields = ['fullname', 'email', 'phone']

        current_user_id = int(get_jwt_identity())
        current_user = UserService.get_user_by_id(current_user_id)

        if current_user.role == 'admin':
            allowed_fields.append('role')

        for field, value in kwargs.items():
            if field in allowed_fields and value is not None:
                if field == 'email':
                    existing_user = User.query.filter_by(email=value).first()
                    if existing_user and existing_user.id != user_id:
                        return None, "Email already in use"
                setattr(user, field, value)

        try:
            db.session.commit()
            return user, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def delete_user(user_id):
        user = User.query.get(user_id)
        if not user:
            return False, "User not found"

        try:
            db.session.delete(user)
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)

