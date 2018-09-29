window.onload = async () => {
    const main = document.getElementById("main");

    const loadingMessage = document.createElement("div");
    loadingMessage.innerText = "Loading...";
    main.appendChild(loadingMessage);

    const { status, data: { items, equipment } } = await (await fetch("/api/inventory")).json();

    /* Show equipment */
    const equipmentDisplay = createEquipmentDisplay(equipment);
    main.appendChild(equipmentDisplay);

    if (items.length === 0) {
        const noItems = document.createElement("div");
        noItems.innerText = "You have no items. Try looking around for some!";
        main.appendChild(noItems);
    } else {
        const itemList = document.createElement("ul");
        itemList.className = "inventory-list";
        main.appendChild(itemList);

        items.forEach((item) => {
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
                const itemEquipped = createItemEquippedDisplay();
                li.appendChild(itemEquipped);
            }

            const itemInfo = createItemInfoBox(item);
            li.appendChild(itemInfo);

            itemList.appendChild(li);
        });
    }

    main.removeChild(loadingMessage);
};

const createItemInfoBox = ({ description, friendlyName, quantity }) => {
    const container = document.createElement("div");

    const itemName = document.createElement("div");
    itemName.innerText = friendlyName;
    container.appendChild(itemName);

    if (quantity !== null) {
        container.appendChild(document.createElement("hr"));

        const quantityDiv = document.createElement("div");
        quantityDiv.innerText = `Quantity: ${quantity}`;
        container.appendChild(quantityDiv);
    }

    container.appendChild(document.createElement("hr"));

    const descriptionDiv = document.createElement("div");
    descriptionDiv.innerText = description;
    container.appendChild(descriptionDiv);

    return container;
};

const createItemQuantityDisplay = (quantity) => {
    const span = document.createElement("span");
    span.className = "inventory-list-item-quantity";
    span.innerText = quantity.toString();

    return span
};

const createItemEquippedDisplay = () => {
    const span = document.createElement("span");
    span.className = "inventory-list-item-equipped";
    span.innerText = "E";
    span.title = "Currently equipped";

    return span;
};

const createEquipmentDisplay = (equipment) => {
    const container = document.createElement("div");
    container.className = "equipment-display";

    const playerImg = document.createElement("img");
    playerImg.src = "/img/inventory/player.png";
    container.appendChild(playerImg);

    const equippedPickaxeImg = document.createElement("img");
    if (equipment.pickaxe !== null) {
        equippedPickaxeImg.src = equipment.pickaxe.imgURL;
        equippedPickaxeImg.className = "equipped-pickaxe";
    } else {
        equippedPickaxeImg.src = "/img/inventory/no-pickaxe.png";
        equippedPickaxeImg.className = "equipped-pickaxe-none";
    }
    container.appendChild(equippedPickaxeImg);

    return container;
};
