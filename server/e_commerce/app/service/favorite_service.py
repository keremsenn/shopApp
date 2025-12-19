from app import db
from app.models.favorite import Favorite
from app.models.product import Product


class FavoriteService:
    @staticmethod
    def add_favorite(user_id, product_id):
        product = Product.query.get(product_id)
        if not product:
            return None, "Ürün bulunamadı."

        existing_fav = Favorite.query.filter_by(user_id=user_id, product_id=product_id).first()
        if existing_fav:
            return existing_fav, "Ürün zaten favorilerinizde."

        new_fav = Favorite(user_id=user_id, product_id=product_id)

        try:
            db.session.add(new_fav)
            db.session.commit()
            return new_fav, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def get_user_favorites(user_id):
        return Favorite.query.filter_by(user_id=user_id).order_by(Favorite.created_at.desc()).all()

    @staticmethod
    def remove_favorite(user_id, product_id):
        fav = Favorite.query.filter_by(user_id=user_id, product_id=product_id).first()

        if not fav:
            return False, "Favori bulunamadı."

        try:
            db.session.delete(fav)
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)