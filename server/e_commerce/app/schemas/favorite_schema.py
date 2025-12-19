# schemas/favorite_schema.py

from flask_marshmallow import Schema
from marshmallow import fields
from .product_schema import ProductSchema


class FavoriteSchema(Schema):
    id = fields.Int(dump_only=True)
    user_id = fields.Int(
        load_only=True)
    product_id = fields.Int(required=True, load_only=True)
    created_at = fields.DateTime(dump_only=True)

    product = fields.Nested(ProductSchema, dump_only=True)

    class Meta:
        fields = ("id", "user_id", "product_id", "created_at", "product")