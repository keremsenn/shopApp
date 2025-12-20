from marshmallow import Schema, fields, validate


class ProductImageSchema(Schema):
    id = fields.Int(dump_only=True)
    url = fields.Str(required=True)

class ProductSchema(Schema):
    id = fields.Int(dump_only=True)
    name = fields.Str(required=True, validate=validate.Length(min=2))
    description = fields.Str(allow_none=True)
    price = fields.Float(required=True, validate=validate.Range(min=0))
    stock = fields.Int(validate=validate.Range(min=0), load_default=0)
    category_id = fields.Int(required=True)
    category_name = fields.Function(lambda obj: obj.category.name if obj.category else None, dump_only=True)
    images = fields.List(fields.Nested(ProductImageSchema), dump_only=True)