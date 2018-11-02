/**
 * @typedef {Object} LevelProgress
 * @property {number} level - Current level.
 * @property {number} relativeExp - Experience since the start of this level.
 * @property {number} relativeExpToNextLevel - Experience required to level up since the start of this level.
 */

/**
 * @typedef {Object} Unit
 * @property {string} name - Unit name.
 * @property {number} hp - Current HP.
 * @property {number} maxHP - Maximum HP.
 * @property {number} atk - Current ATK.
 * @property {LevelProgress} levelProgress - Unit's level and experience information.
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
    height: 5.3em;
    box-sizing: border-box;
    border: 1px solid #333333;
    padding: 6px;
}

:host([hidden]) {
    display: none;
}

:host([dead]) {
    background-color: #4e313114;
}

#name {
    font-weight: bold;
}

#hp-bar {
    width: 120px;
    height: 11px;
    --bar-background-color: linear-gradient(#53ff52, #0f9c16);
}

#exp-bar {
    width: 160px;
    height: 8px;
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
<div>
    <span>Level <span id="level-value"></span></span>
    <pbbg-progress-bar id="exp-bar"></pbbg-progress-bar>
    <span id="exp-value"></span>
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
        this._shadowRoot.getElementById("level-value").innerText = this._unit.levelProgress.level.toString();
        this._shadowRoot.getElementById("exp-bar").value = this._unit.levelProgress.relativeExp;
        this._shadowRoot.getElementById("exp-bar").max = this._unit.levelProgress.relativeExpToNextLevel;
        this._shadowRoot.getElementById("exp-value").innerText = `${this._unit.levelProgress.relativeExp} / ${this._unit.levelProgress.relativeExpToNextLevel}`;

        if (this._unit.hp === 0) {
            this.setAttribute("dead", "");
        }
    }
}

window.customElements.define("pbbg-unit", PBBGUnit);
