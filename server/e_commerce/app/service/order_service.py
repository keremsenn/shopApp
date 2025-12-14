from app import db
from app.models import User, Address
from app.models.order import Order, OrderItem
from app.models.cart import Cart, CartItem
from app.models.product import Product
from app.service.cart_service import CartService


class OrderService:
    @staticmethod
    def get_all_orders():
        return Order.query.order_by(Order.created_at.desc()).all()

    @staticmethod
    def get_order_by_id(order_id):
        return Order.query.get(order_id)

    @staticmethod
    def get_orders_by_user(user_id):
        return Order.query.filter_by(user_id=user_id).order_by(Order.created_at.desc()).all()

    @staticmethod
    def create_order_from_cart(user_id, address_id):
        user = User.query.filter_by(id=user_id, is_deleted=False).first()
        if not user:
            return None, "User not found"

        cart = CartService.get_cart(user_id)
        if not cart or not cart.items:
            return None, "Cart is empty"

        address = Address.query.filter_by(id=address_id, user_id=user_id, is_deleted=False).first()
        if not address:
            return None, "Invalid delivery address"

        total_price = 0

        for item in cart.items:
            if item.product.is_deleted:
                return None, f"Product '{item.product.name}' is no longer available"
            if item.product.stock < item.quantity:
                return None, f"Insufficient stock for: {item.product.name}"
            total_price += float(item.product.price) * item.quantity

        order = Order(
            user_id=user_id,
            total_price=total_price,
            status='pending',
            shipping_title=address.title,
            shipping_city=address.city,
            shipping_district=address.district,
            shipping_detail=address.detail
        )

        try:
            db.session.add(order)
            db.session.flush()

            for cart_item in cart.items:
                order_item = OrderItem(
                    order_id=order.id,
                    product_id=cart_item.product_id,
                    quantity=cart_item.quantity,
                    unit_price=cart_item.product.price
                )
                db.session.add(order_item)
                cart_item.product.stock -= cart_item.quantity

            CartItem.query.filter_by(cart_id=cart.id).delete()

            db.session.commit()
            return order, None

        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def create_order_direct(user_id, address_id, items_data):
        user = User.query.filter_by(id=user_id, is_deleted=False).first()
        if not user:
            return None, "User not found"

        address = Address.query.filter_by(id=address_id, user_id=user_id, is_deleted=False).first()
        if not address:
            return None, "Invalid delivery address"

        total_price = 0
        order_items_buffer = []

        for item in items_data:
            product = Product.query.filter_by(id=item["product_id"], is_deleted=False).first()
            if not product:
                return None, f"Product ID {item['product_id']} not found"

            qty = item['quantity']
            if product.stock < qty:
                return None, f"Insufficient stock for: {product.name}"

            total_price += float(product.price) * qty

            order_items_buffer.append({
                'product': product,
                'quantity': qty,
                'price': product.price
            })

        order = Order(
            user_id=user_id,
            total_price=total_price,
            status='pending',
            shipping_title=address.title,
            shipping_city=address.city,
            shipping_district=address.district,
            shipping_detail=address.detail
        )

        try:
            db.session.add(order)
            db.session.flush()

            for buffer_item in order_items_buffer:
                order_item = OrderItem(
                    order_id=order.id,
                    product_id=buffer_item['product'].id,
                    quantity=buffer_item['quantity'],
                    unit_price=buffer_item['price']
                )
                db.session.add(order_item)
                buffer_item['product'].stock -= buffer_item['quantity']

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

        if order.status == 'cancelled':
            return None, "Cannot update a cancelled order"

        order.status = status
        try:
            db.session.commit()
            return order, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def cancel_order(order_id, user_id, is_admin=False):
        order = Order.query.get(order_id)
        if not order:
            return False, "Order not found"

        if not is_admin and order.user_id != user_id:
            return False, "Access denied"

        if order.status == 'cancelled':
            return False, "Order already cancelled"

        if order.status in ['shipped', 'delivered']:
            return False, f"Cannot cancel order with status: {order.status}"

        try:
            for item in order.items:
                product = Product.query.filter_by(id=item.product_id).first()
                if product:
                    product.stock += item.quantity

            order.status = 'cancelled'
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)