document.addEventListener("DOMContentLoaded", async () => {

    const translations = document.getElementById(
        "readerTranslations"
    );

    function t(key) {
        return translations?.dataset[key] || key;
    }

    const bookId = document.getElementById("readerBookId")
        .dataset.bookId;

    const chapterList = document.getElementById("chapterList");
    const chapterTitle = document.getElementById("chapterTitle");
    const chapterContent = document.getElementById("chapterContent");
    const chapterNumber = document.getElementById("chapterNumber");

    const previousButton = document.getElementById("previousChapter");
    const nextButton = document.getElementById("nextChapter");

    const progressBar = document.getElementById("readingProgressBar");
    const chapterProgress = document.getElementById("chapterProgress");

    const sidebar = document.getElementById("readerSidebar");

    const bookmarkButton = document.getElementById("bookmarkButton");
    const bookmarkMessage = document.getElementById("bookmarkMessage");

    const csrfToken =
        document.querySelector('meta[name="_csrf"]')?.content;

    const csrfHeader =
        document.querySelector('meta[name="_csrf_header"]')?.content;

    let chapters = [];
    let bookmarks = [];
    let currentIndex = 0;
    let fontSize = 18;
    let saveTimeout = null;
    let pendingRestorePercentage = 0;
    let isRestoring = false;

    // =========================================
    // AUTHENTICATION
    // =========================================

    async function apiRequest(url, method = "GET", body = null) {

        const headers = {};

        if (body !== null) {
            headers["Content-Type"] = "application/json";
        }

        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        return fetch(url, {
            method,
            headers,
            credentials: "same-origin",
            body: body !== null ? JSON.stringify(body) : null
        });
    }

    async function checkAuthentication() {

        try {

            const response = await apiRequest(
                `/api/books/${bookId}/progress`
            );

            if (response.status === 401 ||
                response.status === 403) {
                return false;
            }

            return response.ok;

        } catch {
            return false;
        }
    }

    const authenticated = await checkAuthentication();

    if (!authenticated) {
        bookmarkButton.disabled = true;
        bookmarkButton.textContent = t("loginBookmark");
    }

    // =========================================
    // LOAD CHAPTERS
    // =========================================

    async function loadChapters() {

        try {

            const response = await fetch(
                `/api/books/${bookId}/chapters`
            );

            if (!response.ok) {
                throw new Error("Unable to load chapters.");
            }

            chapters = await response.json();

            if (!chapters.length) {

                chapterContent.textContent =
                  t("noChapters");

                return;
            }

            renderChapterList();

            if (authenticated) {
                await loadBookmarks();
                await loadSavedProgress();
            }

            showChapter(currentIndex, true);

        } catch (error) {

            console.error(error);

            chapterList.textContent =
                t("loadError");

            chapterContent.textContent =
                t("chapterLoadError");

        }
    }

    // =========================================
    // LOAD SAVED PROGRESS
    // =========================================

    async function loadSavedProgress() {

        try {

            const response = await apiRequest(
                `/api/books/${bookId}/progress`
            );

            if (!response.ok) return;

            const progress = await response.json();

            if (!progress) return;

            const savedIndex = chapters.findIndex(
                chapter => chapter.id === progress.chapterId
            );

            if (savedIndex >= 0) {

                currentIndex = savedIndex;

                pendingRestorePercentage =
                    progress.scrollPercentage || 0;

            }

        } catch (error) {

            console.error(t("progressError"), error);

        }
    }

    // =========================================
    // SAVE PROGRESS
    // =========================================

    function getScrollPercentage() {

        const maxScroll =
            document.documentElement.scrollHeight -
            window.innerHeight;

        if (maxScroll <= 0) return 0;

        return Math.min(
            100,
            Math.max(0, (window.scrollY / maxScroll) * 100)
        );
    }

    async function saveProgress() {

        if (!authenticated || !chapters.length) return;

        const chapter = chapters[currentIndex];

        if (!chapter) return;

        try {

            await apiRequest(
                `/api/books/${bookId}/progress`,
                "PUT",
                {
                    chapterId: chapter.id,
                    scrollPercentage: Number(
                        getScrollPercentage().toFixed(2)
                    )
                }
            );

        } catch (error) {

            console.error("Progress saving error:", error);

        }
    }

        
    function updateProgressDisplay() {

        const percentage = getScrollPercentage();

        chapterProgress.textContent =
            `${Math.round(percentage)}%`;

        const overallProgress =
            ((currentIndex + percentage / 100) / chapters.length) * 100;

        progressBar.style.width =
            `${overallProgress}%`;
    }


    function scheduleProgressSave() {

        if (!authenticated || isRestoring) return;

        clearTimeout(saveTimeout);

        saveTimeout = setTimeout(saveProgress, 1000);
    }

    window.addEventListener("scroll", () => {

        updateProgressDisplay();
        scheduleProgressSave();

    }, { passive: true });

    window.addEventListener("pagehide", () => {

        if (!authenticated || !chapters.length) return;

        clearTimeout(saveTimeout);

        saveProgress();

    });

    // =========================================
    // LOAD BOOKMARKS
    // =========================================

    async function loadBookmarks() {

        try {

            const response = await apiRequest(
                `/api/books/${bookId}/bookmarks`
            );

            if (!response.ok) return;

            bookmarks = await response.json();

        } catch (error) {

            console.error("Bookmark loading error:", error);

        }
    }

    function updateBookmarkButton() {

        if (!authenticated) return;

        const chapter = chapters[currentIndex];

        const isBookmarked = bookmarks.some(
            bookmark => bookmark.chapterId === chapter.id
        );

        bookmarkButton.classList.toggle(
            "active",
            isBookmarked
        );

        bookmarkButton.setAttribute(
            "aria-pressed",
            String(isBookmarked)
        );

        bookmarkButton.textContent = isBookmarked
            ? `♥ ${t("bookmarked")}`
            : `♡ ${t("bookmark")}`;
    }

    bookmarkButton.addEventListener("click", async () => {

        if (!authenticated) return;

        const chapter = chapters[currentIndex];

        const isBookmarked = bookmarks.some(
            bookmark => bookmark.chapterId === chapter.id
        );

        const method = isBookmarked ? "DELETE" : "POST";

        try {

            const response = await apiRequest(
                `/api/books/${bookId}/bookmarks/${chapter.id}`,
                method
            );

            if (!response.ok) {
                throw new Error("Unable to update bookmark.");
            }

            if (isBookmarked) {

                bookmarks = bookmarks.filter(
                    bookmark => bookmark.chapterId !== chapter.id
                );

                bookmarkMessage.textContent =
                  t("bookmarkRemoved");

            } else {

                bookmarks.push({
                    chapterId: chapter.id
                });

                bookmarkMessage.textContent =
                   t("bookmarkAdded");

            }

            updateBookmarkButton();

        } catch (error) {

            console.error(error);

            bookmarkMessage.textContent =
             t("bookmarkError");

        }
    });

    // =========================================
    // RENDER CHAPTER LIST
    // =========================================

    function renderChapterList() {

        chapterList.replaceChildren();

        chapters.forEach((chapter, index) => {

            const button = document.createElement("button");

            button.type = "button";
            button.className = "chapter-list-item";

            button.textContent =
                `${chapter.numero}. ${chapter.titulo}`;

            button.addEventListener("click", () => {

                showChapter(index);

                sidebar.classList.remove("sidebar-open");

            });

            chapterList.appendChild(button);

        });
    }

    // =========================================
    // SHOW CHAPTER
    // =========================================

    function showChapter(index, restoring = false) {

        if (index < 0 || index >= chapters.length) return;

        if (!restoring) {
            clearTimeout(saveTimeout);
            saveProgress();
        }

        currentIndex = index;

        const chapter = chapters[index];

        chapterNumber.textContent =
            `${t("chapter")} ${chapter.numero}`;

        chapterTitle.textContent = chapter.titulo;

        chapterContent.replaceChildren();

        chapter.contenido.split(/\n\s*\n/).forEach(paragraph => {

            if (!paragraph.trim()) return;

            const p = document.createElement("p");

            p.textContent = paragraph;

            chapterContent.appendChild(p);

        });

        chapterContent.style.fontSize =
            `${fontSize}px`;

        document.querySelectorAll(".chapter-list-item")
            .forEach((button, i) => {

                button.classList.toggle(
                    "active",
                    i === currentIndex
                );

            });

        previousButton.disabled = currentIndex === 0;

        nextButton.disabled =
            currentIndex === chapters.length - 1;

        updateProgressDisplay();

        updateBookmarkButton();

        isRestoring = true;

        requestAnimationFrame(() => {

            const maxScroll =
                document.documentElement.scrollHeight -
                window.innerHeight;

            window.scrollTo({
                top: maxScroll * (
                    restoring ? pendingRestorePercentage / 100 : 0
                ),
                behavior: "instant"
            });

            updateProgressDisplay();

            pendingRestorePercentage = 0;

            isRestoring = false;

            if (authenticated) {
                scheduleProgressSave();
            }

        });

    }

    // =========================================
    // NAVIGATION
    // =========================================

    previousButton.addEventListener("click", () => {
        showChapter(currentIndex - 1);
    });

    nextButton.addEventListener("click", () => {
        showChapter(currentIndex + 1);
    });

    // =========================================
    // FONT SIZE
    // =========================================

    document.getElementById("increaseFont")
        .addEventListener("click", () => {

            fontSize = Math.min(fontSize + 2, 30);

            chapterContent.style.fontSize =
                `${fontSize}px`;

        });

    document.getElementById("decreaseFont")
        .addEventListener("click", () => {

            fontSize = Math.max(fontSize - 2, 12);

            chapterContent.style.fontSize =
                `${fontSize}px`;

        });

    // =========================================
    // SIDEBAR
    // =========================================

    document.getElementById("toggleSidebar")
        .addEventListener("click", () => {

            sidebar.classList.toggle("sidebar-open");

        });

    // INITIALIZE

    await loadChapters();

});
