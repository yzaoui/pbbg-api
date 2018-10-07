let grid;
const GRID_WIDTH = 30;
const GRID_HEIGHT = 20;
const MINING_GRID_ID = "mining-grid";
const GENERATE_MINE_BUTTON_ID = "generate-mine";
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
        setupMiningInterface(data);
    } else {
        main.appendChild(createGenerateMineButton());
    }
};

const setupMiningInterface = (miningData) => {
    const main = document.getElementById("main");

    const mine = createMiningGrid(miningData);
    main.appendChild(mine);

    const exitMineButton = createExitMineButton();
    main.appendChild(exitMineButton);

    setupPickaxeAndResultsList();
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

        const {status, data: results} = await (await fetch("/api/mine", {
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

        levelUps.forEach(({ newLevel }) => {
            const li = document.createElement("li");
            li.className = "mining-results-level-up";
            li.textContent = `Mining levelled up to level ${newLevel}!`;

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

const createGenerateMineButton = () => {
    const button = document.createElement("button");
    button.id = GENERATE_MINE_BUTTON_ID;
    button.innerText = "Generate new mine";
    button.onclick = () => generateMine();

    return button;
};

const generateMine = async () => {
    /* Replace button with loading message */
    const generateMineButton = document.getElementById(GENERATE_MINE_BUTTON_ID);
    generateMineButton.innerText += " (Loading...)";
    generateMineButton.disabled = true;
    generateMineButton.classList.add("loading");

    /* Get mine from API */
    const { status, data } = await (await fetch("/api/mine/generate", { method: "POST" })).json();
    if (status === "success") {
        generateMineButton.parentNode.removeChild(generateMineButton);

        setupMiningInterface(data);
    } else {
        //TODO: Display error
    }
};

const createExitMineButton = () => {
    const button = document.createElement("button");
    button.id = EXIT_MINE_BUTTON_ID;
    button.className = "mining-exit-mine-button";
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
