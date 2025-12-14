from marshmallow import Schema, fields, validate
from app.schemas.user_schema import UserSchema

class SellerRequestApplySchema(Schema):
    company_name = fields.Str(required=True,
                              validate=validate.Length(min=2, error="Şirket adı en az 2 karakter olmalı."))
    tax_number = fields.Str(required=True, error_messages={"required": "Vergi numarası zorunludur."})

class SellerRequestSchema(Schema):
    id = fields.Int(dump_only=True)
    status = fields.Str(dump_only=True)
    created_at = fields.DateTime(dump_only=True)
    company_name = fields.Str()
    tax_number = fields.Str()
    user = fields.Nested(UserSchema(only=("id", "fullname", "email", "phone")), dump_only=True)