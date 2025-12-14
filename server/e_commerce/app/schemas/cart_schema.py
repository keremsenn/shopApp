from marshmallow import Schema, fields, validate
from app.schemas.product_schema import ProductSchema


class CartItemSchema(Schema):
    id = fields.Int(dump_only=True)
    product_id = fields.Int(required=True, load_only=True)
    quantity = fields.Int(
        load_default=1,
        validate=validate.Range(min=1, max=10, error="Bir seferde en az 1, en fazla 10 adet ekleyebilirsiniz."))
    product = fields.Nested(ProductSchema(only=("id", "name", "price", "images", "stock")), dump_only=True)
    subtotal = fields.Method("get_subtotal", dump_only=True)

    def get_subtotal(self, obj):
        if obj.product:
            return float(obj.product.price) * obj.quantity
        return 0.0


class CartSchema(Schema):
    id = fields.Int(dump_only=True)
    user_id = fields.Int(dump_only=True)
    created_at = fields.DateTime(dump_only=True)
    items = fields.List(fields.Nested(CartItemSchema), dump_only=True)
    total_price = fields.Method("get_total_price", dump_only=True)

    def get_total_price(self, obj):
        return sum(float(item.product.price) * item.quantity for item in obj.items if item.product)