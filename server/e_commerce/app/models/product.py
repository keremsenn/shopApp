from app import db

class Product(db.Model):
    __tablename__ = 'products'
    
    id = db.Column(db.Integer, primary_key=True)
    seller_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    category_id = db.Column(db.Integer, db.ForeignKey('categories.id'), nullable=False)
    name = db.Column(db.String(200), nullable=False)
    description = db.Column(db.Text)
    price = db.Column(db.Numeric(10, 2), nullable=False)
    stock = db.Column(db.Integer, default=0, nullable=False)
    is_deleted = db.Column(db.Boolean, default=False, nullable=False)
    deleted_at = db.Column(db.DateTime, nullable=True)
    # Relationships
    images = db.relationship('ProductImage', backref='product', lazy=True, cascade='all, delete-orphan')
    cart_items = db.relationship('CartItem', backref='product', lazy=True)
    order_items = db.relationship('OrderItem', backref='product', lazy=True)


