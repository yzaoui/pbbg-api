const setup = async () => {
    const main = document.getElementById("main");
    main.innerText = "Loading...";

    const res = await (await fetch("/api/user")).json();

    if (res.status === "success") {
        main.innerText = "";

        const { mining } = res.data;

        const div = document.createElement("div");
        main.appendChild(div);

        const levelSpan = document.createElement("span");
        levelSpan.innerText = `Mining Level ${mining.level}`;
        div.appendChild(levelSpan);

        const progressBar = document.createElement("pbbg-progress-bar");
        progressBar.max = mining.relativeExpToNextLevel;
        progressBar.value = mining.relativeExp;
        div.appendChild(progressBar);

        const expSpan = document.createElement("span");
        expSpan.innerText = `${mining.relativeExp} / ${mining.relativeExpToNextLevel} Exp.`;
        div.appendChild(expSpan);
    } else {
        main.innerText = "Error";
    }
};

setup();
