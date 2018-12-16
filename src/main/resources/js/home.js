const setup = async () => {
    insertScript("/js/webcomponents-bundle-2.0.0.js");
    insertScript("/js/component/pbbg-progress-bar.js");

    replaceInterfaceWithText("Loading…");

    const res = await (await fetch("/api/user")).json();

    if (res.status === "success") {
        replaceInterfaceWithText("");

        const { mining } = res.data;

        main.innerHTML = `
<div>
    <span>Mining Level ${mining.level}</span>
    <pbbg-progress-bar max="${mining.relativeExpToNextLevel}" value="${mining.relativeExp}"></pbbg-progress-bar>
    <span>${mining.relativeExp} / ${mining.relativeExpToNextLevel} Exp.</span>
</div>        
`;
    } else {
        main.innerText = "Error.";
    }
};

setup();
