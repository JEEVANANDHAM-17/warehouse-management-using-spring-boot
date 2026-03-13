(() => {
    const KEY = "warehouse.jwt";
    const moneyFmt = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 2 });
    let renderSeq = 0;
    let flashTimer = 0;

    const mods = {
        products: {
            key: "products", label: "Products", singular: "Product", list: "#/products/list", create: "#/products/create",
            desc: "Manage the product catalog, pricing, and SKU-level details.",
            endpoints: [["GET", "/products", "List all products"], ["GET", "/products/{id}", "Open a single product"], ["POST", "/products", "Create a product"], ["PUT", "/products/{id}", "Update a product"]],
            payloads: { create: { sku: "PRD-1001", name: "Forklift Battery", description: "High-cycle battery pack", price: 799.99 }, update: { sku: "PRD-1001", name: "Forklift Battery", description: "Updated specification", price: 849.99 } },
            listFn: () => api("GET", "/products"),
            one: (id) => api("GET", "/products/" + encodeURIComponent(id)),
            ctx: () => Promise.resolve({}),
            fields: () => [{ n: "sku", l: "SKU", t: "text", p: "PRD-1001", r: true }, { n: "name", l: "Name", t: "text", p: "Forklift Battery", r: true }, { n: "description", l: "Description", t: "textarea", p: "Optional product notes" }, { n: "price", l: "Price", t: "number", p: "799.99", r: true, min: "0.01", step: "0.01" }],
            init: (x) => ({ sku: x ? x.sku : "", name: x ? x.name : "", description: x ? (x.description || "") : "", price: x ? x.price : "" }),
            body: (f) => ({ sku: text(f.sku.value), name: text(f.name.value), description: text(f.description.value), price: num(f.price.value) }),
            cols: [["Product", (x) => pairCell(x.name || "Unnamed product", x.sku || "No SKU")], ["Description", (x) => esc(x.description || "No description")], ["Price", (x) => badge(money(x.price))]],
            search: (x) => [x.id, x.sku, x.name, x.description, x.price].join(" "),
            cards: (x) => [["Product ID", "#" + x.id, "Database identifier"], ["SKU", x.sku || "Not set", "Unique product code"], ["Price", money(x.price), "Current sale price"]],
            details: (x) => [["Name", x.name || "Unnamed product"], ["Description", x.description || "No description provided"], ["SKU", x.sku || "No SKU"], ["Price", money(x.price)]],
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
            endpoints: [["GET", "/inventory", "List all inventory records"], ["GET", "/inventory/{id}", "Open a single stock record"], ["POST", "/inventory/add-stock", "Create or add stock"], ["PUT", "/inventory/{id}", "Update a stock record"]],
            payloads: { create: { productId: 1, warehouseId: 1, quantity: 120 }, update: { productId: 1, warehouseId: 2, quantity: 80 } },
            listFn: () => api("GET", "/inventory"),
            one: (id) => api("GET", "/inventory/" + encodeURIComponent(id)),
            ctx: () => Promise.all([api("GET", "/products"), api("GET", "/warehouses")]).then(([products, warehouses]) => ({ products, warehouses })),
            fields: (c) => [{ n: "productId", l: "Product", t: "select", r: true, h: "Loaded from GET /products.", o: (c.products || []).map((p) => ({ v: p.id, t: `${p.name} (${p.sku})` })) }, { n: "warehouseId", l: "Warehouse", t: "select", r: true, h: "Loaded from GET /warehouses.", o: (c.warehouses || []).map((w) => ({ v: w.id, t: `${w.name} - ${w.location}` })) }, { n: "quantity", l: "Quantity", t: "number", p: "120", r: true, min: "1", step: "1" }],
            init: (x) => ({ productId: x && x.product ? x.product.id : "", warehouseId: x && x.warehouse ? x.warehouse.id : "", quantity: x ? x.quantity : "" }),
            body: (f) => ({ productId: whole(f.productId.value), warehouseId: whole(f.warehouseId.value), quantity: whole(f.quantity.value) }),
            cols: [["Product", (x) => pairCell(pName(x.product), pSku(x.product))], ["Warehouse", (x) => pairCell(wName(x.warehouse), wLoc(x.warehouse))], ["Quantity", (x) => badge(intNum(x.quantity) + " units")]],
            search: (x) => [x.id, pName(x.product), pSku(x.product), wName(x.warehouse), wLoc(x.warehouse), x.quantity].join(" "),
            cards: (x) => [["Record ID", "#" + x.id, "Inventory identifier"], ["Product", pName(x.product), pSku(x.product)], ["Quantity", intNum(x.quantity) + " units", "Current on-hand stock"]],
            details: (x) => [["Product", pName(x.product), pSku(x.product)], ["Warehouse", wName(x.warehouse), wLoc(x.warehouse)], ["Quantity", intNum(x.quantity) + " units"], ["Record ID", "#" + x.id]],
            title: (x) => pName(x.product), sub: (x) => wName(x.warehouse),
            save: (b) => api("POST", "/inventory/add-stock", b), edit: (id, b) => api("PUT", "/inventory/" + encodeURIComponent(id), b)
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
        if (r.act === "edit" && r.id) return formView(m, "edit", id, r.id);
        missing("This page could not be resolved.", m.list);
    }

    function overview(id) {
        Promise.all([api("GET", "/dashboard/summary"), api("GET", "/inventory/low-stock")]).then(([summary, lowStock]) => {
            if (id !== renderSeq) return;
            root().innerHTML = `<section class="overview-panel section-stack"><div class="panel__header"><div><p class="panel__eyebrow">Overview</p><h3 class="panel__title">Live module snapshot</h3><p class="panel__copy">Summary metrics and stock warnings are now loaded from dedicated dashboard APIs.</p></div><div class="helper-chip-row"><span class="helper-chip">GET /dashboard/summary</span><span class="helper-chip">GET /inventory/low-stock</span></div></div><div class="metric-grid">${metric("Products", summary.totalProducts, "Catalog records")}${metric("Warehouses", summary.totalWarehouses, "Storage locations")}${metric("Inventory Units", summary.totalInventory, "Total on-hand quantity")}${metric("Low Stock", summary.lowStockCount, "Items below threshold")}</div></section><div class="view-grid view-grid--split"><section class="panel"><div class="panel__header"><div><p class="panel__eyebrow">Attention needed</p><h3 class="panel__title">Low stock alerts</h3><p class="panel__copy">These product and warehouse combinations are below the default threshold of 5 units.</p></div></div>${lowStockHtml(lowStock)}</section>${overviewApi()}</div>`;
        }).catch((e) => error(msg(e), "#/overview"));
    }

    function listView(m, id) {
        m.listFn().then((items) => {
            if (id !== renderSeq) return;
            root().innerHTML = `<div class="view-grid view-grid--split"><section class="table-panel"><div class="table-panel__header"><div><p class="panel__eyebrow">${esc(m.label)}</p><h3 class="panel__title">${esc(m.label + " list")}</h3><p class="panel__copy">${esc(m.desc)}</p></div><div class="table-actions"><a class="button button--secondary" href="${esc(m.create)}">Create ${esc(m.singular)}</a></div></div>${items.length ? tableHtml(m, items) : emptyState(m)}</section>${apiPanel(m, "create")}</div>`;
            if (items.length) bindSearch(m, items.length);
        }).catch((e) => error(msg(e), m.list));
    }

    function detailView(m, id, rowId) {
        m.one(rowId).then((x) => {
            if (id !== renderSeq) return;
            root().innerHTML = `<div class="view-grid view-grid--split"><section class="detail-panel section-stack"><div class="detail-panel__header"><div><p class="panel__eyebrow">${esc(m.label)} detail</p><h3 class="panel__title">${esc(m.title(x))}</h3><p class="detail-subtitle">${esc(m.sub(x))}</p></div><div class="detail-actions"><a class="button button--secondary" href="${esc(m.list)}">Back to list</a><a class="button button--primary" href="#/${esc(m.key)}/edit/${esc(String(x.id))}">Edit</a></div></div><div class="summary-grid">${summaryCards(m.cards(x))}</div><div class="detail-grid">${detailCards(m.details(x))}</div></section>${apiPanel(m, "update")}</div>`;
        }).catch((e) => error(msg(e), m.list));
    }

    function formView(m, mode, id, rowId) {
        Promise.all([m.ctx(), mode === "edit" ? m.one(rowId) : Promise.resolve(null)]).then(([ctx, item]) => {
            if (id !== renderSeq) return;
            if (m.key === "inventory" && (!(ctx.products || []).length || !(ctx.warehouses || []).length)) return inventoryDeps();
            const fid = `${m.key}-${mode}-form`;
            root().innerHTML = `<div class="view-grid view-grid--split"><section class="panel"><div class="panel__header"><div><p class="panel__eyebrow">${esc(m.label)}</p><h3 class="panel__title">${esc(mode === "edit" ? "Edit " + m.singular : "Create " + m.singular)}</h3><p class="panel__copy">${esc(m.desc)}</p></div><div class="panel__actions"><a class="button button--secondary" href="${esc(m.list)}">Back to list</a>${mode === "edit" ? `<a class="button button--ghost" href="#/${esc(m.key)}/view/${esc(String(item.id))}">View item</a>` : ""}</div></div><form id="${esc(fid)}" class="panel__form">${fieldHtml(m.fields(ctx), m.init(item))}<button id="${esc(fid + "-submit")}" type="submit" class="button button--primary">${esc(mode === "edit" ? "Save changes" : "Create " + m.singular)}</button></form></section>${apiPanel(m, mode === "edit" ? "update" : "create")}</div>`;
            bindForm(m, mode, fid, rowId);
        }).catch((e) => error(msg(e), m.list));
    }

    function bindForm(m, mode, fid, rowId) {
        const form = $("#" + fid);
        const btn = $("#" + fid + "-submit");
        form.addEventListener("submit", (e) => {
            e.preventDefault();
            loading(btn, true, mode === "edit" ? "Saving..." : "Creating...");
            let body;
            try {
                body = m.body(form);
            } catch (e2) {
                loading(btn, false, mode === "edit" ? "Save changes" : "Create " + m.singular);
                return flash("error", e2.message || "Invalid form input.");
            }
            (mode === "edit" ? m.edit(rowId, body) : m.save(body))
                .then((x) => {
                    flash("success", `${m.singular} ${mode === "edit" ? "updated" : "created"} successfully.`);
                    location.hash = `#/${m.key}/view/${x.id}`;
                })
                .catch((e3) => flash("error", msg(e3)))
                .finally(() => loading(btn, false, mode === "edit" ? "Save changes" : "Create " + m.singular));
        });
    }

    function tableHtml(m, items) {
        return `<div class="table-panel__toolbar"><label class="field search-box"><span>Search</span><input id="${esc(m.key + "-search")}" type="search" placeholder="Search ${esc(m.label.toLowerCase())}"></label><span id="${esc(m.key + "-count")}" class="helper-chip">Showing ${items.length} of ${items.length}</span></div><div class="table-wrap"><table><thead><tr>${m.cols.map(([h]) => `<th>${esc(h)}</th>`).join("")}<th>Actions</th></tr></thead><tbody>${items.map((x) => `<tr data-row-search="${esc(low(m.search(x)))}">${m.cols.map(([, fn]) => `<td>${fn(x)}</td>`).join("")}<td><div class="table-actions"><a class="button button--ghost" href="#/${esc(m.key)}/view/${esc(String(x.id))}">View</a><a class="button button--secondary" href="#/${esc(m.key)}/edit/${esc(String(x.id))}">Edit</a></div></td></tr>`).join("")}</tbody></table></div>`;
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
        return `<aside class="panel"><div class="panel__header"><div><p class="panel__eyebrow">API Payloads</p><h3 class="panel__title">Dashboard and module calls</h3><p class="panel__copy">The overview now consumes purpose-built summary and alert endpoints alongside the existing CRUD APIs.</p></div></div><div class="api-list"><div class="api-item"><span class="api-method">GET</span><span class="api-path">/dashboard/summary</span><pre>${esc(JSON.stringify({ totalProducts: 10, totalWarehouses: 3, totalInventory: 500, lowStockCount: 2 }, null, 2))}</pre></div><div class="api-item"><span class="api-method">GET</span><span class="api-path">/inventory/low-stock</span><pre>${esc(JSON.stringify([{ productName: "Laptop", warehouseName: "South Hub", quantity: 3, threshold: 5 }], null, 2))}</pre></div><div class="api-item"><span class="api-method">POST</span><span class="api-path">/auth/login</span><pre>${esc(JSON.stringify({ email: "superadmin@warehouse.com", password: "super123" }, null, 2))}</pre></div><div class="api-item"><span class="api-method">POST</span><span class="api-path">/products</span><pre>${esc(JSON.stringify(mods.products.payloads.create, null, 2))}</pre></div><div class="api-item"><span class="api-method">POST</span><span class="api-path">/inventory/add-stock</span><pre>${esc(JSON.stringify(mods.inventory.payloads.create, null, 2))}</pre></div></div></aside>`;
    }

    function lowStockHtml(items) {
        if (!items || !items.length) {
            return '<div class="empty-state"><div><h3>No low stock alerts</h3><p>Everything is currently at or above the default threshold.</p></div></div>';
        }
        return `<div class="table-wrap"><table><thead><tr><th>Product</th><th>Warehouse</th><th>Quantity</th><th>Threshold</th></tr></thead><tbody>${items.map((x) => `<tr><td>${pairCell(x.productName || "Unknown product", x.productSku || "No SKU")}</td><td>${pairCell(x.warehouseName || "Unknown warehouse", x.warehouseLocation || "No location")}</td><td>${badge(intNum(x.quantity) + " units")}</td><td>${badge(intNum(x.threshold) + " units")}</td></tr>`).join("")}</tbody></table></div>`;
    }

    function emptyState(m) { return `<div class="empty-state"><div><h3>No ${esc(m.label.toLowerCase())} yet</h3><p>Create the first ${esc(m.singular.toLowerCase())} to populate this module.</p><a class="button button--primary" href="${esc(m.create)}">Create ${esc(m.singular)}</a></div></div>`; }
    function inventoryDeps() { root().innerHTML = '<div class="empty-state"><div><h3>Inventory needs products and warehouses first</h3><p>Create at least one product and one warehouse before adding or editing inventory records.</p><div class="panel__actions"><a class="button button--secondary" href="#/products/create">Create product</a><a class="button button--primary" href="#/warehouses/create">Create warehouse</a></div></div></div>'; }
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
                if (xhr.status === 401 && !url.startsWith("/auth/")) {
                    clearToken();
                    return goLogin();
                }
                reject({ response: out });
            };
            xhr.onerror = () => reject({ response: { message: "Network error" } });
            xhr.send(body !== undefined && body !== null ? JSON.stringify(body) : null);
        });
    }

    function parse(textValue) { if (!textValue) return null; try { return JSON.parse(textValue); } catch { return textValue; } }
    function msg(err) { return err && err.message ? err.message : err && err.response && err.response.message ? err.response.message : typeof err?.response === "string" ? err.response : "Unexpected error"; }
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
