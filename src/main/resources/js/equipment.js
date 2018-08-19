window.onload = async () => {
    const equippedPickaxeSpan = document.getElementById("equipped-pickaxe");
    equippedPickaxeSpan.innerText = "Loading...";

    const res = await fetch("/api/equipment");
    const { pickaxe } = await res.json();

    if (pickaxe !== null) {
        equippedPickaxeSpan.innerText = pickaxe;
    } else {
        equippedPickaxeSpan.innerText = "None";

        const button = document.createElement("button");
        button.innerText = "Click here to generate a pickaxe";
        button.onclick = () => {
            const res = fetch("/api/pickaxe", {
                method: "POST"
            });
        };

        equippedPickaxeSpan.parentNode.insertBefore(button, equippedPickaxeSpan.nextSibling);
        button.parentNode.insertBefore(document.createElement("br"), button);
    }
};
