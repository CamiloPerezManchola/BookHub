
document.addEventListener("DOMContentLoaded", () => {

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

        if (csrfValue && options.method &&
            options.method !== "GET") {

            headers[csrfHeader] = csrfValue;
        }

        const response = await fetch(url, {
            ...options,
            headers,
            credentials: "same-origin"
        });

        if (response.status === 401 ||
            response.status === 403) {

            throw new Error(
                "You don't have permission to perform this action."
            );
        }

        if (!response.ok) {

            const errorText = await response.text();

            throw new Error(
                errorText || "Something went wrong."
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
            ? "You"
            : "@" + review.username;

        const rating = document.createElement("span");

        rating.className = "review-rating";

        rating.textContent =
            "⭐".repeat(review.rating);

        const date = document.createElement("span");

        date.className = "review-date";

        date.textContent = new Date(
            review.fechaCreacion
        ).toLocaleDateString();

        header.append(name, rating, date);

        const comment = document.createElement("p");

        comment.className = "review-text";

        comment.textContent =
            review.comentario || "No comment provided.";

        card.append(header, comment);

        // Only show management controls on own reviews
        if (ownReview) {

            const actions = document.createElement("div");

            actions.className = "review-actions";

            const editButton = document.createElement("button");

            editButton.type = "button";
            editButton.className = "btn-edit-review";
            editButton.textContent = "Edit";

            editButton.addEventListener("click", () => {
                editReview(review);
            });

            const deleteButton = document.createElement("button");

            deleteButton.type = "button";
            deleteButton.className = "btn-delete-review";
            deleteButton.textContent = "Delete";

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

            if (reviews.length === 0) {

                communityList.textContent =
                    "No reviews yet. Be the first to share your thoughts!";

                return;
            }

            reviews.forEach(review => {

                communityList.appendChild(
                    createReviewCard(review)
                );

            });

        } catch (error) {

            communityList.textContent =
                "Unable to load community reviews.";

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

            if (reviews.length === 0) {

                myReviewsList.textContent =
                    "You haven't reviewed this book yet.";

                return;
            }

            reviews.forEach(review => {

                myReviewsList.appendChild(
                    createReviewCard(review, true)
                );

            });

        } catch (error) {

            myReviewsList.textContent =
                "Unable to load your reviews.";

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
                    "Please select a rating between 1 and 5.";

                return;
            }

            try {

                const review = await apiRequest(
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

                reviewMessage.textContent =
                    "Your review was published successfully!";

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
    // EDIT REVIEW
    // --------------------------------------------------

    async function editReview(review) {

        const newRating = prompt(
            "Enter your new rating (1-5):",
            review.rating
        );

        if (newRating === null) return;

        const rating = Number(newRating);

        if (!Number.isInteger(rating) ||
            rating < 1 || rating > 5) {

            alert("Rating must be between 1 and 5.");

            return;
        }

        const comentario = prompt(
            "Edit your comment:",
            review.comentario || ""
        );

        if (comentario === null) return;

        try {

            await apiRequest(
                `/api/reviews/${review.id}`,
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

            await Promise.all([
                loadCommunityReviews(),
                loadMyReviews()
            ]);

        } catch (error) {

            alert(error.message);
        }
    }

    // --------------------------------------------------
    // DELETE REVIEW
    // --------------------------------------------------

    async function deleteReview(reviewId) {

        if (!confirm(
            "Are you sure you want to delete this review?"
        )) return;

        try {

            await apiRequest(
                `/api/reviews/${reviewId}`,
                {
                    method: "DELETE"
                }
            );

            await Promise.all([
                loadCommunityReviews(),
                loadMyReviews()
            ]);

        } catch (error) {

            alert(error.message);
        }
    }

    // --------------------------------------------------
    // INITIAL LOAD
    // --------------------------------------------------

    loadCommunityReviews();

    if (myReviewsList) {
        loadMyReviews();
    }

});
