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

window.onload = async () => {
    insertScript("/js/webcomponents-bundle-2.0.0.js");
    insertModule("/js/component/pbbg-unit.js");

    replaceInterfaceWithText("Loading…");

    const res = await (await fetch("/api/squad")).json();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {Squad}
         */
        const data = res.data;

        for (const unit of data.units) {
            const el = document.createElement("pbbg-unit");
            el.unit = {
                name: unit.name,
                hp: unit.hp,
                maxHP: unit.maxHP,
                atk: unit.atk,
                levelProgress: unit.levelProgress,
                idleAnimationURL: unit.idleAnimationURL
            };
            main.appendChild(el);
        }
    } else {
        replaceInterfaceWithText("Error loading squad.");
    }
};
