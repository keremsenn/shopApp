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
        if user.role == 'admin' and requesting_user.id != user.id:
            return None, "Access Denied: You cannot update another admin account."
        if 'role' in data:
            if requesting_user.role != 'admin':
                return None, "Unauthorized: Only admins can change roles."
            if data['role'] == 'admin':
                return None, "Security Restriction: Admins cannot grant Admin privileges."
            if requesting_user.id == user.id:
                return None, "Operation not allowed: You cannot change your own role."

        if 'email' in data and data['email'] != user.email:
            existing_user = User.query.filter_by(email=data['email']).first()
            if existing_user:
                return None, "Email already in use by another account"
        try:
            for key, value in data.items():
                if key == 'password':
                    user.set_password(value)
                else:
                    setattr(user, key, value)

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