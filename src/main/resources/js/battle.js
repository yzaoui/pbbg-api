const main = document.getElementById("main");
const GENERATE_BATTLE_BUTTON_ID = "generate-battle";

window.onload = async () => {
    main.innerText = "Loading battle session...";

    const { status, data } = await (await fetch("/api/battle/session")).json();

    if (status === "success") {
        main.innerText = "";

        if (data !== null) {
            const { allies, enemies } = data;
            setupBattle(allies, enemies);
        } else {
            setupGenerateBattle();
        }
    }
};

const setupBattle = (allies, enemies) => {
    main.appendChild(document.createTextNode("Allies:"));
    main.appendChild(document.createTextNode(JSON.stringify(allies)));
    main.appendChild(document.createElement("br"));
    main.appendChild(document.createTextNode("Enemies:"));
    main.appendChild(document.createTextNode(JSON.stringify(enemies)));

    const allyInput = document.createElement("input");
    allyInput.id = "ally-id";
    allyInput.type = "number";
    main.appendChild(allyInput);

    const enemyInput = document.createElement("input");
    enemyInput.id = "enemy-id";
    enemyInput.type = "number";
    main.appendChild(enemyInput);

    const attackButton = document.createElement("button");
    attackButton.innerText = "Attack";
    attackButton.onclick = () => attack();
    main.appendChild(attackButton);
};

const setupGenerateBattle = () => {
    const button = document.createElement("button");
    button.id = GENERATE_BATTLE_BUTTON_ID;
    button.innerText = "Generate battle";
    button.onclick = () => generateBattle();

    main.appendChild(button);
};

const generateBattle = async () => {
    while (main.hasChildNodes()) {
        main.removeChild(main.firstChild);
    }

    main.innerText = "Generating battle...";

    const { status, data } = await (await fetch("/api/battle/session?action=generate", {
        method: "POST",
        headers: {
            "Content-Type": "application/json; charset=utf-8"
        }
    })).json();

    if (status === "success") {
        main.innerText = "";

        setupBattle(data.allies, data.enemies);
    } else {
        main.innerText = "Error";
    }
};

const attack = async () => {
    const allyId = document.getElementById("ally-id").value;
    const enemyId = document.getElementById("enemy-id").value;

    const { status, data } = await (await fetch("/api/battle/attack", {
        method: "POST",
        headers: {
            "Content-Type": "application/json; charset=utf-8"
        },
        body: JSON.stringify({
            allyId: allyId,
            enemyId: enemyId
        })
    })).json();

    if (status === "success") location.reload();
};
