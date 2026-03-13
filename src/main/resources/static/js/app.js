(() => {
    const KEY = "warehouse.jwt";
    const moneyFmt = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 2 });
    let renderSeq = 0;
    let flashTimer = 0;

    const mods = {
        users: {
            key: "users", label: "Users", singular: "User", list: "#/users/create", create: "#/users/create",
            desc: "Create application users and assign an allowed operational role.",
            endpoints: [["POST", "/auth/register", "Create a new user account with a selectable role"]],
            payloads: { create: { name: "Warehouse Staff", email: "staff@warehouse.com", password: "staff123", role: "STAFF" } },
            listFn: () => Promise.resolve([]),
            one: () => Promise.reject(new Error("User detail view is not available.")),
            ctx: () => Promise.resolve({}),
            fields: () => [
                { n: "name", l: "Name", t: "text", p: "Warehouse Staff", r: true },
                { n: "email", l: "Email", t: "email", p: "staff@warehouse.com", r: true },
                { n: "password", l: "Password", t: "password", p: "Enter password", r: true },
                {
                    n: "role", l: "Role", t: "select", r: true, h: "Allowed registration roles are MANAGER and STAFF.",
                    o: [{ v: "STAFF", t: "STAFF" }, { v: "MANAGER", t: "MANAGER" }]
                }
            ],
            init: (x) => ({ name: x ? x.name : "", email: x ? x.email : "", password: "", role: x && x.role ? x.role : "STAFF" }),
            body: (f) => ({ name: text(f.name.value), email: text(f.email.value), password: text(f.password.value), role: text(f.role.value) }),
            cols: [],
            search: () => "",
            cards: () => [],
            details: () => [],
            title: () => "User", sub: () => "Account detail",
            save: (b) => api("POST", "/auth/register", b),
            edit: () => Promise.reject(new Error("User edit is not available.")),
            successRedirect: "#/users/create"
        },
        products: {
            key: "products", label: "Products", singular: "Product", list: "#/products/list", create: "#/products/create",
            desc: "Manage the product catalog, pricing, and SKU-level details.",
            endpoints: [["GET", "/products?name=...&sku=...", "List products or filter by name and SKU"], ["GET", "/products/{id}", "Open a single product"], ["POST", "/products", "Create a product"], ["PUT", "/products/{id}", "Update a product"]],
            payloads: { create: { sku: "PRD-1001", name: "Forklift Battery", description: "High-cycle battery pack", price: 799.99, reorderLevel: 15 }, update: { sku: "PRD-1001", name: "Forklift Battery", description: "Updated specification", price: 849.99, reorderLevel: 20 } },
            listFn: () => api("GET", "/products"),
            one: (id) => api("GET", "/products/" + encodeURIComponent(id)),
            ctx: () => Promise.resolve({}),
            fields: () => [{ n: "sku", l: "SKU", t: "text", p: "PRD-1001", r: true }, { n: "name", l: "Name", t: "text", p: "Forklift Battery", r: true }, { n: "description", l: "Description", t: "textarea", p: "Optional product notes" }, { n: "price", l: "Price", t: "number", p: "799.99", r: true, min: "0.01", step: "0.01" }, { n: "reorderLevel", l: "Reorder Level", t: "number", p: "15", r: true, min: "1", step: "1", h: "Used by low-stock alerts and dashboard warnings." }],
            init: (x) => ({ sku: x ? x.sku : "", name: x ? x.name : "", description: x ? (x.description || "") : "", price: x ? x.price : "", reorderLevel: x && x.reorderLevel ? x.reorderLevel : 5 }),
            body: (f) => ({ sku: text(f.sku.value), name: text(f.name.value), description: text(f.description.value), price: num(f.price.value), reorderLevel: whole(f.reorderLevel.value) }),
            cols: [["Product", (x) => pairCell(x.name || "Unnamed product", x.sku || "No SKU")], ["Description", (x) => esc(x.description || "No description")], ["Price", (x) => badge(money(x.price))], ["Reorder", (x) => badge(intNum(x.reorderLevel) + " units")]],
            search: (x) => [x.id, x.sku, x.name, x.description, x.price, x.reorderLevel].join(" "),
            cards: (x) => [["Product ID", "#" + x.id, "Database identifier"], ["SKU", x.sku || "Not set", "Unique product code"], ["Price", money(x.price), "Current sale price"], ["Reorder Level", intNum(x.reorderLevel) + " units", "Low-stock threshold"]],
            details: (x) => [["Name", x.name || "Unnamed product"], ["Description", x.description || "No description provided"], ["SKU", x.sku || "No SKU"], ["Price", money(x.price)], ["Reorder Level", intNum(x.reorderLevel) + " units"]],
            title: (x) => x.name || "Unnamed product", sub: (x) => x.sku || "Catalog detail",
            save: (b) => api("POST", "/products", b), edit: (id, b) => api("PUT", "/products/" + encodeURIComponent(id), b)
        },
        warehouses: {
            key: "warehouses", label: "Warehouses", singular: "Warehouse", list: "#/warehouses/list", create: "#/warehouses/create",
            desc: "Maintain warehouse locations and inspect each storage site record.",
            endpoints: [["GET", "/warehouses", "List all warehouses"], ["GET", "/warehouses/{id}", "Open a single warehouse"], ["POST", "/warehouses", "Create a warehouse"], ["PUT", "/warehouses/{id}", "Update a warehouse"]],
            payloads: { create: { name: "South Hub", location: "Chennai" }, update: { name: "South Hub", location: "Bangalore" } },
            listFn: () => api("GET", "/warehouses"),
            one: (id) => api("GET", "/warehouses/" + encodeURIComponent(id)),
            ctx: () => Promise.resolve({}),
            fields: () => [{ n: "name", l: "Name", t: "text", p: "South Hub", r: true }, { n: "location", l: "Location", t: "text", p: "Chennai", r: true }],
            init: (x) => ({ name: x ? x.name : "", location: x ? x.location : "" }),
            body: (f) => ({ name: text(f.name.value), location: text(f.location.value) }),
            cols: [["Warehouse", (x) => pairCell(x.name || "Unnamed warehouse", "ID #" + x.id)], ["Location", (x) => esc(x.location || "No location")], ["Status", () => '<span class="badge badge--muted">Operational</span>']],
            search: (x) => [x.id, x.name, x.location].join(" "),
            cards: (x) => [["Warehouse ID", "#" + x.id, "Location identifier"], ["Name", x.name || "Unnamed warehouse", "Primary warehouse label"], ["Location", x.location || "Unknown", "Physical site"]],
            details: (x) => [["Warehouse Name", x.name || "Unnamed warehouse"], ["Location", x.location || "No location provided"], ["Record ID", "#" + x.id], ["Access", "JWT protected"]],
            title: (x) => x.name || "Warehouse", sub: (x) => x.location || "Warehouse detail",
            save: (b) => api("POST", "/warehouses", b), edit: (id, b) => api("PUT", "/warehouses/" + encodeURIComponent(id), b)
        },
        inventory: {
            key: "inventory", label: "Inventory", singular: "Inventory Item", list: "#/inventory/list", create: "#/inventory/create",
            desc: "Track stock by product and warehouse with edit-ready inventory records.",
            endpoints: [["GET", "/inventory/view", "List inventory with product and warehouse details"], ["GET", "/inventory/{id}", "Open a single stock record"], ["POST", "/inventory/add-stock", "Create or add stock"], ["PUT", "/inventory/{id}", "Update a stock record"]],
            payloads: { create: { productId: 1, warehouseId: 1, quantity: 120 }, update: { productId: 1, warehouseId: 2, quantity: 80 } },
            listFn: () => api("GET", "/inventory/view"),
            one: (id) => api("GET", "/inventory/" + encodeURIComponent(id)),
            ctx: () => Promise.all([api("GET", "/products"), api("GET", "/warehouses")]).then(([products, warehouses]) => ({ products, warehouses })),
            fields: (c) => [{ n: "productId", l: "Product", t: "select", r: true, h: "Loaded from GET /products.", o: (c.products || []).map((p) => ({ v: p.id, t: `${p.name} (${p.sku})` })) }, { n: "warehouseId", l: "Warehouse", t: "select", r: true, h: "Loaded from GET /warehouses.", o: (c.warehouses || []).map((w) => ({ v: w.id, t: `${w.name} - ${w.location}` })) }, { n: "quantity", l: "Quantity", t: "number", p: "120", r: true, min: "1", step: "1" }],
            init: (x) => ({ productId: x && x.product ? x.product.id : "", warehouseId: x && x.warehouse ? x.warehouse.id : "", quantity: x ? x.quantity : "" }),
            body: (f) => ({ productId: whole(f.productId.value), warehouseId: whole(f.warehouseId.value), quantity: whole(f.quantity.value) }),
            cols: [["Product", (x) => pairCell(x.productName || "Unknown product", x.productSku || "No SKU")], ["Warehouse", (x) => pairCell(x.warehouseName || "Unknown warehouse", x.warehouseLocation || "No location")], ["Quantity", (x) => badge(intNum(x.quantity) + " units")], ["Reorder", (x) => badge(intNum(x.reorderLevel) + " units")]],
            search: (x) => [x.id, x.productName, x.productSku, x.warehouseName, x.warehouseLocation, x.quantity, x.reorderLevel].join(" "),
            cards: (x) => [["Record ID", "#" + x.id, "Inventory identifier"], ["Product", pName(x.product), pSku(x.product)], ["Quantity", intNum(x.quantity) + " units", "Current on-hand stock"]],
            details: (x) => [["Product", pName(x.product), pSku(x.product)], ["Warehouse", wName(x.warehouse), wLoc(x.warehouse)], ["Quantity", intNum(x.quantity) + " units"], ["Record ID", "#" + x.id]],
            title: (x) => pName(x.product), sub: (x) => wName(x.warehouse),
            save: (b) => api("POST", "/inventory/add-stock", b), edit: (id, b) => api("PUT", "/inventory/" + encodeURIComponent(id), b)
        },
        orders: {
            key: "orders", label: "Orders", singular: "Order", list: "#/orders/list", create: "#/orders/create",
            desc: "Create outbound orders, review fulfillment totals, and inspect line-item details.",
            endpoints: [["GET", "/orders", "List all orders"], ["GET", "/orders/{id}", "Open a single order"], ["POST", "/orders", "Create a new order"]],
            payloads: { create: { warehouseId: 1, customerName: "Acme Corp", items: [{ productId: 1, quantity: 2 }] } },
            listFn: () => api("GET", "/orders"),
            one: (id) => api("GET", "/orders/" + encodeURIComponent(id)),
            ctx: () => Promise.all([api("GET", "/products"), api("GET", "/warehouses")]).then(([products, warehouses]) => ({ products, warehouses })),
            fields: () => [],
            formHtml: (ctx, values, fid, mode) => orderFormHtml(ctx, values, fid, mode),
            initForm: (fid, ctx) => bindOrderBuilder(fid, ctx),
            init: (x) => ({
                warehouseId: x ? x.warehouseId : "",
                customerName: x ? (x.customerName || "") : "",
                items: x && Array.isArray(x.items) && x.items.length
                    ? x.items.map((item) => ({ productId: item.productId, quantity: item.quantity }))
                    : [{ productId: "", quantity: 1 }]
            }),
            body: (f) => orderBody(f),
            cols: [["Order", (x) => pairCell(x.orderNumber || "Order", x.status || "Unknown status")], ["Warehouse", (x) => pairCell(x.warehouseName || "Unknown warehouse", x.customerName || "No customer")], ["Total", (x) => badge(money(x.totalAmount))], ["Created By", (x) => pairCell(x.createdByName || "Unknown user", x.createdByEmail || "No email")], ["Placed", (x) => esc(dt(x.createdAt))]],
            search: (x) => [x.id, x.orderNumber, x.warehouseName, x.customerName, x.status, x.totalAmount, x.createdByName, x.createdByEmail, x.createdAt, x.totalItems].join(" "),
            cards: (x) => [["Order Number", x.orderNumber || "Not assigned", "Generated at creation time"], ["Warehouse", x.warehouseName || "Unknown warehouse", "Fulfillment location"], ["Status", x.status || "Unknown", "Current order state"], ["Total Amount", money(x.totalAmount), "Combined line-item value"]],
            details: (x) => [["Customer", x.customerName || "No customer name"], ["Created By", x.createdByName || "Unknown user", x.createdByEmail || "No email"], ["Created At", dt(x.createdAt)], ["Order ID", "#" + x.id]],
            title: (x) => x.orderNumber || ("Order #" + x.id), sub: (x) => x.warehouseName || "Order detail",
            save: (b) => api("POST", "/orders", b),
            edit: () => Promise.reject(new Error("Order edit is not available.")),
            editable: false,
            detailExtra: (x) => orderItemsHtml(x.items)
        }
    };

    document.addEventListener("DOMContentLoaded", init);

    function init() {
        if ($("#login-form")) initLogin();
        if ($("#view-root")) initDashboard();
    }

    function initLogin() {
        if (token()) return goDash();
        const form = $("#login-form");
        const btn = $("#login-button");
        form.addEventListener("submit", (e) => {
            e.preventDefault();
            setStatus("", "");
            loading(btn, true, "Signing in...");
            api("POST", "/auth/login", { email: text(form.email.value), password: form.password.value })
                .then((r) => {
                    if (!r || !r.success || !r.data) throw new Error("Login failed");
                    setToken(r.data);
                    setStatus("Login successful. Redirecting...", "success");
                    goDash();
                })
                .catch((e2) => setStatus(msg(e2), "error"))
                .finally(() => loading(btn, false, "Login"));
        });
    }

    function initDashboard() {
        if (!token()) return goLogin();
        $("#logout-button").addEventListener("click", logout);
        setTokenStatus();
        addEventListener("hashchange", route);
        if (!location.hash) {
            location.hash = "#/overview";
            return;
        }
        route();
    }

    function route() {
        const r = parseRoute();
        const id = ++renderSeq;
        markNav(r);
        setHead(r);
        root().innerHTML = '<div class="loader"><p>Loading data from the secured API...</p></div>';
        if (r.kind === "overview") return overview(id);
        const m = mods[r.mod];
        if (!m) return missing("This module does not exist.", "#/overview");
        if (r.act === "list") return listView(m, id);
        if (r.act === "create") return formView(m, "create", id);
        if (r.act === "view" && r.id) return detailView(m, id, r.id);
        if (r.act === "edit" && r.id) return m.editable === false ? missing("Edit is not available for this module.", m.list) : formView(m, "edit", id, r.id);
        missing("This page could not be resolved.", m.list);
    }

    function overview(id) {
        Promise.all([api("GET", "/dashboard/summary"), api("GET", "/inventory/low-stock")]).then(([summaryRaw, lowStockRaw]) => {
            const summary = asItem(summaryRaw);
            const lowStock = asList(lowStockRaw);
            if (id !== renderSeq) return;
            root().innerHTML = `<section class="overview-panel section-stack"><div class="panel__header"><div><p class="panel__eyebrow">Overview</p><h3 class="panel__title">Live module snapshot</h3><p class="panel__copy">Summary metrics and stock warnings are now loaded from dedicated dashboard APIs.</p></div><div class="helper-chip-row"><span class="helper-chip">GET /dashboard/summary</span><span class="helper-chip">GET /inventory/low-stock</span></div></div><div class="metric-grid">${metric("Products", summary.totalProducts, "Catalog records")}${metric("Warehouses", summary.totalWarehouses, "Storage locations")}${metric("Inventory Units", summary.totalInventory, "Total on-hand quantity")}${metric("Low Stock", summary.lowStockCount, "Items below reorder level")}</div></section><div class="view-grid view-grid--split"><section class="panel"><div class="panel__header"><div><p class="panel__eyebrow">Attention needed</p><h3 class="panel__title">Low stock alerts</h3><p class="panel__copy">These product and warehouse combinations are below each product's configured reorder level.</p></div></div>${lowStockHtml(lowStock)}</section>${overviewApi()}</div>`;
        }).catch((e) => error(msg(e), "#/overview"));
    }

    function listView(m, id) {
        m.listFn().then((itemsRaw) => {
            const items = asList(itemsRaw);
            if (id !== renderSeq) return;
            root().innerHTML = `<div class="view-grid view-grid--split"><section class="table-panel"><div class="table-panel__header"><div><p class="panel__eyebrow">${esc(m.label)}</p><h3 class="panel__title">${esc(m.label + " list")}</h3><p class="panel__copy">${esc(m.desc)}</p></div><div class="table-actions"><a class="button button--secondary" href="${esc(m.create)}">Create ${esc(m.singular)}</a></div></div>${items.length ? tableHtml(m, items) : emptyState(m)}</section>${apiPanel(m, "create")}</div>`;
            if (items.length) bindSearch(m, items.length);
        }).catch((e) => error(msg(e), m.list));
    }

    function detailView(m, id, rowId) {
        m.one(rowId).then((raw) => {
            const x = asItem(raw);
            if (id !== renderSeq) return;
            root().innerHTML = `<div class="view-grid view-grid--split"><section class="detail-panel section-stack"><div class="detail-panel__header"><div><p class="panel__eyebrow">${esc(m.label)} detail</p><h3 class="panel__title">${esc(m.title(x))}</h3><p class="detail-subtitle">${esc(m.sub(x))}</p></div><div class="detail-actions"><a class="button button--secondary" href="${esc(m.list)}">Back to list</a>${m.editable === false ? "" : `<a class="button button--primary" href="#/${esc(m.key)}/edit/${esc(String(x.id))}">Edit</a>`}</div></div><div class="summary-grid">${summaryCards(m.cards(x))}</div><div class="detail-grid">${detailCards(m.details(x))}</div>${m.detailExtra ? m.detailExtra(x) : ""}</section>${apiPanel(m, "update")}</div>`;
        }).catch((e) => error(msg(e), m.list));
    }

    function formView(m, mode, id, rowId) {
        Promise.all([m.ctx(), mode === "edit" ? m.one(rowId) : Promise.resolve(null)]).then(([ctxRaw, itemRaw]) => {
            const ctx = normalizeCtx(m, ctxRaw);
            const item = itemRaw ? asItem(itemRaw) : null;
            if (id !== renderSeq) return;
            if (m.key === "inventory" && (!(ctx.products || []).length || !(ctx.warehouses || []).length)) return inventoryDeps();
            if (m.key === "orders" && (!(ctx.products || []).length || !(ctx.warehouses || []).length)) return orderDeps();
            const fid = `${m.key}-${mode}-form`;
            const bodyHtml = m.formHtml
                ? m.formHtml(ctx, m.init(item), fid, mode)
                : `${fieldHtml(m.fields(ctx), m.init(item))}<div class="panel__actions"><button id="${esc(fid + "-submit")}" type="submit" class="button button--primary">${esc(mode === "edit" ? "Save changes" : "Create " + m.singular)}</button><p id="${esc(fid + "-message")}" class="status-message" aria-live="polite"></p></div>`;
            root().innerHTML = `<div class="view-grid view-grid--split"><section class="panel"><div class="panel__header"><div><p class="panel__eyebrow">${esc(m.label)}</p><h3 class="panel__title">${esc(mode === "edit" ? "Edit " + m.singular : "Create " + m.singular)}</h3><p class="panel__copy">${esc(m.desc)}</p></div><div class="panel__actions"><a class="button button--secondary" href="${esc(m.list)}">Back to list</a>${mode === "edit" ? `<a class="button button--ghost" href="#/${esc(m.key)}/view/${esc(String(item.id))}">View item</a>` : ""}</div></div><form id="${esc(fid)}" class="panel__form">${bodyHtml}</form></section>${apiPanel(m, mode === "edit" ? "update" : "create")}</div>`;
            if (m.initForm) m.initForm(fid, ctx, item, mode);
            bindForm(m, mode, fid, rowId);
        }).catch((e) => error(msg(e), m.list));
    }

    function bindForm(m, mode, fid, rowId) {
        const form = $("#" + fid);
        const btn = $("#" + fid + "-submit");
        const statusId = fid + "-message";
        form.addEventListener("submit", (e) => {
            e.preventDefault();
            setInlineStatus(statusId, "", "");
            loading(btn, true, mode === "edit" ? "Saving..." : "Creating...");
            let body;
            try {
                body = m.body(form);
            } catch (e2) {
                loading(btn, false, mode === "edit" ? "Save changes" : "Create " + m.singular);
                return setInlineStatus(statusId, e2.message || "Invalid form input.", "error");
            }
            (mode === "edit" ? m.edit(rowId, body) : m.save(body))
                .then((x) => {
                    setInlineStatus(statusId, `${m.singular} ${mode === "edit" ? "updated" : "created"} successfully.`, "success");
                    flash("success", `${m.singular} ${mode === "edit" ? "updated" : "created"} successfully.`);
                    if (mode === "edit" && x && x.id !== undefined && x.id !== null) {
                        location.hash = `#/${m.key}/view/${x.id}`;
                        return;
                    }
                    if (mode !== "edit" && x && x.id !== undefined && x.id !== null) {
                        location.hash = `#/${m.key}/view/${x.id}`;
                        return;
                    }
                    location.hash = m.successRedirect || m.list || "#/overview";
                })
                .catch((e3) => {
                    const message = msg(e3);
                    setInlineStatus(statusId, message, "error");
                    flash("error", message);
                })
                .finally(() => loading(btn, false, mode === "edit" ? "Save changes" : "Create " + m.singular));
        });
    }

    function asList(value) {
        if (Array.isArray(value)) return value;
        if (value && Array.isArray(value.data)) return value.data;
        if (value && Array.isArray(value.content)) return value.content;
        return [];
    }

    function asItem(value) {
        if (value && typeof value === "object" && value.data && !Array.isArray(value.data)) return value.data;
        return value;
    }

    function normalizeCtx(m, ctx) {
        if (!ctx || typeof ctx !== "object") return ctx || {};
        if (m.key === "inventory" || m.key === "orders") {
            return {
                ...ctx,
                products: asList(ctx.products),
                warehouses: asList(ctx.warehouses)
            };
        }
        return ctx;
    }

    function tableHtml(m, items) {
        return `<div class="table-panel__toolbar"><label class="field search-box"><span>Search</span><input id="${esc(m.key + "-search")}" type="search" placeholder="Search ${esc(m.label.toLowerCase())}"></label><span id="${esc(m.key + "-count")}" class="helper-chip">Showing ${items.length} of ${items.length}</span></div><div class="table-wrap"><table><thead><tr>${m.cols.map(([h]) => `<th>${esc(h)}</th>`).join("")}<th>Actions</th></tr></thead><tbody>${items.map((x) => `<tr data-row-search="${esc(low(m.search(x)))}">${m.cols.map(([, fn]) => `<td>${fn(x)}</td>`).join("")}<td><div class="table-actions"><a class="button button--ghost" href="#/${esc(m.key)}/view/${esc(String(x.id))}">View</a>${m.editable === false ? "" : `<a class="button button--secondary" href="#/${esc(m.key)}/edit/${esc(String(x.id))}">Edit</a>`}</div></td></tr>`).join("")}</tbody></table></div>`;
    }

    function bindSearch(m, total) {
        const input = $("#" + m.key + "-search");
        const count = $("#" + m.key + "-count");
        const rows = [...document.querySelectorAll("tr[data-row-search]")];
        input.addEventListener("input", () => {
            const q = low(input.value);
            let shown = 0;
            rows.forEach((row) => {
                const ok = !q || row.getAttribute("data-row-search").includes(q);
                row.style.display = ok ? "" : "none";
                if (ok) shown += 1;
            });
            count.textContent = `Showing ${shown} of ${total}`;
        });
    }

    function fieldHtml(fields, values) {
        return fields.map((f) => {
            const v = values[f.n] ?? "";
            const help = f.h ? `<span class="field__help">${esc(f.h)}</span>` : "";
            if (f.t === "textarea") return `<label class="field"><span>${esc(f.l)}</span><textarea name="${esc(f.n)}" placeholder="${esc(f.p || "")}"${f.r ? " required" : ""}>${esc(String(v))}</textarea>${help}</label>`;
            if (f.t === "select") return `<label class="field"><span>${esc(f.l)}</span><select name="${esc(f.n)}"${f.r ? " required" : ""}><option value="">Select ${esc(f.l.toLowerCase())}</option>${(f.o || []).map((o) => `<option value="${esc(String(o.v))}"${String(o.v) === String(v) ? " selected" : ""}>${esc(o.t)}</option>`).join("")}</select>${help}</label>`;
            return `<label class="field"><span>${esc(f.l)}</span><input name="${esc(f.n)}" type="${esc(f.t || "text")}" value="${esc(String(v))}" placeholder="${esc(f.p || "")}"${f.r ? " required" : ""}${f.min ? ` min="${esc(f.min)}"` : ""}${f.step ? ` step="${esc(f.step)}"` : ""}>${help}</label>`;
        }).join("");
    }

    function apiPanel(m, mode) {
        const body = JSON.stringify(m.payloads[mode] || m.payloads.create || {}, null, 2);
        return `<aside class="panel"><div class="panel__header"><div><p class="panel__eyebrow">API Contract</p><h3 class="panel__title">${esc(m.label + " endpoints")}</h3><p class="panel__copy">Protected requests are sent through XHR with the stored JWT in the Authorization header.</p></div></div><div class="api-list">${m.endpoints.map(([method, path, note]) => `<div class="api-item"><span class="api-method">${esc(method)}</span><span class="api-path">${esc(path)}</span><p class="panel__copy">${esc(note)}</p></div>`).join("")}</div><div class="api-note"><p class="panel__copy">Payload for ${esc(mode === "update" ? "edit" : "create")}:</p><pre>${esc(body)}</pre></div></aside>`;
    }

    function overviewApi() {
        return `<aside class="panel"><div class="panel__header"><div><p class="panel__eyebrow">API Payloads</p><h3 class="panel__title">Dashboard and module calls</h3><p class="panel__copy">The overview now consumes purpose-built summary and alert endpoints alongside the existing CRUD APIs.</p></div></div><div class="api-list"><div class="api-item"><span class="api-method">GET</span><span class="api-path">/dashboard/summary</span><pre>${esc(JSON.stringify({ totalProducts: 10, totalWarehouses: 3, totalInventory: 500, lowStockCount: 2 }, null, 2))}</pre></div><div class="api-item"><span class="api-method">GET</span><span class="api-path">/inventory/low-stock</span><pre>${esc(JSON.stringify([{ productName: "Laptop", warehouseName: "South Hub", quantity: 3, reorderLevel: 5 }], null, 2))}</pre></div><div class="api-item"><span class="api-method">POST</span><span class="api-path">/auth/login</span><pre>${esc(JSON.stringify({ email: "superadmin@warehouse.com", password: "super123" }, null, 2))}</pre></div><div class="api-item"><span class="api-method">POST</span><span class="api-path">/products</span><pre>${esc(JSON.stringify(mods.products.payloads.create, null, 2))}</pre></div><div class="api-item"><span class="api-method">POST</span><span class="api-path">/inventory/add-stock</span><pre>${esc(JSON.stringify(mods.inventory.payloads.create, null, 2))}</pre></div></div></aside>`;
    }

    function lowStockHtml(items) {
        if (!items || !items.length) {
            return '<div class="empty-state"><div><h3>No low stock alerts</h3><p>Everything is currently at or above each product&apos;s reorder level.</p></div></div>';
        }
        return `<div class="table-wrap"><table><thead><tr><th>Product</th><th>Warehouse</th><th>Quantity</th><th>Reorder Level</th></tr></thead><tbody>${items.map((x) => `<tr><td>${pairCell(x.productName || "Unknown product", x.productSku || "No SKU")}</td><td>${pairCell(x.warehouseName || "Unknown warehouse", x.warehouseLocation || "No location")}</td><td>${badge(intNum(x.quantity) + " units")}</td><td>${badge(intNum(x.reorderLevel) + " units")}</td></tr>`).join("")}</tbody></table></div>`;
    }

    function orderFormHtml(ctx, values, fid, mode) {
        const rows = Array.isArray(values.items) && values.items.length ? values.items : [{ productId: "", quantity: 1 }];
        return `<label class="field"><span>Warehouse</span><select name="warehouseId" required><option value="">Select warehouse</option>${(ctx.warehouses || []).map((w) => `<option value="${esc(String(w.id))}"${String(w.id) === String(values.warehouseId || "") ? " selected" : ""}>${esc(`${w.name} - ${w.location}`)}</option>`).join("")}</select><span class="field__help">Loaded from GET /warehouses.</span></label><label class="field"><span>Customer Name</span><input name="customerName" type="text" value="${esc(String(values.customerName || ""))}" placeholder="Acme Corp"></label><div class="section-stack"><div class="panel__actions"><div><span>Order Items</span><p class="panel__copy">Choose one or more products with quantities.</p></div><button id="${esc(fid + "-add-item")}" type="button" class="button button--secondary">Add item</button></div><div id="${esc(fid + "-items")}" class="section-stack">${rows.map((item, index) => orderItemRowHtml(ctx.products || [], item, index)).join("")}</div></div><div class="panel__actions"><button id="${esc(fid + "-submit")}" type="submit" class="button button--primary">${esc(mode === "edit" ? "Save changes" : "Create " + "Order")}</button><p id="${esc(fid + "-message")}" class="status-message" aria-live="polite"></p></div>`;
    }

    function orderItemRowHtml(products, item, index) {
        const row = item || {};
        return `<div class="detail-card order-item-row" data-order-item-row><div class="panel__actions"><strong>Item ${index + 1}</strong><button type="button" class="button button--ghost" data-order-remove>Remove</button></div><label class="field"><span>Product</span><select data-order-product required><option value="">Select product</option>${products.map((p) => `<option value="${esc(String(p.id))}"${String(p.id) === String(row.productId || "") ? " selected" : ""}>${esc(`${p.name} (${p.sku})`)}</option>`).join("")}</select></label><label class="field"><span>Quantity</span><input data-order-qty type="number" min="1" step="1" value="${esc(String(row.quantity || 1))}" required></label></div>`;
    }

    function bindOrderBuilder(fid, ctx) {
        const addBtn = $("#" + fid + "-add-item");
        const items = $("#" + fid + "-items");
        if (!addBtn || !items) return;

        const renderIndexes = () => {
            [...items.querySelectorAll("[data-order-item-row]")].forEach((row, index) => {
                const title = row.querySelector("strong");
                if (title) title.textContent = `Item ${index + 1}`;
            });
        };

        const addRow = (item) => {
            items.insertAdjacentHTML("beforeend", orderItemRowHtml(ctx.products || [], item || {}, items.querySelectorAll("[data-order-item-row]").length));
            renderIndexes();
        };

        addBtn.addEventListener("click", () => addRow({ productId: "", quantity: 1 }));
        items.addEventListener("click", (e) => {
            const btn = e.target.closest("[data-order-remove]");
            if (!btn) return;
            const rows = items.querySelectorAll("[data-order-item-row]");
            if (rows.length === 1) return;
            const row = btn.closest("[data-order-item-row]");
            if (row) row.remove();
            renderIndexes();
        });
    }

    function orderBody(form) {
        const warehouseId = whole(form.warehouseId.value);
        const customerName = text(form.customerName.value);
        const rows = [...form.querySelectorAll("[data-order-item-row]")];
        if (!rows.length) throw new Error("Add at least one order item.");
        const items = rows.map((row) => ({
            productId: whole(row.querySelector("[data-order-product]").value),
            quantity: whole(row.querySelector("[data-order-qty]").value)
        }));
        if (items.some((item) => item.quantity < 1)) throw new Error("Quantity must be at least 1.");
        return { warehouseId, customerName: customerName || null, items };
    }

    function orderItemsHtml(items) {
        const rows = Array.isArray(items) ? items : [];
        if (!rows.length) return "";
        return `<section class="panel"><div class="panel__header"><div><p class="panel__eyebrow">Order items</p><h3 class="panel__title">Line items</h3><p class="panel__copy">Products and quantities included in this order.</p></div></div><div class="table-wrap"><table><thead><tr><th>Product</th><th>Quantity</th><th>Unit Price</th><th>Line Total</th></tr></thead><tbody>${rows.map((item) => `<tr><td>${pairCell(item.productName || "Unknown product", item.productSku || "No SKU")}</td><td>${badge(intNum(item.quantity) + " units")}</td><td>${badge(money(item.unitPrice))}</td><td>${badge(money(item.lineTotal))}</td></tr>`).join("")}</tbody></table></div></section>`;
    }

    function emptyState(m) { return `<div class="empty-state"><div><h3>No ${esc(m.label.toLowerCase())} yet</h3><p>Create the first ${esc(m.singular.toLowerCase())} to populate this module.</p><a class="button button--primary" href="${esc(m.create)}">Create ${esc(m.singular)}</a></div></div>`; }
    function inventoryDeps() { root().innerHTML = '<div class="empty-state"><div><h3>Inventory needs products and warehouses first</h3><p>Create at least one product and one warehouse before adding or editing inventory records.</p><div class="panel__actions"><a class="button button--secondary" href="#/products/create">Create product</a><a class="button button--primary" href="#/warehouses/create">Create warehouse</a></div></div></div>'; }
    function orderDeps() { root().innerHTML = '<div class="empty-state"><div><h3>Orders need products and warehouses first</h3><p>Create at least one product and one warehouse before placing an order.</p><div class="panel__actions"><a class="button button--secondary" href="#/products/create">Create product</a><a class="button button--primary" href="#/warehouses/create">Create warehouse</a></div></div></div>'; }
    function missing(message, routeTo) { root().innerHTML = `<div class="empty-state"><div><h3>Page unavailable</h3><p>${esc(message)}</p><a class="button button--primary" href="${esc(routeTo || "#/overview")}">Go back</a></div></div>`; }
    function error(message, routeTo) { missing(message || "Something went wrong while loading this view.", routeTo || "#/overview"); }

    function setHead(r) {
        if (r.kind === "overview") { $("#breadcrumb").textContent = "Overview"; $("#page-title").textContent = "Operations overview"; return; }
        const m = mods[r.mod];
        if (!m) { $("#breadcrumb").textContent = "Unknown"; $("#page-title").textContent = "Unavailable page"; return; }
        const label = r.act === "list" ? "List" : r.act === "create" ? "Create" : r.act === "edit" ? "Edit" : "Detail";
        $("#breadcrumb").textContent = `${m.label} / ${label}`;
        $("#page-title").textContent = m.label;
    }

    function markNav(r) {
        document.querySelectorAll("[data-nav-link]").forEach((x) => x.classList.remove("is-active"));
        if (r.kind === "overview") return document.querySelector('[href="#/overview"]').classList.add("is-active");
        const m = mods[r.mod];
        if (!m) return;
        document.querySelectorAll("[data-nav-link]").forEach((x) => {
            const href = x.getAttribute("href");
            if (href === m.list && ["list", "view", "edit"].includes(r.act)) x.classList.add("is-active");
            if (href === m.create && r.act === "create") x.classList.add("is-active");
        });
    }

    function parseRoute() {
        const raw = String(location.hash || "").replace(/^#\/?/, "").trim();
        if (!raw || raw === "overview") return { kind: "overview" };
        const [mod, act = "list", id = null] = raw.split("/").filter(Boolean);
        return { kind: "module", mod, act, id };
    }

    function api(method, url, body) {
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            xhr.open(method, url, true);
            xhr.setRequestHeader("Accept", "application/json");
            if (body !== undefined && body !== null) xhr.setRequestHeader("Content-Type", "application/json");
            if (token() && !url.startsWith("/auth/")) xhr.setRequestHeader("Authorization", "Bearer " + token());
            xhr.onload = () => {
                const out = parse(xhr.responseText);
                if (xhr.status >= 200 && xhr.status < 300) return resolve(out);
                reject({ status: xhr.status, response: out });
            };
            xhr.onerror = () => reject({ status: 0, response: { message: "Network error" } });
            xhr.send(body !== undefined && body !== null ? JSON.stringify(body) : null);
        });
    }

    function parse(textValue) { if (!textValue) return null; try { return JSON.parse(textValue); } catch { return textValue; } }
    function msg(err) {
        if (err && err.message) return err.message;
        if (err && err.status === 401) return err?.response?.message || "Unauthorized. Please sign in again.";
        if (err && err.status === 403) return err?.response?.message || "Forbidden. You do not have access to this action.";
        if (err && err.response && err.response.message) return err.response.message;
        if (typeof err?.response === "string") return err.response;
        return "Unexpected error";
    }
    function metric(label, value, copy) { return `<article class="metric-card"><p class="metric-label">${esc(label)}</p><strong>${esc(String(value))}</strong><p>${esc(copy)}</p></article>`; }
    function moduleCard(m, count, copy) { return `<article class="summary-card"><p class="metric-label">${esc(m.label)}</p><h3>${esc(String(count))} records</h3><p>${esc(copy)}</p><div class="panel__actions"><a class="button button--secondary" href="${esc(m.list)}">Open list</a><a class="button button--ghost" href="${esc(m.create)}">Create</a></div></article>`; }
    function summaryCards(items) { return items.map(([a, b, c]) => `<article class="summary-card"><p class="metric-label">${esc(a)}</p><h3>${esc(b)}</h3><p>${esc(c || "")}</p></article>`).join(""); }
    function detailCards(items) { return items.map(([a, b, c]) => `<article class="detail-card"><p class="detail-label">${esc(a)}</p><div class="detail-value">${esc(b)}</div>${c ? `<div class="detail-subvalue">${esc(c)}</div>` : ""}</article>`).join(""); }
    function pairCell(a, b) { return `<div class="table-title">${esc(a)}</div><div class="table-subtitle">${esc(b)}</div>`; }
    function badge(value) { return `<span class="badge">${esc(value)}</span>`; }

    function flash(type, textValue) {
        const box = $("#flash-message");
        if (!box) return;
        box.className = `flash-message is-visible ${type === "error" ? "flash-message--error" : "flash-message--success"}`;
        box.textContent = textValue;
        clearTimeout(flashTimer);
        flashTimer = setTimeout(() => {
            box.className = "flash-message";
            box.textContent = "";
        }, 4500);
    }

    function setStatus(textValue, type) {
        const box = $("#message");
        if (!box) return;
        box.textContent = textValue;
        box.className = "status-message" + (type ? " status-message--" + type : "");
    }

    function setInlineStatus(id, textValue, type) {
        const box = $("#" + id);
        if (!box) return;
        box.textContent = textValue;
        box.className = "status-message" + (type ? " status-message--" + type : "");
    }

    function loading(btn, busy, label) { btn.disabled = busy; btn.textContent = label; }
    function setTokenStatus() { const box = $("#token-status"); if (box) { box.textContent = token() ? "JWT session active" : "No JWT available"; if (token()) box.title = "Stored token preview: " + token().slice(0, 18) + "..."; } }
    function logout() { clearToken(); goLogin(); }
    function goDash() { location.href = "/dashboard.html#/overview"; }
    function goLogin() { location.href = "/"; }
    function token() { return localStorage.getItem(KEY); }
    function setToken(value) { localStorage.setItem(KEY, value); }
    function clearToken() { localStorage.removeItem(KEY); }
    function text(value) { return String(value || "").trim(); }
    function num(value) { const out = Number(value); if (!Number.isFinite(out)) throw new Error("Enter a valid number."); return out; }
    function whole(value) { const out = parseInt(value, 10); if (!Number.isInteger(out)) throw new Error("Enter a valid whole number."); return out; }
    function money(value) { return value === null || value === undefined || value === "" ? "Not set" : moneyFmt.format(Number(value)); }
    function intNum(value) { return value === null || value === undefined || value === "" ? "0" : String(value); }
    function dt(value) {
        if (!value) return "Not available";
        const out = new Date(value);
        return Number.isNaN(out.getTime()) ? String(value) : out.toLocaleString();
    }
    function pName(x) { return x && x.name ? x.name : "Unknown product"; }
    function pSku(x) { return x && x.sku ? x.sku : "No SKU"; }
    function wName(x) { return x && x.name ? x.name : "Unknown warehouse"; }
    function wLoc(x) { return x && x.location ? x.location : "No location"; }
    function low(value) { return String(value || "").toLowerCase(); }
    function root() { return $("#view-root"); }
    function $(selector) { return document.querySelector(selector); }
    function esc(value) {
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }
})();
