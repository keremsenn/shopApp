from elasticsearch import Elasticsearch

es = Elasticsearch(["http://localhost:9200"])

class ElasticSearchService:
    @staticmethod
    def index_product(product):
        try:
            image_url = product.images[0].url if product.images else None

            doc = {
                "id": product.id,
                "name": product.name,
                "description": product.description,
                "price": product.price,
                "category_id": product.category_id,
                "is_deleted": product.is_deleted,
                "image_url": image_url
            }
            es.index(index="products", id=product.id, document=doc)
        except Exception as e:
            print(f"ES Indeksleme Hatası: {e}")

    @staticmethod
    def search_products(query_text):
        body = {
            "query": {
                "multi_match": {
                    "query": query_text,
                    "fields": ["name^3", "description"],
                    "fuzziness": "AUTO"
                }
            }
        }
        try:
            res = es.search(index="products", body=body)
            return [hit["_source"] for hit in res["hits"]["hits"]]
        except Exception as e:
            return []

    @staticmethod
    def delete_product(product_id):
        try:
            es.delete(index="products", id=product_id)
        except Exception as e:
            print(f"ES Silme Hatası: {e}")