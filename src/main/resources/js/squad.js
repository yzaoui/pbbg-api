/**
 * @typedef {Object} Squad
 *
 * @property {MyUnit[]} units
 */

/**
 * @typedef {Object} MyUnit
 *
 * @property {number} id - The unit's ID.
 * @property {string} name - The unit's name.
 * @property {number} baseUnitId - The ID of the unit's base unit type.
 * @property {number} hp - The unit's current HP.
 * @property {number} maxHP - The unit's maximum HP.
 * @property {number} atk - The unit's attack stat.
 * @property {number} def - The unit's defence stat.
 * @property {LevelProgress} levelProgress - The unit's level and experience information.
 * @property {string} idleAnimationURL - The unit's idle animation URL.
 * @property {string} iconURL - The unit's icon URL.
 */

const main = document.getElementById("main");

const healButton = {
    /**
     * @type {?HTMLButtonElement}
     */
    DOM: null,
    /**
     * @param {boolean} val
     */
    set loading(val) {
        this.DOM.innerText = `Heal Squad${val ? " (Loading…)" : ""}`;
        this.DOM.disabled = val;
        val ? this.DOM.classList.add("loading") : this.DOM.classList.remove("loading");
    }
};

window.onload = async () => {
    insertScript("/js/webcomponents-bundle-2.0.0.js");
    insertModule("/js/component/pbbg-unit.js");

    await window.customElements.whenDefined("pbbg-unit");

    replaceInterfaceWithText("Loading…");

    const res = await getSquad();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {Squad}
         */
        const data = res.data;

        setupHealSquad();

        for (const unit of data.units) {
            const el = document.createElement("pbbg-unit");
            el.setAttribute("unit-id", String(unit.id));
            el.unit = unit;
            main.appendChild(el);
        }
    } else {
        replaceInterfaceWithText("Error loading squad.");
    }
};

const setupHealSquad = () => {
    const btn = document.createElement("button");

    btn.innerText = "Heal Squad";
    btn.classList.add("fancy");
    btn.style.alignSelf = "center";
    btn.onclick = () => healSquad();

    healButton.DOM = btn;

    main.appendChild(btn);
};

const healSquad = async () => {
    healButton.loading = true;

    const res = await postHeal();

    healButton.loading = false;

    if (res.status === "success") {
        /**
         * @type {Squad}
         */
        const data = res.data;

        // TODO: Handle case where unit elements and unit response don't match
        for (const unitEl of Array.from(document.querySelectorAll("pbbg-unit"))) {
            unitEl.unit = data.units.find(unit => unit.id === unitEl.unitId);
        }
    } else if (res.status === "fail") {
        replaceInterfaceWithText(`FAIL: ${res.data}`);
    } else {
        replaceInterfaceWithText("ERROR");
    }
};

const getSquad = async() => (await fetch("/api/squad")).json();

const postHeal = async () => (await fetch("/api/squad/heal", {
    method: "POST",
    headers: {
        "Content-Type": "application/json; charset=utf-8"
    }
})).json();
