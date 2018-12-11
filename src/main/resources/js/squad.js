window.onload = async () => {
    insertScript("/js/webcomponents-bundle-2.0.0.js");
    insertScript("/js/component/pbbg-progress-bar.js");
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
