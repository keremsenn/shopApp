from app import db
from datetime import datetime
from werkzeug.security import generate_password_hash, check_password_hash

class User(db.Model):
    __tablename__ = 'users'
    
    id = db.Column(db.Integer, primary_key=True)
    fullname = db.Column(db.String(100), nullable=False)
    email = db.Column(db.String(100), unique=True, nullable=False, index=True)
    password = db.Column(db.String(255), nullable=False)
    phone = db.Column(db.String(20))
    role = db.Column(db.String(20), nullable=False, default='customer')
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    is_deleted = db.Column(db.Boolean, default=False, nullable=False)
    deleted_at = db.Column(db.DateTime, nullable=True)

    # Relationships
    addresses = db.relationship('Address', backref='user', lazy=True, cascade='all, delete-orphan')
    products = db.relationship('Product', backref='seller', lazy=True, foreign_keys='Product.seller_id')
    cart = db.relationship('Cart', backref='user', uselist=False, lazy=True, cascade='all, delete-orphan')
    orders = db.relationship('Order', backref='user', lazy=True, cascade='all, delete-orphan')
    
    def set_password(self, password):
        self.password = generate_password_hash(password)
    
    def check_password(self, password):
        return check_password_hash(self.password, password)



