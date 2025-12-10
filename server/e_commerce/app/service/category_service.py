from app import db
from app.models.category import Category


class CategoryService:
    @staticmethod
    def get_all_categories():
        return Category.query.all()

    @staticmethod
    def get_category_by_id(category_id):
        return Category.query.get(category_id)

    @staticmethod
    def get_root_categories():
        return Category.query.filter_by(parent_id=None).all()

    @staticmethod
    def get_child_categories(parent_id):
        return Category.query.filter_by(parent_id=parent_id).all()

    @staticmethod
    def create_category(name, parent_id=None):
        category = Category(name=name, parent_id=parent_id)

        try:
            db.session.add(category)
            db.session.commit()
            return category, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def update_category(category_id, **kwargs):
        category = Category.query.get(category_id)
        if not category:
            return None, "Category not found"

        allowed_fields = ['name', 'parent_id']
        for field, value in kwargs.items():
            if field in allowed_fields and value is not None:
                setattr(category, field, value)

        try:
            db.session.commit()
            return category, None
        except Exception as e:
            db.session.rollback()
            return None, str(e)

    @staticmethod
    def delete_category(category_id):
        category = Category.query.get(category_id)
        if not category:
            return False, "Category not found"

        if category.children and len(category.children) > 0:
            return False, "Category has subcategories. Delete or move them first."

        try:
            db.session.delete(category)
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, str(e)


