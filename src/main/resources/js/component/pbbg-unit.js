/**
 * @typedef {Object} Unit
 * @property {string} name - Unit name.
 * @property {number} hp - Current HP.
 * @property {number} maxHP - Maximum HP.
 * @property {number} atk - Current ATK.
 */

class PBBGUnit extends HTMLElement {
    constructor() {
        super();

        this._shadowRoot = this.attachShadow({ mode: "open" });

        this._shadowRoot.innerHTML =
`
<style>
:host {
    display: inline-block;
    width: 18em;
    height: 5em;
    box-sizing: border-box;
    border: 1px solid #333333;
    padding: 6px;
}

:host([hidden]) {
    display: none;
}

#hp-bar {
    width: 120px;
    height: 11px;
}
</style>
<div>
    <span id="name"></span>
</div>
<div>
    <span>HP: </span>
    <pbbg-progress-bar id="hp-bar"></pbbg-progress-bar>
    <span id="hp-value"></span>
</div>
<div>
    <span>ATK: <span id="atk-value"></span></span>
</div>
`;

        /**
         * @type {Unit}
         * @private
         */
        this._unit = undefined;
    }

    /**
     * @returns {Unit}
     */
    get unit() {
        return this._unit;
    }

    /**
     * @param {Unit} value
     */
    set unit(value) {
        this._unit = value;
        this.updateDisplay();
    }

    /**
     * @returns {number}
     */
    get unitId() {
        return Number(this.getAttribute("unit-id"));
    }

    updateDisplay() {
        this._shadowRoot.getElementById("name").innerText = this._unit.name;
        this._shadowRoot.getElementById("hp-bar").value = this._unit.hp;
        this._shadowRoot.getElementById("hp-bar").max = this._unit.maxHP;
        this._shadowRoot.getElementById("hp-value").innerText = `${this._unit.hp} / ${this._unit.maxHP}`;
        this._shadowRoot.getElementById("atk-value").innerText = this._unit.atk.toString();
    }
}

window.customElements.define("pbbg-unit", PBBGUnit);
