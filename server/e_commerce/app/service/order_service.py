from app import db
from app.models.order import Order, OrderItem
from app.models.cart import Cart, CartItem
from app.models.product import Product
from app.service.cart_service import CartService


class OrderService:
    @staticmethod
    def get_all_orders():
        return Order.query.all()

    @staticmethod
    def get_order_by_id(order_id):
        return Order.query.get(order_id)

    @staticmethod
    def get_orders_by_user(user_id):
        return Order.query.filter_by(user_id=user_id).all()

    @staticmethod
    def create_order_from_cart(user_id):
        cart = CartService.get_cart(user_id)
        if not cart or not cart.items:
            return None, "Cart is empty"

        total_price = sum(float(item.product.price) * item.quantity for item in cart.items)

        for item in cart.items:
            if item.product.stock < item.quantity:
                return None, f"Insufficient stock for product: {item.product.name}"

        order = Order(user_id=user_id, total_price=total_price, status='pending')

        try:
            db.session.add(order)
            db.session.flush()

            # Create order items and update stock
            for cart_item in cart.items:
                order_item = OrderItem(
                    order_id=order.id,
                    product_id=cart_item.product_id,
                    quantity=cart_item.quantity,
                    unit_price=cart_item.product.price
                )
                db.session.add(order_item)

                cart_item.product.stock -= cart_item.quantity

            # Clear cart
            CartItem.query.filter_by(cart_id=cart.id).delete()

            db.session.commit()
            return order, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def create_order(user_id, items):
        # items should be a list of {product_id, quantity}
        total_price = 0
        order_items_data = []

        for item_data in items:
            product = Product.query.get(item_data['product_id'])
            if not product:
                return None, f"Product {item_data['product_id']} not found"

            if product.stock < item_data['quantity']:
                return None, f"Insufficient stock for product: {product.name}"

            total_price += float(product.price) * item_data['quantity']
            order_items_data.append({
                'product': product,
                'quantity': item_data['quantity']
            })

        order = Order(user_id=user_id, total_price=total_price, status='pending')

        try:
            db.session.add(order)
            db.session.flush()

            # Create order items and update stock
            for item_data in order_items_data:
                order_item = OrderItem(
                    order_id=order.id,
                    product_id=item_data['product'].id,
                    quantity=item_data['quantity'],
                    unit_price=item_data['product'].price
                )
                db.session.add(order_item)
                item_data['product'].stock -= item_data['quantity']

            db.session.commit()
            return order, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def update_order_status(order_id, status):
        order = Order.query.get(order_id)
        if not order:
            return None, "Order not found"

        valid_statuses = ['pending', 'processing', 'shipped', 'delivered', 'cancelled']
        if status not in valid_statuses:
            return None, f"Invalid status. Must be one of: {', '.join(valid_statuses)}"

        order.status = status

        try:
            db.session.commit()
            return order, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def cancel_order(order_id):
        order = Order.query.get(order_id)
        if not order:
            return False, "Order not found"

        if order.status == 'cancelled':
            return False, "Order already cancelled"

        if order.status == 'delivered':
            return False, "Cannot cancel delivered order"

        try:
            # Restore stock
            for item in order.items:
                item.product.stock += item.quantity

            order.status = 'cancelled'
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)
