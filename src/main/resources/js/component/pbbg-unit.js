import "/js/component/pbbg-level-progress.js";
import "/js/component/pbbg-progress-bar.js";

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
 * @property {string} idleAnimationURL - Unit's idle animation path.
 */

class PBBGUnit extends HTMLElement {
    constructor() {
        super();

        this._shadowRoot = this.attachShadow({ mode: "open" });

        this._shadowRoot.innerHTML =
`
<style>
:host {
    display: inline-flex;
    flex-direction: row;
    width: 22em;
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

#sprite {
    margin-right: 12px;
}

:host([facing="left"]) #sprite {
    transform: scaleX(-1); 
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
<img id="sprite" src="" alt="Idle unit animation" width="64" height="64">
<div>
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
        <pbbg-level-progress id="exp-bar"></pbbg-level-progress>
        <span id="exp-value"></span>
    </div>
</div>
`;

        /**
         * @type {Unit}
         * @private
         */
        this._unit = undefined;
    }

    connectedCallback() {
        // In case property was set before this element could be upgraded
        if (this.hasOwnProperty("unit")) {
            const value = this["unit"];
            delete this["unit"];
            this.unit = value;
        }
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
        if (!this._unit) {
            const bar = document.createElement("pbbg-level-progress");
            bar.setAttribute("id", "exp-bar");
            bar.setAttribute("level", value.levelProgress.level.toString());
            bar.setAttribute("max", value.levelProgress.relativeExpToNextLevel.toString());
            bar.setAttribute("value", value.levelProgress.relativeExp.toString());
            this._shadowRoot.getElementById("exp-bar").replaceWith(bar);
        }
        this._unit = value;
        this.updateDisplay();
    }

    /**
     * @returns {number}
     */
    get unitId() {
        return Number(this.getAttribute("unit-id"));
    }

    /**
     * Direction the sprite should be facing.
     *
     * @returns {("left"|"right")}
     */
    get facing() {
        if (this.getAttribute("facing") === "left") {
            return "left";
        } else {
            return "right";
        }
    }

    updateDisplay() {
        this._shadowRoot.getElementById("sprite").src = this._unit.idleAnimationURL;
        this._shadowRoot.getElementById("name").innerText = this._unit.name;
        this._shadowRoot.getElementById("hp-bar").value = this._unit.hp;
        this._shadowRoot.getElementById("hp-bar").max = this._unit.maxHP;
        this._shadowRoot.getElementById("hp-value").innerText = `${this._unit.hp} / ${this._unit.maxHP}`;
        this._shadowRoot.getElementById("atk-value").innerText = this._unit.atk.toString();
        this._shadowRoot.getElementById("level-value").innerText = this._unit.levelProgress.level.toString();
        this._shadowRoot.getElementById("exp-bar").updateProgress({
            level: this._unit.levelProgress.level,
            max: this._unit.levelProgress.relativeExpToNextLevel,
            value: this._unit.levelProgress.relativeExp
        });
        this._shadowRoot.getElementById("exp-value").innerText = `${this._unit.levelProgress.relativeExp} / ${this._unit.levelProgress.relativeExpToNextLevel}`;

        if (this._unit.hp === 0) {
            this.setAttribute("dead", "");
        } else {
            this.removeAttribute("dead");
        }
    }
}

window.customElements.define("pbbg-unit", PBBGUnit);
