window.onload = async () => {
    insertScript("/js/component/pbbg-unit.js");

    replaceInterfaceWithText("Loading…");

    const { status, data } = await (await fetch("/api/squad")).json();

    if (status === "success") {
        replaceInterfaceWithText("");

        const { units } = data;

        units.forEach(unit => {
            const el = document.createElement("pbbg-unit");
            el.unit = unit;
            main.appendChild(el);
        });
    } else {
        replaceInterfaceWithText("Error loading squad.");
    }
};

/**
 * @param {string} text
 */
const replaceInterfaceWithText = (text) => {
    const main = document.getElementById("main");

    while (main.hasChildNodes()) {
        main.removeChild(main.firstChild);
    }

    main.innerText = text;
};

/**
 * @param {string} scriptSrc
 */
const insertScript = (scriptSrc) => {
    const unitScript = document.createElement("script");
    unitScript.src = scriptSrc;
    document.body.insertAdjacentElement("beforeend", unitScript);
};
