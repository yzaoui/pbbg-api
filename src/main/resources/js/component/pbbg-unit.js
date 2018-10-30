window.customElements.define("pbbg-unit", class Unit extends HTMLElement {
    constructor() {
        super();

        this._shadowRoot = this.attachShadow({ mode: "open" });

        this._shadowRoot.innerHTML = `
<style>
:host {
    display: inline-block;
    width: 18em;
    height: 5em;
    border: 1px solid black;
}

:host([hidden]) {
    display: none;
}
</style>
<span>HP: </span>
<pbbg-progress-bar id="hp-bar"></pbbg-progress-bar>
<span id="hp-value"></span>
<br>
<span>ATK: </span>
<span id="atk-value"></span>
`;

        this._unit = null;
    }

    get unit() {
        return this._unit;
    }

    set unit(value) {
        this._unit = value;
        this.updateDisplay();
    }

    get unitId() {
        return Number(this.getAttribute("unit-id"));
    }

    updateDisplay() {
        this._shadowRoot.getElementById("hp-bar").value = this._unit.hp;
        this._shadowRoot.getElementById("hp-bar").max = this._unit.maxHP;
        this._shadowRoot.getElementById("hp-value").innerText = `${this._unit.hp} / ${this._unit.maxHP}`;
        this._shadowRoot.getElementById("atk-value").innerText = this._unit.atk;
    }
});
