/**
 * @typedef {Object} BattleSession
 * @property {UnitResponse[]} allies - The user's units.
 * @property {UnitResponse[]} enemies - The enemy units.
 * @property {Turn[]} turns - Turn order.
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
 * @property {string} iconURL - The unit's icon URL.
 */

/**
 * @typedef {Object} Turn
 * @property {number} unitId
 */

const main = document.getElementById("main");

const STATE = {
    /**
     * @type {?BattleSession}
     */
    battle: null,
    /**
     * @type {?number}
     */
    selectedEnemyId: null,
    /**
     * @param {number} unitId
     * @returns {UnitResponse}
     */
    getUnitById(unitId) {
        const { allies, enemies } = this.battle;
        return allies.concat(enemies).find(unit => unit.id === unitId)
    },
    /**
     * @param {number} unitId
     * @returns {boolean}
     */
    unitIsAlly(unitId) {
        return this.battle.allies.some(unit => unit.id === unitId)
    },
    /**
     * @returns {boolean}
     */
    nextUnitIsAlly() {
        return this.unitIsAlly(this.battle.turns[0].unitId)
    }
};

window.onload = async () => {
    insertScript("/js/webcomponents-bundle-2.0.0.js");
    insertModule("/js/component/pbbg-unit.js");

    replaceInterfaceWithText("Loading…");

    await window.customElements.whenDefined("pbbg-unit");

    const res = await getBattleSession();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {?BattleSession}
         */
        const data = res.data;

        if (data !== null) {
            /* If there is a battle, set up battle interface*/
            STATE.battle = data;
            setupBattle();
        } else {
            /* If there is no battle, set up interface to generate battle*/
            setupGenerateBattle();
        }
    }
};

const setupBattle = () => {
    const battleDiv = document.createElement("div");
    battleDiv.className = "battle-interface";
    main.appendChild(battleDiv);

    battleDiv.appendChild(createBattleQueue(STATE.battle.turns));

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
            unitEl.setAttribute("facing", title === "Allies" ? "right" : "left");
            unitEl.unit = unit;
            unitEl.onclick = () => selectFn(unit.id);
            unitEl.onmouseenter = () => hoverUnit(unit.id);
            unitEl.onmouseleave = () => unhoverUnit(unit.id);
            li.appendChild(unitEl);
        }

        return div;
    };

    battleDiv.appendChild(createUnitDiv("Allies", "ally-list", STATE.battle.allies, ()=>({})));
    battleDiv.appendChild(createUnitDiv("Enemies", "enemy-list", STATE.battle.enemies, selectEnemy));

    const attackButton = document.createElement("button");
    attackButton.id = "attack-button";
    attackButton.className = "fancy";
    attackButton.innerText = "Attack";
    attackButton.onclick = () => attack();
    battleDiv.appendChild(attackButton);

    const processEnemyTurnButton = document.createElement("button");
    processEnemyTurnButton.id = "process-enemy-turn-button";
    processEnemyTurnButton.className = "fancy";
    processEnemyTurnButton.innerText = "Process Enemy Turn";
    processEnemyTurnButton.onclick = () => processEnemyTurn();
    battleDiv.appendChild(processEnemyTurnButton);

    updateUnits();
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
 * @param {Turn[]} turns
 */
const createBattleQueue = (turns) => {
    const list = document.createElement("ol");
    list.id = "battle-queue";

    for (const turn of turns) {
        const li = document.createElement("li");
        list.appendChild(li);

        li.setAttribute("unit-id", String(turn.unitId));
        li.onmouseenter = () => hoverUnit(turn.unitId);
        li.onmouseleave = () => unhoverUnit(turn.unitId);

        li.insertAdjacentHTML("beforeend", `<img src="${STATE.getUnitById(turn.unitId).iconURL}">`);
    }

    return list;
};

const hoverUnit = (unitId) => {
    const queueEl = getBattleQueueChildren().find((el) => el.getAttribute("unit-id") === String(unitId));

    const battleEl = getAllPBBGUnits().find((el) => el.getAttribute("unit-id") === String(unitId));

    queueEl.classList.add("hovered");
    battleEl.classList.add("hovered");
};

const unhoverUnit = (unitId) => {
    const queueEl = getBattleQueueChildren().find((el) => el.getAttribute("unit-id") === String(unitId));

    const battleEl = getAllPBBGUnits().find((el) => el.getAttribute("unit-id") === String(unitId));

    queueEl.classList.remove("hovered");
    battleEl.classList.remove("hovered");
};

const updateUnits = () => {
    for (const unitEl of getAllPBBGUnits()) {
        unitEl.unit = STATE.getUnitById(unitEl.unitId);

        if (unitEl.hasAttribute("dead")) {
            unitEl.onclick = null;
            unitEl.onmouseenter = null;
            unitEl.onmouseleave = null;
        }
    }

    checkForDeaths();
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

        STATE.battle = data;
        setupBattle();
    } else if (res.status === "fail") {
        replaceInterfaceWithText(`FAIL: ${res.data}`);
    } else {
        replaceInterfaceWithText("ERROR");
    }
};

const attack = async () => {
    const attackButton = document.getElementById("attack-button");
    attackButton.innerText = "Attack (Loading…)";
    attackButton.classList.add("loading");
    attackButton.disabled = true;

    const res = await postAllyTurn(STATE.selectedEnemyId);

    attackButton.innerText = "Attack";
    attackButton.classList.remove("loading");
    attackButton.disabled = false;

    if (res.status === "success") {
        /**
         * @type {BattleSession}
         */
        const data = res.data;

        STATE.battle = data;
        updateUnits();
    }
};

const processEnemyTurn = async () => {
    const btn = document.getElementById("process-enemy-turn-button");
    btn.innerText = "Process Enemy Turn (Loading…)";
    btn.classList.add("loading");
    btn.disabled = true;

    const res = await postEnemyTurn();

    btn.innerText = "Process Enemy Turn";
    btn.classList.remove("loading");
    btn.disabled = false;

    if (res.status === "success") {
        /**
         * @type {BattleSession}
         */
        const data = res.data;

        STATE.battle = data;
        updateUnits();
    }
};

/**
 * Removes selected unit if now dead.
 */
const checkForDeaths = () => {
    const checkUnitStillAlive = (unitEls, unitId) => {
        if (unitId === null) return null;

        const unitEl = unitEls.find((el) => el.unitId === unitId);

        if (unitEl.hasAttribute("dead")) {
            return null;
        } else {
            return unitId;
        }
    };

    STATE.selectedEnemyId = checkUnitStillAlive(getEnemyPBBGUnits(), STATE.selectedEnemyId);

    updateUI();
};

const updateUI = () => {
    const turnsEl = document.getElementById("battle-queue");
    turnsEl.parentNode.replaceChild(createBattleQueue(STATE.battle.turns), turnsEl);

    getAllPBBGUnits().forEach((el) => el.removeAttribute("selected"));

    if (STATE.selectedEnemyId !== null) {
        getEnemyPBBGUnits().find(el => el.unitId === STATE.selectedEnemyId).setAttribute("selected", "");
    }

    const atkbtn = document.getElementById("attack-button");
    const enemyTurnBtn = document.getElementById("process-enemy-turn-button");

    if (STATE.nextUnitIsAlly()) {
        atkbtn.classList.remove("hidden");
        enemyTurnBtn.classList.add("hidden");
    } else {
        atkbtn.classList.add("hidden");
        enemyTurnBtn.classList.remove("hidden");
    }

    document.getElementById("attack-button").disabled = STATE.selectedEnemyId === null;
};

const getEnemyPBBGUnits = () => Array.from(document.querySelectorAll("#enemy-list pbbg-unit"));

const getAllPBBGUnits = () => Array.from(document.querySelectorAll("pbbg-unit"));

/**
 * @returns {Element[]}
 */
const getBattleQueueChildren = () => Array.from(document.getElementById("battle-queue").children);

const selectEnemy = (enemyId) => {
    STATE.selectedEnemyId = selectUnit(getEnemyPBBGUnits(), enemyId, STATE.selectedEnemyId);

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
 * @param {number} enemyId
 *
 * On success, returns {@see BattleSession}
 */
const postAllyTurn = async (enemyId) => (await fetch("/api/battle/allyTurn", {
    method: "POST",
    headers: {
        "Content-Type": "application/json; charset=utf-8"
    },
    body: JSON.stringify({
        enemyId: enemyId
    })
})).json();

const postEnemyTurn = async () => (await fetch("/api/battle/enemyTurn", {
    method: "POST",
    headers: {
        "Content-Type": "application/json; charset=utf-8"
    }
})).json();
