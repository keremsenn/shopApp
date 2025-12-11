import re
from app import db
from app.models.user import User
from flask_jwt_extended import create_access_token
from datetime import datetime

class AuthService:
    @staticmethod
    def register_user(fullname, email, password, phone=None, role='customer'):
        email = email.lower().strip()
        email_regex = r'^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$'
        if not re.match(email_regex, email):
            return None, "Invalid email format"

        if len(password) < 6:
            return None, "Password must be at least 6 characters"

        existing_user = User.query.filter_by(email=email).first()
        if existing_user:
            if not existing_user.is_deleted:
                return None, "Email already registered"

            else:
                existing_user.is_deleted = False
                existing_user.fullname = fullname
                existing_user.phone = phone
                existing_user.role = role
                existing_user.set_password(password)
                existing_user.created_at = datetime.utcnow()

                try:
                    db.session.commit()
                    return existing_user, None
                except Exception as e:
                    db.session.rollback()
                    return None, str(e)

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
        email = email.lower().strip()

        user = User.query.filter_by(email=email, is_deleted=False).first()

        if not user or not user.check_password(password):
            return None, "Invalid email or password"

        additional_claims = {"role": user.role}
        access_token = create_access_token(identity=str(user.id), additional_claims=additional_claims)

        return {
            'user': user.to_dict(),
            'access_token': access_token
        }, None