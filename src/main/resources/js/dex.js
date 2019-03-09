/**
 * @typedef {Object} Dex
 *
 * @property {Object.<number, ItemEnum>[]} discoveredItems - Item indices the user has discovered.
 * @property {boolean} lastItemIsDiscovered - Whether the last possible item has been discovered, i.e. if the list's bottom is discovered.
 */

window.onload = async () => {
    replaceInterfaceWithText("Loading…");

    const res = await getDex();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {Dex}
         */
        const dex = res.data;

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

            tbody.insertAdjacentElement("beforeend", createDiscoveredDexRow(idnum, item));

            lastDiscoveredId = idnum;
        }

        if (!dex.lastItemIsDiscovered) {
            tbody.insertAdjacentElement("beforeend", createUnknownDexRow());
        }
    }
};

/**
 * @param {number} id
 * @param {ItemEnum} item
 */
const createDiscoveredDexRow = (id, item) => {
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

const createUnknownDexRow = () => {
    const tr = document.createElement("tr");
    tr.classList.add("dex-unknown");
    tr.innerHTML = `<td colspan="4"></td>`

    return tr;
};

/**
 * On success, returns {@see Dex}.
 */
const getDex = async () => (await fetch("/api/dex", {
    method: "GET",
})).json();
