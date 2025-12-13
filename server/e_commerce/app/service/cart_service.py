from sqlalchemy.orm import joinedload
from app import db
from app.models import User
from app.models.cart import Cart, CartItem
from app.models.product import Product

class CartService:
    @staticmethod
    def get_cart(user_id):
        cart = Cart.query \
            .join(User, Cart.user_id == User.id) \
            .options(
            joinedload(Cart.items).joinedload(CartItem.product)
        ) \
            .filter(
            Cart.user_id == user_id,
            User.is_deleted == False
        ) \
            .first()

        if not cart:
            return None

        return cart

    @staticmethod
    def get_or_create_cart(user_id):
        cart = Cart.query.filter_by(user_id=user_id).first()
        if not cart:
            cart = Cart(user_id=user_id)
            try:
                db.session.add(cart)
                db.session.commit()
                return cart, None
            except Exception as e:
                db.session.rollback()
                return None, str(e)
        return cart, None

    @staticmethod
    def add_item_to_cart(user_id, product_id, quantity=1):
        if quantity <= 0:
            return None, "Quantity must be greater than 0"

        cart, error = CartService.get_or_create_cart(user_id)
        if error:
            return None, error

        product = Product.query.filter_by(id=product_id, is_deleted=False).first()
        if not product:
            return None, "Product not found"

        if product.stock < quantity:
            return None, f"Insufficient stock. Available: {product.stock}"

        cart_item = CartItem.query.filter_by(
            cart_id=cart.id,
            product_id=product_id
        ).first()

        if cart_item:
            new_quantity = cart_item.quantity + quantity
            if product.stock < new_quantity:
                return None, f"Insufficient stock. Available: {product.stock}"
            cart_item.quantity = new_quantity
        else:
            cart_item = CartItem(
                cart_id=cart.id,
                product_id=product_id,
                quantity=quantity
            )
            db.session.add(cart_item)

        try:
            db.session.commit()
            return cart_item, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def update_cart_item(user_id, cart_item_id, quantity):

        cart_item = CartItem.query.join(Cart).filter(
            CartItem.id == cart_item_id,
            Cart.user_id == user_id
        ).first()

        if not cart_item:
            return None, "Cart item not found"

        if cart_item.product.is_deleted:
            return None, "This product is no longer available"

        if quantity <= 0:
            return None, "Quantity must be greater than 0"

        if cart_item.product.stock < quantity:
            return None, f"Insufficient stock. Available: {cart_item.product.stock}"

        cart_item.quantity = quantity

        try:
            db.session.commit()
            return cart_item, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def remove_item_from_cart(user_id, cart_item_id):
        cart_item = CartItem.query.join(Cart).filter(
            CartItem.id == cart_item_id,
            Cart.user_id == user_id
        ).first()

        if not cart_item:
            return False, "Cart item not found"

        try:
            db.session.delete(cart_item)
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)

    @staticmethod
    def clear_cart(user_id):
        cart = Cart.query.filter_by(user_id=user_id).first()
        if not cart:
            return False, "Cart not found"

        try:
            CartItem.query.filter_by(cart_id=cart.id).delete()
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)
