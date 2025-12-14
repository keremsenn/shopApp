from app import db
from app.models.category import Category
from datetime import datetime


class CategoryService:
    @staticmethod
    def get_all_categories():
        return Category.query.filter_by(is_deleted=False).all()

    @staticmethod
    def get_category_by_id(category_id):
        return Category.query.filter_by(id=category_id, is_deleted=False).first()

    @staticmethod
    def get_root_categories():
        return Category.query.filter_by(parent_id=None, is_deleted=False).all()

    @staticmethod
    def get_child_categories(parent_id):
        return Category.query.filter_by(parent_id=parent_id, is_deleted=False).all()

    @staticmethod
    def create_category(data, requesting_user):
        if requesting_user.role != 'admin':
            return None, "Access denied. Only admins can create categories."

        parent_id = data.get('parent_id')
        if parent_id:
            parent = Category.query.filter_by(id=parent_id, is_deleted=False).first()
            if not parent:
                return None, "Parent category not found"

        category = Category(
            name=data['name'],
            parent_id=parent_id
        )

        try:
            db.session.add(category)
            db.session.commit()
            return category, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def update_category(category_id, data, requesting_user):
        if requesting_user.role != 'admin':
            return None, "Access denied"

        category = Category.query.filter_by(id=category_id, is_deleted=False).first()
        if not category:
            return None, "Category not found"

        if 'parent_id' in data:
            new_parent_id = data['parent_id']
            if new_parent_id == category.id:
                return None, "A category cannot be its own parent"

            if new_parent_id is not None:
                new_parent = Category.query.get(new_parent_id)
                if not new_parent or new_parent.is_deleted:
                    return None, "Parent category not found"

        for key, value in data.items():
            setattr(category, key, value)

        try:
            db.session.commit()
            return category, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def delete_category(category_id, requesting_user):
        if requesting_user.role != 'admin':
            return False, "Access denied"

        category = Category.query.filter_by(id=category_id, is_deleted=False).first()
        if not category:
            return False, "Category not found"

        active_children_count = Category.query.filter_by(parent_id=category_id, is_deleted=False).count()
        if active_children_count > 0:
            return False, "Category has active subcategories. Move or delete them first."

        try:
            category.is_deleted = True
            category.deleted_at = datetime.utcnow()
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)