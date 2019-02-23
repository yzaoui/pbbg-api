const template = document.createElement("template");
template.innerHTML = `
<style>
:host {
    display: inline-block;
}

table {
    border-collapse: collapse;
}

table, td {
  border: 1px solid black;
}

td {
    width: 16px;
    height: 16px;
    border: 1px solid black;
}

td.active {
    background-color: green;
}

#inner {
    height: 100%;
    background: var(--fill-color, linear-gradient(#6de1ff, #00789c));
}
</style>
<table>
    <tbody>
        
    </tbody>
</table>
`;

class PBBGGridPreview extends HTMLElement {
    constructor() {
        super();

        this._shadowRoot = this.attachShadow({ mode: "open" });
        this._shadowRoot.appendChild(template.content.cloneNode(true));

        /**
         * @type {Object}
         * @private
         */
        this._grid = [];
    }

    connectedCallback() {
        // Upgrade property in case it was set before class definition
        if (this.hasOwnProperty("grid")) {
            const grid = this["grid"];
            delete this["grid"];
            this.grid = grid;
        }
    }

    set grid(val) {
        this._grid = val;
        const tbody = this._shadowRoot.querySelector("tbody");
        tbody.innerHTML = "";

        for (let row = 0; row < 3; row++) {
            const tr = document.createElement("tr");
            for (let col = 0; col < 3; col++) {
                const cellClass = val.some(cell => (cell.x === col - 1) && (cell.y === row - 1)) ? "active" : "inactive";

                tr.insertAdjacentHTML("beforeend", `<td class="${cellClass}"></td>`);
            }
            tbody.appendChild(tr);
        }
    }

    get grid() {
        return this._grid;
    }
}

window.customElements.define("pbbg-grid-preview", PBBGGridPreview);
