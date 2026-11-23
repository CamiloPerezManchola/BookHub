document.addEventListener("DOMContentLoaded", function () {

    // --------------------------------
    // ELEMENTS
    // --------------------------------

    const searchInput = document.getElementById("bookSearch");

    const yearFrom = document.getElementById("yearFrom");
    const yearTo = document.getElementById("yearTo");

    const ratingInput = document.getElementById("minimumRating");
    const ratingOutput = document.getElementById("ratingOutput");

    const genreSearch = document.querySelector(".genre-search");

    const genreCheckboxes = document.querySelectorAll(
        'input[name="genre"]'
    );

    const clearGenresButton = document.querySelector(
        ".btn-clear-genres"
    );

    const filterForm = document.getElementById("filterForm");

    const booksGrid = document.getElementById("booksGrid");

    const noBooksMessage = document.getElementById(
        "noBooksMessage"
    );

    const searchResults = document.getElementById(
        "searchResults"
    );

    const translations = document.getElementById(
        "exploreTranslations"
    );

    const books = Array.from(
        document.querySelectorAll(".book-card")
    );

    // --------------------------------
    // TRANSLATIONS
    // --------------------------------

    const messages = {
        anyRating: translations.dataset.ratingAny,
        resultsAll: translations.dataset.resultsAll,
        resultsFound: translations.dataset.resultsFound,
        selectAll: clearGenresButton.dataset.selectAll,
        clearAll: clearGenresButton.dataset.clearAll
    };

    // Replace placeholders in translated messages
    function formatMessage(message, values) {

        let result = message;

        Object.keys(values).forEach(function (key) {

            result = result.replace(
                `{${key}}`,
                values[key]
            );

        });

        return result;
    }

    // --------------------------------
    // NORMALIZE TEXT
    // --------------------------------

    function normalize(value) {

        return (value || "")
            .toString()
            .toLowerCase()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .trim();

    }

    // --------------------------------
    // GENRE BUTTON LABEL
    // --------------------------------

    function updateGenreButton() {

        const allSelected = genreCheckboxes.length > 0 &&
            Array.from(genreCheckboxes).every(
                checkbox => checkbox.checked
            );

        clearGenresButton.textContent =
            allSelected ? messages.clearAll : messages.selectAll;

    }

    // --------------------------------
    // MAIN FILTER FUNCTION
    // --------------------------------

    function filterBooks() {

        const search = normalize(searchInput.value);

        const from = yearFrom.value
            ? Number(yearFrom.value)
            : null;

        const to = yearTo.value
            ? Number(yearTo.value)
            : null;

        const minRating = Number(ratingInput.value);

        const selectedGenres = Array.from(
            genreCheckboxes
        )
            .filter(checkbox => checkbox.checked)
            .map(checkbox => normalize(checkbox.value));

        let visibleCount = 0;

        books.forEach(function (book) {

            const title = normalize(book.dataset.title);
            const author = normalize(book.dataset.author);

            const genre = normalize(book.dataset.genre);

            const year = Number(book.dataset.year) || 0;

            const rating = Number(book.dataset.rating) || 0;

            // Search by title or author
            const matchesSearch =
                !search ||
                title.includes(search) ||
                author.includes(search);

            // Publication year
            const matchesYear =
                (from === null || (year > 0 && year >= from)) &&
                (to === null || (year > 0 && year <= to));

            // Minimum rating
            const matchesRating = rating >= minRating;

            // Selected genres (OR behavior)
            const matchesGenre =
                selectedGenres.length === 0 ||
                selectedGenres.includes(genre);

            const visible =
                matchesSearch &&
                matchesYear &&
                matchesRating &&
                matchesGenre;

            book.hidden = !visible;

            if (visible) {
                visibleCount++;
            }

        });

        // --------------------------------
        // RESULTS MESSAGE
        // --------------------------------

        booksGrid.hidden = visibleCount === 0;

        noBooksMessage.hidden = visibleCount !== 0;

        if (searchResults) {

            searchResults.textContent =
                visibleCount === books.length

                    ? formatMessage(messages.resultsAll, {
                        count: visibleCount
                    })

                    : formatMessage(messages.resultsFound, {
                        visible: visibleCount,
                        total: books.length
                    });

        }

    }

    // --------------------------------
    // SEARCH
    // --------------------------------

    searchInput.addEventListener("input", filterBooks);

    // --------------------------------
    // YEAR FILTER
    // --------------------------------

    yearFrom.addEventListener("input", filterBooks);

    yearTo.addEventListener("input", filterBooks);

    // --------------------------------
    // RATING FILTER
    // --------------------------------

    ratingInput.addEventListener("input", function () {

        const value = Number(this.value);

        ratingOutput.textContent =
            value === 0
                ? messages.anyRating
                : value.toFixed(1) + " ★";

        filterBooks();

    });

    // --------------------------------
    // GENRE FILTER
    // --------------------------------

    genreCheckboxes.forEach(function (checkbox) {

        checkbox.addEventListener("change", function () {

            updateGenreButton();
            filterBooks();

        });

    });

    // Search genres inside dropdown
    if (genreSearch) {

        genreSearch.addEventListener("input", function () {

            const search = normalize(this.value);

            document.querySelectorAll(
                ".genre-grid .checkbox-label"
            ).forEach(function (label) {

                label.hidden = !normalize(label.textContent)
                    .includes(search);

            });

        });

    }

    // --------------------------------
    // SELECT ALL / CLEAR GENRES
    // --------------------------------

    if (clearGenresButton) {

        clearGenresButton.addEventListener("click", function () {

            const allSelected = Array.from(genreCheckboxes)
                .every(checkbox => checkbox.checked);

            genreCheckboxes.forEach(function (checkbox) {

                checkbox.checked = !allSelected;

            });

            updateGenreButton();
            filterBooks();

        });

    }

    // --------------------------------
    // RESET FILTERS
    // --------------------------------

    filterForm.addEventListener("reset", function () {

        setTimeout(function () {

            ratingOutput.textContent = messages.anyRating;

            if (genreSearch) {

                genreSearch.value = "";

                document.querySelectorAll(
                    ".genre-grid .checkbox-label"
                ).forEach(label => {

                    label.hidden = false;

                });

            }

            updateGenreButton();
            filterBooks();

        }, 0);

    });

    // --------------------------------
    // INITIALIZATION
    // --------------------------------

    updateGenreButton();

    filterBooks();

});