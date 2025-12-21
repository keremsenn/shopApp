import os
from datetime import datetime
from werkzeug.utils import secure_filename
from flask import current_app
from app import db
from app.models.category import Category
from app.models.product import Product
from app.models.product_image import ProductImage
from app.utils.ElasticSearchService import ElasticSearchService
from config import Config


class ProductService:
    @staticmethod
    def search_in_elastic(query_text):
        hits = ElasticSearchService.search_products(query_text)

        results = []
        for hit in hits:
            product_data = hit
            if "image_url" in hit:
                product_data["images"] = [{"url": hit["image_url"]}]
            else:
                product_data["images"] = []
            results.append(product_data)

        return results

    @staticmethod
    def get_all_products():
        return Product.query.filter_by(is_deleted=False).all()

    @staticmethod
    def get_product_by_id(product_id):
        return Product.query.filter_by(id=product_id, is_deleted=False).first()

    @staticmethod
    def get_products_by_category(category_id):
        return Product.query.filter_by(category_id=category_id, is_deleted=False).all()

    @staticmethod
    def create_product(data, requesting_user):
        if requesting_user.role != 'admin':
            return None, "Erişim engellendi: Sadece admin ürün oluşturabilir"

        category = Category.query.filter_by(id=data['category_id'], is_deleted=False).first()
        if not category:
            return None, "Geçersiz Kategori ID"

        product = Product(
            seller_id=requesting_user.id,
            category_id=data['category_id'],
            name=data['name'],
            description=data.get('description'),
            price=data['price'],
            stock=data.get('stock', 0),
            rating=0.0
        )

        try:
            db.session.add(product)
            db.session.commit()
            ElasticSearchService.index_product(product)

            return product, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def update_product(product_id, data, requesting_user):
        if requesting_user.role != 'admin':
            return None, "Erişim engellendi"

        product = Product.query.filter_by(id=product_id, is_deleted=False).first()
        if not product:
            return None, "Ürün bulunamadı"

        if 'category_id' in data:
            category = Category.query.filter_by(id=data['category_id'], is_deleted=False).first()
            if not category:
                return None, "Geçersiz Kategori ID"

        try:
            for key, value in data.items():
                if hasattr(product, key):
                    setattr(product, key, value)

            db.session.commit()
            ElasticSearchService.index_product(product)

            return product, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def delete_product(product_id, requesting_user):
        if requesting_user.role != 'admin':
            return False, "Erişim engellendi"

        product = Product.query.filter_by(id=product_id, is_deleted=False).first()
        if not product:
            return False, "Ürün bulunamadı"

        try:
            product.is_deleted = True
            product.deleted_at = datetime.utcnow()
            db.session.commit()
            ElasticSearchService.delete_product(product_id)

            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)

    @staticmethod
    def add_product_images(product_id, requesting_user, files):
        if requesting_user.role != 'admin':
            return None, "Erişim engellendi"
        product = Product.query.filter_by(id=product_id, is_deleted=False).first()
        if not product:
            return None, "Ürün bulunamadı"
        saved_images = []
        try:
            for file in files:
                if file.filename == '': continue
                saved_path, error = ProductService.save_uploaded_file(file, product_id)
                if error: continue
                new_image = ProductImage(product_id=product_id, url=saved_path)
                db.session.add(new_image)
                saved_images.append(new_image)
            db.session.commit()
            return saved_images, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def delete_product_image(image_id, requesting_user):
        if requesting_user.role != 'admin':
            return False, "Erişim engellendi"
        image = ProductImage.query.get(image_id)
        if not image:
            return False, "Resim bulunamadı"
        if image.url:
            full_path = os.path.join(current_app.root_path, 'static', image.url)
            if os.path.exists(full_path):
                try:
                    os.remove(full_path)
                except:
                    pass
        try:
            db.session.delete(image)
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)

    @staticmethod
    def save_uploaded_file(file, product_id):
        allowed = ('.' in file.filename and file.filename.rsplit('.', 1)[1].lower() in Config.ALLOWED_EXTENSIONS)
        if not file or not allowed:
            return None, "Geçersiz dosya tipi"
        upload_folder = os.path.join(current_app.root_path, 'static', 'uploads', 'products')
        os.makedirs(upload_folder, exist_ok=True)
        filename = secure_filename(file.filename)
        unique_filename = f"{product_id}_{datetime.now().strftime('%Y%m%d_%H%M%S')}_{filename}"
        filepath = os.path.join(upload_folder, unique_filename)
        try:
            file.save(filepath)
            return f"uploads/products/{unique_filename}", None
        except Exception as e:
            return None, str(e)