"""智能零售平台 - 商家端 API 客户端"""
import requests


class ApiError(Exception):
    pass


class AdminClient:
    def __init__(self, base_url: str, token: str = None):
        self.base_url = base_url.rstrip("/")
        self.session = requests.Session()
        if token:
            self.session.headers["token"] = token

    def _req(self, method, path, json=None, params=None):
        r = self.session.request(method, f"{self.base_url}{path}",
                                 json=json, params=params, timeout=30)
        try:
            body = r.json()
        except Exception:
            raise ApiError(f"服务异常 (HTTP {r.status_code})，请确认后端已启动")
        if body.get("code") == 1:
            return body.get("data")
        raise ApiError(body.get("msg") or "请求失败")

    # ---------- 账号 ----------
    def login(self, username, password):
        return self._req("POST", "/api/admin/auth/login",
                         {"username": username, "password": password})

    # ---------- 工作台 ----------
    def stats(self):
        return self._req("GET", "/api/admin/workspace/stats")

    # ---------- 商品 ----------
    def products_page(self, page=1, page_size=20, keyword=None, status=None):
        return self._req("GET", "/api/admin/products/page",
                         params={"page": page, "pageSize": page_size,
                                 "keyword": keyword, "status": status})

    def product_save(self, payload):
        if payload.get("id"):
            return self._req("PUT", "/api/admin/products", payload)
        return self._req("POST", "/api/admin/products", payload)

    def product_status(self, pid, status):
        return self._req("PUT", f"/api/admin/products/{pid}/status/{status}")

    def product_delete(self, pid):
        return self._req("DELETE", f"/api/admin/products/{pid}")

    # ---------- 分类 ----------
    def categories_list(self):
        return self._req("GET", "/api/admin/category/list") or []

    def categories_page(self, page=1, page_size=50, keyword=None):
        return self._req("GET", "/api/admin/category/page",
                         params={"page": page, "pageSize": page_size, "keyword": keyword})

    def category_save(self, payload):
        if payload.get("id"):
            return self._req("PUT", "/api/admin/category", payload)
        return self._req("POST", "/api/admin/category", payload)

    def category_status(self, cid, status):
        return self._req("PUT", f"/api/admin/category/{cid}/status/{status}")

    def category_delete(self, cid):
        return self._req("DELETE", f"/api/admin/category/{cid}")

    # ---------- 订单 ----------
    def orders_page(self, page=1, page_size=20, status=None, order_no=None):
        return self._req("GET", "/api/admin/orders/page",
                         params={"page": page, "pageSize": page_size,
                                 "status": status, "orderNo": order_no})

    def order_detail(self, oid):
        return self._req("GET", f"/api/admin/orders/{oid}")

    def order_status(self, oid, status):
        return self._req("PUT", f"/api/admin/orders/{oid}/status/{status}")

    # ---------- 优惠活动 ----------
    def promotions_page(self, page=1, page_size=20, keyword=None):
        return self._req("GET", "/api/admin/promotion/page",
                         params={"page": page, "pageSize": page_size, "keyword": keyword})

    def promotion_save(self, payload):
        if payload.get("id"):
            return self._req("PUT", "/api/admin/promotion", payload)
        return self._req("POST", "/api/admin/promotion", payload)

    def promotion_status(self, pid, status):
        return self._req("PUT", f"/api/admin/promotion/{pid}/status/{status}")

    def promotion_delete(self, pid):
        return self._req("DELETE", f"/api/admin/promotion/{pid}")
