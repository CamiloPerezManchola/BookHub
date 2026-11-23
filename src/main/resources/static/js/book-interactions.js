$(document).ready(function () {

    const bookId = $(".book-stats-bar").data("book-id");

    const csrfToken = $("#csrfToken").val();

    const csrfHeader = $("meta[name='_csrf_header']").attr("content");

    const translations = document.getElementById("reviewTranslations");

    function t(key) {
        return translations?.dataset[key] || key;
    }

    function updateInteraction(url, button, icon, counter, activeIcon, inactiveIcon) {

        $.ajax({
            url: url,
            type: "POST",

            beforeSend: function (xhr) {
                if (csrfToken && csrfHeader) {
                    xhr.setRequestHeader(csrfHeader, csrfToken);
                }
            },

            success: function (response) {

                button.toggleClass("active", response.active);

                button.attr("aria-pressed", response.active);

                icon.text(
                    response.active ? activeIcon : inactiveIcon
                );

                counter.text(response.count);
            },

            error: function (xhr) {

                if (xhr.status === 401 || xhr.status === 403) {
                    alert(t("loginRequired"));
                } else {
                    alert(t("tryAgain"));
                }

            }
        });
    }

    // FAVORITE
    $("#favoriteButton").on("click", function () {

        updateInteraction(
            "/api/books/" + bookId + "/favorite",
            $(this),
            $("#favoriteIcon"),
            $("#favoriteCount"),
            "❤️",
            "🤍"
        );

    });

    // SAVE
    $("#saveButton").on("click", function () {

        updateInteraction(
            "/api/books/" + bookId + "/save",
            $(this),
            $("#saveIcon"),
            $("#savedCount"),
            "🔖",
            "📑"
        );

    });

});
