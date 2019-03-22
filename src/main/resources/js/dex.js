/**
 * @typedef {Object} DexItems
 *
 * @property {Object.<number, ItemEnum>[]} discoveredItems - Item indices the user has discovered.
 * @property {boolean} lastItemIsDiscovered - Whether the last possible item has been discovered, i.e. if the list's bottom is discovered.
 */

/**
 * @typedef {Object} MyUnitEnum
 *
 * @property {string} friendlyName
 * @property {string} imgURL
 * @property {string} description
 */

/**
 * @typedef {Object} DexUnits
 *
 * @property {Object.<number, MyUnitEnum>[]} discoveredUnits - Units the user has discovered, associated by unit index.
 * @property {boolean} lastUnitIsDiscovered - Whether the last possible unit has been discovered, i.e. if the list's bottom is discovered.
 */

window.onload = async () => {
    route();
};

window.onpopstate = () => {
    route();
};

const route = async () => {
    const splitPath = window.location.pathname.match(/[^/?]*[^/?]/g);

    if (splitPath.length === 1) {
        setupRootPage();
    } else if (splitPath.length === 2 && splitPath[1] === "items") {
        setupItemsPage();
    } else if (splitPath.length === 2 && splitPath[1] === "units") {
        setupUnitsPage();
    } else {
        window.location.pathname = "/dex";
    }
};

const setupRootPage = () => {
    document.title = "Dex";

    document.getElementById("main").innerHTML =
        `<div class="dex-categories">` +
            `<a href="/dex/items" class="dex-category">Items</a>` +
            `<a href="/dex/units" class="dex-category">Units</a>` +
        `</div>`;

    const links = document.querySelectorAll("#main a");

    for (const link of links) {
        link.addEventListener("click", (e) => {
            window.history.pushState({}, "", link.href);
            route();
            e.preventDefault();
        });
    }
};

const setupItemsPage = async () => {
    document.title = "Items - Dex";

    replaceInterfaceWithText("Loading…");

    const res = await getDexItems();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {DexItems}
         */
        const dex = res.data;

        document.getElementById("main").insertAdjacentElement("beforeend", createBackToDex());

        const table = document.createElement("table");
        table.classList.add("dex");
        document.getElementById("main").insertAdjacentElement("beforeend", table);

        const tbody = document.createElement("tbody");
        table.insertAdjacentElement("beforeend", tbody);

        let lastDiscoveredId = -1;

        for (const [id, item] of Object.entries(dex.discoveredItems)) {
            const idnum = parseInt(id);

            if (idnum !== lastDiscoveredId + 1) {
                tbody.insertAdjacentElement("beforeend", createUnknownDexRow());
            }

            tbody.insertAdjacentElement("beforeend", createDiscoveredDexItemRow(idnum, item));

            lastDiscoveredId = idnum;
        }

        if (!dex.lastItemIsDiscovered) {
            tbody.insertAdjacentElement("beforeend", createUnknownDexRow());
        }
    }
};

const setupUnitsPage = async () => {
    document.title = "Units - Dex";

    replaceInterfaceWithText("Loading…");

    const res = await getDexUnits();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {DexUnits}
         */
        const dex = res.data;

        document.getElementById("main").insertAdjacentElement("beforeend", createBackToDex());

        const table = document.createElement("table");
        table.classList.add("dex");
        document.getElementById("main").insertAdjacentElement("beforeend", table);

        const tbody = document.createElement("tbody");
        table.insertAdjacentElement("beforeend", tbody);

        let lastDiscoveredId = -1;

        for (const [id, unit] of Object.entries(dex.discoveredUnits)) {
            const idnum = parseInt(id);

            if (idnum !== lastDiscoveredId + 1) {
                tbody.insertAdjacentElement("beforeend", createUnknownDexRow());
            }

            tbody.insertAdjacentElement("beforeend", createDiscoveredDexUnitRow(idnum, unit));

            lastDiscoveredId = idnum;
        }

        if (!dex.lastUnitIsDiscovered) {
            tbody.insertAdjacentElement("beforeend", createUnknownDexRow());
        }
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

/**
 * @param {number} id
 * @param {MyUnitEnum} unit
 */
const createDiscoveredDexUnitRow = (id, unit) => {
    const { friendlyName, imgURL, description } = unit;

    const tr = document.createElement("tr");

    tr.insertAdjacentHTML("beforeend",
        `<td>${id}</td>` +
        `<td>${friendlyName}</td>` +
        `<td><img src="${imgURL}" alt="Unit sprite"></td>` +
        `<td>${description}</td>`
    );

    return tr;
};

const createUnknownDexRow = () => {
    const tr = document.createElement("tr");
    tr.classList.add("dex-unknown");
    tr.innerHTML = `<td colspan="4"><img src="/img/three-dots.svg" alt="Three vertical circles indicating missing row(s)"></td>`;

    return tr;
};

const createBackToDex = () => {
    const a = document.createElement("a");
    a.setAttribute("href", "/dex");
    a.classList.add("dex-return");
    a.innerText = "⬅️ Return to Dex";
    a.addEventListener("click", (e) => {
        window.history.pushState({}, "", a.href);
        route();
        e.preventDefault();
    });

    return a;
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
