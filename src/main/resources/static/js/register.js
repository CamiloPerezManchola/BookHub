
document.addEventListener("DOMContentLoaded", function () {

    const registerForm = document.getElementById("registerForm");

    if (!registerForm) return;

    const password = document.getElementById("password");
    const confirmPassword = document.getElementById("confirmPassword");
    const errorContainer = document.getElementById("registerClientError");

    // Translated messages from Thymeleaf
    const messages = {
        passwordMismatch: registerForm.dataset.passwordMismatch,
        securityError: registerForm.dataset.securityError,
        registrationError: registerForm.dataset.registrationError,
        registrationFailed: registerForm.dataset.registrationFailed
    };

    messages.required = registerForm.dataset.required;
    messages.invalidEmail = registerForm.dataset.invalidEmail;
    messages.minLength = registerForm.dataset.minLength;

    // Validate fields using translated messages

    function validateForm() {

        const fields = registerForm.querySelectorAll(
            "input[required]"
        );

        for (const field of fields) {

            field.setCustomValidity("");

            if (!field.value.trim()) {
                field.setCustomValidity(messages.required);
            }

            else if (
                field.type === "email" &&
                !field.validity.valid
            ) {
                field.setCustomValidity(messages.invalidEmail);
            }

            else if (
                field.type === "password" &&
                field.value.length < 8
            ) {
                field.setCustomValidity(messages.minLength);
            }

            if (!field.validity.valid) {
                field.reportValidity();
                field.focus();
                return false;
            }
        }

        return true;
    }

    // Display client-side errors
    function showError(message) {
        errorContainer.textContent = message;
        errorContainer.hidden = false;
    }

    // Clear errors
    function clearError() {
        errorContainer.textContent = "";
        errorContainer.hidden = true;
    }

    // Clear errors when the user changes the form
    registerForm.addEventListener("input", clearError);

    // Clear password confirmation validation when changed
    confirmPassword.addEventListener("input", function () {
        confirmPassword.setCustomValidity("");
    });

    registerForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        clearError();

        // Validate all required fields before sending anything
        if (!validateForm()) {
            return;
        }

        // Check whether passwords match
        if (password.value !== confirmPassword.value) {

            confirmPassword.setCustomValidity(
                messages.passwordMismatch
            );

            confirmPassword.reportValidity();
            confirmPassword.focus();

            return;
        }

        confirmPassword.setCustomValidity("");

        clearError();

        const nombre = document.getElementById("nombre").value.trim();
        const username = document.getElementById("username").value.trim();
        const email = document.getElementById("email").value.trim();

        // Check whether passwords match
        if (password.value !== confirmPassword.value) {

            confirmPassword.setCustomValidity(
                messages.passwordMismatch
            );

            confirmPassword.reportValidity();

            return;
        }

        // Clear previous validation error
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
                throw new Error(messages.securityError);
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
                    messages.registrationFailed + " " + response.status
                );
            }

            const result = await response.json();

            console.log("User registered successfully:", result);

            window.location.href = "/login?registered=true";

        } catch (error) {

            console.error("Registration error:", error);

            showError(
                messages.registrationError + " " + error.message
            );

        }

    });

});
