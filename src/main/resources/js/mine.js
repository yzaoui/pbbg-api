let grid;
const GRID_WIDTH = 30;
const GRID_HEIGHT = 20;
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
        const mine = createMiningGrid("mining-grid", data);
        main.appendChild(mine);

        setupPickaxeAndResultsList();
    } else {
        main.appendChild(generateMineButton());
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

        const resultsList = document.getElementById("results-list");

        results.forEach(({ item: { friendlyName, quantity }, expPerIndividualItem}) => {
            const li = document.createElement("li");
            if (quantity !== null) {
                li.textContent = `Obtained ${friendlyName} ×${quantity} (+${expPerIndividualItem * quantity} exp)`;
            } else {
                li.textContent = `Obtained ${friendlyName} (+${expPerIndividualItem} exp)`;
            }

            resultsList.appendChild(li);
        });

        const affectedCells = reachableCells(x, y, GRID_WIDTH, GRID_HEIGHT, equippedPickaxe.cells);

        affectedCells.forEach(cell => {
            const [x, y] = cell;
            grid[y][x].style = "";
        });

        mineActionSubmitting = false;
    }
};

const generateMineButton = () => {
    const button = document.createElement("button");
    button.id = "generate-mine";
    button.innerText = "Generate new mine";
    button.onclick = () => generateMine();

    return button;
};

const generateMine = async () => {
    /* Replace button with loading message */
    const generateMineButton = document.getElementById("generate-mine");
    const statusMessage = document.createElement("div");
    statusMessage.innerText = "Loading...";
    generateMineButton.parentNode.replaceChild(statusMessage, generateMineButton);

    /* Get mine from API */
    const { status, data } = await (await fetch("/api/mine/generate", { method: "POST" })).json();
    if (status === "success") {
        const mine = createMiningGrid("mining-grid", data);
        statusMessage.parentNode.replaceChild(mine, statusMessage);
    } else {
        statusMessage.innerText = "Error occurred. Try refreshing."
    }

    setupPickaxeAndResultsList();
};

const createMiningGrid = (id, { width, height, cells }) => {
    const table = document.createElement("table");
    table.id = id;
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
        resultsList.id = "results-list";
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
