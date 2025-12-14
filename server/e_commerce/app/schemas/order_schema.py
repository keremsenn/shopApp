from marshmallow import Schema, fields, validate
from app.schemas.product_schema import ProductSchema

class OrderItemSchema(Schema):
    id = fields.Int(dump_only=True)
    product_id = fields.Int(dump_only=True)
    quantity = fields.Int(dump_only=True)
    unit_price = fields.Float(dump_only=True)
    product = fields.Nested(ProductSchema(only=("id", "name", "images")), dump_only=True)
    subtotal = fields.Method("get_subtotal", dump_only=True)

    def get_subtotal(self, obj):
        return float(obj.unit_price) * obj.quantity

class OrderSchema(Schema):
    id = fields.Int(dump_only=True)
    user_id = fields.Int(dump_only=True)
    status = fields.Str(dump_only=True)
    total_price = fields.Float(dump_only=True)
    created_at = fields.DateTime(dump_only=True)
    shipping_title = fields.Str(dump_only=True)
    shipping_city = fields.Str(dump_only=True)
    shipping_district = fields.Str(dump_only=True)
    shipping_detail = fields.Str(dump_only=True)
    items = fields.List(fields.Nested(OrderItemSchema), dump_only=True)

class OrderCreateItemSchema(Schema):
    product_id = fields.Int(required=True, error_messages={"required": "Product ID is required"})
    quantity = fields.Int(load_default=1, validate=validate.Range(min=1))