
document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("createBookForm");
    const message = document.getElementById("bookMessage");
    const publishButton = document.getElementById("publishBookBtn");

    form.addEventListener("submit", async (event) => {

        event.preventDefault();

        const csrfToken =
            document.getElementById("csrfToken").value;

        const csrfHeader =
            document.getElementById("csrfHeader").value;

        const bookData = {

            titulo: document.getElementById("titulo").value.trim(),

            autor: document.getElementById("autor").value.trim(),

            portadaUrl:
                document.getElementById("portadaUrl").value.trim(),

            anio: Number(document.getElementById("anio").value),

            genero: document.getElementById("genero").value,

            sinopsis:
                document.getElementById("sinopsis").value.trim()

        };

        message.textContent = "";
        message.style.color = "";

        publishButton.disabled = true;
        publishButton.textContent = "Publishing...";

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

                throw new Error(
                    errorText || "Unable to publish the book."
                );

            }

            const savedBook = await response.json();

            message.textContent =
                "Your book has been published successfully!";

            message.style.color = "green";

            form.reset();

            setTimeout(() => {

                window.location.href =
                    `/libro/${savedBook.id}`;

            }, 1200);

        } catch (error) {

            console.error("Publishing error:", error);

            message.textContent =
                "Something went wrong while publishing your book.";

            message.style.color = "red";

        } finally {

            publishButton.disabled = false;
            publishButton.textContent = "Publish Book";

        }

    });

});
