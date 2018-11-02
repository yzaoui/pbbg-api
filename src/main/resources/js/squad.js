const unitScript = document.createElement("script");
unitScript.src = "/js/component/pbbg-unit.js";
document.body.insertAdjacentElement("beforeend", unitScript);

window.onload = async () => {
    const main = document.getElementById("main");

    main.innerText = "Loading squad...";

    const { status, data } = await (await fetch("/api/squad")).json();

    if (status === "success") {
        main.innerText = "";

        const { units } = data;

        units.forEach(unit => {
            const el = document.createElement("pbbg-unit");
            el.unit = unit;
            main.appendChild(el);
        });
    } else {
        main.innerText = "Error loading squad."
    }
};
