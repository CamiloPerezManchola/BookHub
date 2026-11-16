document.addEventListener("DOMContentLoaded", function () {

    const registerForm = document.getElementById("registerForm");

    if (!registerForm) return;

    const password = document.getElementById("password");
    const confirmPassword = document.getElementById("confirmPassword");

    // Clear the custom error when the user changes the confirmation password
    confirmPassword.addEventListener("input", function () {
        confirmPassword.setCustomValidity("");
    });

    registerForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        const nombre = document.getElementById("nombre").value.trim();
        const username = document.getElementById("username").value.trim();
        const email = document.getElementById("email").value.trim();

        // Check whether passwords match
        if (password.value !== confirmPassword.value) {

            confirmPassword.setCustomValidity("Passwords do not match.");
            confirmPassword.reportValidity();

            return;
        }

        // Clear any previous validation error
        confirmPassword.setCustomValidity("");

        const userData = {
            nombre: nombre,
            username: username,
            email: email,
            password: password.value
        };

        try {

            const csrfToken = document.querySelector(
                'meta[name="_csrf"]'
            )?.getAttribute("content");

            const csrfHeader = document.querySelector(
                'meta[name="_csrf_header"]'
            )?.getAttribute("content");

            if (!csrfToken || !csrfHeader) {
                throw new Error("Security token unavailable. Please refresh the page.");
            }

            const response = await fetch(registerForm.action, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                credentials: "same-origin",
                body: JSON.stringify(userData)
            });

            if (!response.ok) {

                const errorText = await response.text();

                console.error("Server response:", errorText);

                throw new Error(
                    "Registration failed. Status: " + response.status
                );
            }

            const result = await response.json();

            console.log("User registered successfully:", result);

            window.location.href = "/login?registered=true";

        } catch (error) {

            console.error("Registration error:", error);

            // Display the error using the browser's native validation bubble
            registerForm.setCustomValidity(
                "Something went wrong during registration: " + error.message
            );

            registerForm.reportValidity();

            registerForm.addEventListener("input", function () {
                registerForm.setCustomValidity("");
            }, { once: true });

        }

    });

});
