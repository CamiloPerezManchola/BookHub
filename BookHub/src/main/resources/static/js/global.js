document.addEventListener("DOMContentLoaded", () => {

    const toast = document.getElementById("logoutToast");

    if (!toast) return;

    // Show notification for 4 seconds
    setTimeout(() => {

        toast.classList.add("toast-hide");

        // Remove notification after animation
        setTimeout(() => {
            toast.remove();
        }, 500);

    }, 4000);

});


document.addEventListener("DOMContentLoaded", () => {

    // =====================================================
    // BOOK EXPLORATION: SEARCH AND FILTERS
    // =====================================================

    const searchInput = document.getElementById("bookSearch");
    const books = document.querySelectorAll(".book-card");

    // Only execute on the Explore page
    if (!searchInput || books.length === 0) return;

    const yearFrom = document.getElementById("yearFrom");
    const yearTo = document.getElementById("yearTo");

    const genreCheckboxes = document.querySelectorAll(
        'input[name="genre"]'
    );

    const resultsMessage = document.getElementById("searchResults");
    const filterForm = document.getElementById("filterForm");

    function filterBooks() {

        const searchTerm = searchInput.value
            .trim()
            .toLowerCase();

        const minYear = yearFrom?.value
            ? Number(yearFrom.value)
            : null;

        const maxYear = yearTo?.value
            ? Number(yearTo.value)
            : null;

        const selectedGenres = Array.from(genreCheckboxes)
            .filter(checkbox => checkbox.checked)
            .map(checkbox => checkbox.value.toLowerCase());

        let visibleCount = 0;

        books.forEach(book => {

            const title = (book.dataset.title || "").toLowerCase();
            const author = (book.dataset.author || "").toLowerCase();
            const year = Number(book.dataset.year);
            const genre = (book.dataset.genre || "").toLowerCase();

            // Search by title or author
            const matchesSearch =
                title.includes(searchTerm) ||
                author.includes(searchTerm);

            // Publication year
            const matchesYear =
                (!minYear || year >= minYear) &&
                (!maxYear || year <= maxYear);

            // Selected genres
            const matchesGenre =
                selectedGenres.length === 0 ||
                selectedGenres.includes(genre);

            const isVisible =
                matchesSearch &&
                matchesYear &&
                matchesGenre;

            book.style.display = isVisible ? "" : "none";

            if (isVisible) {
                visibleCount++;
            }
        });

        // Update results message
        if (resultsMessage) {

            if (visibleCount === 0) {
                resultsMessage.textContent =
                    "No books found. Try changing your search or filters.";

            } else {
                resultsMessage.textContent =
                    `Showing ${visibleCount} book${visibleCount !== 1 ? "s" : ""}`;
            }
        }
    }

    // Search while typing
    searchInput.addEventListener("input", filterBooks);

    // Year filters
    yearFrom?.addEventListener("input", filterBooks);
    yearTo?.addEventListener("input", filterBooks);

    // Genre filters
    genreCheckboxes.forEach(checkbox => {
        checkbox.addEventListener("change", filterBooks);
    });

    // Reset filters
    filterForm?.addEventListener("reset", () => {
        setTimeout(filterBooks, 0);
    });

    // Apply URL search parameter on page load
    const params = new URLSearchParams(window.location.search);
    const initialSearch = params.get("q");

    if (initialSearch) {
        searchInput.value = initialSearch;
    }

    // Initial filtering
    filterBooks();

    // Select all / clear genres
    const clearGenresButton = document.querySelector(".btn-clear-genres");

    clearGenresButton?.addEventListener("click", () => {

        const allSelected = Array.from(genreCheckboxes)
            .every(checkbox => checkbox.checked);

        genreCheckboxes.forEach(checkbox => {
            checkbox.checked = !allSelected;
        });

        filterBooks();
    });

    // Search inside the genre dropdown
    const genreSearch = document.querySelector(".genre-search");

    genreSearch?.addEventListener("input", () => {

        const term = genreSearch.value.toLowerCase();

        genreCheckboxes.forEach(checkbox => {

            const label = checkbox.closest("label");

            if (!label) return;

            label.style.display = label.textContent
                .toLowerCase()
                .includes(term) ? "" : "none";
        });

    });

});
