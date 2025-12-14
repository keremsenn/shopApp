from marshmallow import Schema, fields, validate


class AddressSchema(Schema):
    id = fields.Int(dump_only=True)
    user_id = fields.Int(dump_only=True)

    title = fields.Str(
        required=True,
        validate=validate.Length(min=2, error="Adres başlığı en az 2 karakter olmalıdır.")
    )
    city = fields.Str(required=True, error_messages={"required": "Şehir bilgisi zorunludur."})
    district = fields.Str(required=True, error_messages={"required": "İlçe bilgisi zorunludur."})
    detail = fields.Str(
        required=True,
        validate=validate.Length(min=10, error="Açık adres en az 10 karakter olmalıdır.")
    )