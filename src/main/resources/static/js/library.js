
document.addEventListener("DOMContentLoaded", function () {

    const tabs = document.querySelectorAll(".library-tab");
    const panels = document.querySelectorAll(".library-panel");

    function activateTab(tab) {

        const selectedTab = tab.dataset.tab;

        tabs.forEach(function (item) {

            const active = item === tab;

            item.classList.toggle("active", active);
            item.setAttribute("aria-selected", active);
            item.setAttribute("tabindex", active ? "0" : "-1");

        });

        panels.forEach(function (panel) {

            panel.hidden = panel.id !== "panel-" + selectedTab;

        });

    }

    tabs.forEach(function (tab, index) {

        tab.addEventListener("click", function () {
            activateTab(tab);
        });

        tab.addEventListener("keydown", function (event) {

            let nextIndex = index;

            if (event.key === "ArrowRight") {
                nextIndex = (index + 1) % tabs.length;
            } else if (event.key === "ArrowLeft") {
                nextIndex = (index - 1 + tabs.length) % tabs.length;
            } else {
                return;
            }

            event.preventDefault();

            tabs[nextIndex].focus();
            activateTab(tabs[nextIndex]);

        });

    });

});
