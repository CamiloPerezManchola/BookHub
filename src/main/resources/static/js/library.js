document.addEventListener("DOMContentLoaded", function () {

    const tabs = document.querySelectorAll(".library-tab");
    const panels = document.querySelectorAll(".library-panel");

    if (!tabs.length || !panels.length) {
        console.error("Library tabs or panels were not found.");
        return;
    }

    function activateTab(tab) {

        const selectedTab = tab.dataset.tab;

        tabs.forEach(function (item) {

            const active = item === tab;

            item.classList.toggle("active", active);

            item.setAttribute(
                "aria-selected",
                String(active)
            );

            item.setAttribute(
                "tabindex",
                active ? "0" : "-1"
            );

        });

        panels.forEach(function (panel) {

            const active =
                panel.id === "panel-" + selectedTab;

            panel.hidden = !active;
            panel.classList.toggle("active", active);

        });

    }

    tabs.forEach(function (tab) {

        tab.addEventListener("click", function () {
            activateTab(tab);
        });

        tab.addEventListener("keydown", function (event) {

            const currentIndex = Array.from(tabs).indexOf(tab);

            let nextIndex;

            switch (event.key) {

                case "ArrowRight":
                    nextIndex = (currentIndex + 1) % tabs.length;
                    break;

                case "ArrowLeft":
                    nextIndex =
                        (currentIndex - 1 + tabs.length) % tabs.length;
                    break;

                case "Home":
                    nextIndex = 0;
                    break;

                case "End":
                    nextIndex = tabs.length - 1;
                    break;

                default:
                    return;
            }

            event.preventDefault();

            tabs[nextIndex].focus();
            activateTab(tabs[nextIndex]);

        });

    });

    // Initialize the published tab
    const initialTab =
        document.querySelector(".library-tab.active") || tabs[0];

    activateTab(initialTab);

});
