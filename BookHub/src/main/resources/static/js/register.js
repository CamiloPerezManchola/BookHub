document.addEventListener("DOMContentLoaded", function () {

    const registerForm = document.getElementById("registerForm");

    if (!registerForm) return;

    registerForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        const password = document.getElementById("password").value;
        const confirmPassword = document.getElementById("confirmPassword").value;

        if (password !== confirmPassword) {
            alert("Passwords do not match.");
            return;
        }

        const formData = new FormData(registerForm);

        try {

            const response = await fetch(registerForm.action, {
                method: "POST",
                body: formData,
                redirect: "follow"
            });

            if (response.redirected) {
                window.location.href = response.url;
                return;
            }

            if (!response.ok) {
                throw new Error("Registration failed. Status: " + response.status);
            }

            window.location.href = "/login?registered=true";

        } catch (error) {

            console.error("Registration error:", error);
            alert("Something went wrong during registration.");

        }

    });

});