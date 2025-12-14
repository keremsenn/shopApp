from marshmallow import Schema, fields, validate

class LoginSchema(Schema):
    email = fields.Email(required=True, error_messages={"required": "Email gereklidir.", "invalid": "Geçersiz email formatı."})
    password = fields.Str(required=True, error_messages={"required": "Şifre gereklidir."})

class RegisterSchema(Schema):
    fullname = fields.Str(required=True, error_messages={"required": "Ad Soyad gereklidir."})
    email = fields.Email(required=True, error_messages={"required": "Email gereklidir.", "invalid": "Geçersiz email formatı."})
    password = fields.Str(
        required=True,
        validate=validate.Length(min=6, error="Şifre en az 6 karakter olmalıdır.")
    )
    phone = fields.Str(load_default=None)