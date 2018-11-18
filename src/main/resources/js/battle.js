/**
 * @typedef {Object} BattleSession
 * @property {UnitResponse[]} allies - The user's units.
 * @property {UnitResponse[]} enemies - The enemy units.
 */

/**
 * @typedef {Object} UnitResponse
 * @property {number} id - The unit's ID.
 * @property {string} name - The unit's name.
 * @property {number} baseUnitId - The ID of the unit's base unit type.
 * @property {number} hp - The unit's current HP.
 * @property {number} maxHP - The unit's maximum HP.
 * @property {number} atk - The unit's attack stat.
 * @property {LevelProgress} levelProgress - The unit's level and experience information.
 */

const main = document.getElementById("main");
let selectedAllyId;
let selectedEnemyId;

window.onload = async () => {
    main.innerText = "Loading battle session...";

    const res = await getBattleSession();

    if (res.status === "success") {
        /**
         * @type {?BattleSession}
         */
        const data = res.data;

        main.innerText = "";

        if (data !== null) {
            /* If there is a battle, set up battle interface*/
            setupBattle(data.allies, data.enemies);
        } else {
            /* If there is no battle, set up interface to generate battle*/
            const button = document.createElement("button");
            button.innerText = "Generate battle";
            button.onclick = () => generateBattle();

            main.appendChild(button);
        }
    }
};

/**
 * On success, returns {@see ?BattleSession}.
 */
const getBattleSession = async () => {
    return (await fetch("/api/battle/session", {
        method: "GET",
    })).json();
};

/**
 * On success, returns {@see BattleSession}.
 */
const postGenerateBattleSession = async () => {
    return (await fetch("/api/battle/session?action=generate", {
        method: "POST",
        headers: {
            "Content-Type": "application/json; charset=utf-8"
        }
    })).json();
};

/**
 * @param {number} allyId
 * @param {number} enemyId
 *
 * On success, returns {@see BattleSession}
 */
const postBattleAttack = async (allyId, enemyId) => {
    return (await fetch("/api/battle/attack", {
        method: "POST",
        headers: {
            "Content-Type": "application/json; charset=utf-8"
        },
        body: JSON.stringify({
            allyId: allyId,
            enemyId: enemyId
        })
    })).json();
};

/**
 * @param {UnitResponse[]} allies
 * @param {UnitResponse[]} enemies
 */
const setupBattle = (allies, enemies) => {
    const battleDiv = document.createElement("div");
    battleDiv.className = "battle-interface";
    main.appendChild(battleDiv);

    const allyDiv = document.createElement("div");
    battleDiv.appendChild(allyDiv);

    allyDiv.insertAdjacentHTML("beforeend", `<h1>Allies</h1>`);

    const allyList = document.createElement("ul");
    allyList.id = "ally-list";
    allyDiv.appendChild(allyList);

    allies.forEach(ally => {
        const li = document.createElement("li");
        allyList.appendChild(li);

        const unitEl = document.createElement("pbbg-unit");
        unitEl.setAttribute("unit-id", String(ally.id));
        unitEl.unit = ally;
        unitEl.onclick = () => selectAlly(ally.id);
        li.appendChild(unitEl);
    });

    const enemyDiv = document.createElement("div");
    battleDiv.appendChild(enemyDiv);

    enemyDiv.insertAdjacentHTML("beforeend", `<h1>Enemies</h1>`);

    const enemyList = document.createElement("ul");
    enemyList.id = "enemy-list";
    enemyDiv.appendChild(enemyList);

    enemies.forEach(enemy => {
        const li = document.createElement("li");
        enemyList.appendChild(li);

        const unitEl = document.createElement("pbbg-unit");
        unitEl.setAttribute("unit-id", String(enemy.id));
        unitEl.unit = enemy;
        unitEl.onclick = () => selectEnemy(enemy.id);
        li.appendChild(unitEl);
    });

    const attackButton = document.createElement("button");
    attackButton.id = "attack-button";
    attackButton.innerText = "Attack";
    attackButton.onclick = () => attack();
    attackButton.disabled = true;
    main.appendChild(attackButton);
};

/**
 * @param {UnitResponse[]} allies
 * @param {UnitResponse[]} enemies
 */
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

const generateBattle = async () => {
    while (main.hasChildNodes()) {
        main.removeChild(main.firstChild);
    }

    main.innerText = "Generating battle...";

    const res = await postGenerateBattleSession();

    if (res.status === "success") {
        /**
         * @type {BattleSession}
         */
        const data = res.data;

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

    const res = await postBattleAttack(selectedAllyId, selectedEnemyId);

    attackButton.classList.remove("loading");
    attackButton.disabled = false;

    if (res.status === "success") {
        /**
         * @type {BattleSession}
         */
        const data = res.data;

        updateBattle(data.allies, data.enemies);
    }
};

const selectAlly = (allyId) => {
    const allyUnitEls = document.getElementById("ally-list").querySelectorAll("pbbg-unit");

    selectUnit(allyUnitEls, allyId);
    selectedAllyId = allyId;

    document.getElementById("attack-button").disabled = !(selectedAllyId && selectedEnemyId);
};

const selectEnemy = (enemyId) => {
    const enemyUnitEls = document.getElementById("enemy-list").querySelectorAll("pbbg-unit");

    selectUnit(enemyUnitEls, enemyId);
    selectedEnemyId = enemyId;

    document.getElementById("attack-button").disabled = !(selectedAllyId && selectedEnemyId);
};

const selectUnit = (unitEls, unitId) => {
    for (let i = 0; i < unitEls.length; i++) {
        const el = unitEls[i];

        if (!el.hasAttribute("dead") && unitId === Number(el.unitId)) {
            el.setAttribute("selected", "");
        } else {
            el.removeAttribute("selected");
        }
    }
};
