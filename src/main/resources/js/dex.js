/**
 * @typedef {Object} DexItems
 *
 * @property {Object.<number, ItemEnum>[]} discoveredItems - Item indices the user has discovered.
 * @property {boolean} lastItemIsDiscovered - Whether the last possible item has been discovered, i.e. if the list's bottom is discovered.
 */

/**
 * @typedef {Object} MyUnitEnum
 *
 * @property {number} id
 * @property {string} friendlyName
 * @property {string} description
 * @property {string} iconURL
 * @property {string} fullURL
 */

/**
 * @typedef {Object} DexUnits
 *
 * @property {Object.<number, MyUnitEnum>[]} discoveredUnits - Units the user has discovered, associated by unit index.
 * @property {boolean} lastUnitIsDiscovered - Whether the last possible unit has been discovered, i.e. if the list's bottom is discovered.
 */

const main = document.getElementById("main");

window.onload = async () => {
    route();
};

window.onpopstate = () => {
    route();
};

const route = async () => {
    const splitPath = window.location.pathname.match(/[^/?]*[^/?]/g);

    if (splitPath.length === 1) { // /dex
        setupRootPage();
    } else if (splitPath.length === 2 && splitPath[1] === "items") { // /dex/items
        setupItemsPage();
    } else if (splitPath.length === 2 && splitPath[1] === "units") { // /dex/units
        setupUnitsPage();
    } else if (splitPath.length === 3 && splitPath[1] === "units" && isFinite(splitPath[2])) { // /dex/units/{id}
        setupUnitPage(parseInt(splitPath[2]));
    } else {
        window.location.pathname = "/dex";
    }
};

const setupRootPage = () => {
    document.title = "Dex";

    main.innerHTML =
        `<div class="dex-categories">` +
            `<a href="/dex/items" class="dex-category">Items</a>` +
            `<a href="/dex/units" class="dex-category">Units</a>` +
        `</div>`;

    const links = document.querySelectorAll("#main a");

    for (const link of links) {
        link.addEventListener("click", e => clickRouteListener(e, link.href));
    }
};

const setupItemsPage = async () => {
    replaceInterfaceWithText("Loading…");

    const res = await getDexItems();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {DexItems}
         */
        const dex = res.data;

        document.title = "Items - Dex";

        main.insertAdjacentElement("beforeend", createBackToDex());

        const table = document.createElement("table");
        table.classList.add("dex");
        main.insertAdjacentElement("beforeend", table);

        const tbody = document.createElement("tbody");
        table.insertAdjacentElement("beforeend", tbody);

        let lastDiscoveredId = -1;

        for (const [id, item] of Object.entries(dex.discoveredItems)) {
            const idnum = parseInt(id);

            if (idnum !== lastDiscoveredId + 1) {
                tbody.insertAdjacentElement("beforeend", createUnknownDexItemRow());
            }

            tbody.insertAdjacentElement("beforeend", createDiscoveredDexItemRow(idnum, item));

            lastDiscoveredId = idnum;
        }

        if (!dex.lastItemIsDiscovered) {
            tbody.insertAdjacentElement("beforeend", createUnknownDexItemRow());
        }
    }
};

const setupUnitsPage = async () => {
    replaceInterfaceWithText("Loading…");

    const res = await getDexUnits();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {DexUnits}
         */
        const dex = res.data;

        document.title = "Units - Dex";

        main.insertAdjacentElement("beforeend", createBackToDex());
        main.insertAdjacentElement("beforeend", createUnitsList(dex));
    }
};

/**
 * @param {number} unitId
 */
const setupUnitPage = async (unitId) => {
    replaceInterfaceWithText("Loading…");

    const res = await getDexUnit(unitId);

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {MyUnitEnum}
         */
        const unit = res.data;

        document.title = `${unit.friendlyName} - Units - Dex`;

        main.insertAdjacentElement("beforeend", createBackToUnitDex());

        main.insertAdjacentElement("beforeend", createDetailedUnitDex(unit));
    }
};

/**
 * @param {number} id
 * @param {ItemEnum} item
 */
const createDiscoveredDexItemRow = (id, item) => {
    const { friendlyName, imgURL, description } = item;

    const tr = document.createElement("tr");

    tr.insertAdjacentHTML("beforeend",
        `<td>${id}</td>` +
        `<td>${friendlyName}</td>` +
        `<td><img src="${imgURL}" alt="Item image"></td>` +
        `<td>${description}</td>`
    );

    return tr;
};

const createUnknownDexItemRow = () => {
    const tr = document.createElement("tr");
    tr.classList.add("dex-unknown");
    tr.innerHTML = `<td colspan="4"><img src="/img/three-dots.svg" alt="Three vertical circles indicating missing row(s)"></td>`;

    return tr;
};

/**
 *
 * @param {DexUnits} dex
 */
const createUnitsList = (dex) => {
    const ol = document.createElement("ol");
    ol.classList.add("dex");

    let lastDiscoveredId = -1;

    for (const [id, unit] of Object.entries(dex.discoveredUnits)) {
        const idnum = parseInt(id);

        if (idnum !== lastDiscoveredId + 1) {
            ol.insertAdjacentElement("beforeend", createUnknownDexUnitRow());
        }

        ol.insertAdjacentElement("beforeend", createDiscoveredDexUnitRow(idnum, unit));

        lastDiscoveredId = idnum;
    }

    if (!dex.lastUnitIsDiscovered) {
        ol.insertAdjacentElement("beforeend", createUnknownDexUnitRow());
    }

    return ol;
};

/**
 * @param {number} id
 * @param {MyUnitEnum} unit
 */
const createDiscoveredDexUnitRow = (id, unit) => {
    const { friendlyName, iconURL } = unit;

    const li = document.createElement("li");

    li.insertAdjacentHTML("beforeend",
        `<a href="/dex/units/${id}">` +
            `<span>#${id}</span>` +
            `<img src="${iconURL}" alt="Unit sprite">` +
            `<span>${friendlyName}</span>` +
        `</a>`
    );

    const a = li.children[0];

    a.addEventListener("click", e => clickRouteListener(e, a.href));

    return li;
};

/**
 * @returns {HTMLLIElement}
 */
const createUnknownDexUnitRow = () => {
    const li = document.createElement("li");
    li.classList.add("dex-unknown");
    li.innerHTML = `<img src="/img/three-dots.svg" alt="Three vertical circles indicating missing row(s)">`;

    return li;
};

/**
 * @param {MyUnitEnum} unit
 */
const createDetailedUnitDex = (unit) => {
    const div = document.createElement("div");
    div.insertAdjacentHTML("beforeend",
        `<h1>${unit.friendlyName}</h1>` +
        `<i>${unit.description}</i>` +
        `<img src="${unit.iconURL}" alt="Unit's icon">` +
        `<img src="${unit.fullURL}" alt="Unit's full sprite">`
    );

    return div;
};

const createBackToDex = () => {
    const a = document.createElement("a");
    a.setAttribute("href", "/dex");
    a.classList.add("dex-return");
    a.innerText = "⬅️ Return to Dex";
    a.addEventListener("click", e => clickRouteListener(e, a.href));

    return a;
};

const createBackToUnitDex = () => {
    const a = document.createElement("a");
    a.setAttribute("href", "/dex/units");
    a.classList.add("dex-return");
    a.innerText = "⬅️ Return to Units";
    a.addEventListener("click", e => clickRouteListener(e, a.href));

    return a;
};

const clickRouteListener = (event, href) => {
    window.history.pushState({}, "", href);
    route();
    event.preventDefault();
};

/**
 * On success, returns {@see DexItems}.
 */
const getDexItems = async () => (await fetch("/api/dex/items", {
    method: "GET",
})).json();

/**
 * On success, returns {@see DexUnits}.
 */
const getDexUnits = async () => (await fetch("/api/dex/units", {
    method: "GET",
})).json();

/**
 * @param {number} unitId
 *
 * On success, returns {@see MyUnitEnum}.
 */
const getDexUnit = async (unitId) => (await fetch(`/api/dex/units/${unitId}`, {
    method: "GET",
})).json();
