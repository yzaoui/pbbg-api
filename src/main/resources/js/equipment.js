window.onload = async () => {
    const equippedPickaxeLabel = document.getElementById("equipped-pickaxe");
    equippedPickaxeLabel.innerText = "Loading...";

    const res = await fetch("/api/equipment");
    const { pickaxe } = await res.json();

    if (pickaxe !== null) {
        equippedPickaxeLabel.innerText = pickaxe;
    } else {
        equippedPickaxeLabel.innerText = "None";

        const generatePickaxeButton = document.createElement("button");
        generatePickaxeButton.id = "generate-pickaxe";
        generatePickaxeButton.innerText = "Click here to generate a pickaxe";
        generatePickaxeButton.onclick = async (e) => {
            const equippedPickaxeLabel = document.getElementById("equipped-pickaxe");
            equippedPickaxeLabel.innerText = "Loading...";

            const { status, data: pickaxe } = await (await fetch("/api/pickaxe", {
                method: "POST"
            })).json();

            equippedPickaxeLabel.innerText = pickaxe.type;

            const generatePickaxeButton = document.getElementById("generate-pickaxe");
            const message = document.createTextNode("Success! Obtained new pickaxe");
            generatePickaxeButton.parentNode.replaceChild(message, generatePickaxeButton);
        };

        equippedPickaxeLabel.parentNode.insertBefore(generatePickaxeButton, equippedPickaxeLabel.nextSibling);
        generatePickaxeButton.parentNode.insertBefore(document.createElement("br"), generatePickaxeButton);
    }
};
