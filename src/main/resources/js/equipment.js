window.onload = async () => {
    const equippedPickaxeLabel = document.getElementById("equipped-pickaxe");
    equippedPickaxeLabel.innerText = "Loading...";

    const res = await fetch("/api/equipment");
    const { pickaxe } = await res.json();

    if (pickaxe !== null) {
        equippedPickaxeLabel.innerText = pickaxe.friendlyName;
        const main = document.getElementById("main");

        const pickaxeImg = document.createElement("img");
        pickaxeImg.src = pickaxe.imgURL;

        main.appendChild(document.createElement("br"));
        main.appendChild(pickaxeImg);
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

            equippedPickaxeLabel.innerText = pickaxe.friendlyName;

            const generatePickaxeButton = document.getElementById("generate-pickaxe");
            const message = document.createElement("div");
            message.classList.add("success-message");
            message.innerText = "Success! Obtained new pickaxe: " + pickaxe.friendlyName;

            const pickaxeImg = document.createElement("img");
            pickaxeImg.src = pickaxe.imgURL;

            const main = document.getElementById("main");
            main.replaceChild(message, generatePickaxeButton);
            main.appendChild(document.createElement("br"));
            main.appendChild(pickaxeImg);

        };

        equippedPickaxeLabel.parentNode.parentNode.insertBefore(generatePickaxeButton, equippedPickaxeLabel.nextSibling);
        generatePickaxeButton.parentNode.insertBefore(document.createElement("br"), generatePickaxeButton);
    }
};
