function slugify(text) {
    return text
        .toLowerCase()
        .trim()
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/(^-|-$)/g, "");
}

async function loadDoc(url, containerId) {
    const res = await fetch(url);
    const markdown = await res.text();
    const container = document.getElementById(containerId);
    container.innerHTML = marked.parse(markdown);
    return container;
}

// The markdown files have their own "## Table of Contents" section with
// in-page links — redundant now that we build a real sidebar, so remove it.
function removeTableOfContents(container) {
    const heading = Array.from(container.querySelectorAll("h2"))
        .find((h) => h.textContent.trim() === "Table of Contents");
    if (!heading) return;
    const next = heading.nextElementSibling;
    if (next && next.tagName === "UL") next.remove();
    heading.remove();
}

// Turn fenced ```mermaid code blocks into real <pre class="mermaid"> blocks
// so Mermaid.js can render them, instead of showing raw diagram syntax as text.
function convertMermaidBlocks(container) {
    container.querySelectorAll("code.language-mermaid").forEach((code) => {
        const pre = document.createElement("pre");
        pre.className = "mermaid";
        pre.textContent = code.textContent;
        code.parentElement.replaceWith(pre);
    });
}

// Assign a unique id to every h1/h2 (prefixed per-document so two docs can't
// collide, e.g. both having a "Frontend" section), tag them for styling,
// and collect them for the sidebar.
function processHeadings(container, docPrefix) {
    const headings = [];
    container.querySelectorAll("h1, h2").forEach((h) => {
        const id = `${docPrefix}-${slugify(h.textContent)}`;
        h.id = id;
        h.classList.add(h.tagName === "H1" ? "docs-h1" : "docs-h2");
        headings.push({ level: h.tagName, text: h.textContent, id });
    });
    return headings;
}

function buildSidebar(sections) {
    const toc = document.getElementById("docsToc");
    toc.innerHTML = "";

    sections.forEach((section, index) => {
        const h1 = section.headings.find((h) => h.level === "H1");
        if (!h1) return;

        const wrapper = document.createElement("div");
        wrapper.className = "mb-4";

        const topLink = document.createElement("a");
        topLink.href = `#${h1.id}`;
        topLink.className = "docs-toc-h1";
        topLink.textContent = `${index + 1}. ${section.title}`;
        wrapper.appendChild(topLink);

        const ul = document.createElement("ul");
        ul.className = "docs-toc-h2";
        section.headings
            .filter((h) => h.level === "H2")
            .forEach((h) => {
                const li = document.createElement("li");
                const a = document.createElement("a");
                a.href = `#${h.id}`;
                a.textContent = h.text;
                li.appendChild(a);
                ul.appendChild(li);
            });
        wrapper.appendChild(ul);

        toc.appendChild(wrapper);
    });
}

function wireSmoothScrollAndSpy() {
    document.getElementById("docsToc").addEventListener("click", (event) => {
        const link = event.target.closest('a[href^="#"]');
        if (!link) return;
        event.preventDefault();
        const id = link.getAttribute("href").slice(1);
        document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });
        history.pushState(null, "", `#${id}`);
    });

    const updateActiveLink = () => {
        const subheadings = document.querySelectorAll(".docs-h2");
        let currentId = null;
        subheadings.forEach((h) => {
            if (h.getBoundingClientRect().top <= 120) currentId = h.id;
        });
        document.querySelectorAll(".docs-toc-h2 a").forEach((a) => {
            a.classList.toggle("active", a.getAttribute("href") === `#${currentId}`);
        });
    };

    window.addEventListener("scroll", updateActiveLink);
    updateActiveLink();
}

async function init() {
    mermaid.initialize({ startOnLoad: false, theme: "neutral" });

    const architectureContainer = await loadDoc("/docs/ARCHITECTURE.md", "architectureContainer");
    removeTableOfContents(architectureContainer);
    convertMermaidBlocks(architectureContainer);
    const architectureHeadings = processHeadings(architectureContainer, "architecture");

    const decisionsContainer = await loadDoc("/docs/DECISIONS.md", "decisionsContainer");
    removeTableOfContents(decisionsContainer);
    convertMermaidBlocks(decisionsContainer);
    const decisionsHeadings = processHeadings(decisionsContainer, "decisions");

    buildSidebar([
        { title: "Architecture", headings: architectureHeadings },
        { title: "Design Decisions", headings: decisionsHeadings },
    ]);

    wireSmoothScrollAndSpy();
    mermaid.run({ querySelector: ".mermaid" });

    if (window.location.hash) {
        document.getElementById(window.location.hash.slice(1))
            ?.scrollIntoView({ behavior: "smooth" });
    }
}

init();