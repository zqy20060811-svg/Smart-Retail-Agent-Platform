"""
智能零售客服与订单协同平台 - 用户 web 端 (Streamlit)

启动：
    pip install -r requirements.txt
    streamlit run app.py
"""
import streamlit as st

from client import ApiError, RetailClient

st.set_page_config(page_title="智能零售平台", page_icon="🧋", layout="wide")

# ---------------- 会话状态 ----------------
if "base_url" not in st.session_state:
    st.session_state.base_url = "http://localhost:8080"
if "token" not in st.session_state:
    st.session_state.token = None
if "user" not in st.session_state:
    st.session_state.user = None
if "cart" not in st.session_state:
    st.session_state.cart = {}  # product_id -> {name, price, number}
if "ai_session_id" not in st.session_state:
    st.session_state.ai_session_id = None
if "ai_messages" not in st.session_state:
    st.session_state.ai_messages = []


def get_client() -> RetailClient:
    return RetailClient(st.session_state.base_url, st.session_state.token)


# ---------------- 侧边栏：服务地址 + 登录 ----------------
with st.sidebar:
    st.title("🧋 智能零售平台")
    st.session_state.base_url = st.text_input("后端地址", value=st.session_state.base_url)

    if st.session_state.user:
        st.success(f"已登录：{st.session_state.user.get('name') or st.session_state.user.get('username')}")
        if st.button("退出登录"):
            st.session_state.token = None
            st.session_state.user = None
            st.session_state.cart = {}
            st.session_state.ai_session_id = None
            st.session_state.ai_messages = []
            st.rerun()
    else:
        tab_login, tab_register = st.tabs(["登录", "注册"])
        with tab_login:
            with st.form("login_form"):
                username = st.text_input("用户名", value="demo")
                password = st.text_input("密码", type="password", value="123456")
                if st.form_submit_button("登录", use_container_width=True):
                    try:
                        data = RetailClient(st.session_state.base_url).login(username, password)
                        st.session_state.token = data["token"]
                        st.session_state.user = data
                        st.rerun()
                    except ApiError as e:
                        st.error(str(e))
        with tab_register:
            with st.form("register_form"):
                new_user = st.text_input("用户名")
                new_pwd = st.text_input("密码", type="password")
                if st.form_submit_button("注册", use_container_width=True):
                    try:
                        RetailClient(st.session_state.base_url).register(new_user, new_pwd)
                        st.success("注册成功，请去登录页登录")
                    except ApiError as e:
                        st.error(str(e))

    st.caption("演示账号：demo / 123456\n管理员：admin / 123456")

# 未登录只展示提示
if not st.session_state.user:
    st.info("请在左侧登录后使用（演示账号 demo / 123456）")
    st.stop()

client = get_client()
STATUS = {1: "待付款", 2: "待接单", 3: "制作中", 5: "已完成", 6: "已取消"}

# ---------------- 主界面 ----------------
tab_shop, tab_orders, tab_ai = st.tabs(["🛍️ 商品下单", "📋 我的订单", "🤖 AI 客服"])

# ================= 商品下单 =================
with tab_shop:
    try:
        categories = client.categories()
    except ApiError as e:
        st.error(str(e))
        st.stop()

    cat_names = ["全部分类"] + [c["name"] for c in categories]
    col1, col2 = st.columns([2, 3])
    with col1:
        chosen = st.selectbox("分类", cat_names)
    with col2:
        keyword = st.text_input("搜索商品名", placeholder="例如：柠檬")

    cat_id = None
    if chosen != "全部分类":
        cat_id = next(c["id"] for c in categories if c["name"] == chosen)

    data = client.products(category_id=cat_id, keyword=keyword or None)
    products = (data or {}).get("records", [])
    if not products:
        st.warning("没有符合条件的商品")

    for p in products:
        c1, c2, c3 = st.columns([4, 1, 2])
        with c1:
            st.markdown(f"**{p['name']}**　￥{p['price']}")
            st.caption(p.get("description") or "")
        with c2:
            qty = st.number_input("数量", 1, 20, 1, key=f"qty_{p['id']}")
        with c3:
            st.write("")
            if st.button("加入购物车", key=f"add_{p['id']}", use_container_width=True):
                item = st.session_state.cart.get(p["id"])
                if item:
                    item["number"] += qty
                else:
                    st.session_state.cart[p["id"]] = {"name": p["name"], "price": float(p["price"]), "number": qty}
                st.toast(f"已加入 {p['name']} x{qty}")

    st.divider()
    st.subheader("🛒 购物车")
    cart = st.session_state.cart
    if not cart:
        st.caption("购物车是空的")
    else:
        total = 0.0
        for pid, item in list(cart.items()):
            total += item["price"] * item["number"]
            c1, c2, c3 = st.columns([4, 2, 1])
            c1.write(f"{item['name']}　￥{item['price']}")
            c2.write(f"x {item['number']}")
            if c3.button("移除", key=f"del_{pid}"):
                del cart[pid]
                st.rerun()
        st.markdown(f"**合计：￥{total:.2f}**")
        if st.button("提交订单", type="primary"):
            items = [{"productId": int(pid), "number": it["number"]} for pid, it in cart.items()]
            try:
                order_no = client.create_order(items, remark="Streamlit 用户下单")
                st.session_state.cart = {}
                st.success(f"下单成功！订单号：{order_no}")
                st.balloons()
            except ApiError as e:
                st.error(str(e))

# ================= 我的订单 =================
with tab_orders:
    try:
        orders = (client.my_orders() or {}).get("records", [])
    except ApiError as e:
        st.error(str(e))
        orders = []

    if not orders:
        st.caption("暂无订单，去商品页下一单吧~")
    for o in orders:
        status = STATUS.get(o["status"], "未知")
        with st.expander(f"订单 {o['orderNo']}　|　￥{o['amount']}　|　{status}"):
            try:
                detail = client.order_detail(o["id"])
                for it in (detail or {}).get("items", []):
                    st.write(f"- {it['productName']} x{it['number']}　￥{it['amount']}")
            except ApiError:
                pass
            if o["status"] in (1, 2):
                if st.button("取消订单", key=f"cancel_{o['id']}"):
                    try:
                        client.cancel_order(o["id"])
                        st.success("已取消")
                        st.rerun()
                    except ApiError as e:
                        st.error(str(e))

# ================= AI 客服 =================
with tab_ai:
    st.caption("可以问：「我的奶茶做好了吗」「有什么奶茶」「最近有什么优惠」"
               "（配置 retail.llm.api-key 后为大模型 Agent 真实回复，未配置时为本地知识库模拟回复，订单/商品/优惠数据真实可查）")

    for msg in st.session_state.ai_messages:
        with st.chat_message(msg["role"]):
            st.markdown(msg["content"])

    if prompt := st.chat_input("输入你的问题…"):
        st.session_state.ai_messages.append({"role": "user", "content": prompt})
        with st.chat_message("user"):
            st.markdown(prompt)
        with st.chat_message("assistant"):
            with st.spinner("客服思考中…"):
                try:
                    reply = client.ai_chat(prompt, st.session_state.ai_session_id)
                    answer = reply["reply"]
                    st.session_state.ai_session_id = reply.get("sessionId")
                    source = reply.get("source")
                    st.markdown(answer)
                    if source == "mock":
                        st.caption("ℹ️ 当前为本地模拟回复，配置 Dify 后接入大模型 Agent")
                    st.session_state.ai_messages.append({"role": "assistant", "content": answer})
                except ApiError as e:
                    st.error(str(e))
