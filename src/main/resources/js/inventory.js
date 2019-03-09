/**
 * @typedef {Object} Inventory
 *
 * @property {InventoryItem[]} items
 * @property {Equipment} equipment
 */

/**
 * @typedef {Object} InventoryItem
 *
 * @property {number} id
 * @property {Item} item
 */

/**
 * @typedef {Object} Equipment
 *
 * @property {?Item} pickaxe
 */

/**
 * @typedef {Object} Item
 *
 * @property {ItemEnum} baseItem
 * @property {?number} quantity
 * @property {?boolean} equipped
 * @property {?Point[]} grid
 */

/**
 * @typedef {Object} ItemEnum
 *
 * @property {string} friendlyName
 * @property {string} imgURL
 * @property {string} description
 */

window.onload = async () => {
    insertModule("/js/component/pbbg-grid-preview.js");

    const main = document.getElementById("main");

    replaceInterfaceWithText("Loading…");

    const res = await getInventory();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {Inventory}
         */
        const data = res.data;

        /* Show equipment */
        main.appendChild(createEquipmentDisplay(data.equipment));

        const itemList = document.createElement("ul");
        itemList.className = "inventory-list";
        main.appendChild(itemList);

        for (const {id, item} of data.items) {
            const li = document.createElement("li");
            li.className = "inventory-list-item";

            li.insertAdjacentHTML("beforeend", `<img src="${item.baseItem.imgURL}">`);

            if (item.quantity !== null) {
                li.appendChild(createItemQuantityDisplay(item.quantity));
            }

            if (item.equipped !== null) {
                if (item.equipped === true) {
                    li.appendChild(createItemEquippedDisplay());
                }
            }

            li.appendChild(createItemTooltip(id, item));

            itemList.appendChild(li);
        }
    } else {
        replaceInterfaceWithText("Error.");
    }
};

/**
 * On success, returns {@see Inventory}.
 */
const getInventory = async () => (await fetch("/api/inventory")).json();

/**
 * @param {number} itemId
 * @param {Item} item
 *
 * @returns {HTMLElement}
 */
const createItemTooltip = (itemId, item) => {
    const container = document.createElement("div");
    container.className = "inventory-list-item-tooltip";

    container.insertAdjacentHTML("beforeend", `<div>${item.baseItem.friendlyName}</div>`);

    if (item.quantity !== null) {
        container.insertAdjacentHTML("beforeend", `<hr><div>Quantity: ${item.quantity}</div>`);
    }

    if (item.equipped !== null) {
        container.insertAdjacentHTML("beforeend", `<hr>`);

        const equipActionButton = document.createElement("button");

        if (item.equipped) {
            equipActionButton.innerText = "Unequip";
            equipActionButton.onclick = () => unequip(itemId);
        } else {
            equipActionButton.innerText = "Equip";
            equipActionButton.onclick = () => equip(itemId);
        }

        container.appendChild(equipActionButton);
    }

    if (item.grid !== null) {
        container.insertAdjacentHTML("beforeend", `<hr>`);

        const preview = document.createElement("pbbg-grid-preview");
        preview.grid = item.grid;

        container.insertAdjacentElement("beforeend", preview);
    }

    container.insertAdjacentHTML("beforeend", `<hr><div>${item.baseItem.description}</div>`);

    return container;
};

/**
 * @param {number} itemId
 */
const equip = async (itemId) => {
    const res = await postEquipmentAction(itemId, "equip");

    if (res.status === "success") location.reload();
};

/**
 * @param {number} itemId
 */
const unequip = async (itemId) => {
    const res = await postEquipmentAction(itemId, "unequip");

    if (res.status === "success") location.reload();
};

/**
 * @param {number} inventoryItemId
 * @param {("equip"|"unequip")} action
 */
const postEquipmentAction = async (inventoryItemId, action) => (await fetch(`/api/inventory/equipment?action=${action}`, {
    method: "POST",
    headers: {
        "Content-Type": "application/json; charset=utf-8"
    },
    body: JSON.stringify({
        inventoryItemId: inventoryItemId
    })
})).json();

/**
 * @param {number} quantity
 *
 * @returns {HTMLElement}
 */
const createItemQuantityDisplay = (quantity) => {
    const span = document.createElement("span");
    span.className = "inventory-list-item-quantity";
    span.innerText = quantity.toString();

    return span
};

/**
 * @returns {HTMLElement}
 */
const createItemEquippedDisplay = () => {
    const span = document.createElement("span");
    span.className = "inventory-list-item-equipped";
    span.innerText = "E";
    span.title = "Currently equipped";

    return span;
};

/**
 * @param {Equipment} equipment
 *
 * @returns {HTMLElement}
 */
const createEquipmentDisplay = (equipment) => {
    const container = document.createElement("div");
    container.className = "equipment-display";

    container.insertAdjacentHTML("beforeend", `<img src="/img/inventory/player.png">`);

    const equippedPickaxeContainer = document.createElement("div");
    equippedPickaxeContainer.className = "equipment-pickaxe-slot";
    container.appendChild(equippedPickaxeContainer);

    const equippedPickaxeImg = document.createElement("img");
    equippedPickaxeContainer.appendChild(equippedPickaxeImg);
    if (equipment.pickaxe !== null) {
        equippedPickaxeImg.src = equipment.pickaxe.baseItem.imgURL;
        equippedPickaxeContainer.classList.add("equipped");
    } else {
        equippedPickaxeImg.src = "/img/inventory/no-pickaxe.png";
    }

    return container;
};
