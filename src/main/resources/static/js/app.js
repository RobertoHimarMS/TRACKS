/************************************************************/
/***   TrackYours Sports - JavaScript Principal           ***/
/***   Funciones para sliders, búsqueda, login y modales  ***/
/************************************************************/

(function () {
    'use strict';

    /* ═════════════════════════════════════════════════════════
       INICIALIZACIÓN GLOBAL
       ═════════════════════════════════════════════════════════ */

    // --- Año dinámico en el footer ---
    const yearEl = document.getElementById('year');
    if (yearEl) {
        yearEl.textContent = new Date().getFullYear();
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 1: SLIDER GENÉRICO
       Función reutilizable para inicializar sliders con
       botones de navegación prev/next
       ═════════════════════════════════════════════════════════ */

    const initSlider = (config) => {
        const { viewportId, trackId, prevId, nextId } = config;

        // --- Obtener elementos del DOM ---
        const viewport = document.getElementById(viewportId);
        const track = document.getElementById(trackId);
        const prevBtn = document.getElementById(prevId);
        const nextBtn = document.getElementById(nextId);
        const slides = track ? track.querySelectorAll('.slide-item') : [];

        // --- Validar que existen todos los elementos ---
        if (!viewport || !track || !prevBtn || !nextBtn || slides.length === 0) {
            return null;
        }

        // --- Calcular el ancho de un slide + gap ---
        const getSlideStep = () => {
            const visibleSlide = Array.from(slides).find(slide => slide.offsetParent !== null) || slides[0];
            if (!visibleSlide) return 0;

            const slideWidth = visibleSlide.getBoundingClientRect().width;
            const trackStyles = window.getComputedStyle(track);
            const gap = parseFloat(trackStyles.columnGap || trackStyles.gap || 0);

            return slideWidth + gap;
        };

        // --- Calcular el scroll máximo ---
        const getMaxScroll = () => {
            return Math.max(0, viewport.scrollWidth - viewport.clientWidth);
        };

        // --- Calcular slides visibles ---
        const getVisibleSlides = () => {
            const step = getSlideStep();
            if (!step) return 1;
            return Math.max(1, Math.floor(viewport.clientWidth / step));
        };

        // --- Calcular cantidad de scroll ---
        const getScrollAmount = () => {
            const step = getSlideStep();
            const visibleSlides = getVisibleSlides();
            return step * visibleSlides;
        };

        // --- Actualizar estado de los botones ---
        const updateButtons = () => {
            const scrollLeft = viewport.scrollLeft;
            const maxScroll = getMaxScroll();
            const tolerance = 4;

            prevBtn.disabled = scrollLeft <= tolerance;
            nextBtn.disabled = scrollLeft >= maxScroll - tolerance;
        };

        // --- Desplazar el slider ---
        const scrollSlider = (direction) => {
            const amount = getScrollAmount();
            viewport.scrollBy({
                left: direction === 'next' ? amount : -amount,
                behavior: 'smooth'
            });
        };

        // --- Event listeners ---
        prevBtn.addEventListener('click', () => scrollSlider('prev'));
        nextBtn.addEventListener('click', () => scrollSlider('next'));
        viewport.addEventListener('scroll', updateButtons);
        window.addEventListener('resize', updateButtons);

        // --- Inicializar estado ---
        updateButtons();

        return viewport;
    };

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 2: INICIALIZACIÓN DE SLIDERS
       Se ejecutan al cargar el DOM
       ═════════════════════════════════════════════════════════ */

    let clubsViewport = null; // Referencia global para el buscador

    document.addEventListener('DOMContentLoaded', () => {
        // --- Slider de clubes (home.html) ---
        clubsViewport = initSlider({
            viewportId: 'clubsViewport',
            trackId: 'clubsTrack',
            prevId: 'clubsPrev',
            nextId: 'clubsNext'
        });

        // --- Slider de actividades (club.html) ---
        initSlider({
            viewportId: 'actividadesViewport',
            trackId: 'actividadesTrack',
            prevId: 'actividadesPrev',
            nextId: 'actividadesNext'
        });

        // --- Slider de publicaciones (club.html) ---
        initSlider({
            viewportId: 'publicacionesViewport',
            trackId: 'publicacionesTrack',
            prevId: 'publicacionesPrev',
            nextId: 'publicacionesNext'
        });

        // --- Slider de tienda (club.html) ---
        initSlider({
            viewportId: 'tiendaViewport',
            trackId: 'tiendaTrack',
            prevId: 'tiendaPrev',
            nextId: 'tiendaNext'
        });
    });

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 3: FILTRO DE BÚSQUEDA DE CLUBES
       Filtra los slides del slider de clubes
       ═════════════════════════════════════════════════════════ */

    const searchInput = document.getElementById('clubSearch');
    const clubItems = document.querySelectorAll('.slide-item');

    if (searchInput && clubItems.length > 0) {
        searchInput.addEventListener('input', () => {
            const query = searchInput.value.trim().toLowerCase();

            clubItems.forEach((item) => {
                const name = (item.dataset.name || '').toLowerCase();
                item.style.display = name.includes(query) ? '' : 'none';
            });

            // Resetear scroll al buscar
            if (clubsViewport) {
                clubsViewport.scrollTo({ left: 0, behavior: 'smooth' });
            }
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 4: MODAL DE DETALLE GENÉRICO
       Población dinámica del modal según data attributes
       ═════════════════════════════════════════════════════════ */

    const detailModalEl = document.getElementById('detailModal');

    if (detailModalEl) {
        detailModalEl.addEventListener('show.bs.modal', (ev) => {
            const btn = ev.relatedTarget;
            if (!btn) return;

            // --- Obtener datos de los atributos ---
            const title = btn.getAttribute('data-item-title') || 'Detalle';
            const type = btn.getAttribute('data-item-type') || 'item';
            const desc = btn.getAttribute('data-item-desc') || '—';
            const meta1 = btn.getAttribute('data-item-meta1') || '';
            const meta2 = btn.getAttribute('data-item-meta2') || '';

            // --- Referencias a los elementos del modal ---
            const t = detailModalEl.querySelector('[data-detail-title]');
            const k = detailModalEl.querySelector('[data-detail-kind]');
            const d = detailModalEl.querySelector('[data-detail-desc]');
            const m1 = detailModalEl.querySelector('[data-detail-meta1]');
            const m2 = detailModalEl.querySelector('[data-detail-meta2]');

            // --- Actualizar contenido ---
            if (t) t.textContent = title;
            if (k) k.textContent = type.toUpperCase();
            if (d) d.textContent = desc;
            if (m1) {
                m1.textContent = meta1;
                m1.classList.toggle('d-none', !meta1);
            }
            if (m2) {
                m2.textContent = meta2;
                m2.classList.toggle('d-none', !meta2);
            }
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 4.1: MODAL DE DETALLE DE ACTIVIDAD
       Población dinámica del modal de actividad
       ═════════════════════════════════════════════════════════ */

    const actividadModalEl = document.getElementById('actividadModal');

    if (actividadModalEl) {
        actividadModalEl.addEventListener('show.bs.modal', (ev) => {
            const btn = ev.relatedTarget;
            if (!btn) return;

            // --- Obtener datos de los atributos ---
            const title = btn.getAttribute('data-actividad-title') || '—';
            const desc = btn.getAttribute('data-actividad-desc') || '—';
            const sport = btn.getAttribute('data-actividad-sport') || '—';
            const fecha = btn.getAttribute('data-actividad-fecha') || '—';
            const place = btn.getAttribute('data-actividad-place') || '—';
            const dist = btn.getAttribute('data-actividad-dist');

            // --- Referencias a los elementos del modal ---
            const titleEls = actividadModalEl.querySelectorAll('[data-actividad-title]');
            const descEl = actividadModalEl.querySelector('[data-actividad-desc]');
            const sportEl = actividadModalEl.querySelector('[data-actividad-sport]');
            const fechaEl = actividadModalEl.querySelector('[data-actividad-fecha]');
            const placeEl = actividadModalEl.querySelector('[data-actividad-place]');
            const distEl = actividadModalEl.querySelector('[data-actividad-dist]');

            // --- Actualizar contenido ---
            titleEls.forEach(el => el.textContent = title);
            if (descEl) descEl.textContent = desc;
            if (sportEl) sportEl.textContent = sport;
            if (fechaEl) fechaEl.textContent = fecha;
            if (placeEl) placeEl.textContent = place;
            if (distEl) {
                distEl.textContent = dist && dist !== 'null' && dist !== '0' ? dist + ' km' : '—';
            }
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 4.2: MODAL DE DETALLE DE PUBLICACIÓN
       Población dinámica del modal de publicación
       ═════════════════════════════════════════════════════════ */

    const publicacionModalEl = document.getElementById('publicacionModal');

    if (publicacionModalEl) {
        publicacionModalEl.addEventListener('show.bs.modal', (ev) => {
            const btn = ev.relatedTarget;
            if (!btn) return;

            // --- Obtener datos de los atributos ---
            const subject = btn.getAttribute('data-publicacion-subject') || '—';
            const text = btn.getAttribute('data-publicacion-text') || '—';

            // --- Referencias a los elementos del modal ---
            const subjectEls = publicacionModalEl.querySelectorAll('[data-publicacion-subject]');
            const textEl = publicacionModalEl.querySelector('[data-publicacion-text]');

            // --- Actualizar contenido ---
            subjectEls.forEach(el => el.textContent = subject);
            if (textEl) textEl.textContent = text;
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 4.3: MODAL DE DETALLE DE PRODUCTO
       Población dinámica del modal de producto
       ═════════════════════════════════════════════════════════ */

    const productoModalEl = document.getElementById('productoModal');

    if (productoModalEl) {
        productoModalEl.addEventListener('show.bs.modal', (ev) => {
            const btn = ev.relatedTarget;
            if (!btn) return;

            // --- Obtener datos de los atributos ---
            const name = btn.getAttribute('data-producto-name') || '—';
            const desc = btn.getAttribute('data-producto-desc') || '—';
            const precio = btn.getAttribute('data-producto-precio') || '0,00';
            const stock = btn.getAttribute('data-producto-stock') || '0';
            const photo = btn.getAttribute('data-producto-photo');

            // --- Referencias a los elementos del modal ---
            const nameEls = productoModalEl.querySelectorAll('[data-producto-name]');
            const descEl = productoModalEl.querySelector('[data-producto-desc]');
            const precioEl = productoModalEl.querySelector('[data-producto-precio]');
            const stockEl = productoModalEl.querySelector('[data-producto-stock]');
            const imgContainer = productoModalEl.querySelector('#producto-imagen-container');
            const imgEl = productoModalEl.querySelector('#producto-imagen');

            // --- Actualizar contenido ---
            nameEls.forEach(el => el.textContent = name);
            if (descEl) descEl.textContent = desc;
            if (precioEl) precioEl.textContent = precio + ' €';
            if (stockEl) stockEl.textContent = stock + ' unidades';

            // --- Mostrar imagen si existe ---
            if (imgContainer && imgEl) {
                if (photo && photo !== 'null' && photo.trim() !== '') {
                    imgEl.src = CONTEXT_PATH + 'imgs/products/' + photo;
                    imgEl.alt = name;
                    imgContainer.style.display = 'block';
                } else {
                    imgContainer.style.display = 'none';
                }
            }
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 5: LOGIN - CARGA DINÁMICA DE CLUBES
       Carga los clubs del usuario vía AJAX cuando sale del email
       ═════════════════════════════════════════════════════════ */

    const authModal = document.getElementById('authModal');
    const loginEmail = document.getElementById('loginEmail');
    const clubSelector = document.getElementById('clubSelector');

    // --- Limpiar modal al abrirse ---
    if (authModal) {
        authModal.addEventListener('show.bs.modal', function() {
            const loginForm = document.getElementById('loginForm');
            if (loginForm) {
                loginForm.reset();
            }
            clubSelector.innerHTML = '<option value="">Introduce tu email para ver tus clubs</option>';
            clubSelector.disabled = true;
        });
    }

    if (loginEmail) {
        loginEmail.addEventListener('blur', function() {
            const email = this.value.trim();

            if (email && email.includes('@')) {
                $.ajax({
                    url: CONTEXT_PATH + 'api/misclubes',
                    method: 'GET',
                    data: { email: email },
                    dataType: 'json',
                    success: function(clubs) {
                        clubSelector.innerHTML = '';

                        if (clubs.length > 0) {
                            clubs.forEach(function(club) {
                                const option = document.createElement('option');
                                option.value = club.idclub;
                                option.textContent = club.name + ' (' + club.sport + ')';
                                clubSelector.appendChild(option);
                            });
                            clubSelector.disabled = false;
                        } else {
                            const option = document.createElement('option');
                            option.value = '';
                            option.textContent = 'No tienes clubs activos';
                            clubSelector.appendChild(option);
                            clubSelector.disabled = true;
                        }
                    },
                    error: function() {
                        clubSelector.innerHTML = '';
                        const option = document.createElement('option');
                        option.value = '';
                        option.textContent = 'Error al cargar clubs';
                        clubSelector.appendChild(option);
                        clubSelector.disabled = true;
                    }
                });
            } else {
                clubSelector.innerHTML = '';
                const option = document.createElement('option');
                option.value = '';
                option.textContent = 'Introduce tu email para ver tus clubs';
                clubSelector.appendChild(option);
                clubSelector.disabled = true;
            }
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 6: FORMULARIO DE CONTACTO
       Validación y envío del ticket
       ═════════════════════════════════════════════════════════ */

    const contactForm = document.getElementById('contactForm');

    if (contactForm) {
        const subjectInput = document.getElementById('ticketSubject');
        const emailInput = document.getElementById('ticketEmail');
        const messageInput = document.getElementById('ticketMessage');
        const submitBtn = document.getElementById('btnSendTicket');

        // --- Función para marcar/desmarcar error ---
        const setFieldError = (input, hasError) => {
            if (hasError) {
                input.style.borderColor = '#f59e0b';
                input.style.borderWidth = '2px';
            } else {
                input.style.borderColor = '';
                input.style.borderWidth = '';
            }
        };

        // --- Limpiar errores al escribir ---
        [subjectInput, emailInput, messageInput].forEach(input => {
            input.addEventListener('input', () => setFieldError(input, false));
        });

        // --- Validación y envío ---
        contactForm.addEventListener('submit', function(e) {
            e.preventDefault();

            let isValid = true;

            // Validar asunto
            if (!subjectInput.value.trim()) {
                setFieldError(subjectInput, true);
                isValid = false;
            } else {
                setFieldError(subjectInput, false);
            }

            // Validar email
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailInput.value.trim() || !emailRegex.test(emailInput.value.trim())) {
                setFieldError(emailInput, true);
                isValid = false;
            } else {
                setFieldError(emailInput, false);
            }

            // Validar mensaje
            if (!messageInput.value.trim()) {
                setFieldError(messageInput, true);
                isValid = false;
            } else {
                setFieldError(messageInput, false);
            }

            if (!isValid) return;

            // Enviar formulario
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-2"></i>Enviando...';

            // Obtener token CSRF
            const csrfToken = $('meta[name="_csrf"]').attr('content');

            $.ajax({
                url: CONTEXT_PATH + 'api/ticket/new',
                method: 'POST',
                data: {
                    subject: subjectInput.value.trim(),
                    email: emailInput.value.trim(),
                    description: messageInput.value.trim(),
                    _csrf: csrfToken
                },
                success: function(response) {
                    // Mostrar mensaje de éxito
                    mostrarMensaje(response.message || 'Ticket enviado correctamente', 'success');
                    // Limpiar formulario
                    contactForm.reset();
                },
                error: function() {
                    mostrarMensaje('Error al enviar el ticket. Inténtelo de nuevo.', 'danger');
                },
                complete: function() {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fa-solid fa-paper-plane me-2"></i>Enviar';
                }
            });
        });

        // --- Limpiar formulario ---
        document.getElementById('btnClearTicket').addEventListener('click', function() {
            [subjectInput, emailInput, messageInput].forEach(input => {
                setFieldError(input, false);
            });
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 7: PANEL DE ADMINISTRACIÓN
       Ver detalles de tickets y solicitudes
       ═════════════════════════════════════════════════════════ */

    // --- Ver detalles de Ticket ---
    const ticketModal = document.getElementById('ticketModal');
    const viewTicketBtns = document.querySelectorAll('.view-ticket-btn');

    if (ticketModal && viewTicketBtns.length > 0) {
        viewTicketBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const ticketId = this.getAttribute('data-id');

                $.ajax({
                    url: CONTEXT_PATH + 'api/ticket/ver/' + ticketId,
                    method: 'GET',
                    success: function(ticket) {
                        document.getElementById('ticket-subject').textContent = ticket.subject || 'Sin asunto';
                        document.getElementById('ticket-asunto').textContent = ticket.subject || 'Sin asunto';
                        document.getElementById('ticket-descripcion').textContent = ticket.description || 'Sin descripción';
                        document.getElementById('ticket-email').textContent = ticket.email || 'Sin email';

                        const estadoBadge = document.getElementById('ticket-estado');
                        if (ticket.handled) {
                            estadoBadge.textContent = 'Resuelto';
                            estadoBadge.className = 'badge badge-status badge-success';
                        } else {
                            estadoBadge.textContent = 'Pendiente';
                            estadoBadge.className = 'badge badge-status badge-warning';
                        }

                        const modal = new bootstrap.Modal(ticketModal);
                        modal.show();
                    },
                    error: function() {
                        alert('Error al cargar el ticket. Inténtelo de nuevo.');
                    }
                });
            });
        });
    }

    // --- Resolver Ticket ---
    const resolveTicketBtns = document.querySelectorAll('#tickets-soporte .check-btn');

    if (resolveTicketBtns.length > 0) {
        resolveTicketBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const ticketId = this.getAttribute('data-id');
                const row = this.closest('tr');
                const badge = row.querySelector('.badge-status');
                const checkBtn = this;

                // Obtener CSRF token
                const csrfToken = $('meta[name="_csrf"]').attr('content');

                $.ajax({
                    url: CONTEXT_PATH + 'api/ticket/edit/' + ticketId,
                    method: 'POST',
                    data: { _csrf: csrfToken },
                    success: function() {
                        // Ocultar botón check
                        checkBtn.style.display = 'none';
                        // Actualizar badge
                        badge.textContent = 'Resuelto';
                        badge.className = 'badge badge-status badge-success';
                    },
                    error: function(xhr) {
                        if (xhr.status === 401) {
                            alert('No autorizado. Por favor, inicie sesión.');
                        } else {
                            alert('Error al resolver el ticket. Inténtelo de nuevo.');
                        }
                    }
                });
            });
        });
    }

    // --- Ver detalles de Solicitud de Club ---
    const solicitudClubModal = document.getElementById('solicitudClubModal');
    const viewRequestBtns = document.querySelectorAll('.view-request-btn');

    if (solicitudClubModal && viewRequestBtns.length > 0) {
        viewRequestBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const requestId = this.getAttribute('data-id');

                $.ajax({
                    url: CONTEXT_PATH + 'api/request/ver/' + requestId,
                    method: 'GET',
                    success: function(req) {
                        // Título del modal
                        document.getElementById('requestClubNombre').textContent = (req.clbTarget || 'Club') + ' - ' + (req.clbSport || 'Deporte');

                        // Datos del club
                        document.getElementById('req-clb-target').textContent = req.clbTarget || '—';
                        document.getElementById('req-clb-sport').textContent = req.clbSport || '—';
                        document.getElementById('req-clb-description').textContent = req.clbDescription || '—';
                        document.getElementById('req-clb-email').textContent = req.clbEmail || '—';
                        document.getElementById('req-clb-cp').textContent = req.clbCp || '—';
                        document.getElementById('req-clb-city').textContent = req.clbCity || '—';

                        // Datos del gestor
                        document.getElementById('req-usr-name').textContent = req.usrName || '—';
                        document.getElementById('req-usr-surname').textContent = req.usrSurname || '—';
                        document.getElementById('req-usr-dni').textContent = req.usrDni || '—';
                        document.getElementById('req-usr-email').textContent = req.usrEmail || '—';
                        document.getElementById('req-usr-phone').textContent = req.usrPhone || '—';
                        document.getElementById('req-usr-city').textContent = req.usrCity || '—';

                        // Estado
                        const estadoBadge = document.getElementById('req-estado');
                        if (req.estado === 'pending') {
                            estadoBadge.textContent = 'Pendiente';
                            estadoBadge.className = 'badge badge-status badge-warning';
                        } else if (req.estado === 'accepted') {
                            estadoBadge.textContent = 'Aceptada';
                            estadoBadge.className = 'badge badge-status badge-success';
                        } else {
                            estadoBadge.textContent = 'Rechazada';
                            estadoBadge.className = 'badge badge-status badge-danger';
                        }

                        const modal = new bootstrap.Modal(solicitudClubModal);
                        modal.show();
                    },
                    error: function() {
                        alert('Error al cargar la solicitud. Inténtelo de nuevo.');
                    }
                });
            });
        });
    }

    // --- Ver detalles de Solicitud de Socio ---
    const solicitudSocioModal = document.getElementById('solicitudSocioModal');
    const viewPartnerRequestBtns = document.querySelectorAll('.view-partner-request-btn');

    if (solicitudSocioModal && viewPartnerRequestBtns.length > 0) {
        viewPartnerRequestBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const requestId = this.getAttribute('data-id');

                $.ajax({
                    url: CONTEXT_PATH + 'api/request/ver/' + requestId,
                    method: 'GET',
                    success: function(req) {
                        // Título del modal
                        document.getElementById('partnerRequestNombre').textContent = (req.usrName || 'Nombre') + ' ' + (req.usrSurname || '');

                        // Club solicitado
                        document.getElementById('partner-club-name').textContent = (req.clbTarget || 'Club') + ' - ' + (req.clbSport || 'Deporte');

                        // Datos del solicitante
                        document.getElementById('partner-usr-name').textContent = req.usrName || '—';
                        document.getElementById('partner-usr-surname').textContent = req.usrSurname || '—';
                        document.getElementById('partner-usr-dni').textContent = req.usrDni || '—';
                        document.getElementById('partner-usr-email').textContent = req.usrEmail || '—';
                        document.getElementById('partner-usr-phone').textContent = req.usrPhone || '—';
                        document.getElementById('partner-usr-city').textContent = (req.usrCity || '—') + ' (' + (req.usrCp || '—') + ')';

                        // Estado
                        const estadoBadge = document.getElementById('partner-estado');
                        if (req.estado === 'pending') {
                            estadoBadge.textContent = 'Pendiente';
                            estadoBadge.className = 'badge badge-status badge-warning';
                        } else if (req.estado === 'accepted') {
                            estadoBadge.textContent = 'Aceptada';
                            estadoBadge.className = 'badge badge-status badge-success';
                        } else {
                            estadoBadge.textContent = 'Rechazada';
                            estadoBadge.className = 'badge badge-status badge-danger';
                        }

                        const modal = new bootstrap.Modal(solicitudSocioModal);
                        modal.show();
                    },
                    error: function() {
                        alert('Error al cargar la solicitud. Inténtelo de nuevo.');
                    }
                });
            });
        });
    }

    // --- Aprobar Solicitud de Club ---
    const approveClubRequestBtns = document.querySelectorAll('#solicitudes-clubes .check-btn');

    if (approveClubRequestBtns.length > 0) {
        approveClubRequestBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const requestId = this.getAttribute('data-id');
                const row = this.closest('tr');
                const badge = row.querySelector('.badge-status');
                const checkBtn = this;
                const deleteBtn = row.querySelector('.delete-btn');

                // Obtener CSRF token
                const csrfToken = $('meta[name="_csrf"]').attr('content');

                $.ajax({
                    url: CONTEXT_PATH + 'api/request/edit/' + requestId,
                    method: 'POST',
                    data: { accept: true, _csrf: csrfToken },
                    dataType: 'json',
                    success: function(response) {
                        // Ocultar botones de acción
                        checkBtn.style.display = 'none';
                        if (deleteBtn) deleteBtn.style.display = 'none';
                        // Actualizar badge según estado
                        if (response.status === 'accepted') {
                            badge.textContent = 'Aceptada';
                            badge.className = 'badge badge-status badge-success';
                        } else {
                            badge.textContent = 'Rechazada';
                            badge.className = 'badge badge-status badge-danger';
                        }
                    },
                    error: function(xhr) {
                        if (xhr.status === 401) {
                            alert('No autorizado. Por favor, inicie sesión.');
                        } else {
                            alert('Error al aceptar la solicitud. Inténtelo de nuevo.');
                        }
                    }
                });
            });
        });
    }

    // --- Rechazar Solicitud de Club ---
    const rejectClubRequestBtns = document.querySelectorAll('#solicitudes-clubes .delete-btn');

    if (rejectClubRequestBtns.length > 0) {
        rejectClubRequestBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const requestId = this.getAttribute('data-id');
                const row = this.closest('tr');
                const badge = row.querySelector('.badge-status');
                const checkBtn = row.querySelector('.check-btn');
                const deleteBtn = this;

                // Obtener CSRF token
                const csrfToken = $('meta[name="_csrf"]').attr('content');

                $.ajax({
                    url: CONTEXT_PATH + 'api/request/edit/' + requestId,
                    method: 'POST',
                    data: { accept: false, _csrf: csrfToken },
                    dataType: 'json',
                    success: function(response) {
                        // Ocultar botones de acción
                        if (checkBtn) checkBtn.style.display = 'none';
                        deleteBtn.style.display = 'none';
                        // Actualizar badge según estado
                        if (response.status === 'accepted') {
                            badge.textContent = 'Aceptada';
                            badge.className = 'badge badge-status badge-success';
                        } else {
                            badge.textContent = 'Rechazada';
                            badge.className = 'badge badge-status badge-danger';
                        }
                    },
                    error: function(xhr) {
                        if (xhr.status === 401) {
                            alert('No autorizado. Por favor, inicie sesión.');
                        } else {
                            alert('Error al rechazar la solicitud. Inténtelo de nuevo.');
                        }
                    }
                });
            });
        });
    }

    // --- Aprobar Solicitud de Socio ---
    const approvePartnerRequestBtns = document.querySelectorAll('#solicitudes-socios .check-btn');

    if (approvePartnerRequestBtns.length > 0) {
        approvePartnerRequestBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const requestId = this.getAttribute('data-id');
                const row = this.closest('tr');
                const badge = row.querySelector('.badge-status');
                const checkBtn = this;
                const deleteBtn = row.querySelector('.delete-btn');

                // Obtener CSRF token
                const csrfToken = $('meta[name="_csrf"]').attr('content');

                $.ajax({
                    url: CONTEXT_PATH + 'api/request/edit/' + requestId,
                    method: 'POST',
                    data: { accept: true, _csrf: csrfToken },
                    dataType: 'json',
                    success: function(response) {
                        // Ocultar botones de acción
                        checkBtn.style.display = 'none';
                        if (deleteBtn) deleteBtn.style.display = 'none';
                        // Actualizar badge según estado
                        if (response.status === 'accepted') {
                            badge.textContent = 'Aceptada';
                            badge.className = 'badge badge-status badge-success';
                        } else {
                            badge.textContent = 'Rechazada';
                            badge.className = 'badge badge-status badge-danger';
                        }
                    },
                    error: function(xhr) {
                        if (xhr.status === 401) {
                            alert('No autorizado. Por favor, inicie sesión.');
                        } else {
                            alert('Error al aceptar la solicitud. Inténtelo de nuevo.');
                        }
                    }
                });
            });
        });
    }

    // --- Rechazar Solicitud de Socio ---
    const rejectPartnerRequestBtns = document.querySelectorAll('#solicitudes-socios .delete-btn');

    if (rejectPartnerRequestBtns.length > 0) {
        rejectPartnerRequestBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const requestId = this.getAttribute('data-id');
                const row = this.closest('tr');
                const badge = row.querySelector('.badge-status');
                const checkBtn = row.querySelector('.check-btn');
                const deleteBtn = this;

                // Obtener CSRF token
                const csrfToken = $('meta[name="_csrf"]').attr('content');

                $.ajax({
                    url: CONTEXT_PATH + 'api/request/edit/' + requestId,
                    method: 'POST',
                    data: { accept: false, _csrf: csrfToken },
                    dataType: 'json',
                    success: function(response) {
                        // Ocultar botones de acción
                        if (checkBtn) checkBtn.style.display = 'none';
                        deleteBtn.style.display = 'none';
                        // Actualizar badge según estado
                        if (response.status === 'accepted') {
                            badge.textContent = 'Aceptada';
                            badge.className = 'badge badge-status badge-success';
                        } else {
                            badge.textContent = 'Rechazada';
                            badge.className = 'badge badge-status badge-danger';
                        }
                    },
                    error: function(xhr) {
                        if (xhr.status === 401) {
                            alert('No autorizado. Por favor, inicie sesión.');
                        } else {
                            alert('Error al rechazar la solicitud. Inténtelo de nuevo.');
                        }
                    }
                });
            });
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 8: MODAL DE BAJA DE CLUB
       Confirmación antes de darse de baja
       ═════════════════════════════════════════════════════════ */

    const bajaModalEl = document.getElementById('bajaModal');

    if (bajaModalEl) {
        bajaModalEl.addEventListener('show.bs.modal', function(ev) {
            const btn = ev.relatedTarget;
            if (!btn) return;

            // Obtener nombre del club
            const clubName = btn.getAttribute('data-club-name') || 'este club';

            // Actualizar mensaje
            const mensajeEl = bajaModalEl.querySelector('#bajaMensaje');
            if (mensajeEl) {
                mensajeEl.textContent = '¿Confirma que se va a dar de baja en el club ' + clubName + '?';
            }

            // URL sin parámetros - usa datos de sesión del servidor
            const confirmarBtn = bajaModalEl.querySelector('#bajaConfirmarBtn');
            if (confirmarBtn) {
                confirmarBtn.href = CONTEXT_PATH + 'club/unsuscribe';
            }
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 9: MODAL DE DETALLE DE COMPRA (CARNÉ)
       Población dinámica del modal de compra
       ═════════════════════════════════════════════════════════ */

    const compraModalEl = document.getElementById('compraModal');

    if (compraModalEl) {
        compraModalEl.addEventListener('show.bs.modal', function(ev) {
            const btn = ev.relatedTarget;
            if (!btn) return;

            // Obtener datos del botón
            const nombre = btn.getAttribute('data-compra-nombre') || '—';
            const desc = btn.getAttribute('data-compra-desc') || '—';
            const precio = btn.getAttribute('data-compra-precio') || '0,00';
            const cant = btn.getAttribute('data-compra-cant') || '1';
            const total = btn.getAttribute('data-compra-total') || '0,00';
            const estado = btn.getAttribute('data-compra-estado') || 'reserved';

            // Referencias a elementos
            const nombreEls = compraModalEl.querySelectorAll('[data-compra-nombre]');
            const descEl = compraModalEl.querySelector('[data-compra-desc]');
            const precioEl = compraModalEl.querySelector('[data-compra-precio]');
            const cantEl = compraModalEl.querySelector('[data-compra-cant]');
            const totalEl = compraModalEl.querySelector('[data-compra-total]');
            const estadoBadge = compraModalEl.querySelector('#compra-estado-badge');

            // Actualizar contenido
            nombreEls.forEach(el => el.textContent = nombre);
            if (descEl) descEl.textContent = desc;
            if (precioEl) precioEl.textContent = precio;
            if (cantEl) cantEl.textContent = cant;
            if (totalEl) totalEl.textContent = total;

            // Actualizar badge de estado
            if (estadoBadge) {
                if (estado === 'reserved') {
                    estadoBadge.textContent = 'Pendiente';
                    estadoBadge.className = 'badge badge-status badge-warning';
                } else if (estado === 'paid') {
                    estadoBadge.textContent = 'Pagado';
                    estadoBadge.className = 'badge badge-status badge-success';
                } else if (estado === 'collected') {
                    estadoBadge.textContent = 'Entregado';
                    estadoBadge.className = 'badge badge-status badge-success';
                }
            }
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 10: TOGGLE DE INSCRIPCIÓN A ACTIVIDADES
       Maneja la inscripción/desinscripción mediante AJAX
       ═════════════════════════════════════════════════════════ */

    const toggleSwitches = document.querySelectorAll('.toggle-switch input[type="checkbox"][data-actividad-id]');

    if (toggleSwitches.length > 0) {
        // Obtener token CSRF de los meta tags
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        toggleSwitches.forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                const actividadId = this.getAttribute('data-actividad-id');
                const isChecked = this.checked;

                // Determinar acción y URL
                const action = isChecked ? 'suscribe' : 'unsuscribe';
                const url = CONTEXT_PATH + `user/activity/${action}/${actividadId}`;

                // Referencia al checkbox
                const toggleInput = this;
                const toggleLabel = this.closest('.toggle-switch');

                // Preparar headers
                const headers = {
                    'X-Requested-With': 'XMLHttpRequest'
                };
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                // Llamada AJAX
                fetch(url, {
                    method: 'POST',
                    headers: headers
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success && toggleLabel) {
                        // Actualizar title
                        toggleLabel.title = isChecked ? 'Desinscribirse' : 'Inscribirse';
                    } else {
                        // Revertir el cambio del checkbox si falla
                        toggleInput.checked = !isChecked;
                        console.error('Error:', data.error);
                    }
                })
                .catch(error => {
                    // Revertir el cambio del checkbox si falla
                    toggleInput.checked = !isChecked;
                    console.error('Error de conexión:', error);
                });
            });
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 11: BOTÓN ELIMINAR INSCRIPCIÓN (CARNÉ)
       Botón de aspa roja para desinscribirse en "Mis actividades"
       ═════════════════════════════════════════════════════════ */

    const deleteInscripcionBtns = document.querySelectorAll('.delete-btn[data-actividad-id]');

    if (deleteInscripcionBtns.length > 0) {
        // Obtener token CSRF de los meta tags
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        deleteInscripcionBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const actividadId = this.getAttribute('data-actividad-id');
                const url = CONTEXT_PATH + `user/activity/unsuscribe/${actividadId}`;
                const row = this.closest('tr');

                // Preparar headers
                const headers = {
                    'X-Requested-With': 'XMLHttpRequest'
                };
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                // Llamada AJAX
                fetch(url, {
                    method: 'POST',
                    headers: headers
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success && row) {
                        // Eliminar fila con transición
                        row.style.transition = 'opacity 0.3s ease';
                        row.style.opacity = '0';
                        setTimeout(() => {
                            row.remove();
                        }, 300);
                    } else {
                        console.error('Error:', data.error);
                    }
                })
                .catch(error => {
                    console.error('Error de conexión:', error);
                });
            });
        });
    }

    /**
     * Muestra un mensaje dinámico en el contenedor superior
     * @param {string} mensaje - Texto del mensaje
     * @param {string} tipo - 'success', 'danger', 'warning'
     */
    function mostrarMensaje(mensaje, tipo) {
        const container = document.getElementById('mensajeContainer');
        if (!container) return;

        // Limpiar mensajes anteriores
        container.innerHTML = '';

        // Crear alerta
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${tipo} alert-dismissible fade show`;
        alertDiv.setAttribute('role', 'alert');

        const icon = tipo === 'success' ? 'fa-circle-check' :
                     tipo === 'danger' ? 'fa-circle-exclamation' : 'fa-triangle-exclamation';

        alertDiv.innerHTML = `
            <i class="fa-solid ${icon} me-2"></i>
            <strong>${mensaje}</strong>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
        `;

        container.appendChild(alertDiv);

        // Scroll al inicio para ver el mensaje
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 12: COMPRA DE PRODUCTOS (TIENDA)
       Botón de carrito para reservar productos
       ═════════════════════════════════════════════════════════ */

    const comprarBtns = document.querySelectorAll('.check-btn[data-producto-id]');

    if (comprarBtns.length > 0) {
        // Obtener token CSRF de los meta tags
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        comprarBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const productoId = this.getAttribute('data-producto-id');
                const qtyInput = document.getElementById('qty-' + productoId);
                const cantidad = parseInt(qtyInput?.value || 0, 10);

                // Verificar cantidad válida
                if (cantidad <= 0) {
                    mostrarMensajeTienda('La cantidad debe ser mayor que 0', 'warning');
                    return;
                }

                // Preparar headers
                const headers = {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                };
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                // Llamada AJAX
                fetch(CONTEXT_PATH + `user/product/reserve/${productoId}?cantidad=${cantidad}`, {
                    method: 'POST',
                    headers: headers
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        mostrarMensajeTienda(data.message, 'success');
                        // Resetear input de cantidad
                        if (qtyInput) qtyInput.value = 0;
                    } else {
                        mostrarMensajeTienda(data.error, 'danger');
                    }
                })
                .catch(error => {
                    mostrarMensajeTienda('Error de conexión', 'danger');
                    console.error('Error:', error);
                });
            });
        });
    }

    /**
     * Muestra un mensaje dinámico en el contenedor de la sección Tienda
     * @param {string} mensaje - Texto del mensaje
     * @param {string} tipo - 'success', 'danger', 'warning'
     */
    function mostrarMensajeTienda(mensaje, tipo) {
        const container = document.getElementById('mensajeContainerTienda');
        if (!container) return;

        // Limpiar mensajes anteriores
        container.innerHTML = '';

        // Crear alerta
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${tipo} alert-dismissible fade show`;
        alertDiv.setAttribute('role', 'alert');

        const icon = tipo === 'success' ? 'fa-circle-check' :
                     tipo === 'danger' ? 'fa-circle-exclamation' : 'fa-triangle-exclamation';

        alertDiv.innerHTML = `
            <i class="fa-solid ${icon} me-2"></i>
            <strong>${mensaje}</strong>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
        `;

        container.appendChild(alertDiv);
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 13: CANCELAR COMPRA (CARNÉ)
       Botón de aspa roja para cancelar compras pendientes
       ═════════════════════════════════════════════════════════ */

    const cancelarCompraBtns = document.querySelectorAll('.delete-btn[data-compra-id]');

    if (cancelarCompraBtns.length > 0) {
        // Obtener token CSRF de los meta tags
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        cancelarCompraBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const compraId = this.getAttribute('data-compra-id');
                const row = this.closest('tr');

                // Preparar headers
                const headers = {
                    'X-Requested-With': 'XMLHttpRequest'
                };
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                // Llamada AJAX
                fetch(CONTEXT_PATH + `user/product/cancel/${compraId}`, {
                    method: 'POST',
                    headers: headers
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        // Actualizar badge de estado
                        const badge = row.querySelector('.badge-status');
                        if (badge) {
                            badge.className = 'badge badge-status badge-danger';
                            badge.textContent = 'Cancelado';
                        }
                        // Ocultar botón cancelar
                        btn.style.display = 'none';
                    } else {
                        console.error('Error:', data.error);
                    }
                })
                .catch(error => {
                    console.error('Error de conexión:', error);
                });
            });
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 14: ELIMINAR ACTIVIDAD (CLUB)
       Botón de papelera para eliminar actividades del club
       ═════════════════════════════════════════════════════════ */

    const deleteActividadBtns = document.querySelectorAll('.delete-btn[data-delete-actividad]');

    if (deleteActividadBtns.length > 0) {
        // Obtener token CSRF de los meta tags
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        deleteActividadBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const actividadId = this.getAttribute('data-delete-actividad');
                const url = CONTEXT_PATH + `api/activity/delete/${actividadId}`;

                // Confirmar antes de eliminar
                if (!confirm('¿Estás seguro de que quieres eliminar esta actividad?')) {
                    return;
                }

                // Preparar headers
                const headers = {
                    'X-Requested-With': 'XMLHttpRequest'
                };
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                // Llamada AJAX
                fetch(url, {
                    method: 'POST',
                    headers: headers
                })
                .then(response => {
                    // Como devuelve RedirectView, recargamos la página
                    window.location.reload();
                })
                .catch(error => {
                    console.error('Error de conexión:', error);
                    mostrarMensaje('Error al eliminar la actividad', 'danger');
                });
            });
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 15: ELIMINAR PUBLICACIÓN (CLUB)
       Botón de papelera para eliminar publicaciones del club
       ═════════════════════════════════════════════════════════ */

    const deletePublicacionBtns = document.querySelectorAll('.delete-btn[data-delete-publicacion]');

    if (deletePublicacionBtns.length > 0) {
        // Obtener token CSRF de los meta tags
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        deletePublicacionBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const publicacionId = this.getAttribute('data-delete-publicacion');
                const url = CONTEXT_PATH + `api/publish/delete/${publicacionId}`;

                // Confirmar antes de eliminar
                if (!confirm('¿Estás seguro de que quieres eliminar esta publicación?')) {
                    return;
                }

                // Preparar headers
                const headers = {
                    'X-Requested-With': 'XMLHttpRequest'
                };
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                // Llamada AJAX
                fetch(url, {
                    method: 'POST',
                    headers: headers
                })
                .then(response => {
                    // Como devuelve RedirectView, recargamos la página
                    window.location.reload();
                })
                .catch(error => {
                    console.error('Error de conexión:', error);
                    mostrarMensaje('Error al eliminar la publicación', 'danger');
                });
            });
        });
    }

    /* ═════════════════════════════════════════════════════════
       SECCIÓN 16: ELIMINAR PRODUCTO (CLUB)
       Botón de papelera para eliminar productos del club (soft delete)
       ═════════════════════════════════════════════════════════ */

    const deleteProductoBtns = document.querySelectorAll('.delete-btn[data-delete-producto]');

    if (deleteProductoBtns.length > 0) {
        // Obtener token CSRF de los meta tags
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        deleteProductoBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const productoId = this.getAttribute('data-delete-producto');
                const url = CONTEXT_PATH + `api/product/delete/${productoId}`;

                // Confirmar antes de eliminar
                if (!confirm('¿Estás seguro de que quieres eliminar este producto?')) {
                    return;
                }

                // Preparar headers
                const headers = {
                    'X-Requested-With': 'XMLHttpRequest'
                };
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                // Llamada AJAX
                fetch(url, {
                    method: 'POST',
                    headers: headers
                })
                .then(response => {
                    // Como devuelve RedirectView, recargamos la página
                    window.location.reload();
                })
                .catch(error => {
                    console.error('Error de conexión:', error);
                    mostrarMensaje('Error al eliminar el producto', 'danger');
                });
            });
        });
    }

})();