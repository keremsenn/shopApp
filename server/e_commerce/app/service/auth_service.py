from app import db
from app.models.user import User
from flask_jwt_extended import create_access_token, create_refresh_token
from datetime import datetime


class AuthService:
    @staticmethod
    def register_user(data):
        email = data['email'].lower().strip()
        existing_user = User.query.filter_by(email=email).first()

        if existing_user:
            if not existing_user.is_deleted:
                return None, "Email already registered"

            else:
                existing_user.is_deleted = False
                existing_user.fullname = data['fullname']
                existing_user.phone = data.get('phone')
                existing_user.role = 'customer'
                existing_user.set_password(data['password'])
                existing_user.created_at = datetime.utcnow()

                try:
                    db.session.commit()
                    return existing_user, None
                except Exception as e:
                    db.session.rollback()
                    return None, str(e)
        user = User(
            fullname=data['fullname'],
            email=email,
            phone=data.get('phone'),
            role='customer'
        )
        user.set_password(data['password'])

        try:
            db.session.add(user)
            db.session.commit()
            return user, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def login_user(data):
        email = data['email'].lower().strip()
        password = data['password']

        user = User.query.filter_by(email=email, is_deleted=False).first()

        if not user or not user.check_password(password):
            return None, "Invalid email or password"

        additional_claims = {"role": user.role}

        access_token = create_access_token(identity=str(user.id), additional_claims=additional_claims)
        refresh_token = create_refresh_token(identity=str(user.id))

        return {
            'user': user,
            'access_token': access_token,
            'refresh_token': refresh_token
        }, None