/**
 * @typedef {Object} Mine
 *
 * @property {number} width - The width of the mine in cells.
 * @property {number} height - The height of the mine in cells.
 * @property {MineEntity[][]} cells - 2D array of entities, with indices indicating coordinates.
 */

/**
 * @typedef {Object} MineEntity
 *
 * @property {string} imageURL - Link to the image of this entity.
 */

/**
 * @typedef {Object} Point
 *
 * @property {number} x
 * @property {number} y
 */

/**
 * @typedef {Object} Pickaxe
 *
 * @property {string} pickaxeKind - Name of this pickaxe's kind.
 * @property {Point[]} cells - Cells that this pickaxe can reach, relative to (0, 0).
 */

/**
 * @typedef {Object} MineTypeList
 *
 * @property {MineType[]} types - Individual mine types.
 * @property {?number} nextUnlockLevel - Next level to unlock a new mine, if any.
 */

/**
 * @typedef {Object} MineType
 *
 * @property {number} id - Mine type's ID.
 * @property {string} name - Mine type's name.
 * @property {number} minLevel - Minimum level requirement to generate this type of mine.
 */

/**
 * @typedef {Object} MineActionResult
 *
 * @property {MinedItemResult[]} minedItemResults
 * @property {LevelUp[]} levelUps
 */

/**
 * @typedef {Object} MinedItemResult
 *
 * @property {Item} item
 * @property {number} expPerIndividualItem
 */

/**
 * @typedef {Object} LevelUp
 *
 * @property {number} newLevel
 * @property {?string} additionalMessage
 */

/**
 * @typedef {Object} UserDetails
 *
 * @property {LevelProgress} mining
 */

const main = document.getElementById("main");

const STATE = {
    /**
     * @type {boolean}
     */
    mineActionSubmitting: false,
    /**
     * @type {Pickaxe}
     */
    equippedPickaxe: undefined
};

const VIEW = {
    /**
     * @property {number} width
     * @property {number} height
     * @property {HTMLTableDataCellElement[][]} cells
     */
    mineInfo: undefined,
    /**
     * @param {number} x
     * @param {number} y
     * @returns {HTMLTableDataCellElement}
     */
    cellAt(x, y) {
        return this.mineInfo.cells[y][x];
    },
    setInRangeCellAt(x, y, val) {
        this.cellAt(x, y).toggleAttribute("data-in-range", val);
    },
    setPendingCellAt(x, y, val) {
        this.cellAt(x, y).toggleAttribute("data-pending", val);
    },
    removeItemCell(x, y) {
        this.cellAt(x, y).removeAttribute("style");
    },
};

const IDs = {
    MINING_GRID: "mining-grid",
    EXIT_MINE_BUTTON: "exit-mine",
    MINING_RESULTS_LIST: "mining-results-list",
    MINING_LEVEL_PROGRESS: "mining-level-progress",
    MINING_LEVEL_TEXT: "mining-level-text"
};

window.onload = async () => {
    insertScript("/js/webcomponents-bundle-2.0.0.js");
    insertModule("/js/component/pbbg-level-progress.js");
    insertModule("/js/component/pbbg-grid-preview.js");

    replaceInterfaceWithText("Loading…");

    const res = await getMine();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {?Mine}
         */
        const data = res.data;

        if (data !== null) {
            // Currently in mine session
            setupMiningInterface(data);
        } else {
            // No mine session in progress
            setupGenerateMineInterface();
        }
    } else {
        replaceInterfaceWithText("Error.");
    }
};

/**
 * @param {Mine} mine
 */
const setupMiningInterface = (mine) => {
    main.appendChild(createExitMineButton());
    const miningGrid = createMiningGrid(mine);
    main.appendChild(miningGrid);

    VIEW.mineInfo = {
        width: mine.width,
        height: mine.height,
        cells: [...miningGrid.firstElementChild.children].map(row => [...row.children])
    };

    setupPickaxeAndResultsList();
};

const setupGenerateMineInterface = async () => {
    const table = document.createElement("table");
    table.className = "mining-mine-info";
    table.innerText = "Loading list of mines…";
    main.appendChild(table);

    const res = await getMineTypes();

    if (res.status === "success") {
        /**
         * @type {MineTypeList}
         */
        const data = res.data;

        table.innerText = "";

        table.insertAdjacentHTML("beforeend", `<thead><tr><th>Mine name</th><th>Minimum lvl.</th><th>Generate mine</th></tr></thead>`);

        const tbody = document.createElement("tbody");
        table.appendChild(tbody);

        for (const mineType of data.types) {
            tbody.appendChild(createAvailableMineRow(mineType));
        }

        if (data.nextUnlockLevel !== null) {
            tbody.appendChild(createMineToUnlockRow(data.nextUnlockLevel))
        }
    }
};

/**
 * @param {MineType} mineType
 * @returns {HTMLElement}
 */
const createAvailableMineRow = (mineType) => {
    const tr = document.createElement("tr");

    tr.insertAdjacentHTML("beforeend", `<td>${mineType.name}</td><td>${mineType.minLevel}</td>`);

    const button = document.createElement("button");
    button.className = "mining-generate-mine";
    button.innerText = "Generate";
    button.onclick = () => generateMine(mineType.id);
    const generateTd = document.createElement("td");
    generateTd.appendChild(button);
    tr.appendChild(generateTd);

    return tr;
};

const createMineToUnlockRow = (nextUnlockLevel) => {
    const tr = document.createElement("tr");
    tr.className = "unmet-minimum-level";

    tr.insertAdjacentHTML("beforeend",
        `<td>???</td>` +
        `<td>${nextUnlockLevel}</td>` +
        `<td><button class="mining-generate-mine" disabled>Need to unlock</button></td>`
    );

    return tr;
};

/**
 * @param {Point} center
 * @param {number} gridWidth
 * @param {number} gridHeight
 * @param {Point[]} targets
 * @returns {Point[]}
 */
const reachableCells = (center, gridWidth, gridHeight, targets) => {
    let reachableCells = [];

    for (const target of targets) {
        const newX = center.x + target.x;
        const newY = center.y + target.y;

        if (newX >= 0 && newX < gridWidth && newY >= 0 && newY < gridHeight) {
            reachableCells.push({ x: newX, y: newY });
        }
    }

    return reachableCells;
};

/**
 * @param {Point} center
 */
const enteredCell = (center) =>
    reachableCells(center, VIEW.mineInfo.width, VIEW.mineInfo.height, STATE.equippedPickaxe.cells)
        .map(cell => VIEW.setInRangeCellAt(cell.x, cell.y, true));

/**
 * @param {Point} center
 */
const leftCell = (center) =>
    reachableCells(center, VIEW.mineInfo.width, VIEW.mineInfo.height, STATE.equippedPickaxe.cells)
        .map(cell => VIEW.setInRangeCellAt(cell.x, cell.y, false));

const updateMiningLevel = async () => {
    /**
     * @type {LevelProgress}
     */
    const { level, relativeExp, relativeExpToNextLevel } = (await getUserDetails()).data.mining;

    document.getElementById(IDs.MINING_LEVEL_PROGRESS).updateProgress({ level: level, max: relativeExpToNextLevel, value: relativeExp });

    document.getElementById(IDs.MINING_LEVEL_TEXT).innerText = `Lv. ${level} — ${relativeExp} / ${relativeExpToNextLevel}`
};

const clickedCell = async (center) => {
    if (!STATE.mineActionSubmitting) {
        STATE.mineActionSubmitting = true;

        document.getElementById(IDs.MINING_GRID).classList.add("pending-mine-action");

        const affectedCells = reachableCells(center, VIEW.mineInfo.width, VIEW.mineInfo.height, STATE.equippedPickaxe.cells);

        affectedCells.forEach(cell => VIEW.setPendingCellAt(cell.x, cell.y, true));

        const res = await postPerformMine(center.x, center.y);

        if (res.status === "success") {
            /**
             * @type {MineActionResult}
             */
            const data = res.data;

            await updateMiningLevel();

            for (const { item, expPerIndividualItem } of data.minedItemResults) {
                const li = document.createElement("li");
                if (item.quantity !== null) {
                    li.insertAdjacentHTML("beforeend", `Obtained <img src="${item.baseItem.imgURL}"> [${item.baseItem.friendlyName}] ×${item.quantity} (+${expPerIndividualItem * item.quantity} exp)`);
                } else {
                    li.insertAdjacentHTML("beforeend", `Obtained ${item.baseItem.friendlyName} (+${expPerIndividualItem} exp)`);
                }

                appendListItemToResultsList(li);
            }

            for (const { newLevel, additionalMessage } of data.levelUps) {
                const li = document.createElement("li");
                li.className = "mining-results-level-up";
                li.textContent = `Mining levelled up to level ${newLevel}!` + (additionalMessage !== null ? ` ${additionalMessage}` : "");

                appendListItemToResultsList(li);
            }

            affectedCells.forEach(cell => {
                VIEW.setPendingCellAt(cell.x, cell.y, false);
                VIEW.removeItemCell(cell.x, cell.y);
            });

            document.getElementById(IDs.MINING_GRID).classList.remove("pending-mine-action");

            STATE.mineActionSubmitting = false;
        }
    }
};

const generateMine = async (mineTypeId) => {
    replaceInterfaceWithText("Generating mine…");

    const res = await postGenerateMine(mineTypeId);

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {Mine}
         */
        const data = res.data;

        setupMiningInterface(data);
    } else {
        replaceInterfaceWithText("Error generating mine.");
    }
};

const createExitMineButton = () => {
    const button = document.createElement("button");
    button.id = IDs.EXIT_MINE_BUTTON;
    button.className = "fancy";
    button.innerText = "Exit mine";
    button.style.alignSelf = "center";
    button.onclick = () => exitMine();

    return button;
};

const exitMine = async() => {
    const exitMineButton = document.getElementById(IDs.EXIT_MINE_BUTTON);
    exitMineButton.innerText += " (Loading…)";
    exitMineButton.disabled = true;
    exitMineButton.classList.add("loading");

    const res = await postMineExit();

    if (res.status === "success") {
        // Remove mining grid
        const miningGrid = document.getElementById(IDs.MINING_GRID);
        miningGrid.parentNode.removeChild(miningGrid);

        const exitMessageLi = document.createElement("li");
        exitMessageLi.innerText = "Successfully exited mine.";

        appendListItemToResultsList(exitMessageLi);

        const toMineListBtn = document.createElement("button");
        toMineListBtn.innerText = "Return to mine list";
        toMineListBtn.onclick = () => { returnToMineList() };
        toMineListBtn.className = "fancy";
        toMineListBtn.style.alignSelf = "center";

        exitMineButton.parentNode.replaceChild(toMineListBtn, exitMineButton);

        document.getElementById(IDs.MINING_RESULTS_LIST).classList.add("expanded");
    } else {
        //TODO: Display error
    }
};

const returnToMineList = () => {
    location.reload();
};

/**
 * @param {Mine} mine
 * @returns {HTMLTableElement}
 */
const createMiningGrid = (mine) => {
    const table = document.createElement("table");
    table.id = IDs.MINING_GRID;
    table.classList.add("mining-grid");
    const tbody = document.createElement("tbody");
    table.appendChild(tbody);

    for (const row of mine.cells) {
        const tr = document.createElement("tr");
        for (const cell of row) {
            const td = document.createElement("td");
            if (cell !== null) {
                td.style.backgroundImage = `url("${cell.imageURL}")`
            }
            tr.appendChild(td);
        }
        tbody.appendChild(tr);
    }

    return table;
};

const setupPickaxeAndResultsList = async () => {
    const res = await getPickaxe();

    if (res.status === "success") {
        /**
         * @type {?Pickaxe}
         */
        const data = res.data;

        if (data !== null) {
            STATE.equippedPickaxe = data;

            /**
             * @type {UserDetails}
             */
            const userDetails = (await getUserDetails()).data;

            const { level, relativeExp, relativeExpToNextLevel } = userDetails.mining;

            main.insertAdjacentHTML("beforeend",
                `<div class="level-info" style="margin-left: auto; margin-right: auto; margin-top: 4px;">` +
                    `<pbbg-level-progress id="${IDs.MINING_LEVEL_PROGRESS}" level="${level}" value="${relativeExp}" max="${relativeExpToNextLevel}"></pbbg-level-progress>` +
                    `<span id="${IDs.MINING_LEVEL_TEXT}">Lv. ${level} — ${relativeExp} / ${relativeExpToNextLevel}</span>` +
                `</div>` +
                `<div>Equipped pickaxe: ${STATE.equippedPickaxe.pickaxeKind}</div>`
            );

            const pickaxeGrid = document.createElement("pbbg-grid-preview");
            pickaxeGrid.grid = STATE.equippedPickaxe.cells;
            main.insertAdjacentElement("beforeend", pickaxeGrid);

            main.insertAdjacentHTML("beforeend", `<ul id="${IDs.MINING_RESULTS_LIST}" class="${IDs.MINING_RESULTS_LIST}"></ul>`);

            VIEW.mineInfo.cells.forEach((row, y) => {
                row.forEach((cell, x) => {
                    const center = {x, y};
                    cell.onmouseenter = () => { enteredCell(center) };
                    cell.onmouseleave = () => { leftCell(center) };
                    cell.onclick = () => { clickedCell(center) };
                });
            });

            // Display mine grid as enabled.
            document.getElementById(IDs.MINING_GRID).classList.add("enabled");
        } else {
            main.insertAdjacentHTML("beforeend", `<div>No pickaxe equipped. Go to your inventory and equip one.</div>`);
        }
    }
};

/**
 * @param {HTMLLIElement} li
 */
const appendListItemToResultsList = (li) => {
    const list = document.getElementById(IDs.MINING_RESULTS_LIST);

    let needToScroll = (list.scrollTop + list.clientHeight) === list.scrollHeight;

    list.appendChild(li);

    if (needToScroll) {
        list.scrollTop = list.scrollHeight;
    }
};

/**************************************************
 *                  API REQUESTS                  *
 **************************************************/

/**
 * On success, returns {@see Mine} or null.
 */
const getMine = async () => (await fetch("/api/mine")).json();

/**
 * On success, returns {@see MineTypeList}.
 */
const getMineTypes = async () => (await fetch("/api/mine/types")).json();

/**
 * On success, returns {@see Mine}
 *
 * @param {number} mineTypeId
 */
const postGenerateMine = async (mineTypeId) => (await fetch("/api/mine/generate", {
    method: "POST",
    headers: {
        "Content-Type": "application/json; charset=utf-8"
    },
    body: JSON.stringify({
        mineTypeId: mineTypeId
    })
})).json();

/**
 * On success, returns {@see ?Pickaxe}
 */
const getPickaxe = async () => (await fetch("/api/pickaxe")).json();

/**
 * On success, returns {@see MineActionResult}
 *
 * @param {number} x
 * @param {number} y
 */
const postPerformMine = async (x, y) => (await fetch("/api/mine/perform", {
    method: "POST",
    headers: {
        "Content-Type": "application/json; charset=utf-8"
    },
    body: JSON.stringify({
        x: x,
        y: y
    })
})).json();

/**
 * On success, returns null.
 */
const postMineExit = async () => (await fetch("/api/mine/exit", { method: "POST" })).json();

/**
 * On success, returns {@see UserDetails}.
 */
const getUserDetails = async() => (await fetch("/api/user")).json();
