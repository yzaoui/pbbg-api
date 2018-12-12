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
 * @property {number} width
 * @property {number} height
 * @property {HTMLTableDataCellElement[][]} cells
 */
let mineInfo;

const MINING_GRID_ID = "mining-grid";
const GENERATE_MINE_INTERFACE_ID = "generate-mine-container";
const EXIT_MINE_BUTTON_ID = "exit-mine";
const MINING_RESULTS_LIST_ID = "mining-results-list";
/**
 * @type {Pickaxe}
 */
let equippedPickaxe;

/**
 * @type {boolean}
 */
let mineActionSubmitting = false;

window.onload = async () => {
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
 * On success, returns {@see Mine} or null.
 */
const getMine = async () => (await fetch("/api/mine")).json();

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
    table.id = GENERATE_MINE_INTERFACE_ID;
    table.className = "mining-mine-info";
    table.innerText = "Loading list of mines...";
    main.appendChild(table);

    const res = await getMineTypes();

    if (res.status === "success") {
        /**
         * @typedef {MineTypeList}
         */
        const data = res.data;

        table.innerText = "";

        const thead = document.createElement("thead");
        table.appendChild(thead);

        const headerRow = document.createElement("tr");
        thead.appendChild(headerRow);

        const nameHeader = document.createElement("th");
        nameHeader.innerText = "Mine name";
        headerRow.appendChild(nameHeader);

        const minLevelHeader = document.createElement("th");
        minLevelHeader.innerText = "Minimum lvl.";
        headerRow.appendChild(minLevelHeader);

        const generateHeader = document.createElement("th");
        generateHeader.innerText = "Generate mine";
        headerRow.appendChild(generateHeader);

        const tbody = document.createElement("tbody");
        table.appendChild(tbody);

        data.types.forEach(mine => {
            tbody.appendChild(createAvailableMineRow(mine));
        });

        if (data.nextUnlockLevel !== null) {
            tbody.appendChild(createMineToUnlockRow(data.nextUnlockLevel))
        }
    }
};

/**
 * On success, returns {@see MineTypeList}.
 */
const getMineTypes = async () => (await fetch("/api/mine/types")).json();

const createAvailableMineRow = ({ id, name, minLevel }) => {
    const tr = document.createElement("tr");

    const nameTd = document.createElement("td");
    nameTd.innerText = name;
    tr.appendChild(nameTd);

    const minimumLvlTd = document.createElement("td");
    minimumLvlTd.innerText = minLevel;
    tr.appendChild(minimumLvlTd);

    const button = document.createElement("button");
    button.className = "mining-generate-mine";
    button.innerText = "Generate";
    button.onclick = () => generateMine(id);
    const generateTd = document.createElement("td");
    generateTd.appendChild(button);
    tr.appendChild(generateTd);

    return tr;
};

const createMineToUnlockRow = (nextUnlockLevel) => {
    const tr = document.createElement("tr");
    tr.className = "unmet-minimum-level";

    const nameTd = document.createElement("td");
    nameTd.innerText = "???";
    tr.appendChild(nameTd);

    const minimumLvlTd = document.createElement("td");
    minimumLvlTd.innerText = nextUnlockLevel;
    tr.appendChild(minimumLvlTd);

    const button = document.createElement("button");
    button.className = "mining-generate-mine";
    button.innerText = "Need to unlock";
    button.disabled = true;
    const generateTd = document.createElement("td");
    generateTd.appendChild(button);
    tr.appendChild(generateTd);

    return tr;
};

const reachableCells = (x, y, gridWidth, gridHeight, targets) => {
    let cells = [];

    targets.forEach(target => {
        const newX = x + target[0];
        const newY = y + target[1];

        if (newX >= 0 && newX < gridWidth && newY >= 0 && newY < gridHeight) {
            cells.push([newX, newY]);
        }
    });

    return cells;
};

const enteredCell = (x, y) => {
    const affectedCells = reachableCells(x, y, mineInfo.width, mineInfo.height, equippedPickaxe.cells);

    affectedCells.forEach(cell => {
        const [x, y] = cell;
        mineInfo.cells[y][x].classList.add("selected-item");
    })
};

const leftCell = (x, y) => {
    const affectedCells = reachableCells(x, y, mineInfo.width, mineInfo.height, equippedPickaxe.cells);

    affectedCells.forEach(cell => {
        const [x, y] = cell;
        mineInfo.cells[y][x].classList.remove("selected-item");
    })
};

const clickedCell = async (x, y) => {
    if (!mineActionSubmitting) {
        mineActionSubmitting = true;

        const {status, data: results} = await (await fetch("/api/mine/perform", {
            method: "POST",
            headers: {
                "Content-Type": "application/json; charset=utf-8"
            },
            body: JSON.stringify({
                x: x,
                y: y
            })
        })).json();

        const { minedItemResults, levelUps } = results;

        minedItemResults.forEach(({ item: { friendlyName, imgURL, quantity }, expPerIndividualItem}) => {
            const li = document.createElement("li");
            if (quantity !== null) {
                li.appendChild(document.createTextNode("Obtained "));

                const itemImg = document.createElement("img");
                itemImg.src = imgURL;
                li.appendChild(itemImg);

                li.appendChild(document.createTextNode(` [${friendlyName}] ×${quantity} (+${expPerIndividualItem * quantity} exp)`));
            } else {
                li.textContent = `Obtained ${friendlyName} (+${expPerIndividualItem} exp)`;
            }

            appendListItemToResultsList(li);
        });

        levelUps.forEach(({ newLevel, additionalMessage }) => {
            const li = document.createElement("li");
            li.className = "mining-results-level-up";
            li.textContent = `Mining levelled up to level ${newLevel}!` + (additionalMessage !== null ? ` ${additionalMessage}` : "");

            appendListItemToResultsList(li);
        });

        const affectedCells = reachableCells(x, y, mineInfo.width, mineInfo.height, equippedPickaxe.cells);

        affectedCells.forEach(cell => {
            const [x, y] = cell;
            mineInfo.cells[y][x].removeAttribute("style");
        });

        mineActionSubmitting = false;
    }
};

const generateMine = async (mineTypeId) => {
    const mineListContainer = document.getElementById(GENERATE_MINE_INTERFACE_ID);

    while (mineListContainer.hasChildNodes()) {
        mineListContainer.removeChild(mineListContainer.firstChild);
    }

    mineListContainer.innerText = "Loading mine...";

    const { status, data } = await postGenerateMine(mineTypeId);

    if (status === "success") {
        mineListContainer.parentNode.removeChild(mineListContainer);

        setupMiningInterface(data);
    } else {
        //TODO: Display error
    }
};

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
    exitMineButton.innerText += " (Loading...)";
    exitMineButton.disabled = true;
    exitMineButton.classList.add("loading");

    const { status, data } = await (await fetch("/api/mine/exit", { method: "POST" })).json();
    if (status === "success") {
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

/**
 * @param {string} pickaxeName
 * @returns {HTMLDivElement}
 */
const createEquippedPickaxeDisplay = (pickaxeName) => {
    const div = document.createElement("div");
    div.innerText = `Equipped pickaxe: ${pickaxeName}`;

    return div;
};

const setupPickaxeAndResultsList = async () => {
    const res = await getPickaxe();

    const main = document.getElementById("main");

    if (res.status === "success") {
        /**
         * @type {?Pickaxe}
         */
        const data = res.data;

        if (data !== null) {
            equippedPickaxe = data;

            main.appendChild(createEquippedPickaxeDisplay(equippedPickaxe.pickaxeKind));

            const resultsList = document.createElement("ul");
            resultsList.id = MINING_RESULTS_LIST_ID;
            resultsList.className = "mining-results-list";
            main.appendChild(resultsList);

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
            const noPickaxe = document.createElement("div");
            noPickaxe.innerText = "No pickaxe equipped. Go to your inventory and equip one.";
            main.appendChild(noPickaxe);
        }
    }
};

/**
 * On success, returns {@see ?Pickaxe}
 */
const getPickaxe = async () => (await fetch("/api/pickaxe")).json();

const appendListItemToResultsList = (li) => {
    const list = document.getElementById(MINING_RESULTS_LIST_ID);

    let needToScroll = (list.scrollTop + list.clientHeight) === list.scrollHeight;

    list.appendChild(li);

    if (needToScroll) {
        list.scrollTop = list.scrollHeight;
    }
};
