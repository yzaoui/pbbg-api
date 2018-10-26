window.onload = async () => {
    const main = document.getElementById("main");

    main.innerText = "Loading squad...";

    const { status, data } = await (await fetch("/api/squad")).json();

    if (status === "success") {
        main.innerText = "";

        const { units } = data;

        main.innerText = JSON.stringify(units);
    } else {
        main.innerText = "Error loading squad."
    }
};
