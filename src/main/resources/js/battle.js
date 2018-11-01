const main = document.getElementById("main");
const GENERATE_BATTLE_BUTTON_ID = "generate-battle";
let selectedAllyId;
let selectedEnemyId;

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
    const div = document.createElement("div");
    main.appendChild(div);

    div.insertAdjacentHTML("beforeend", `<span>Allies:</span>`);

    const allyList = document.createElement("ul");
    allyList.id = "ally-list";
    div.appendChild(allyList);

    allies.forEach(ally => {
        const li = document.createElement("li");
        allyList.appendChild(li);

        const unitEl = document.createElement("pbbg-unit");
        unitEl.setAttribute("unit-id", ally.id);
        unitEl.unit = ally;
        unitEl.onclick = () => selectAlly(ally.id);
        li.appendChild(unitEl);
    });

    div.insertAdjacentHTML("beforeend", `<br><span>Enemies:</span>`);

    const enemyList = document.createElement("ul");
    enemyList.id = "enemy-list";
    div.appendChild(enemyList);

    enemies.forEach(enemy => {
        const li = document.createElement("li");
        enemyList.appendChild(li);

        const unitEl = document.createElement("pbbg-unit");
        unitEl.setAttribute("unit-id", enemy.id);
        unitEl.unit = enemy;
        unitEl.onclick = () => selectEnemy(enemy.id);
        li.appendChild(unitEl);
    });

    const attackButton = document.createElement("button");
    attackButton.id = "attack-button";
    attackButton.innerText = "Attack";
    attackButton.onclick = () => attack();
    div.appendChild(attackButton);
};

const updateBattle = (allies, enemies) => {
    const allyUnitEls = document.getElementById("ally-list").querySelectorAll("pbbg-unit");
    const enemyUnitEls = document.getElementById("enemy-list").querySelectorAll("pbbg-unit");

    const update = (unitObjs, unitEls) => {
        for (let i = 0; i < unitEls.length; i++) {
            const unitEl = unitEls[i];

            for (let j = 0; j < unitObjs.length; j++) {
                const unitElId = Number(unitEl.unitId);

                if (unitElId === unitObjs[j].id) {
                    unitEl.unit = unitObjs[j];
                    break;
                }
            }
        }
    };

    update(allies, allyUnitEls);
    update(enemies, enemyUnitEls);
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
    const attackButton = document.getElementById("attack-button");
    attackButton.classList.add("loading");
    attackButton.disabled = true;

    const { status, data } = await (await fetch("/api/battle/attack", {
        method: "POST",
        headers: {
            "Content-Type": "application/json; charset=utf-8"
        },
        body: JSON.stringify({
            allyId: selectedAllyId,
            enemyId: selectedEnemyId
        })
    })).json();

    if (status === "success") {
        updateBattle(data.allies, data.enemies);

        attackButton.classList.remove("loading");
        attackButton.disabled = false;
    }
};

const selectAlly = (allyId) => {
    const allyUnitEls = document.getElementById("ally-list").querySelectorAll("pbbg-unit");

    for (let i = 0; i < allyUnitEls.length; i++) {
        const el = allyUnitEls[i];

        if (allyId === Number(el.unitId)) {
            el.setAttribute("selected", true);
        } else {
            el.removeAttribute("selected");
        }
    }

    selectedAllyId = allyId;
};

const selectEnemy = (enemyId) => {
    const enemyUnitEls = document.getElementById("enemy-list").querySelectorAll("pbbg-unit");

    for (let i = 0; i < enemyUnitEls.length; i++) {
        const el = enemyUnitEls[i];

        if (enemyId === Number(el.unitId)) {
            el.setAttribute("selected", true);
        } else {
            el.removeAttribute("selected");
        }
    }

    selectedEnemyId = enemyId;
};
