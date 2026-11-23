
document.addEventListener("DOMContentLoaded", () => {

    // --------------------------------------------------
    // TRANSLATIONS
    // --------------------------------------------------

    const translations = document.getElementById("reviewTranslations");

    function t(key) {
        return translations?.dataset[key] || key;
    }

    const locale = document.documentElement.lang || "es";

    // --------------------------------------------------
    // REVIEW MODAL ELEMENTS
    // --------------------------------------------------

    const reviewModal = document.getElementById("reviewModal");

    const modalTitle = document.getElementById("reviewModalTitle");
    const modalEyebrow = document.getElementById("reviewModalEyebrow");

    const editContent = document.getElementById("editReviewContent");
    const deleteContent = document.getElementById("deleteReviewContent");

    const editReviewForm = document.getElementById("editReviewForm");

    const editRating = document.getElementById("editReviewRating");
    const editComment = document.getElementById("editReviewComment");

    const modalMessage = document.getElementById("reviewModalMessage");

    const saveReviewButton = document.getElementById("saveReviewButton");
    const deleteReviewButton = document.getElementById("confirmDeleteButton");

    const cancelModalButton = document.getElementById("cancelReviewModal");
    const closeModalButton = document.getElementById("closeReviewModal");

    const characterCount = document.getElementById("reviewCharacterCount");

    let selectedReview = null;
    let modalMode = null;
    let previousFocus = null;

    const section = document.getElementById("reviewsSection");

    if (!section) return;

    const bookId = section.dataset.bookId;

    const communityList = document.getElementById(
        "communityReviewsList"
    );

    const myReviewsList = document.getElementById(
        "myReviewsList"
    );

    const reviewForm = document.getElementById("reviewForm");

    const reviewMessage = document.getElementById(
        "reviewMessage"
    );

    const csrfValue = document.querySelector(
        'meta[name="_csrf"]'
    )?.getAttribute("content");

    const csrfHeader = document.querySelector(
        'meta[name="_csrf_header"]'
    )?.getAttribute("content");

    // --------------------------------------------------
    // API REQUEST HELPER
    // --------------------------------------------------

    async function apiRequest(url, options = {}) {

        const headers = {
            ...options.headers
        };

        if (csrfValue &&
            options.method &&
            options.method !== "GET") {

            if (csrfHeader) {
                headers[csrfHeader] = csrfValue;
            }
        }

        let response;

        try {

            response = await fetch(url, {
                ...options,
                headers,
                credentials: "same-origin"
            });

        } catch (error) {

            throw new Error(t("networkError"));

        }

        if (response.status === 401 ||
            response.status === 403) {

            throw new Error(t("permissionError"));
        }

        if (!response.ok) {

            const errorText = await response.text();

            const errorKey = errorText.trim();

            if (errorKey === "serverError") {
                throw new Error(t("serverError"));
            }

            throw new Error(
                errorText || t("unknownError")
            );
        }

        if (response.status === 204) {
            return null;
        }

        return response.json();
    }

    // --------------------------------------------------
    // CREATE REVIEW CARD
    // --------------------------------------------------

    function createReviewCard(review, ownReview = false) {

        const card = document.createElement("div");

        card.className = ownReview
            ? "review-card my-review"
            : "review-card";

        const header = document.createElement("div");

        header.className = "reviewer-header";

        const name = document.createElement("span");

        name.className = "reviewer-name";

        name.textContent = ownReview
            ? t("you")
            : "@" + review.username;

        const rating = document.createElement("span");

        rating.className = "review-rating";

        rating.textContent =
            "⭐".repeat(review.rating);

        const date = document.createElement("span");

        date.className = "review-date";

        const reviewDate = new Date(review.fechaCreacion);

        date.textContent = !isNaN(reviewDate.getTime())
            ? reviewDate.toLocaleDateString(locale)
            : "";

        header.append(name, rating, date);

        const comment = document.createElement("p");

        comment.className = "review-text";

        comment.textContent =
            review.comentario || t("noComment");

        card.append(header, comment);

        // Only show management controls on own reviews
        if (ownReview) {

            const actions = document.createElement("div");

            actions.className = "review-actions";

            const editButton = document.createElement("button");

            editButton.type = "button";
            editButton.className = "btn-edit-review";
            editButton.textContent = "✏️ " + t("edit");

            editButton.addEventListener("click", () => {
                editReview(review);
            });

            const deleteButton = document.createElement("button");

            deleteButton.type = "button";
            deleteButton.className = "btn-delete-review";
            deleteButton.textContent = "🗑️ " + t("delete");

            deleteButton.addEventListener("click", () => {
                deleteReview(review.id);
            });

            actions.append(editButton, deleteButton);

            card.append(actions);
        }

        return card;
    }

    // --------------------------------------------------
    // LOAD COMMUNITY REVIEWS
    // --------------------------------------------------

    async function loadCommunityReviews() {

        try {

            const reviews = await apiRequest(
                `/api/books/${bookId}/reviews`
            );

            communityList.replaceChildren();

            if (!Array.isArray(reviews) || reviews.length === 0) {

                communityList.textContent = t("noCommunityReviews");

                return;
            }

            reviews.forEach(review => {

                communityList.appendChild(
                    createReviewCard(review)
                );

            });

        } catch (error) {

            communityList.textContent =
                t("communityLoadError");

            console.error(error);
        }
    }

    // --------------------------------------------------
    // LOAD MY REVIEWS
    // --------------------------------------------------

    async function loadMyReviews() {

        if (!myReviewsList) return;

        try {

            const reviews = await apiRequest(
                `/api/books/${bookId}/reviews/mine`
            );

            myReviewsList.replaceChildren();

            if (!Array.isArray(reviews) || reviews.length === 0) {

                myReviewsList.textContent = t("noPersonalReviews");

                return;
            }

            reviews.forEach(review => {

                myReviewsList.appendChild(
                    createReviewCard(review, true)
                );

            });

        } catch (error) {

            myReviewsList.textContent =
                t("personalLoadError");

            console.error(error);
        }
    }

    // --------------------------------------------------
    // CREATE REVIEW
    // --------------------------------------------------

    if (reviewForm) {

        reviewForm.addEventListener("submit", async (event) => {

            event.preventDefault();

            const rating = Number(
                document.getElementById("reviewRating").value
            );

            const comentario = document.getElementById(
                "reviewComment"
            ).value.trim();

            if (!rating || rating < 1 || rating > 5) {

                reviewMessage.textContent =
                    t("invalidRating");

                return;
            }

            try {

                await apiRequest(
                    `/api/books/${bookId}/reviews`,
                    {
                        method: "POST",

                        headers: {
                            "Content-Type": "application/json"
                        },

                        body: JSON.stringify({
                            rating,
                            comentario
                        })
                    }
                );

                reviewForm.reset();

                reviewMessage.textContent = t("createSuccess");

                await Promise.all([
                    loadCommunityReviews(),
                    loadMyReviews()
                ]);

            } catch (error) {

                reviewMessage.textContent = error.message;

            }

        });
    }

    // --------------------------------------------------
    // MODAL MANAGEMENT
    // --------------------------------------------------

    function openReviewModal(mode, review = null) {

        modalMode = mode;
        selectedReview = review;

        previousFocus = document.activeElement;

        modalMessage.textContent = "";

        if (mode === "edit") {

            modalTitle.textContent = t("editTitle");
            modalEyebrow.textContent = t("yourReview");

            editContent.hidden = false;
            deleteContent.hidden = true;

            saveReviewButton.hidden = false;
            deleteReviewButton.hidden = true;

            editRating.value = review.rating;
            editComment.value = review.comentario || "";

            updateCharacterCount();

            setTimeout(() => editRating.focus(), 100);

        } else {

            modalTitle.textContent = t("deleteTitle");
            modalEyebrow.textContent = t("confirmAction");

            editContent.hidden = true;
            deleteContent.hidden = false;

            saveReviewButton.hidden = true;
            deleteReviewButton.hidden = false;

            setTimeout(() => deleteReviewButton.focus(), 100);
        }

        reviewModal.classList.add("active");
        reviewModal.setAttribute("aria-hidden", "false");

        document.body.style.overflow = "hidden";
    }

    function closeReviewModal() {

        if (saveReviewButton.disabled ||
            deleteReviewButton.disabled) {
            return;
        }

        reviewModal.classList.remove("active");
        reviewModal.setAttribute("aria-hidden", "true");

        document.body.style.overflow = "";

        selectedReview = null;
        modalMode = null;

        if (previousFocus) {
            previousFocus.focus();
        }
    }

    function updateCharacterCount() {

        characterCount.textContent =
            `${editComment.value.length} / 2000`;
    }

    // Close buttons

    cancelModalButton.addEventListener(
        "click",
        closeReviewModal
    );

    closeModalButton.addEventListener(
        "click",
        closeReviewModal
    );

    // Close when clicking outside the modal

    reviewModal.addEventListener("click", event => {

        if (event.target === reviewModal) {
            closeReviewModal();
        }
    });

    // Close with Escape

    document.addEventListener("keydown", event => {

        if (event.key === "Escape" &&
            reviewModal.classList.contains("active")) {

            closeReviewModal();
        }
    });

    // Character counter

    editComment.addEventListener(
        "input",
        updateCharacterCount
    );

    // --------------------------------------------------
    // EDIT REVIEW
    // --------------------------------------------------

    function editReview(review) {

        openReviewModal("edit", review);
    }

    editReviewForm.addEventListener("submit", async event => {

        event.preventDefault();

        if (!selectedReview) return;

        const rating = Number(editRating.value);
        const comentario = editComment.value.trim();

        if (!Number.isInteger(rating) ||
            rating < 1 || rating > 5) {

            modalMessage.textContent =
                t("invalidRating");

            return;
        }

        saveReviewButton.disabled = true;
        saveReviewButton.textContent = t("saving");

        cancelModalButton.disabled = true;
        closeModalButton.disabled = true;

        modalMessage.textContent = "";

        try {

            await apiRequest(
                `/api/reviews/${selectedReview.id}`,
                {
                    method: "PUT",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        rating,
                        comentario
                    })
                }
            );

            reviewModal.classList.remove("active");
            reviewModal.setAttribute("aria-hidden", "true");

            document.body.style.overflow = "";

            selectedReview = null;
            modalMode = null;

            await Promise.all([
                loadCommunityReviews(),
                loadMyReviews()
            ]);

        } catch (error) {

            modalMessage.textContent = error.message;

        } finally {

            saveReviewButton.disabled = false;
            saveReviewButton.textContent = t("saveChanges");

            cancelModalButton.disabled = false;
            closeModalButton.disabled = false;
        }

    });

    // --------------------------------------------------
    // DELETE REVIEW
    // --------------------------------------------------

    function deleteReview(reviewId) {

        openReviewModal("delete", { id: reviewId });
    }

    deleteReviewButton.addEventListener("click", async () => {

        if (!selectedReview) return;

        const reviewId = selectedReview.id;

        deleteReviewButton.disabled = true;
        deleteReviewButton.textContent = t("deleting");

        cancelModalButton.disabled = true;
        closeModalButton.disabled = true;

        modalMessage.textContent = "";

        try {

            await apiRequest(
                `/api/reviews/${reviewId}`,
                {
                    method: "DELETE"
                }
            );

            reviewModal.classList.remove("active");
            reviewModal.setAttribute("aria-hidden", "true");

            document.body.style.overflow = "";

            selectedReview = null;
            modalMode = null;

            await Promise.all([
                loadCommunityReviews(),
                loadMyReviews()
            ]);

        } catch (error) {

            modalMessage.textContent = error.message;

        } finally {

            deleteReviewButton.disabled = false;
            deleteReviewButton.textContent = t("deleteReview");

            cancelModalButton.disabled = false;
            closeModalButton.disabled = false;
        }

    });

    // --------------------------------------------------
    // INITIAL LOAD
    // --------------------------------------------------

    loadCommunityReviews();

    if (myReviewsList) {
        loadMyReviews();
    }

});
