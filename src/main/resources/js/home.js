window.onload = async () => {
    const main = document.getElementById("main");

    const equippedPickaxeDisplay = document.createElement("div");
    equippedPickaxeDisplay.id = "equipped-pickaxe";
    main.appendChild(equippedPickaxeDisplay);

    const loadingMessage = document.createElement("span");
    loadingMessage.innerText = "Loading pickaxe generate button...";
    equippedPickaxeDisplay.appendChild(loadingMessage);

    const res = await fetch("/api/inventory");
    const { status, data: { equipment } } = await res.json();

    equippedPickaxeDisplay.removeChild(loadingMessage);

    if (equipment.pickaxe === null) {
        const generatePickaxeButton = document.createElement("button");
        generatePickaxeButton.id = "generate-pickaxe";
        generatePickaxeButton.innerText = "Click here to generate a pickaxe";
        generatePickaxeButton.onclick = async (e) => {
            const equippedPickaxeContainer = document.getElementById("equipped-pickaxe");

            equippedPickaxeContainer.innerText = "Loading...";

            const { status, data: pickaxe } = await (await fetch("/api/pickaxe", {
                method: "POST"
            })).json();

            equippedPickaxeContainer.innerText = "";

            const message = document.createElement("span");
            message.innerText = "Success! Obtained new pickaxe: " + pickaxe.friendlyName;
            equippedPickaxeContainer.appendChild(message);

            equippedPickaxeContainer.appendChild(document.createElement("br"));

            const pickaxeImg = document.createElement("img");
            pickaxeImg.src = pickaxe.imgURL;
            equippedPickaxeContainer.appendChild(pickaxeImg);
        };

        equippedPickaxeDisplay.appendChild(generatePickaxeButton);
    } else {
        equippedPickaxeDisplay.parentNode.removeChild(equippedPickaxeDisplay);
    }
};
