window.onload = async () => {
    const equippedPickaxeSpan = document.getElementById("equipped-pickaxe");
    equippedPickaxeSpan.innerText = "Loading...";

    const res = await fetch("/api/equipment");
    const equipment = await res.json();

    if (equipment.hasOwnProperty("pickaxe")) {
        equippedPickaxeSpan.innerText = equipment.pickaxe;
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
