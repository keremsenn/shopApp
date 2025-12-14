from app import db
from app.models.address import Address
from datetime import datetime


class AddressService:

    @staticmethod
    def get_user_addresses(user_id):
        return Address.query.filter_by(user_id=user_id, is_deleted=False).all()

    @staticmethod
    def get_address_by_id(address_id, user_id):
        return Address.query.filter_by(id=address_id, user_id=user_id, is_deleted=False).first()

    @staticmethod
    def create_address(user_id, data):
        count = Address.query.filter_by(user_id=user_id, is_deleted=False).count()
        if count >= 10:
            return None, "Address limit reached (Max 10)"

        address = Address(
            user_id=user_id,
            title=data['title'],
            city=data['city'],
            district=data['district'],
            detail=data['detail']
        )

        try:
            db.session.add(address)
            db.session.commit()
            return address, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def update_address(address_id, user_id, data):
        address = Address.query.filter_by(id=address_id, user_id=user_id, is_deleted=False).first()

        if not address:
            return None, "Address not found or access denied"
        try:
            for key, value in data.items():
                setattr(address, key, value)

            db.session.commit()
            return address, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def delete_address(address_id, user_id):
        address = Address.query.filter_by(id=address_id, user_id=user_id, is_deleted=False).first()

        if not address:
            return False, "Address not found or access denied"

        try:
            address.is_deleted = True
            address.deleted_at = datetime.utcnow()

            db.session.commit()
            return True, None

        except Exception as e:
            db.session.rollback()
            return False, str(e)