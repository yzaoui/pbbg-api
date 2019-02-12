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
 * @typedef {Object} Pickaxe
 *
 * @property {string} pickaxeKind - Name of this pickaxe's kind.
 * @property {number[][]} cells - Cells that this pickaxe can reach, in the form of [x, y] coordinates, relative to [0, 0].
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

/**
 * @property {number} width
 * @property {number} height
 * @property {HTMLTableDataCellElement[][]} cells
 */
let mineInfo;

const MINING_GRID_ID = "mining-grid";
const EXIT_MINE_BUTTON_ID = "exit-mine";
const MINING_RESULTS_LIST_ID = "mining-results-list";
const MINING_LEVEL_PROGRESS_ID = "mining-level-progress";
const MINING_LEVEL_TEXT_ID = "mining-level-text";
/**
 * @type {Pickaxe}
 */
let equippedPickaxe;

/**
 * @type {boolean}
 */
let mineActionSubmitting = false;

window.onload = async () => {
    insertScript("/js/webcomponents-bundle-2.0.0.js");
    insertModule("/js/component/pbbg-level-progress.js");

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
    const main = document.getElementById("main");

    main.appendChild(createExitMineButton());
    const miningGrid = createMiningGrid(mine);
    main.appendChild(miningGrid);

    mineInfo = {
        width: mine.width,
        height: mine.height,
        cells: [...miningGrid.firstElementChild.children].map(row => [...row.children])
    };

    setupPickaxeAndResultsList();
};

const setupGenerateMineInterface = async () => {
    const main = document.getElementById("main");

    const table = document.createElement("table");
    table.id = "generate-mine-container";
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

const reachableCells = (x, y, gridWidth, gridHeight, targets) => {
    let cells = [];

    for (const target of targets) {
        const newX = x + target[0];
        const newY = y + target[1];

        if (newX >= 0 && newX < gridWidth && newY >= 0 && newY < gridHeight) {
            cells.push([newX, newY]);
        }
    }

    return cells;
};

const enteredCell = (x, y) => {
    const affectedCells = reachableCells(x, y, mineInfo.width, mineInfo.height, equippedPickaxe.cells);

    for (const cell of affectedCells) {
        const [x, y] = cell;
        mineInfo.cells[y][x].classList.add("selected-item");
    }
};

const leftCell = (x, y) => {
    const affectedCells = reachableCells(x, y, mineInfo.width, mineInfo.height, equippedPickaxe.cells);

    for (const cell of affectedCells) {
        const [x, y] = cell;
        mineInfo.cells[y][x].classList.remove("selected-item");
    }
};

const updateMiningLevel = async () => {
    /**
     * @type {LevelProgress}
     */
    const { level, relativeExp, relativeExpToNextLevel } = (await getUserDetails()).data.mining;

    document.getElementById(MINING_LEVEL_PROGRESS_ID).updateProgress({ level: level, max: relativeExpToNextLevel, value: relativeExp });

    document.getElementById(MINING_LEVEL_TEXT_ID).innerText = `Lv. ${level} — ${relativeExp} / ${relativeExpToNextLevel}`
};

const clickedCell = async (x, y) => {
    if (!mineActionSubmitting) {
        mineActionSubmitting = true;

        const res = await postPerformMine(x, y);

        if (res.status === "success") {
            /**
             * @type {MineActionResult}
             */
            const data = res.data;

            await updateMiningLevel();

            for (const { item: { friendlyName, imgURL, quantity }, expPerIndividualItem } of data.minedItemResults) {
                const li = document.createElement("li");
                if (quantity !== null) {
                    li.insertAdjacentHTML("beforeend", `Obtained <img src="${imgURL}"> [${friendlyName}] ×${quantity} (+${expPerIndividualItem * quantity} exp)`);
                } else {
                    li.insertAdjacentHTML("beforeend", `Obtained ${friendlyName} (+${expPerIndividualItem} exp)`);
                }

                appendListItemToResultsList(li);
            }

            for (const { newLevel, additionalMessage } of data.levelUps) {
                const li = document.createElement("li");
                li.className = "mining-results-level-up";
                li.textContent = `Mining levelled up to level ${newLevel}!` + (additionalMessage !== null ? ` ${additionalMessage}` : "");

                appendListItemToResultsList(li);
            }

            const affectedCells = reachableCells(x, y, mineInfo.width, mineInfo.height, equippedPickaxe.cells);

            for (const cell of affectedCells) {
                const [x, y] = cell;
                mineInfo.cells[y][x].removeAttribute("style");
            }

            mineActionSubmitting = false;
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
    button.id = EXIT_MINE_BUTTON_ID;
    button.className = "fancy";
    button.innerText = "Exit mine";
    button.style.alignSelf = "center";
    button.onclick = () => exitMine();

    return button;
};

const exitMine = async() => {
    const exitMineButton = document.getElementById(EXIT_MINE_BUTTON_ID);
    exitMineButton.innerText += " (Loading…)";
    exitMineButton.disabled = true;
    exitMineButton.classList.add("loading");

    const res = await postMineExit();

    if (res.status === "success") {
        // Remove mining grid
        const miningGrid = document.getElementById(MINING_GRID_ID);
        miningGrid.parentNode.removeChild(miningGrid);

        // Replace button with success message
        const message = document.createElement("div");
        message.innerText = "Successfully exited mine";
        message.style.display = "block";

        exitMineButton.parentNode.replaceChild(message, exitMineButton);
    } else {
        //TODO: Display error
    }
};

/**
 * @param {Mine} mine
 * @returns {HTMLTableElement}
 */
const createMiningGrid = (mine) => {
    const table = document.createElement("table");
    table.id = MINING_GRID_ID;
    table.classList.add(MINING_GRID_ID);
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
    const main = document.getElementById("main");

    const res = await getPickaxe();

    if (res.status === "success") {
        /**
         * @type {?Pickaxe}
         */
        const data = res.data;

        if (data !== null) {
            equippedPickaxe = data;

            /**
             * @type {UserDetails}
             */
            const userDetails = (await getUserDetails()).data;

            const { level, relativeExp, relativeExpToNextLevel } = userDetails.mining;

            main.insertAdjacentHTML("beforeend",
                `<div class="level-info" style="margin-left: auto; margin-right: auto;">` +
                    `<pbbg-level-progress id="${MINING_LEVEL_PROGRESS_ID}" level="${level}" value="${relativeExp}" max="${relativeExpToNextLevel}"></pbbg-level-progress>` +
                    `<span id="${MINING_LEVEL_TEXT_ID}">Lv. ${level} — ${relativeExp} / ${relativeExpToNextLevel}</span>` +
                `</div>` +
                `<div>Equipped pickaxe: ${equippedPickaxe.pickaxeKind}</div>` +
                `<ul id="${MINING_RESULTS_LIST_ID}" class="${MINING_RESULTS_LIST_ID}"></ul>`
            );

            mineInfo.cells.forEach((row, y) => {
                row.forEach((cell, x) => {
                    cell.onmouseenter = () => { enteredCell(x, y) };
                    cell.onmouseleave = () => { leftCell(x, y) };
                    cell.onclick = () => { clickedCell(x, y) };
                });
            });

            // Display mine grid as enabled.
            document.getElementById(MINING_GRID_ID).classList.add("enabled");
        } else {
            main.insertAdjacentHTML("beforeend", `<div>No pickaxe equipped. Go to your inventory and equip one.</div>`);
        }
    }
};

/**
 * @param {HTMLLIElement} li
 */
const appendListItemToResultsList = (li) => {
    const list = document.getElementById(MINING_RESULTS_LIST_ID);

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
