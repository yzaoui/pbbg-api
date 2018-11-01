class ProgressBar extends HTMLElement {
    static get observedAttributes() {
        return ["max", "value"];
    }

    constructor() {
        super();

        this._shadowRoot = this.attachShadow({ mode: "open" });

        this._shadowRoot.innerHTML = CSS;

        const progressBar = document.createElement("div");
        progressBar.id = "outer";
        this._shadowRoot.appendChild(progressBar);

        const progressBarInner = document.createElement("div");
        progressBarInner.id = "inner";
        progressBar.appendChild(progressBarInner);
    }

    connectedCallback() {
        if (!this.hasAttribute("max")) this.setAttribute("max", "1");
        if (!this.hasAttribute("value")) this.setAttribute("value", "0");
        this.updateProgress();
    }

    attributeChangedCallback(name, oldValue, newValue) {
        this.updateProgress();
    }

    get max() {
        return Number(this.getAttribute("max"));
    }

    set max(value) {
        if (typeof value !== "number") throw TypeError();
        this.setAttribute("max", String(value));
    }

    get value() {
        return Number(this.getAttribute("value"));
    }

    set value(value) {
        if (typeof value !== "number") throw TypeError();
        this.setAttribute("value", String(value));
    }

    get progress() {
        return this.value / this.max;
    }

    updateProgress() {
        this._shadowRoot.getElementById("inner").style.width = (this.progress * 100) + "%";
    }
}

const CSS = `
<style>
:host {
    display: inline-block;
    width: 5em;
    height: 1em;
}

:host([hidden]) {
    display: none;
}

#outer {
    height: 100%;
    border: 1px solid black;
    box-sizing: border-box;
}

#inner {
    height: 100%;
    background: var(--bar-background-color, linear-gradient(#6de1ff, #00789c));
    transition: width 0.8s cubic-bezier(.8,0,.2,1);
}
</style>
`;

window.customElements.define("pbbg-progress-bar", ProgressBar);
