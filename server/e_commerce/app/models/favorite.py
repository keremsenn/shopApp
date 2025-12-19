

from app import db
from datetime import datetime

class Favorite(db.Model):
    __tablename__ = 'favorites'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    product_id = db.Column(db.Integer, db.ForeignKey('products.id'), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    product = db.relationship("Product", backref="favorited_by_users")
    user = db.relationship("User", backref="favorites")

    __table_args__ = (
        db.UniqueConstraint('user_id', 'product_id', name='unique_user_product_favorite'),
    )

    def __repr__(self):
        return f"<Favorite User:{self.user_id} Product:{self.product_id}>"