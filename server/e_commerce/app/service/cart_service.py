from app import db
from app.models.cart import Cart, CartItem
from app.models.product import Product

class CartService:
    @staticmethod
    def get_or_create_cart(user_id):
        cart = Cart.query.filter_by(user_id=user_id).first()
        if not cart:
            cart = Cart(user_id=user_id)
            try:
                db.session.add(cart)
                db.session.commit()
            except Exception as e:
                db.session.rollback()
                return None, str(e)
        return cart, None
    
    @staticmethod
    def get_cart(user_id):
        return Cart.query.filter_by(user_id=user_id).first()
    
    @staticmethod
    def add_item_to_cart(user_id, product_id, quantity=1):
        cart, error = CartService.get_or_create_cart(user_id)
        if error:
            return None, error

        product = Product.query.get(product_id)
        if not product:
            return None, "Product not found"
        
        if product.stock < quantity:
            return None, "Insufficient stock"

        cart_item = CartItem.query.filter_by(cart_id=cart.id, product_id=product_id).first()
        if cart_item:
            if product.stock < cart_item.quantity + quantity:
                return None, "Insufficient stock"
            cart_item.quantity += quantity
        else:
            cart_item = CartItem(cart_id=cart.id, product_id=product_id, quantity=quantity)
            db.session.add(cart_item)
        
        try:
            db.session.commit()
            return cart_item, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)
    
    @staticmethod
    def update_cart_item(cart_item_id, quantity):
        cart_item = CartItem.query.get(cart_item_id)
        if not cart_item:
            return None, "Cart item not found"
        
        if quantity <= 0:
            return CartService.remove_item_from_cart(cart_item_id)

        if cart_item.product.stock < quantity:
            return None, "Insufficient stock"
        
        cart_item.quantity = quantity
        
        try:
            db.session.commit()
            return cart_item, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)
    
    @staticmethod
    def remove_item_from_cart(cart_item_id):
        cart_item = CartItem.query.get(cart_item_id)
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
        cart = CartService.get_cart(user_id)
        if not cart:
            return True, None
        
        try:
            CartItem.query.filter_by(cart_id=cart.id).delete()
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)

