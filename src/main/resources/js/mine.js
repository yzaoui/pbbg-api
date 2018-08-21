let grid;
const GRID_WIDTH = 30;
const GRID_HEIGHT = 20;
let equippedPickaxe;
let mineActionSubmitting = false;

window.onload = async () => {
    grid = [...document.getElementById("mining-grid").firstElementChild.children].map(row => [...row.children]);

    const {status, data}  = await (await fetch("/api/pickaxe")).json();
    equippedPickaxe = data;

    if (equippedPickaxe !== null) {
        document.getElementById("equipped-pickaxe").innerText = equippedPickaxe.type;

        grid.forEach((row, y) => {
            row.forEach((cell, x) => {
                cell.onmouseenter = () => { enteredCell(x, y) };
                cell.onmouseleave = () => { leftCell(x, y) };
                cell.onclick = () => { clickedCell(x, y) };
            });
        });
    } else {
        document.getElementById("equipped-pickaxe").innerText = "None. Go to your inventory and generate one."
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

        const {status, data} = await (await fetch("/api/mine", {
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

        data.results.forEach(result => {
            const li = document.createElement("li");
            li.textContent = `Obtained ${result.item} ×${result.amount}`;
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
