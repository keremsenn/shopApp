from sqlalchemy.orm import joinedload
from app import db
from app.models.cart import Cart, CartItem
from app.models.product import Product


class CartService:
    @staticmethod
    def get_cart(user_id):
        cart = Cart.query \
            .options(joinedload(Cart.items).joinedload(CartItem.product)) \
            .filter_by(user_id=user_id) \
            .first()

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
    def add_item_to_cart(user_id, data):
        quantity = data['quantity']
        product_id = data['product_id']

        max_item_limit = 10

        cart, error = CartService.get_or_create_cart(user_id)
        if error:
            return None, error

        product = Product.query.filter_by(id=product_id, is_deleted=False).first()
        if not product:
            return None, "Product not found"

        if product.stock < quantity:
            return None, f"Insufficient stock. Available: {product.stock}"

        cart_item = CartItem.query.filter_by(cart_id=cart.id, product_id=product_id).first()

        current_quantity_in_cart = cart_item.quantity if cart_item else 0
        new_total_quantity = current_quantity_in_cart + quantity

        if new_total_quantity > max_item_limit:
            return None, f"Bir üründen en fazla {max_item_limit} adet alabilirsiniz. Sepetinizde zaten {current_quantity_in_cart} adet var."

        if product.stock < new_total_quantity:
            return None, f"Stok yetersiz. Toplam istenen: {new_total_quantity}, Stoktaki: {product.stock}"

        if cart_item:
            cart_item.quantity = new_total_quantity
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
        max_item_limit = 10

        cart_item = CartItem.query.join(Cart).filter(
            CartItem.id == cart_item_id,
            Cart.user_id == user_id
        ).first()

        if not cart_item:
            return None, "Cart item not found"

        if quantity > max_item_limit:
            return None, f"Bir üründen en fazla {max_item_limit} adet alabilirsiniz."

        if cart_item.product.is_deleted:
            return None, "This product is no longer available"

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