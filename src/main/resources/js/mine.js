let grid;
const GRID_WIDTH = 30;
const GRID_HEIGHT = 20;
const MINING_GRID_ID = "mining-grid";
const GENERATE_MINE_INTERFACE_ID = "generate-mine-container";
const EXIT_MINE_BUTTON_ID = "exit-mine";
const MINING_RESULTS_LIST_ID = "mining-results-list";
let equippedPickaxe;
let mineActionSubmitting = false;

window.onload = async () => {
    const main = document.getElementById("main");

    const statusMessage = document.createElement("div");
    statusMessage.innerText = "Loading...";
    main.appendChild(statusMessage);

    const { status, data } = await (await fetch("/api/mine")).json();

    statusMessage.parentNode.removeChild(statusMessage);

    if (data !== null) {
        // Currently in mine session
        setupMiningInterface(data);
    } else {
        // No mine session in progress
        setupGenerateMineInterface();
    }
};

const setupMiningInterface = (miningData) => {
    const main = document.getElementById("main");

    const exitMineButton = createExitMineButton();
    main.appendChild(exitMineButton);

    const mine = createMiningGrid(miningData);
    main.appendChild(mine);

    setupPickaxeAndResultsList();
};

const setupGenerateMineInterface = async () => {
    const main = document.getElementById("main");

    const table = document.createElement("table");
    table.id = GENERATE_MINE_INTERFACE_ID;
    table.className = "mining-mine-info";
    table.innerText = "Loading list of mines...";
    main.appendChild(table);

    const mineTypesRequest = fetch("/api/mine/types");
    const userStatsRequest = fetch("/api/user");

    const mineTypesResponse = await (await mineTypesRequest).json();
    const userStatsResponse = await (await userStatsRequest).json();

    if (mineTypesResponse.status === "success" && userStatsResponse.status === "success") {
        const mineTypes = mineTypesResponse.data.types;
        const userMiningStats = userStatsResponse.data.mining;

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

        for (let i = 0; i < mineTypes.length; i++) {
            const mineType = mineTypes[i];

            const tr = document.createElement("tr");
            tbody.appendChild(tr);

            const nameTd = document.createElement("td");
            nameTd.innerText = mineType.name;
            tr.appendChild(nameTd);

            const minimumLvlTd = document.createElement("td");
            minimumLvlTd.innerText = mineType.minLevel;
            tr.appendChild(minimumLvlTd);

            const button = document.createElement("button");
            button.className = "mining-generate-mine";
            button.innerText = "Generate";
            button.onclick = () => generateMine(mineType.id);
            const generateTd = document.createElement("td");
            generateTd.appendChild(button);
            tr.appendChild(generateTd);

            if (userMiningStats.level < mineType.minLevel) {
                tr.classList.add("unmet-minimum-level");
                button.disabled = true;
            }
        }
    }
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
    const affectedCells = reachableCells(x, y, GRID_WIDTH, GRID_HEIGHT, equippedPickaxe.cells);

    affectedCells.forEach(cell => {
        const [x, y] = cell;
        grid[y][x].classList.add("selected-item");
    })
};

const leftCell = (x, y) => {
    const affectedCells = reachableCells(x, y, GRID_WIDTH, GRID_HEIGHT, equippedPickaxe.cells);

    affectedCells.forEach(cell => {
        const [x, y] = cell;
        grid[y][x].classList.remove("selected-item");
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

        const affectedCells = reachableCells(x, y, GRID_WIDTH, GRID_HEIGHT, equippedPickaxe.cells);

        affectedCells.forEach(cell => {
            const [x, y] = cell;
            grid[y][x].style = "";
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

    const { status, data } = await (await fetch("/api/mine/generate", {
        method: "POST",
        headers: {
            "Content-Type": "application/json; charset=utf-8"
        },
        body: JSON.stringify({
            mineTypeId: mineTypeId
        })
    })).json();

    if (status === "success") {
        mineListContainer.parentNode.removeChild(mineListContainer);

        setupMiningInterface(data);
    } else {
        //TODO: Display error
    }
};

const createExitMineButton = () => {
    const button = document.createElement("button");
    button.id = EXIT_MINE_BUTTON_ID;
    button.className = "mining-exit-mine";
    button.innerText = "Exit mine";
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

const createMiningGrid = ({ width, height, cells }) => {
    const table = document.createElement("table");
    table.id = MINING_GRID_ID;
    table.classList.add("mining-grid");
    const tbody = document.createElement("tbody");
    table.appendChild(tbody);
    cells.forEach(row => {
        const tr = document.createElement("tr");
        row.forEach(cell => {
            const td = document.createElement("td");
            if (cell !== null) {
                td.style.backgroundImage = `url("${cell.imageURL}")`
            }
            tr.appendChild(td);
        });
        tbody.appendChild(tr);
    });

    return table;
};

const createEquippedPickaxeDisplay = (pickaxeName) => {
    const div = document.createElement("div");
    div.innerText = "Equipped pickaxe: " + pickaxeName;

    return div;
};

const setupPickaxeAndResultsList = async () => {
    const { status, data } = await (await fetch("/api/pickaxe")).json();
    const main = document.getElementById("main");

    if (data !== null) {
        const miningGrid = document.getElementById("mining-grid");
        equippedPickaxe = data;

        main.appendChild(createEquippedPickaxeDisplay(equippedPickaxe.pickaxeKind));

        const resultsList = document.createElement("ul");
        resultsList.id = MINING_RESULTS_LIST_ID;
        resultsList.className = "mining-results-list";
        main.appendChild(resultsList);

        grid = [...miningGrid.firstElementChild.children].map(row => [...row.children]);

        grid.forEach((row, y) => {
            row.forEach((cell, x) => {
                cell.onmouseenter = () => { enteredCell(x, y) };
                cell.onmouseleave = () => { leftCell(x, y) };
                cell.onclick = () => { clickedCell(x, y) };
            });
        });

        miningGrid.classList.add("enabled");
    } else {
        const noPickaxe = document.createElement("div");
        noPickaxe.innerText = "No pickaxe equipped. Go to your inventory and generate one.";
        main.appendChild(noPickaxe);
    }
};

const appendListItemToResultsList = (li) => {
    const list = document.getElementById(MINING_RESULTS_LIST_ID);

    let needToScroll = (list.scrollTop + list.clientHeight) === list.scrollHeight;

    list.appendChild(li);

    if (needToScroll) {
        list.scrollTop = list.scrollHeight;
    }
};
