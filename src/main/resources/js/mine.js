let grid;
const GRID_WIDTH = 20;
const GRID_HEIGHT = 20;
let equippedPickaxe;

window.onload = async () => {
    grid = [...document.getElementById("mining-grid").firstElementChild.children].map(row => [...row.children]);

    const res = await fetch("/api/pickaxe");
    equippedPickaxe = await res.json();

    document.getElementById("equipped-pickaxe").innerText = equippedPickaxe.type;

    grid.forEach((row, y) => {
        row.forEach((cell, x) => {
            cell.onmouseenter = () => {enteredCell(x, y)};
            cell.onmouseleave = () => {leftCell(x, y)};
        });
    });
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
    const affectedCells = reachableCells(x, y, GRID_WIDTH, GRID_HEIGHT, equippedPickaxe.tiles);

    affectedCells.forEach(cell => {
        const [x, y] = cell;
        grid[y][x].classList.add("selected-item");
    })
};

const leftCell = (x, y) => {
    const affectedCells = reachableCells(x, y, GRID_WIDTH, GRID_HEIGHT, equippedPickaxe.tiles);

    affectedCells.forEach(cell => {
        const [x, y] = cell;
        grid[y][x].classList.remove("selected-item");
    })
};
