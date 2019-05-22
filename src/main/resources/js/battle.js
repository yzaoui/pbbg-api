/**
 * @typedef {Object} Battle
 * @property {UnitResponse[]} allies - The user's units.
 * @property {UnitResponse[]} enemies - The enemy units.
 * @property {Turn[]} turns - Turn order.
 */

/**
 * @typedef {Object} UnitEffect
 * @abstract
 * @property {string} type
 */

/**
 * @typedef {Object} UnitEffect.Health
 * @extends {UnitEffect}
 * @property {number} delta
 */

/**
 * @typedef {Object} BattleReward
 * @property {number} gold
 * @property {Item[]} items
 */

/**
 * @typedef {Object} BattleActionResult
 * @property {Battle} battle
 * @property {Object.<number, UnitEffect>} unitEffects
 * @property {?BattleReward} reward
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
     * @type {?Battle}
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
        return allies.concat(enemies).find(unit => unit.id === unitId);
    },
    /**
     * @param {number} unitId
     * @returns {boolean}
     */
    unitIsAlly(unitId) {
        return this.battle.allies.some(unit => unit.id === unitId);
    },
    /**
     * @returns {number}
     */
    nextUnitId() {
        return this.battle.turns[0].unitId;
    },
    /**
     * @returns {boolean}
     */
    nextUnitIsAlly() {
        return this.unitIsAlly(this.nextUnitId());
    }
};

const VIEW = {
    attackButton: {
        /**
         * @type {HTMLButtonElement}
         */
        DOM: null,
        set loading(val) {
            this.DOM.innerText = `Attack${val ? " (Loading…)" : ""}`;
            this.DOM.disabled = val;
            val ? this.DOM.classList.add("loading") : this.DOM.classList.remove("loading");
        },
        set hidden(val) { val ? this.DOM.classList.add("hidden") : this.DOM.classList.remove("hidden"); },
        set disabled(val) { this.DOM.disabled = val; }
    },
    attackAudio: {
        /**
         * @type {HTMLAudioElement[]}
         */
        DOMs: [],
        play() {
            setTimeout(() => this.DOMs[Math.floor(Math.random() * this.DOMs.length)].play(), 230);
        }
    },
    /**
     * @returns {PBBGUnit[]}
     */
    get enemyPBBGUnits() {
        return Array.from(document.querySelectorAll("#enemy-list pbbg-unit"));
    },
    /**
     * @returns {PBBGUnit[]}
     */
    get allPBBGUnits() {
        return Array.from(document.querySelectorAll("pbbg-unit"));
    },
    /**
     * @param {number} unitId
     * @returns {PBBGUnit}
     */
    getPBBGUnitById(unitId) {
        return this.allPBBGUnits.find(el => el.getAttribute("unit-id") === String(unitId));
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
         * @type {?Battle}
         */
        const data = res.data;

        if (data !== null) {
            /* If there is a battle, set up battle interface*/
            STATE.battle = data;
            setupBattle(STATE.battle);
        } else {
            /* If there is no battle, set up interface to generate battle*/
            setupGenerateBattle();
        }
    }
};

/**
 * @param {Battle} battle
 */
const setupBattle = (battle) => {
    VIEW.attackAudio.DOMs = ["attack1", "attack2", "attack3"].map(name => createAudio(name));

    const battleDiv = document.createElement("div");
    battleDiv.className = "battle-interface";
    main.appendChild(battleDiv);

    const queueSection = document.createElement("div");
    queueSection.classList.add("queue-section");
    battleDiv.appendChild(queueSection);

    queueSection.insertAdjacentHTML("beforeend", `<span>Turn Order ►</span>`);
    queueSection.insertAdjacentElement("beforeend", createBattleQueue(battle.turns));

    const createUnitDiv = (title, listId, units, selectFn) => {
        const div = document.createElement("div");
        div.classList.add("unit-list");
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
            unitEl.addEventListener("animationend" , (event) => {
                unitEl.classList.remove(`animation-${event.animationName}`);
            });
            li.appendChild(unitEl);
        }

        return div;
    };

    battleDiv.appendChild(createUnitDiv("Allies", "ally-list", battle.allies, ()=>({})));
    battleDiv.appendChild(createUnitDiv("Enemies", "enemy-list", battle.enemies, selectEnemy));

    const attackButton = document.createElement("button");
    attackButton.id = "attack-button";
    attackButton.className = "fancy";
    attackButton.innerText = "Attack";
    attackButton.onclick = () => attack();
    VIEW.attackButton = attackButton;
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

const createAudio = (name) => {
    const audio = document.createElement("audio");
    audio.insertAdjacentHTML("beforeend",
        `<source src="/audio/${name}.mp3" type="audio/mpeg">` +
        `<source src="/audio/${name}.ogg" type="audio/ogg">`
    );
    audio.preload = "auto";

    return audio;
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

        li.classList.add(STATE.unitIsAlly(turn.unitId) ? "ally" : "enemy");

        li.insertAdjacentHTML("beforeend", `<img src="${STATE.getUnitById(turn.unitId).iconURL}">`);
    }

    return list;
};

/**
 * @param {number} unitId
 */
const hoverUnit = (unitId) => {
    const queueEl = getBattleQueueChildren().find((el) => el.getAttribute("unit-id") === String(unitId));

    queueEl.classList.add("hovered");
    VIEW.getPBBGUnitById(unitId).classList.add("hovered");
};

/**
 * @param {number} unitId
 */
const unhoverUnit = (unitId) => {
    const queueEl = getBattleQueueChildren().find((el) => el.getAttribute("unit-id") === String(unitId));

    queueEl.classList.remove("hovered");
    VIEW.getPBBGUnitById(unitId).classList.remove("hovered");
};

const updateUnits = () => {
    for (const unitEl of VIEW.allPBBGUnits) {
        unitEl.unit = STATE.getUnitById(unitEl.unitId);

        if (unitEl.hasAttribute("dead")) {
            unitEl.onclick = null;
            unitEl.onmouseenter = null;
            unitEl.onmouseleave = null;
        }

        // Highlight next unit
        (STATE.nextUnitId() === unitEl.unitId) ? unitEl.classList.add("current-turn") : unitEl.classList.remove("current-turn");
    }

    checkForDeaths();
};

const generateBattle = async () => {
    replaceInterfaceWithText("Generating battle…");

    const res = await postGenerateBattleSession();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {Battle}
         */
        const data = res.data;

        STATE.battle = data;
        setupBattle(STATE.battle);
    } else if (res.status === "fail") {
        replaceInterfaceWithText(`FAIL: ${res.data}`);
    } else {
        replaceInterfaceWithText("ERROR");
    }
};

const attack = async () => {
    VIEW.attackButton.loading = true;

    const res = await postAllyTurn(STATE.selectedEnemyId);

    VIEW.attackButton.loading = false;

    if (res.status === "success") {
        /**
         * @type {BattleActionResult}
         */
        const data = res.data;

        processHealthEffects(filterObject(data.unitEffects, effect => effect.type === "health"));

        STATE.battle = data.battle;
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
         * @type {BattleActionResult}
         */
        const data = res.data;

        processHealthEffects(filterObject(data.unitEffects, effect => effect.type === "health"));

        STATE.battle = data.battle;
        updateUnits();
    }
};

/**
 * @param {Object.<number, UnitEffect.Health>} healthEffects
 */
const processHealthEffects = (healthEffects) => {
    const sender = VIEW.getPBBGUnitById(STATE.nextUnitId());
    const targets = VIEW.allPBBGUnits.filter(el => healthEffects.hasOwnProperty(String(el.unitId)));

    animate(sender, STATE.nextUnitIsAlly() ? "attack-send-right" : "attack-send-left");

    for (const target of targets) {
        animate(target, "attack-receive");
    }

    VIEW.attackAudio.play();
};

const animate = (el, animationName) => {
    const animClass = `animation-${animationName}`;

    el.classList.remove(animClass);
    window.requestAnimationFrame(() => window.requestAnimationFrame(() => {
        el.classList.add(animClass);
    }));
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

    STATE.selectedEnemyId = checkUnitStillAlive(VIEW.enemyPBBGUnits, STATE.selectedEnemyId);

    updateUI();
};

const updateUI = () => {
    const turnsEl = document.getElementById("battle-queue");
    turnsEl.parentNode.replaceChild(createBattleQueue(STATE.battle.turns), turnsEl);

    VIEW.allPBBGUnits.forEach((el) => el.removeAttribute("selected"));

    if (STATE.selectedEnemyId !== null) {
        VIEW.enemyPBBGUnits.find(el => el.unitId === STATE.selectedEnemyId).setAttribute("selected", "");
    }

    const enemyTurnBtn = document.getElementById("process-enemy-turn-button");

    if (STATE.nextUnitIsAlly()) {
        VIEW.attackButton.hidden = false;
        enemyTurnBtn.classList.add("hidden");
    } else {
        VIEW.attackButton.hidden = true;
        enemyTurnBtn.classList.remove("hidden");
    }

    VIEW.attackButton.disabled = STATE.selectedEnemyId === null;
};

/**
 * @returns {Element[]}
 */
const getBattleQueueChildren = () => Array.from(document.getElementById("battle-queue").children);

const selectEnemy = (enemyId) => {
    STATE.selectedEnemyId = selectUnit(VIEW.enemyPBBGUnits, enemyId, STATE.selectedEnemyId);

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
 * Filter an object's properties based on a predicate applied to their value.
 */
const filterObject = (obj, pred) => Object.keys(obj)
    .filter(key => pred(obj[key]))
    .reduce( (res, key) => {
        res[key] = obj[key];
        return res;
    }, {} );

/**
 * On success, returns {@see ?Battle}.
 */
const getBattleSession = async () => (await fetch("/api/battle/session", {
    method: "GET",
})).json();

/**
 * On success, returns {@see Battle}.
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
 * On success, returns {@see BattleActionResult}
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

/**
 * On success, returns {@see BattleActionResult}
 */
const postEnemyTurn = async () => (await fetch("/api/battle/enemyTurn", {
    method: "POST",
    headers: {
        "Content-Type": "application/json; charset=utf-8"
    }
})).json();
