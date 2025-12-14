from app import db
from app.models.user import User
from flask_jwt_extended import create_access_token
from datetime import datetime


class AuthService:
    @staticmethod
    def register_user(data):
        """
        data: Schema'dan geçmiş temiz veri (email, password, fullname, phone)
        """
        # Email küçük harfe çevrilir (Veri tutarlılığı için logic burada kalmalı)
        email = data['email'].lower().strip()

        # 1. Kullanıcı daha önce kayıt olmuş mu?
        existing_user = User.query.filter_by(email=email).first()

        if existing_user:
            # Eğer aktifse hata dön
            if not existing_user.is_deleted:
                return None, "Email already registered"

            # Eğer silinmişse hesabı tekrar aktifleştir (Reactivate)
            else:
                existing_user.is_deleted = False
                existing_user.fullname = data['fullname']
                existing_user.phone = data.get('phone')
                existing_user.role = 'customer'  # Varsayılan rol
                existing_user.set_password(data['password'])
                existing_user.created_at = datetime.utcnow()  # Kayıt tarihini yenile

                try:
                    db.session.commit()
                    return existing_user, None
                except Exception as e:
                    db.session.rollback()
                    return None, str(e)

        # 2. Yeni Kullanıcı Oluşturma
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
        # Data şemadan geldiği için email ve password kesin var.
        email = data['email'].lower().strip()
        password = data['password']

        user = User.query.filter_by(email=email, is_deleted=False).first()

        if not user or not user.check_password(password):
            return None, "Invalid email or password"

        # JWT Token oluşturma
        additional_claims = {"role": user.role}
        access_token = create_access_token(identity=str(user.id), additional_claims=additional_claims)

        return {
            'user': user,  # User objesi döner (API'de dump edilecek)
            'access_token': access_token
        }, None