"""智能零售平台 - 后端 API 客户端"""
import requests


class ApiError(Exception):
    pass


class RetailClient:
    def __init__(self, base_url: str, token: str = None):
        self.base_url = base_url.rstrip("/")
        self.session = requests.Session()
        if token:
            self.session.headers["token"] = token

    def _post(self, path, json=None):
        r = self.session.post(f"{self.base_url}{path}", json=json, timeout=30)
        return self._handle(r)

    def _get(self, path, params=None):
        r = self.session.get(f"{self.base_url}{path}", params=params, timeout=30)
        return self._handle(r)

    def _put(self, path, json=None):
        r = self.session.put(f"{self.base_url}{path}", json=json, timeout=30)
        return self._handle(r)

    @staticmethod
    def _handle(r):
        try:
            body = r.json()
        except Exception:
            raise ApiError(f"服务异常 (HTTP {r.status_code})，请确认后端已启动")
        if body.get("code") == 1:
            return body.get("data")
        raise ApiError(body.get("msg") or "请求失败")

    # ---------- 账号 ----------
    def login(self, username, password):
        return self._post("/api/user/auth/login",
                          {"username": username, "password": password})

    def register(self, username, password, nickname=None):
        return self._post("/api/user/auth/register",
                          {"username": username, "password": password,
                           "nickname": nickname or username})

    # ---------- 商品 ----------
    def categories(self):
        return self._get("/api/user/categories") or []

    def products(self, page=1, page_size=20, category_id=None, keyword=None):
        return self._get("/api/user/products",
                         {"page": page, "pageSize": page_size,
                          "categoryId": category_id, "keyword": keyword})

    # ---------- 订单 ----------
    def create_order(self, items, phone=None, address=None, remark=None):
        return self._post("/api/user/orders",
                          {"items": items, "phone": phone,
                           "address": address, "remark": remark})

    def my_orders(self, page=1, page_size=10, status=None):
        return self._get("/api/user/orders",
                         {"page": page, "pageSize": page_size, "status": status})

    def order_detail(self, order_id):
        return self._get(f"/api/user/orders/{order_id}")

    def cancel_order(self, order_id):
        return self._put(f"/api/user/orders/{order_id}/cancel")

    # ---------- AI 客服 ----------
    def ai_chat(self, content, session_id=None):
        payload = {"content": content}
        if session_id:
            payload["sessionId"] = session_id
        return self._post("/api/user/ai/chat", payload)
