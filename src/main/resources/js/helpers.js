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
