/**
 * @typedef {Object} Dex
 *
 * @property {number[]} discoveredItems - Item indices the user has discovered.
 * @property {boolean} lastItemIsDiscovered - Whether the last possible item has been discovered, i.e. if the list's bottom is discovered.
 */

window.onload = async () => {
    replaceInterfaceWithText("Loading…");

    const res = await getDex();

    if (res.status === "success") {
        /**
         * @type {Dex}
         */
        const dex = res.data;

        replaceInterfaceWithText(JSON.stringify(dex));
    }
};

/**
 * On success, returns {@see Dex}.
 */
const getDex = async () => (await fetch("/api/dex", {
    method: "GET",
})).json();
