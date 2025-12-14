from app import db
from datetime import datetime


class SellerRequest(db.Model):
    __tablename__ = 'seller_requests'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    company_name = db.Column(db.String(100), nullable=True)
    tax_number = db.Column(db.String(50), nullable=True)
    status = db.Column(db.String(20), default='pending', nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, onupdate=datetime.utcnow)


    user = db.relationship('User', backref=db.backref('seller_requests', lazy=True))
