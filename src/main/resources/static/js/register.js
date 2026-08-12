import { register } from "./auth.js";

const form = document.getElementById("registerForm");
const errorAlert = document.getElementById("errorAlert");

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorAlert.classList.add("d-none");

    const name = document.getElementById("name").value;
    const surname = document.getElementById("surname").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        await register(email, name, surname, password);
        window.location.href = "/dashboard.html";
    } catch (err) {
        errorAlert.textContent = err.message;
        errorAlert.classList.remove("d-none");
    }
});