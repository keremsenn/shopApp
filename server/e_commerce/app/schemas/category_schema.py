from marshmallow import Schema, fields


class CategorySchema(Schema):
    id = fields.Int(dump_only=True)
    name = fields.Str(required=True, error_messages={"required": "Kategori adı zorunludur."})
    parent_id = fields.Int(allow_none=True)
    children = fields.List(fields.Nested(lambda: CategorySchema(exclude=("parent_id",))), dump_only=True)