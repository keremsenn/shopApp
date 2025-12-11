import os
from datetime import datetime
from werkzeug.utils import secure_filename
from flask import current_app
from app import db
from app.models.product import Product
from app.models.product_image import ProductImage
from config import Config


class ProductService:
    @staticmethod
    def get_all_products():
        return Product.query.filter_by(is_deleted=False).all()

    @staticmethod
    def get_product_by_id(product_id):
        return Product.query.filter_by(id=product_id, is_deleted=False).first()

    @staticmethod
    def get_products_by_seller(seller_id):
        return Product.query.filter_by(seller_id=seller_id, is_deleted=False).all()

    @staticmethod
    def get_products_by_category(category_id):
        return Product.query.filter_by(category_id=category_id, is_deleted=False).all()

    @staticmethod
    def create_product(data, requesting_user):
        if requesting_user.role not in ['seller', 'admin']:
            return None, "Only sellers and admins can create products"


        product = Product(
            seller_id=requesting_user.id,
            category_id=data.get('category_id'),
            name=data.get('name'),
            description=data.get('description'),
            price=data.get('price'),
            stock=data.get('stock', 0),
            rating=data.get('rating', 0.0)
        )

        try:
            db.session.add(product)
            db.session.commit()
            return product, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def update_product(product_id, data, requesting_user):
        product = Product.query.filter_by(id=product_id, is_deleted=False).first()
        if not product:
            return None, "Product not found"

        if requesting_user.role != 'admin' and product.seller_id != requesting_user.id:
            return None, "Access denied"

        allowed_fields = ['name', 'description', 'price', 'stock', 'rating', 'category_id']
        for field, value in data.items():
            if field in allowed_fields and value is not None:
                setattr(product, field, value)

        try:
            db.session.commit()
            return product, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def delete_product(product_id, requesting_user):
        product = Product.query.filter_by(id=product_id, is_deleted=False).first()
        if not product:
            return False, "Product not found"

        if requesting_user.role != 'admin' and product.seller_id != requesting_user.id:
            return False, "Access denied"

        try:
            product.is_deleted = True
            product.deleted_at = datetime.utcnow()
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)


    @staticmethod
    def allowed_file(filename):
        return '.' in filename and \
            filename.rsplit('.', 1)[1].lower() in Config.ALLOWED_EXTENSIONS

    @staticmethod
    def save_uploaded_file(file, product_id):
        if not file or not ProductService.allowed_file(file.filename):
            return None, "Invalid file type. Allowed: " + ", ".join(Config.ALLOWED_EXTENSIONS)

        upload_folder = os.path.join(current_app.root_path, 'static', 'uploads', 'products')
        os.makedirs(upload_folder, exist_ok=True)

        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        filename = secure_filename(file.filename)
        name, ext = os.path.splitext(filename)
        unique_filename = f"{product_id}_{timestamp}_{name}{ext}"

        filepath = os.path.join(upload_folder, unique_filename)

        try:
            file.save(filepath)
            relative_path = f"uploads/products/{unique_filename}"
            return relative_path, None
        except Exception as e:
            return None, f"Error saving file: {str(e)}"

    @staticmethod
    def add_product_image(product_id, requesting_user, url=None, file=None):
        product = Product.query.filter_by(id=product_id, is_deleted=False).first()
        if not product:
            return None, "Product not found"

        if requesting_user.role != 'admin' and product.seller_id != requesting_user.id:
            return None, "Access denied"

        final_url = url
        if file:
            saved_path, error = ProductService.save_uploaded_file(file, product_id)
            if error:
                return None, error
            final_url = saved_path

        if not final_url:
            return None, "Either URL or file must be provided"

        image = ProductImage(product_id=product_id, url=final_url)

        try:
            db.session.add(image)
            db.session.commit()
            return image, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def delete_product_image(image_id, requesting_user):
        image = ProductImage.query.get(image_id)
        if not image:
            return False, "Image not found"

        product = Product.query.get(image.product_id)

        if requesting_user.role != 'admin':
            if not product or product.seller_id != requesting_user.id:
                return False, "Access denied"

        if image.url and not image.url.startswith(('http://', 'https://')):
            full_path = os.path.join(current_app.root_path, 'static', image.url)

            if os.path.exists(full_path):
                try:
                    os.remove(full_path)
                except Exception as e:
                    print(f"Warning: Could not delete file {full_path}: {e}")

        try:
            db.session.delete(image)
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)