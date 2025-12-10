from app import db
from app.models.address import Address


class AddressService:
    @staticmethod
    def get_user_addresses(user_id):
        return Address.query.filter_by(user_id=user_id).all()

    @staticmethod
    def get_address_by_id(address_id):
        return Address.query.get(address_id)

    @staticmethod
    def create_address(user_id, title, city, district, detail):
        address = Address(
            user_id=user_id,
            title=title,
            city=city,
            district=district,
            detail=detail
        )

        try:
            db.session.add(address)
            db.session.commit()
            return address, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def update_address(address_id, **kwargs):
        address = Address.query.get(address_id)
        if not address:
            return None, "Address not found"

        allowed_fields = ['title', 'city', 'district', 'detail']
        for field, value in kwargs.items():
            if field in allowed_fields and value is not None:
                setattr(address, field, value)

        try:
            db.session.commit()
            return address, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def delete_address(address_id):
        address = Address.query.get(address_id)
        if not address:
            return False, "Address not found"

        try:
            db.session.delete(address)
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)
