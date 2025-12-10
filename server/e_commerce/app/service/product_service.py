import shutil

from flask import current_app

from app import db
from app.models.product import Product
from app.models.product_image import ProductImage
from werkzeug.utils import secure_filename
from config import Config
import os
from datetime import datetime


class ProductService:
    @staticmethod
    def get_all_products():
        return Product.query.all()

    @staticmethod
    def get_product_by_id(product_id):
        return Product.query.get(product_id)

    @staticmethod
    def get_products_by_seller(seller_id):
        return Product.query.filter_by(seller_id=seller_id).all()

    @staticmethod
    def get_products_by_category(category_id):
        return Product.query.filter_by(category_id=category_id).all()

    @staticmethod
    def create_product(seller_id, category_id, name, description, price, stock, rating=0.0):
        product = Product(
            seller_id=seller_id,
            category_id=category_id,
            name=name,
            description=description,
            price=price,
            stock=stock,
            rating=rating
        )

        try:
            db.session.add(product)
            db.session.commit()
            return product, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def update_product(product_id, **kwargs):
        product = Product.query.get(product_id)
        if not product:
            return None, "Product not found"

        allowed_fields = ['name', 'description', 'price', 'stock', 'rating', 'category_id']
        for field, value in kwargs.items():
            if field in allowed_fields and value is not None:
                setattr(product, field, value)

        try:
            db.session.commit()
            return product, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def delete_product(product_id):
        product = Product.query.get(product_id)
        if not product:
            return False, "Product not found"

        images = ProductImage.query.filter_by(product_id=product_id).all()
        product_folder = os.path.join(
            current_app.config['UPLOAD_FOLDER'],
            str(product_id)
        )

        try:
            for img in images:
                if os.path.exists(img.url):
                    os.remove(img.url)

            if os.path.exists(product_folder):
                shutil.rmtree(product_folder)

            for img in images:
                db.session.delete(img)

            db.session.delete(product)
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

        os.makedirs(Config.UPLOAD_FOLDER, exist_ok=True)

        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        filename = secure_filename(file.filename)
        name, ext = os.path.splitext(filename)
        unique_filename = f"{product_id}_{timestamp}_{name}{ext}"
        filepath = os.path.join(Config.UPLOAD_FOLDER, unique_filename)

        try:
            file.save(filepath)
            relative_path = f"uploads/products/{unique_filename}"
            return relative_path, None
        except Exception as e:
            return None, f"Error saving file: {str(e)}"

    @staticmethod
    def add_product_image(product_id, url=None, file=None):
        product = Product.query.get(product_id)
        if not product:
            return None, "Product not found"

        if file:
            filepath, error = ProductService.save_uploaded_file(file, product_id)
            if error:
                return None, error
            url = filepath

        if not url:
            return None, "Either URL or file must be provided"

        image = ProductImage(product_id=product_id, url=url)

        try:
            db.session.add(image)
            db.session.commit()
            return image, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def delete_product_image(image_id):
        image = ProductImage.query.get(image_id)
        if not image:
            return False, "Image not found"

        file_path = image.url
        if file_path and not file_path.startswith('http'):
            full_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))),
                                     file_path)
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

