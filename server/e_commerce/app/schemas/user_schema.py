from marshmallow import Schema, fields, validate, validates, ValidationError


class UserSchema(Schema):
    id = fields.Int(dump_only=True)
    created_at = fields.DateTime(dump_only=True)
    fullname = fields.Str(required=True, error_messages={"required": "Ad Soyad alanı zorunludur."})
    email = fields.Email(required=True, error_messages={"required": "Email alanı zorunludur."})
    password = fields.Str(
        load_only=True,
        validate=validate.Length(min=6, error="Şifre en az 6 karakter olmalıdır.")
    )
    phone = fields.Str(allow_none=True)
    role = fields.Str(
        validate=validate.OneOf(
            ["customer", "seller", "admin"],
            error="Geçersiz rol. Sadece customer, seller veya admin olabilir."
        ),
        missing='customer'
    )
class UserUpdateSchema(UserSchema):
    fullname = fields.Str()
    email = fields.Email()
    password = fields.Str(validate=validate.Length(min=6))