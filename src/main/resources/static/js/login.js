import { login } from "./auth.js";

const form = document.getElementById("loginForm");
const errorAlert = document.getElementById("errorAlert");

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorAlert.classList.add("d-none");

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        await login(email, password);
        window.location.href = "/dashboard.html";
    } catch (err) {
        errorAlert.textContent = err.message;
        errorAlert.classList.remove("d-none");
    }
});