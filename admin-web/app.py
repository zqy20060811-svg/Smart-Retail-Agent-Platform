"""
智能零售客服与订单协同平台 - 商家管理端 (Streamlit)

启动（可复用 user-web 的虚拟环境）：
    streamlit run app.py --server.port 8503
默认账号：admin / 123456
"""
import streamlit as st

from client import ApiError, AdminClient

st.set_page_config(page_title="智能零售 - 商家管理端", page_icon="🏪", layout="wide")

ORDER_STATUS = {1: "待付款", 2: "待接单", 3: "已接单", 4: "派送中", 5: "已完成", 6: "已取消"}
PROMO_TYPE = {1: "满减", 2: "折扣", 3: "赠品"}

# ---------------- 会话状态 ----------------
if "base_url" not in st.session_state:
    st.session_state.base_url = "http://localhost:8080"
if "token" not in st.session_state:
    st.session_state.token = None
if "admin" not in st.session_state:
    st.session_state.admin = None


@st.cache_resource(show_spinner=False)
def get_client(base_url: str, token: str) -> AdminClient:
    return AdminClient(base_url, token)


# ---------------- 弹窗表单 ----------------
@st.dialog("新增/编辑商品")
def product_dialog(client: AdminClient, categories: list, product: dict | None = None):
    p = product or {}
    with st.form("product_form"):
        name = st.text_input("商品名", value=p.get("name", ""))
        cat_options = {c["name"]: c["id"] for c in categories}
        cat_name = st.selectbox("分类", list(cat_options.keys()),
                                index=list(cat_options.values()).index(p["categoryId"])
                                if p.get("categoryId") in cat_options.values() else 0)
        price = st.number_input("价格 (元)", min_value=0.0, step=0.5, value=float(p.get("price", 0.0)),
                                format="%.2f")
        stock = st.number_input("库存", min_value=0, step=1, value=int(p.get("stock", 999)))
        description = st.text_area("描述", value=p.get("description", ""))
        image = st.text_input("图片 URL（可选）", value=p.get("image", ""))
        on_sale = st.checkbox("起售中", value=(p.get("status", 1) == 1))
        if st.form_submit_button("保存"):
            if not name:
                st.warning("请填写商品名")
            else:
                try:
                    client.product_save({
                        "id": p.get("id"),
                        "categoryId": cat_options[cat_name],
                        "name": name,
                        "price": price,
                        "stock": stock,
                        "description": description,
                        "image": image or None,
                        "status": 1 if on_sale else 0,
                    })
                    st.toast("保存成功")
                    st.rerun()
                except ApiError as e:
                    st.error(str(e))


@st.dialog("新增/编辑分类")
def category_dialog(client: AdminClient, cat: dict | None = None):
    c = cat or {}
    with st.form("category_form"):
        name = st.text_input("分类名", value=c.get("name", ""))
        sort = st.number_input("排序（越小越靠前）", min_value=0, step=1,
                               value=int(c.get("sort", 0)))
        enabled = st.checkbox("启用", value=(c.get("status", 1) == 1))
        if st.form_submit_button("保存"):
            if not name:
                st.warning("请填写分类名")
            else:
                try:
                    client.category_save({
                        "id": c.get("id"),
                        "name": name,
                        "sort": sort,
                        "status": 1 if enabled else 0,
                    })
                    st.toast("保存成功")
                    st.rerun()
                except ApiError as e:
                    st.error(str(e))


@st.dialog("新增/编辑优惠活动")
def promotion_dialog(client: AdminClient, promo: dict | None = None):
    import datetime
    p = promo or {}
    with st.form("promotion_form"):
        title = st.text_input("活动标题", value=p.get("title", ""))
        ptype = st.selectbox("类型", list(PROMO_TYPE.values()),
                             index=(list(PROMO_TYPE.keys()).index(p["type"])
                                    if p.get("type") in PROMO_TYPE else 0))
        content = st.text_input("规则描述（如：满20减3、第二杯半价）", value=p.get("content", ""))
        default_start = datetime.date.today()
        default_end = default_start + datetime.timedelta(days=30)
        start = st.date_input("开始日期", value=default_start)
        end = st.date_input("结束日期", value=default_end)
        if st.form_submit_button("保存"):
            if not (title and content):
                st.warning("请填写标题和规则")
            else:
                type_code = [k for k, v in PROMO_TYPE.items() if v == ptype][0]
                try:
                    client.promotion_save({
                        "id": p.get("id"),
                        "title": title,
                        "type": type_code,
                        "content": content,
                        "startTime": f"{start}T00:00:00",
                        "endTime": f"{end}T23:59:59",
                        "status": 1,
                    })
                    st.toast("保存成功")
                    st.rerun()
                except ApiError as e:
                    st.error(str(e))


# ---------------- 侧边栏：登录 ----------------
with st.sidebar:
    st.title("🏪 智能零售 · 商家端")
    st.session_state.base_url = st.text_input("后端地址", value=st.session_state.base_url)

    if st.session_state.admin:
        st.success(f"已登录：{st.session_state.admin.get('name') or st.session_state.admin.get('username')}")
        if st.button("退出登录"):
            st.session_state.token = None
            st.session_state.admin = None
            st.rerun()
    else:
        with st.form("admin_login"):
            st.subheader("商家登录")
            username = st.text_input("用户名", value="admin")
            password = st.text_input("密码", type="password", value="123456")
            if st.form_submit_button("登录"):
                try:
                    data = AdminClient(st.session_state.base_url).login(username, password)
                    st.session_state.token = data["token"]
                    st.session_state.admin = data
                    st.rerun()
                except ApiError as e:
                    st.error(str(e))
        st.caption("默认账号：admin / 123456")

if not st.session_state.admin:
    st.info("请在左侧登录商家管理端（admin / 123456）")
    st.stop()

client = get_client(st.session_state.base_url, st.session_state.token)

tab_dash, tab_products, tab_orders, tab_categories, tab_promos = st.tabs(
    ["📊 工作台", "📦 商品管理", "📋 订单管理", "🗂️ 分类管理", "🎁 优惠活动"])

# ================= 工作台 =================
with tab_dash:
    try:
        s = client.stats()
        c1, c2, c3, c4 = st.columns(4)
        c1.metric("注册用户", s["userCount"])
        c2.metric("商品总数", s["productCount"], delta=f"{s['onSaleProductCount']} 个起售中")
        c3.metric("累计订单", s["orderCount"])
        c4.metric("今日订单", s["todayOrderCount"])
        c5, c6, c7 = st.columns(3)
        c5.metric("今日营业额", f"¥{s['todaySales']}")
        c6.metric("分类数", s["categoryCount"])
        c7.metric("⏰ 待接单", s["pendingOrderCount"],
                  delta="需尽快处理" if s["pendingOrderCount"] > 0 else "无积压",
                  delta_color="inverse")

        st.divider()
        st.subheader("待处理订单")
        pending = client.orders_page(page=1, page_size=10, status=2) or {}
        recs = pending.get("records", [])
        if not recs:
            st.caption("没有待接单订单")
        else:
            for o in recs:
                st.write(f"- 订单 **{o['orderNo']}**　¥{o['amount']}　{o.get('phone') or ''}　"
                         f"{o.get('remark') or ''}")
    except ApiError as e:
        st.error(str(e))

# ================= 商品管理 =================
with tab_products:
    try:
        categories = client.categories_list()
    except ApiError as e:
        st.error(str(e))
        st.stop()
    cat_name_of = {c["id"]: c["name"] for c in categories}

    col1, col2 = st.columns([4, 1])
    keyword = col1.text_input("搜索商品名", placeholder="例如：柠檬", label_visibility="collapsed")
    if col2.button("➕ 新增商品"):
        product_dialog(client, categories)

    try:
        data = client.products_page(page=1, page_size=50, keyword=keyword or None) or {}
        products = data.get("records", [])
    except ApiError as e:
        st.error(str(e))
        products = []

    if not products:
        st.caption("没有商品")
    for p in products:
        with st.container(border=True):
            c1, c2, c3, c4, c5 = st.columns([3, 1.5, 1, 1, 2.5])
            c1.markdown(f"**{p['name']}**　:small-blue[{cat_name_of.get(p['categoryId'], '未分类')}]")
            c1.caption(p.get("description") or "")
            c2.write(f"¥{p['price']}")
            c3.write(f"库存 {p.get('stock', 0)}")
            c4.write(f"销量 {p.get('sales', 0)}")
            with c5:
                b1, b2, b3 = st.columns(3)
                if b1.button("停售" if p["status"] == 1 else "起售", key=f"ps_{p['id']}"):
                    client.product_status(p["id"], 0 if p["status"] == 1 else 1)
                    st.toast("状态已更新")
                    st.rerun()
                if b2.button("编辑", key=f"pe_{p['id']}"):
                    product_dialog(client, categories, p)
                if b3.button("删除", key=f"pd_{p['id']}"):
                    client.product_delete(p["id"])
                    st.toast("已删除")
                    st.rerun()

# ================= 订单管理 =================
with tab_orders:
    status_filter = st.selectbox("订单状态", ["全部", "待接单", "已接单", "派送中", "已完成", "已取消"])
    status_code = next((k for k, v in ORDER_STATUS.items() if v == status_filter), None)
    try:
        data = client.orders_page(page=1, page_size=30, status=status_code) or {}
        orders = data.get("records", [])
    except ApiError as e:
        st.error(str(e))
        orders = []

    if not orders:
        st.caption("没有订单")
    for o in orders:
        with st.expander(f"{o['orderNo']}　|　¥{o['amount']}　|　{ORDER_STATUS.get(o['status'])}"):
            try:
                detail = client.order_detail(o["id"]) or {}
                for it in detail.get("items", []):
                    st.write(f"- {it['productName']} x{it['number']}　¥{it['amount']}")
                if o.get("phone") or o.get("address"):
                    st.caption(f"联系：{o.get('phone') or '-'}　地址：{o.get('address') or '-'}")
            except ApiError:
                pass
            next_status = {2: (3, "✅ 接单"), 3: (4, "🛵 开始派送"), 4: (5, "📦 完成订单")}
            if o["status"] in next_status:
                code, label = next_status[o["status"]]
                if st.button(label, key=f"os_{o['id']}"):
                    try:
                        client.order_status(o["id"], code)
                        st.toast(f"订单已更新为「{ORDER_STATUS[code]}」，已实时推送用户")
                        st.rerun()
                    except ApiError as e:
                        st.error(str(e))

# ================= 分类管理 =================
with tab_categories:
    if st.button("➕ 新增分类"):
        category_dialog(client)
    try:
        data = client.categories_page() or {}
        cats = data.get("records", [])
    except ApiError as e:
        st.error(str(e))
        cats = []
    for c in cats:
        with st.container(border=True):
            c1, c2, c3, c4 = st.columns([3, 1, 1, 2])
            c1.markdown(f"**{c['name']}**")
            c2.write(f"排序 {c.get('sort', 0)}")
            c3.write("启用" if c["status"] == 1 else "禁用")
            with c4:
                b1, b2, b3 = st.columns(3)
                if b1.button("禁用" if c["status"] == 1 else "启用", key=f"cs_{c['id']}"):
                    client.category_status(c["id"], 0 if c["status"] == 1 else 1)
                    st.rerun()
                if b2.button("编辑", key=f"ce_{c['id']}"):
                    category_dialog(client, c)
                if b3.button("删除", key=f"cd_{c['id']}"):
                    client.category_delete(c["id"])
                    st.toast("已删除")
                    st.rerun()

# ================= 优惠活动 =================
with tab_promos:
    st.caption("优惠活动会被 AI 客服的「优惠查询」工具检索，可直接影响智能客服回复内容")
    if st.button("➕ 新增活动"):
        promotion_dialog(client)
    try:
        data = client.promotions_page() or {}
        promos = data.get("records", [])
    except ApiError as e:
        st.error(str(e))
        promos = []
    for p in promos:
        with st.container(border=True):
            c1, c2, c3, c4 = st.columns([3, 1, 3, 2])
            c1.markdown(f"**{p['title']}**")
            c2.write(PROMO_TYPE.get(p.get("type"), "-"))
            c3.write(p.get("content", ""))
            c4.write("进行中" if p["status"] == 1 else "已停用")
            b1, b2, b3 = st.columns(3)
            if b1.button("停用" if p["status"] == 1 else "启用", key=f"ms_{p['id']}"):
                client.promotion_status(p["id"], 0 if p["status"] == 1 else 1)
                st.rerun()
            if b2.button("编辑", key=f"me_{p['id']}"):
                promotion_dialog(client, p)
            if b3.button("删除", key=f"md_{p['id']}"):
                client.promotion_delete(p["id"])
                st.toast("已删除")
                st.rerun()
