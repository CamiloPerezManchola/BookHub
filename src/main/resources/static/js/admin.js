document.addEventListener("DOMContentLoaded", () => {

    const translations = document.getElementById("adminTranslations");

    const i18n = {
        usersNotFound: translations.dataset.usersNotFound,
        booksNotFound: translations.dataset.booksNotFound,
        reviewsNotFound: translations.dataset.reviewsNotFound,
        bookCover: translations.dataset.bookCover,
        noCover: translations.dataset.noCover,
        noComment: translations.dataset.noComment,
        edit: translations.dataset.edit,
        delete: translations.dataset.delete,
        userNotFound: translations.dataset.userNotFound,
        userUpdated: translations.dataset.userUpdated,
        userDeleted: translations.dataset.userDeleted,
        deleteUserConfirm: translations.dataset.deleteUserConfirm,
        bookNotFound: translations.dataset.bookNotFound,
        addBook: translations.dataset.addBook,
        editBook: translations.dataset.editBook,
        bookChapterRequired: translations.dataset.bookChapterRequired,
        bookUpdated: translations.dataset.bookUpdated,
        bookCreated: translations.dataset.bookCreated,
        bookDeleted: translations.dataset.bookDeleted,
        deleteBookConfirm: translations.dataset.deleteBookConfirm,
        reviewDeleted: translations.dataset.reviewDeleted,
        deleteReviewConfirm: translations.dataset.deleteReviewConfirm
    };

    const csrfToken = document.querySelector(
        'meta[name="_csrf"]'
    )?.content;

    const csrfHeader = document.querySelector(
        'meta[name="_csrf_header"]'
    )?.content;

    const API = {
        users: "/api/users",
        books: "/api/books",
        reviews: "/api/admin/reviews"
    };

    const state = {
        users: [],
        books: [],
        reviews: []
    };

    // ---------------------------------
    // HTTP CLIENT
    // ---------------------------------

    async function apiRequest(url, options = {}) {

        const headers = {
            ...options.headers
        };

        if (options.body) {
            headers["Content-Type"] = "application/json";
        }

        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        const response = await fetch(url, {
            ...options,
            headers,
            credentials: "same-origin"
        });

        if (!response.ok) {

            let message = `Request failed (${response.status})`;

            try {
                const error = await response.json();
                message = error.message || message;
            } catch {
                // Response has no JSON body.
            }

            throw new Error(message);
        }

        if (response.status === 204) {
            return null;
        }

        return response.json();
    }

    // ---------------------------------
    // NOTIFICATIONS
    // ---------------------------------

    function notify(message, type = "success") {

        const alert = document.getElementById("adminAlert");

        if (!alert) return;

        alert.textContent = message;
        alert.className = `alert alert-${type}`;

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    }

    // ---------------------------------
    // LOAD DATA
    // ---------------------------------

    async function loadUsers() {
        state.users = await apiRequest(API.users);
        renderUsers();
        updateStatistics();
    }

    async function loadBooks() {
        state.books = await apiRequest(API.books);
        renderBooks();
        updateStatistics();
    }

    async function loadReviews() {
        state.reviews = await apiRequest(API.reviews);
        renderReviews();
        updateStatistics();
    }

    async function loadDashboard() {

        try {

            await Promise.all([
                loadUsers(),
                loadBooks(),
                loadReviews()
            ]);

        } catch (error) {

            notify(error.message, "danger");
            console.error(error);
        }
    }

    // ---------------------------------
    // STATISTICS
    // ---------------------------------

    function updateStatistics() {

        document.getElementById("totalUsers").textContent =
            state.users.length;

        document.getElementById("totalBooks").textContent =
            state.books.length;

        document.getElementById("totalReviews").textContent =
            state.reviews.length;
    }

    // ---------------------------------
    // SEARCH
    // ---------------------------------

    document.getElementById("userSearch")
        ?.addEventListener("input", renderUsers);

    document.getElementById("bookSearch")
        ?.addEventListener("input", renderBooks);

    // ---------------------------------
    // HTML ESCAPING
    // ---------------------------------

    function escapeHTML(value) {
        return String(value ?? "").replace(/[&<>"']/g, char => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#039;"
        })[char]);
    }

    // ---------------------------------
    // RENDER USERS
    // ---------------------------------

    function renderUsers() {

        const tbody = document.getElementById("usersTableBody");

        if (!tbody) return;

        const search = document.getElementById("userSearch")
            ?.value.toLowerCase() || "";

        const users = state.users.filter(user =>
            [
                user.nombre,
                user.username,
                user.email,
                user.rol
            ].some(value =>
                String(value ?? "").toLowerCase().includes(search)
            )
        );

        if (users.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-state">
                        ${escapeHTML(i18n.usersNotFound)}
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = users.map(user => {

            const role = String(user.rol || "USER").toUpperCase();

            return `
                <tr>
                    <td>${escapeHTML(user.id)}</td>

                    <td>
                        <strong>${escapeHTML(user.nombre)}</strong>
                        <span class="table-subtitle">
                            @${escapeHTML(user.username || "")}
                        </span>
                    </td>

                    <td>${escapeHTML(user.email)}</td>

                    <td>
                        <span class="role-badge ${
                            role === "ADMIN" ? "role-admin" : "role-user"
                        }">
                            ${escapeHTML(role)}
                        </span>
                    </td>

                    <td>
                        ${escapeHTML(user.fechaRegistro || "N/A")}
                    </td>

                    <td class="table-actions">
                        <button class="btn btn-sm btn-outline-primary"
                                data-action="edit-user"
                                data-id="${escapeHTML(user.id)}">
                            ${escapeHTML(i18n.edit)}
                        </button>

                        <button class="btn btn-sm btn-outline-danger"
                                data-action="delete-user"
                                data-id="${escapeHTML(user.id)}">
                            ${escapeHTML(i18n.delete)}
                        </button>
                    </td>
                </tr>
            `;
        }).join("");
    }

    // ---------------------------------
    // RENDER BOOKS
    // ---------------------------------

    function renderBooks() {

        const tbody = document.getElementById("booksTableBody");

        if (!tbody) return;

        const search = document.getElementById("bookSearch")
            ?.value.toLowerCase() || "";

        const books = state.books.filter(book =>
            [
                book.titulo,
                book.autor,
                book.genero,
                book.publisherName
            ].some(value =>
                String(value ?? "").toLowerCase().includes(search)
            )
        );

        if (books.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="empty-state">
                        ${escapeHTML(i18n.booksNotFound)}
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = books.map(book => {

            const cover = book.portadaUrl
                ? `
                    <img
                        src="${escapeHTML(book.portadaUrl)}"
                        alt="${escapeHTML(i18n.bookCover)}"
                        class="book-thumbnail"
                        onerror="this.style.display='none'"
                    >
                `
                : `<span class="no-cover">${escapeHTML(i18n.noCover)}</span>`;

            return `
                <tr>
                    <td>${escapeHTML(book.id)}</td>

                    <td>${cover}</td>

                    <td>
                        <strong>${escapeHTML(book.titulo)}</strong>
                    </td>

                    <td>${escapeHTML(book.autor)}</td>

                    <td>${escapeHTML(book.anio)}</td>

                    <td>${escapeHTML(book.publisherName || "N/A")}</td>

                    <td class="table-actions">
                        <button class="btn btn-sm btn-outline-primary"
                                data-action="edit-book"
                                data-id="${escapeHTML(book.id)}">
                            ${escapeHTML(i18n.edit)}
                        </button>

                        <button class="btn btn-sm btn-outline-danger"
                                data-action="delete-book"
                                data-id="${escapeHTML(book.id)}">
                            ${escapeHTML(i18n.delete)}
                        </button>
                    </td>
                </tr>
            `;
        }).join("");
    }

    // ---------------------------------
    // RENDER REVIEWS
    // ---------------------------------

    function renderReviews() {

        const tbody = document.getElementById("reviewsTableBody");

        if (!tbody) return;

        if (state.reviews.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-state">
                        ${escapeHTML(i18n.reviewsNotFound)}
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = state.reviews.map(review => {

            const stars = "★".repeat(
                Math.max(0, Math.min(5, Number(review.rating) || 0))
            );

            return `
                <tr>
                    <td>${escapeHTML(review.id)}</td>

                    <td>
                        <strong>${escapeHTML(review.nombre || review.username)}</strong>
                        <span class="table-subtitle">
                            @${escapeHTML(review.username || "")}
                        </span>
                    </td>

                    <td>
                        <span class="rating">
                            ${stars}
                        </span>
                        (${escapeHTML(review.rating)}/5)
                    </td>

                    <td>
                        <div class="review-comment">
                            ${escapeHTML(review.comentario || i18n.noComment)}
                        </div>
                    </td>

                    <td>${escapeHTML(review.fechaCreacion || "N/A")}</td>

                    <td class="table-actions">
                        <button class="btn btn-sm btn-outline-danger"
                                data-action="delete-review"
                                data-id="${escapeHTML(review.id)}">
                            Delete
                        </button>
                    </td>
                </tr>
            `;
        }).join("");
    }

    // ---------------------------------
    // TAB NAVIGATION
    // ---------------------------------

    document.querySelectorAll(".admin-tab").forEach(tab => {

        tab.addEventListener("click", () => {

            document.querySelectorAll(".admin-tab")
                .forEach(item => item.classList.remove("active"));

            tab.classList.add("active");

            document.querySelectorAll(".admin-section")
                .forEach(section => section.hidden = true);

            const section = document.getElementById(
                `${tab.dataset.section}Section`
            );

            if (section) section.hidden = false;
        });
    });

    
    // ---------------------------------
    // MODAL MANAGEMENT
    // ---------------------------------

    function openModal(id) {
        const modal = document.getElementById(id);
        if (modal) modal.showModal();
    }

    function closeModal(id) {
        const modal = document.getElementById(id);
        if (modal) modal.close();
    }

    document.querySelectorAll("[data-close]").forEach(button => {
        button.addEventListener("click", () => {
            closeModal(button.dataset.close);
        });
    });

    // ---------------------------------
    // DELETE CONFIRMATION
    // ---------------------------------

    let pendingDelete = null;

    function askDelete(message, callback) {
        document.getElementById("confirmMessage").textContent = message;

        pendingDelete = callback;

        openModal("confirmModal");
    }

    document.getElementById("confirmDeleteBtn")
        ?.addEventListener("click", async () => {

            if (!pendingDelete) return;

            const button = document.getElementById("confirmDeleteBtn");

            button.disabled = true;

            try {
                await pendingDelete();

                closeModal("confirmModal");

            } catch (error) {
                notify(error.message, "danger");

            } finally {
                pendingDelete = null;
                button.disabled = false;
            }
        });

    // ---------------------------------
    // USER CRUD
    // ---------------------------------

    function editUser(id) {

        const user = state.users.find(
            item => String(item.id) === String(id)
        );

        if (!user) {
            notify(i18n.userNotFound, "danger");
            return;
        }

        document.getElementById("userId").value = user.id;
        document.getElementById("userName").value = user.nombre || "";
        document.getElementById("userUsername").value = user.username || "";
        document.getElementById("userEmail").value = user.email || "";
        document.getElementById("userDescription").value =
            user.descripcion || "";
        document.getElementById("userPhoto").value =
            user.fotoPerfil || "";
        document.getElementById("userRole").value =
            user.rol || "USER";

        document.getElementById("userPassword").value = "";

        openModal("userModal");
    }

    document.getElementById("userForm")
        ?.addEventListener("submit", async event => {

            event.preventDefault();

            const id = document.getElementById("userId").value;

            const password = document.getElementById("userPassword").value;

            const data = {
                nombre: document.getElementById("userName").value.trim(),
                username: document.getElementById("userUsername").value.trim(),
                email: document.getElementById("userEmail").value.trim(),
                descripcion: document.getElementById("userDescription").value.trim(),
                fotoPerfil: document.getElementById("userPhoto").value.trim(),
                rol: document.getElementById("userRole").value
            };

            if (password.trim()) {
                data.password = password;
            }

            try {

                await apiRequest(`${API.users}/${id}`, {
                    method: "PUT",
                    body: JSON.stringify(data)
                });

                closeModal("userModal");

                await loadUsers();

                notify(i18n.userUpdated);

            } catch (error) {
                notify(error.message, "danger");
            }
        });

    async function deleteUser(id) {

        askDelete(
            i18n.deleteUserConfirm,
            async () => {

                await apiRequest(`${API.users}/${id}`, {
                    method: "DELETE"
                });

                await loadUsers();

                notify(i18n.userDeleted);
            }
        );
    }

    // ---------------------------------
    // BOOK CRUD
    // ---------------------------------

    function resetBookForm() {

        document.getElementById("bookForm").reset();

        document.getElementById("bookId").value = "";

        document.getElementById("chapterTitle").value = "";
        document.getElementById("chapterContent").value = "";

        document.getElementById("bookModalTitle").textContent =
            i18n.addBook;;

        document.getElementById("chapterFields").hidden = false;
    }

    function createBookModal() {

        resetBookForm();

        openModal("bookModal");
    }

    function editBook(id) {

        const book = state.books.find(
            item => String(item.id) === String(id)
        );

        if (!book) {
            notify(i18n.bookNotFound, "danger");
            return;
        }

        resetBookForm();

        document.getElementById("bookId").value = book.id;
        document.getElementById("bookTitle").value = book.titulo || "";
        document.getElementById("bookAuthor").value = book.autor || "";
        document.getElementById("bookCover").value =
            book.portadaUrl || "";
        document.getElementById("bookYear").value = book.anio ?? "";
        document.getElementById("bookGenre").value = book.genero || "";
        document.getElementById("bookSynopsis").value =
            book.sinopsis || "";

        document.getElementById("bookModalTitle").textContent =
            i18n.editBook;

        // Existing chapters are preserved when capitulos is omitted.
        document.getElementById("chapterFields").hidden = true;

        openModal("bookModal");
    }

    document.getElementById("addBookBtn")
        ?.addEventListener("click", createBookModal);

    document.getElementById("bookForm")
        ?.addEventListener("submit", async event => {

            event.preventDefault();

            const id = document.getElementById("bookId").value;

            const data = {
                titulo: document.getElementById("bookTitle").value.trim(),
                autor: document.getElementById("bookAuthor").value.trim(),
                portadaUrl: document.getElementById("bookCover").value.trim(),
                anio: Number(document.getElementById("bookYear").value),
                genero: document.getElementById("bookGenre").value.trim(),
                sinopsis: document.getElementById("bookSynopsis").value.trim()
            };

            // New books require at least one chapter in BookService.
            if (!id) {

                const chapterTitle =
                    document.getElementById("chapterTitle").value.trim();

                const chapterContent =
                    document.getElementById("chapterContent").value.trim();

                if (!chapterTitle || !chapterContent) {
                    notify(i18n.bookChapterRequired, "danger");
                    return;
                }

                data.capitulos = [{
                    titulo: chapterTitle,
                    contenido: chapterContent
                }];
            }

            try {

                if (id) {

                    await apiRequest(`${API.books}/${id}`, {
                        method: "PUT",
                        body: JSON.stringify(data)
                    });

                } else {

                    await apiRequest(API.books, {
                        method: "POST",
                        body: JSON.stringify(data)
                    });
                }

                closeModal("bookModal");

                await loadBooks();

                notify(
                    id
                        ? i18n.bookUpdated
                        : i18n.bookCreated
                );

            } catch (error) {
                notify(error.message, "danger");
            }
        });

    async function deleteBook(id) {

        askDelete(
            i18n.deleteBookConfirm,
            async () => {

                await apiRequest(`${API.books}/${id}`, {
                    method: "DELETE"
                });

                await loadBooks();

                notify(i18n.bookDeleted);
            }
        );
    }

    // ---------------------------------
    // REVIEW CRUD
    // ---------------------------------

    async function deleteReview(id) {

        askDelete(
            i18n.deleteReviewConfirm,
            async () => {

                await apiRequest(`${API.reviews}/${id}`, {
                    method: "DELETE"
                });

                await loadReviews();

                notify(i18n.reviewDeleted);
            }
        );
    }

    // ---------------------------------
    // BUTTON EVENT DELEGATION
    // ---------------------------------

    document.addEventListener("click", event => {

        const button = event.target.closest("[data-action]");

        if (!button) return;

        const id = button.dataset.id;

        switch (button.dataset.action) {

            case "edit-user":
                editUser(id);
                break;

            case "delete-user":
                deleteUser(id);
                break;

            case "edit-book":
                editBook(id);
                break;

            case "delete-book":
                deleteBook(id);
                break;

            case "delete-review":
                deleteReview(id);
                break;
        }
    });

    // ---------------------------------
    // REFRESH BUTTONS
    // ---------------------------------

    document.getElementById("refreshUsersBtn")
        ?.addEventListener("click", loadDashboard);

    // Initial load
    loadDashboard();

    // Expose functions for the CRUD modules
    window.BookHubAdmin = {
        apiRequest,
        notify,
        loadUsers,
        loadBooks,
        loadReviews,
        state
    };

});
