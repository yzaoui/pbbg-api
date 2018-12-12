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
 * @property {number} baseId
 * @property {string} friendlyName
 * @property {string} imgURL
 * @property {?number} quantity
 * @property {string} description
 * @property {?boolean} equipped
 */

window.onload = async () => {
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

            const itemImg = document.createElement("img");
            itemImg.src = item.imgURL;
            li.appendChild(itemImg);

            if (item.quantity !== null) {
                const itemQuantity = createItemQuantityDisplay(item.quantity);
                li.appendChild(itemQuantity);
            }

            if (item.equipped !== null) {
                if (item.equipped === true) {
                    const itemEquipped = createItemEquippedDisplay();
                    li.appendChild(itemEquipped);
                }
            }

            const itemInfo = createItemTooltip(id, item);
            li.appendChild(itemInfo);

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

    const itemName = document.createElement("div");
    itemName.innerText = item.friendlyName;
    container.appendChild(itemName);

    if (item.quantity !== null) {
        container.appendChild(document.createElement("hr"));

        const quantityDiv = document.createElement("div");
        quantityDiv.innerText = `Quantity: ${item.quantity}`;
        container.appendChild(quantityDiv);
    }

    if (item.equipped !== null) {
        container.appendChild(document.createElement("hr"));

        const equipActionButton = document.createElement("button");

        if (item.equipped === true) {
            equipActionButton.innerText = "Unequip";
            equipActionButton.onclick = () => unequip(itemId);
        } else if (item.equipped === false) {
            equipActionButton.innerText = "Equip";
            equipActionButton.onclick = () => equip(itemId);
        }

        container.appendChild(equipActionButton);
    }

    container.appendChild(document.createElement("hr"));

    const descriptionDiv = document.createElement("div");
    descriptionDiv.innerText = item.description;
    container.appendChild(descriptionDiv);

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

    const playerImg = document.createElement("img");
    playerImg.src = "/img/inventory/player.png";
    container.appendChild(playerImg);

    const equippedPickaxeContainer = document.createElement("div");
    equippedPickaxeContainer.className = "equipment-pickaxe-slot";
    container.appendChild(equippedPickaxeContainer);

    const equippedPickaxeImg = document.createElement("img");
    equippedPickaxeContainer.appendChild(equippedPickaxeImg);
    if (equipment.pickaxe !== null) {
        equippedPickaxeImg.src = equipment.pickaxe.imgURL;
        equippedPickaxeContainer.classList.add("equipped");
    } else {
        equippedPickaxeImg.src = "/img/inventory/no-pickaxe.png";
    }

    return container;
};
