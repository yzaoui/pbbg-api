const setup = async () => {
    insertScript("/js/webcomponents-bundle-2.0.0.js");
    insertModule("/js/component/pbbg-level-progress.js");

    replaceInterfaceWithText("Loading…");

    const res = await (await fetch("/api/user")).json();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        /**
         * @type {LevelProgress}
         */
        const mining = res.data.mining;

        main.innerHTML = `
Mining:
<div class="level-info">
    <pbbg-level-progress level="${mining.level}" max="${mining.relativeExpToNextLevel}" value="${mining.relativeExp}"></pbbg-level-progress>
    <span>Lv. ${mining.level} — ${mining.relativeExp} / ${mining.relativeExpToNextLevel}</span>
</div>        
`;
    } else {
        main.innerText = "Error.";
    }
};

setup();
