document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("createBookForm");
    const message = document.getElementById("bookMessage");
    const publishButton = document.getElementById("publishBookBtn");

    const chaptersContainer =
        document.getElementById("chaptersContainer");

    const addChapterBtn =
        document.getElementById("addChapterBtn");

    const chapterCount =
        document.getElementById("chapterCount");

    // Translations provided by Thymeleaf
    const messages = {
        chapter: form.dataset.chapterLabel,
        chapters: form.dataset.chapterLabelPlural,
        chapterTitle: form.dataset.chapterTitle,
        chapterTitlePlaceholder: form.dataset.chapterTitlePlaceholder,
        chapterContent: form.dataset.chapterContent,
        chapterContentPlaceholder: form.dataset.chapterContentPlaceholder,
        remove: form.dataset.remove,
        required: form.dataset.required,
        invalidUrl: form.dataset.invalidUrl,
        invalidYear: form.dataset.invalidYear,
        invalidGenre: form.dataset.invalidGenre,
        invalidChapter: form.dataset.invalidChapter,
        publishing: form.dataset.publishing,
        publish: form.dataset.publish,
        publishSuccess: form.dataset.publishSuccess,
        publishError: form.dataset.publishError
    };

    function showMessage(text, color = "red") {
        message.textContent = text;
        message.style.color = color;
    }

    function clearMessage() {
        message.textContent = "";
    }

    // Update chapter numbers and count
    function updateChapters() {

        const chapters = chaptersContainer.querySelectorAll(
            "[data-chapter]"
        );

        chapters.forEach((chapter, index) => {

            chapter.querySelector("h4").textContent =
                `${messages.chapter} ${index + 1}`;

            const removeButton =
                chapter.querySelector(".remove-chapter-btn");

            removeButton.disabled = chapters.length === 1;

        });

        const count = chapters.length;

        chapterCount.textContent =
            `${count} ${count === 1
                ? messages.chapter
                : messages.chapters}`;
    }

    // Create a new chapter
    function createChapter() {

        const chapter = document.createElement("div");

        chapter.className = "chapter-form-card";
        chapter.dataset.chapter = "";

        const heading = document.createElement("div");
        heading.className = "chapter-form-heading";

        const title = document.createElement("h4");

        const removeButton = document.createElement("button");
        removeButton.type = "button";
        removeButton.className = "remove-chapter-btn";
        removeButton.textContent = messages.remove;

        heading.append(title, removeButton);

        const titleGroup = document.createElement("div");
        titleGroup.className = "form-group";

        const titleLabel = document.createElement("label");
        titleLabel.textContent = messages.chapterTitle;

        const titleInput = document.createElement("input");
        titleInput.type = "text";
        titleInput.className = "chapter-title";
        titleInput.maxLength = 200;
        titleInput.placeholder = messages.chapterTitlePlaceholder;
        titleInput.required = true;

        titleGroup.append(titleLabel, titleInput);

        const contentGroup = document.createElement("div");
        contentGroup.className = "form-group";

        const contentLabel = document.createElement("label");
        contentLabel.textContent = messages.chapterContent;

        const contentInput = document.createElement("textarea");
        contentInput.className = "chapter-content";
        contentInput.rows = 8;
        contentInput.placeholder = messages.chapterContentPlaceholder;
        contentInput.required = true;

        contentGroup.append(contentLabel, contentInput);

        chapter.append(heading, titleGroup, contentGroup);

        chaptersContainer.appendChild(chapter);

        updateChapters();
    }

    // Add chapter
    addChapterBtn.addEventListener("click", () => {
        clearMessage();
        createChapter();
    });

    // Remove chapter
    chaptersContainer.addEventListener("click", (event) => {

        const removeButton = event.target.closest(
            ".remove-chapter-btn"
        );

        if (!removeButton || removeButton.disabled) return;

        removeButton.closest("[data-chapter]").remove();

        clearMessage();
        updateChapters();
    });

    // Validate the form with translated messages
    function validateForm() {

        const requiredFields = form.querySelectorAll(
            "input[required], select[required], textarea[required]"
        );

        for (const field of requiredFields) {

            field.setCustomValidity("");

            if (!field.value.trim()) {
                field.setCustomValidity(messages.required);
            }

            else if (
                field.type === "url" &&
                !field.validity.valid
            ) {
                field.setCustomValidity(messages.invalidUrl);
            }

            else if (
                field.type === "number" &&
                !field.validity.valid
            ) {
                field.setCustomValidity(messages.invalidYear);
            }

            if (!field.validity.valid) {
                field.reportValidity();
                field.focus();
                return false;
            }
        }

        // Validate optional cover URL if provided
        const coverInput = document.getElementById("portadaUrl");

        coverInput.setCustomValidity("");

        if (
            coverInput.value.trim() &&
            !coverInput.validity.valid
        ) {
            coverInput.setCustomValidity(messages.invalidUrl);
            coverInput.reportValidity();
            coverInput.focus();
            return false;
        }

        // Validate genre
        const genre = document.getElementById("genero");

        if (!genre.value) {
            genre.setCustomValidity(messages.invalidGenre);
            genre.reportValidity();
            genre.focus();
            return false;
        }

        genre.setCustomValidity("");

        return true;
    }

    // Clear custom validation when fields change
    form.addEventListener("input", (event) => {
        event.target.setCustomValidity("");
        clearMessage();
    });

    form.addEventListener("change", (event) => {
        event.target.setCustomValidity("");
        clearMessage();
    });

    // Submit form
    form.addEventListener("submit", async (event) => {

        event.preventDefault();

        clearMessage();

        // Validate before sending the request
        if (!validateForm()) {
            return;
        }

        const csrfToken =
            document.getElementById("csrfToken").value;

        const csrfHeader =
            document.getElementById("csrfHeader").value;

        if (!csrfToken || !csrfHeader) {
            showMessage(messages.publishError);
            return;
        }

        const chapters = Array.from(
            chaptersContainer.querySelectorAll("[data-chapter]")
        ).map(chapter => ({

            titulo: chapter.querySelector(
                ".chapter-title"
            ).value.trim(),

            contenido: chapter.querySelector(
                ".chapter-content"
            ).value.trim()

        }));

        const hasInvalidChapter = chapters.some(chapter =>
            !chapter.titulo || !chapter.contenido
        );

        if (hasInvalidChapter) {

            showMessage(messages.invalidChapter);

            return;
        }

        const bookData = {

            titulo: document.getElementById("titulo").value.trim(),

            autor: document.getElementById("autor").value.trim(),

            portadaUrl:
                document.getElementById("portadaUrl").value.trim(),

            anio: Number(document.getElementById("anio").value),

            genero: document.getElementById("genero").value,

            sinopsis:
                document.getElementById("sinopsis").value.trim(),

            capitulos: chapters

        };

        publishButton.disabled = true;
        addChapterBtn.disabled = true;
        publishButton.textContent = messages.publishing;

        try {

            const response = await fetch("/api/books", {

                method: "POST",

                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },

                credentials: "same-origin",

                body: JSON.stringify(bookData)

            });

            if (!response.ok) {

                const errorText = await response.text();

                console.error("Server response:", errorText);

                throw new Error(messages.publishError);

            }

            const savedBook = await response.json();

            showMessage(messages.publishSuccess, "green");

            form.reset();

            setTimeout(() => {

                window.location.href =
                    `/libro/${savedBook.id}`;

            }, 1200);

        } catch (error) {

            console.error("Publishing error:", error);

            showMessage(messages.publishError);

        } finally {

            publishButton.disabled = false;
            addChapterBtn.disabled = false;
            publishButton.textContent = messages.publish;

        }

    });

    updateChapters();

});
