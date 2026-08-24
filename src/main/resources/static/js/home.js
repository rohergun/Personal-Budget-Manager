// Smooth-scroll for in-page anchor links (e.g. "Home" -> #home)
document.querySelectorAll('a[href^="#"]').forEach((link) => {
    link.addEventListener("click", (event) => {
        const targetId = link.getAttribute("href").slice(1);
        const target = document.getElementById(targetId);
        if (target) {
            event.preventDefault();
            target.scrollIntoView({ behavior: "smooth" });
        }
    });
});

// Subtle shadow on the fixed navbar once the page is scrolled
const nav = document.getElementById("mainNav");
if (nav) {
    window.addEventListener("scroll", () => {
        if (window.scrollY > 8) {
            nav.classList.add("shadow-sm");
        } else {
            nav.classList.remove("shadow-sm");
        }
    });
}