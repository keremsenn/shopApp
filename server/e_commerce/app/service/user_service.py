from app import db
from app.models.user import User
from datetime import datetime


class UserService:
    @staticmethod
    def get_all_users():
        return User.query.filter_by(is_deleted=False).all()

    @staticmethod
    def get_user_by_id(user_id):
        return User.query.filter_by(id=user_id, is_deleted=False).first()

    @staticmethod
    def get_user_by_email(email):
        return User.query.filter_by(email=email, is_deleted=False).first()

    @staticmethod
    def update_user(user_id, data, requesting_user):

        user = User.query.filter_by(id=user_id, is_deleted=False).first()
        if not user:
            return None, "User not found"

        allowed_fields = ['fullname', 'email', 'phone']

        if requesting_user and requesting_user.role == 'admin':
            allowed_fields.append('role')

        if 'email' in data and data['email'] != user.email:
            existing_user = User.query.filter_by(email=data['email']).first()
            if existing_user:
                return None, "Email already in use by another account"

        for field in allowed_fields:
            if field in data:
                setattr(user, field, data[field])

        if 'password' in data and data['password']:
            if len(data['password']) < 6:
                return None, "Password must be at least 6 characters"
            user.set_password(data['password'])

        try:
            db.session.commit()
            return user, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def delete_user(user_id):
        user = User.query.filter_by(id=user_id, is_deleted=False).first()
        if not user:
            return False, "User not found"

        try:
            user.is_deleted = True
            user.deleted_at = datetime.utcnow()
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)