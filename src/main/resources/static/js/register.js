
document.addEventListener("DOMContentLoaded", function () {

    const registerForm = document.getElementById("registerForm");

    if (!registerForm) return;

    registerForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        const nombre = document.getElementById("nombre").value.trim();
        const username = document.getElementById("username").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;
        const confirmPassword = document.getElementById("confirmPassword").value;

        if (password !== confirmPassword) {
            alert("Passwords do not match.");
            return;
        }

        const userData = {
            nombre: nombre,
            username: username,
            email: email,
            password: password
        };

        try {
            
            const csrfToken = document.querySelector(
                'meta[name="_csrf"]'
            )?.getAttribute("content");

            const csrfHeader = document.querySelector(
                'meta[name="_csrf_header"]'
            )?.getAttribute("content");

            if (!csrfToken || !csrfHeader) {
                alert("Security token unavailable. Please refresh the page.");
                return;
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

            alert("Something went wrong during registration: " + error.message);

        }

    });

});
