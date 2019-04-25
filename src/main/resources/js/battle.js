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
 * @property {string} idleAnimationURL - The unit's idle animation URL.
 */

const main = document.getElementById("main");
const selectedUnits = {
    allyId: null,
    enemyId: null
};

window.onload = async () => {
    insertScript("/js/webcomponents-bundle-2.0.0.js");
    insertModule("/js/component/pbbg-unit.js");

    replaceInterfaceWithText("Loading…");

    const res = await getBattleSession();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {?BattleSession}
         */
        const data = res.data;

        if (data !== null) {
            /* If there is a battle, set up battle interface*/
            setupBattle(data);
        } else {
            /* If there is no battle, set up interface to generate battle*/
            setupGenerateBattle();
        }
    }
};

/**
 * @param {BattleSession} battle
 */
const setupBattle = (battle) => {
    const battleDiv = document.createElement("div");
    battleDiv.className = "battle-interface";
    main.appendChild(battleDiv);

    const createUnitDiv = (title, listId, units, selectFn) => {
        const div = document.createElement("div");
        div.insertAdjacentHTML("beforeend", `<h1>${title}</h1>`);

        const list = document.createElement("ul");
        list.id = listId;
        div.appendChild(list);

        for (const unit of units) {
            const li = document.createElement("li");
            list.appendChild(li);

            const unitEl = document.createElement("pbbg-unit");
            unitEl.setAttribute("unit-id", String(unit.id));
            unitEl.unit = unit;
            unitEl.onclick = () => selectFn(unit.id);
            li.appendChild(unitEl);
        }

        return div;
    };

    battleDiv.appendChild(createUnitDiv("Allies", "ally-list", battle.allies, selectAlly));
    battleDiv.appendChild(createUnitDiv("Enemies", "enemy-list", battle.enemies, selectEnemy));

    const attackButton = document.createElement("button");
    attackButton.id = "attack-button";
    attackButton.className = "fancy";
    attackButton.innerText = "Attack";
    attackButton.onclick = () => attack();
    attackButton.disabled = true;
    battleDiv.appendChild(attackButton);
};

const setupGenerateBattle = () => {
    const button = document.createElement("button");
    button.className = "fancy";
    button.style.alignSelf = "center";
    button.innerText = "Generate battle";
    button.onclick = () => generateBattle();

    main.appendChild(button);
};

/**
 * @param {UnitResponse[]} allies
 * @param {UnitResponse[]} enemies
 */
const updateUnits = (allies, enemies) => {
    const unitEls = getAllPBBGUnits();
    const unitObjs = allies.concat(enemies);

    for (const unitEl of unitEls) {
        unitEl.unit = unitObjs.find((obj) => unitEl.unitId === obj.id)
    }
};

const generateBattle = async () => {
    replaceInterfaceWithText("Generating battle…");

    const res = await postGenerateBattleSession();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {BattleSession}
         */
        const data = res.data;

        setupBattle(data);
    } else {
        replaceInterfaceWithText("Error");
    }
};

const attack = async () => {
    const attackButton = document.getElementById("attack-button");
    attackButton.innerText = "Attack (Loading…)";
    attackButton.classList.add("loading");
    attackButton.disabled = true;

    const res = await postBattleAttack(selectedUnits.allyId, selectedUnits.enemyId);

    attackButton.innerText = "Attack";
    attackButton.classList.remove("loading");
    attackButton.disabled = false;

    if (res.status === "success") {
        /**
         * @type {BattleSession}
         */
        const data = res.data;

        updateUnits(data.allies, data.enemies);
        checkForDeaths();
    }
};

/**
 * Removes selected unit if now dead.
 */
const checkForDeaths = () => {
    const checkUnitStillAlive = (unitEls, unitId) => {
        const unitEl = unitEls.find((el) => el.unitId === unitId);

        if (unitEl.hasAttribute("dead")) {
            return null;
        } else {
            return unitId;
        }
    };

    selectedUnits.allyId = checkUnitStillAlive(getAllyPBBGUnits(), selectedUnits.allyId);
    selectedUnits.enemyId = checkUnitStillAlive(getEnemyPBBGUnits(), selectedUnits.enemyId);

    updateUI();
};

const updateUI = () => {
    getAllPBBGUnits().forEach((el) => el.removeAttribute("selected"));

    const updateSelectedUnit = (unitEls, unitId) => {
        if (unitId === null) return;

        unitEls.find((el) => el.unitId === unitId).setAttribute("selected", "");
    };

    updateSelectedUnit(getAllyPBBGUnits(), selectedUnits.allyId);
    updateSelectedUnit(getEnemyPBBGUnits(), selectedUnits.enemyId);

    document.getElementById("attack-button").disabled = !(selectedUnits.allyId && selectedUnits.enemyId);
};

const getAllyPBBGUnits = () => Array.from(document.querySelectorAll("#ally-list pbbg-unit"));

const getEnemyPBBGUnits = () => Array.from(document.querySelectorAll("#enemy-list pbbg-unit"));

const getAllPBBGUnits = () => Array.from(document.querySelectorAll("pbbg-unit"));

const selectAlly = (allyId) => {
    selectedUnits.allyId = selectUnit(getAllyPBBGUnits(), allyId, selectedUnits.allyId);

    updateUI();
};

const selectEnemy = (enemyId) => {
    selectedUnits.enemyId = selectUnit(getEnemyPBBGUnits(), enemyId, selectedUnits.enemyId);

    updateUI();
};

/**
 * @returns {?number} Selected unit's ID, or null if invalid.
 */
const selectUnit = (unitEls, unitId, currentlySelectedId) => {
    const unitEl = unitEls.find((el) => el.unitId === unitId);

    if (!unitEl.hasAttribute("dead")) {
        unitEl.setAttribute("selected", "");
        return unitId;
    } else {
        return currentlySelectedId;
    }
};

/**
 * On success, returns {@see ?BattleSession}.
 */
const getBattleSession = async () => (await fetch("/api/battle/session", {
    method: "GET",
})).json();

/**
 * On success, returns {@see BattleSession}.
 */
const postGenerateBattleSession = async () => (await fetch("/api/battle/session?action=generate", {
    method: "POST",
    headers: {
        "Content-Type": "application/json; charset=utf-8"
    }
})).json();

/**
 * @param {number} allyId
 * @param {number} enemyId
 *
 * On success, returns {@see BattleSession}
 */
const postBattleAttack = async (allyId, enemyId) => (await fetch("/api/battle/attack", {
    method: "POST",
    headers: {
        "Content-Type": "application/json; charset=utf-8"
    },
    body: JSON.stringify({
        allyId: allyId,
        enemyId: enemyId
    })
})).json();
