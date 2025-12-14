from app import db
from app.models.seller_request import SellerRequest
from app.models.user import User
from sqlalchemy.orm import joinedload

class SellerRequestService:

    @staticmethod
    def create_request(user_id, data):
        user = User.query.filter_by(id=user_id, is_deleted=False).first()
        if not user:
            return None, "User not found"

        if user.role in ['seller', 'admin']:
            return None, "You are already a seller or admin"

        existing_request = SellerRequest.query.filter_by(user_id=user_id, status='pending').first()
        if existing_request:
            return None, "You already have a pending request"

        new_request = SellerRequest(
            user_id=user_id,
            company_name=data['company_name'],
            tax_number=data['tax_number']
        )

        try:
            db.session.add(new_request)
            db.session.commit()
            return new_request, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def get_pending_requests():
        return SellerRequest.query \
            .join(User) \
            .filter(SellerRequest.status == 'pending') \
            .filter(User.is_deleted == False) \
            .options(joinedload(SellerRequest.user)) \
            .all()

    @staticmethod
    def approve_request(request_id, requesting_admin):
        if requesting_admin.role != 'admin':
            return None, "Access denied"

        seller_req = SellerRequest.query.get(request_id)
        if not seller_req:
            return None, "Request not found"

        if seller_req.status != 'pending':
            return None, f"Request is already {seller_req.status}"

        user = User.query.get(seller_req.user_id)
        if not user or user.is_deleted:
            seller_req.status = 'rejected'
            db.session.commit()
            return None, "User account not found. Request rejected automatically."

        try:
            seller_req.status = 'approved'
            user.role = 'seller'
            db.session.commit()
            return seller_req, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def reject_request(request_id, requesting_admin):
        if requesting_admin.role != 'admin':
            return None, "Access denied"

        seller_req = SellerRequest.query.get(request_id)
        if not seller_req:
            return None, "Request not found"

        if seller_req.status != 'pending':
            return None, "Request is already processed"

        try:
            seller_req.status = 'rejected'
            db.session.commit()
            return seller_req, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)