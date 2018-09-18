window.onload = async () => {
    const main = document.getElementById("main");

    const loadingMessage = document.createElement("div");
    loadingMessage.innerText = "Loading...";
    main.appendChild(loadingMessage);

    const { status, data: items } = await (await fetch("/api/inventory")).json();

    if (items.length === 0) {
        const noItems = document.createElement("div");
        noItems.innerText = "You have no items. Try looking around for some!";
        loadingMessage.parentNode.replaceChild(noItems, loadingMessage)
    } else {
        const itemList = document.createElement("ul");
        itemList.className = "inventory-list";
        loadingMessage.parentNode.replaceChild(itemList, loadingMessage);

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

            const itemInfo = createItemInfoBox(item);
            li.appendChild(itemInfo);

            itemList.appendChild(li);
        });
    }
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
