/**
 * @typedef {Object} Squad
 *
 * @property {MyUnit[]} units
 */

/**
 * @typedef {Object} MyUnit
 *
 * @property {number} id
 * @property {string} name
 * @property {number} baseUnitId
 * @property {number} hp
 * @property {number} maxHP
 * @property {number} atk
 * @property {LevelProgress} levelProgress
 * @property {string} idleAnimationURL
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
