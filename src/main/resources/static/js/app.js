/**
 * TrackYours Sports - JavaScript principal
 */
(function () {
  
  // Año dinámico en el footer
  const yearEl = document.getElementById("year");
  if (yearEl) yearEl.textContent = new Date().getFullYear();

  // ========================================
  // SLIDER DE CLUBES
  // ========================================
  const viewport = document.getElementById("clubsViewport");
  const prevBtn = document.getElementById("clubsPrev");
  const nextBtn = document.getElementById("clubsNext");
  const slides = document.querySelectorAll(".club-slide");

  if (viewport && prevBtn && nextBtn && slides.length > 0) {
    
    const slideWidth = 276; // 260px card + 16px gap

    // Calcular posición máxima de scroll
    const getMaxScroll = () => viewport.scrollWidth - viewport.clientWidth;

    // Actualizar estado de botones
    const updateButtons = () => {
      const scrollPos = viewport.scrollLeft;
      prevBtn.disabled = scrollPos <= 5;
      nextBtn.disabled = scrollPos >= getMaxScroll() - 5;
    };

    // Mover slider con botones
    const scrollSlider = (direction) => {
      const amount = slideWidth * 2;
      viewport.scrollBy({
        left: direction === "next" ? amount : -amount,
        behavior: "smooth"
      });
    };

    // Eventos
    prevBtn.addEventListener("click", () => scrollSlider("prev"));
    nextBtn.addEventListener("click", () => scrollSlider("next"));
    viewport.addEventListener("scroll", updateButtons);
    window.addEventListener("resize", updateButtons);

    // Inicializar
    updateButtons();
  }

  // ========================================
  // FILTRO DE BÚSQUEDA DE CLUBES
  // ========================================
  const searchInput = document.getElementById("clubSearch");
  const clubItems = document.querySelectorAll(".club-item");

  if (searchInput && clubItems.length > 0) {
    searchInput.addEventListener("input", () => {
      const query = searchInput.value.trim().toLowerCase();
      
      clubItems.forEach((item) => {
        const name = (item.dataset.name || "").toLowerCase();
        item.style.display = name.includes(query) ? "" : "none";
      });

      // Resetear scroll
      if (viewport) viewport.scrollTo({ left: 0, behavior: "smooth" });
    });
  }

  // ========================================
  // MODAL DE DETALLE
  // ========================================
  const detailModalEl = document.getElementById("detailModal");
  
  if (detailModalEl) {
    detailModalEl.addEventListener("show.bs.modal", (ev) => {
      const btn = ev.relatedTarget;
      if (!btn) return;

      const title = btn.getAttribute("data-item-title") || "Detalle";
      const type = btn.getAttribute("data-item-type") || "item";
      const desc = btn.getAttribute("data-item-desc") || "—";
      const meta1 = btn.getAttribute("data-item-meta1") || "";
      const meta2 = btn.getAttribute("data-item-meta2") || "";

      const t = detailModalEl.querySelector("[data-detail-title]");
      const k = detailModalEl.querySelector("[data-detail-kind]");
      const d = detailModalEl.querySelector("[data-detail-desc]");
      const m1 = detailModalEl.querySelector("[data-detail-meta1]");
      const m2 = detailModalEl.querySelector("[data-detail-meta2]");

      if (t) t.textContent = title;
      if (k) k.textContent = type.toUpperCase();
      if (d) d.textContent = desc;
      if (m1) { m1.textContent = meta1; m1.classList.toggle("d-none", !meta1); }
      if (m2) { m2.textContent = meta2; m2.classList.toggle("d-none", !meta2); }
    });
  }

})();


