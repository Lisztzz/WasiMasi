// WasiMasi · Landing Page v1: selector de idioma ES/EN y menú mobile.
const I18N = {
  es: {
    'a11y.saltar': 'Saltar al contenido',
    'nav.menu': 'Abrir menú', 'nav.cerrar': 'Cerrar menú', 'nav.principal': 'Principal',
    'nav.how': 'Cómo funciona', 'nav.safety': 'Seguridad', 'nav.testimonials': 'Testimonios',
    'nav.login': 'Iniciar sesión', 'nav.signup': 'Registrarse',
    'hero.tag': 'Para universitarios en Lima',
    'hero.title': 'Encuentra tu roomie verificado en Lima',
    'hero.subtitle': 'WasiMasi conecta a estudiantes que buscan habitación con anfitriones verificados por correo institucional o DNI. Busca por zona y presupuesto, conversa y coordina tu visita en un solo lugar.',
    'hero.search': 'Buscar habitación', 'hero.publish': 'Publicar mi habitación', 'hero.badge': 'Anfitrión verificado',
    'hero.art': 'Ilustración: casa con dos roomies y sello de anfitrión verificado',
    'stats.label': 'Cifras del mercado',
    'stats.n1': '578 000', 'stats.students': 'estudiantes universitarios en Lima Metropolitana',
    'stats.n2': '25.6 %', 'stats.migrate': 'de los estudiantes migra de provincia para estudiar',
    'stats.n3': 'S/ 300 – 1 200', 'stats.rent': 'rango mensual de alquiler de una habitación',
    'how.title': 'Cómo funciona',
    'how.s1.title': '1. Regístrate y verifícate',
    'how.s1.text': 'Crea tu cuenta como buscador o anfitrión y valida tu identidad con tu correo institucional o tu DNI.',
    'how.s2.title': '2. Busca por zona y presupuesto',
    'how.s2.text': 'Filtra las habitaciones activas en el distrito que te queda cerca y dentro de lo que puedes pagar.',
    'how.s3.title': '3. Conversa y agenda tu visita',
    'how.s3.text': 'Habla con el anfitrión, resuelve tus dudas y coordina una visita antes de decidir.',
    'safety.title': 'Tu seguridad primero',
    'safety.text': 'En nuestras entrevistas, el mayor miedo fue no saber quién está del otro lado. Por eso en WasiMasi cada persona se verifica antes de publicar o de agendar una visita.',
    'safety.c1.title': 'Estudiante verificado',
    'safety.c1.text': 'Los buscadores se validan con su correo institucional y su código de estudiante.',
    'safety.c2.title': 'Identidad verificada',
    'safety.c2.text': 'Los anfitriones se validan con su DNI antes de publicar habitaciones.',
    'testi.title': 'Lo que nos dijeron en las entrevistas',
    'testi.q1': '“Es como el perfil de RENIEC, pero para roomies.”', 'testi.a1': 'Alejandro · Buscador',
    'testi.q2': '“Siempre se desconoce a la persona, por eso primero pediría un DNI.”', 'testi.a2': 'Alfonso · Anfitrión',
    'cta.title': '¿Listo para encontrar tu wasi?', 'cta.text': 'Crea tu cuenta gratis y empieza a buscar hoy.', 'cta.button': 'Crear cuenta gratis',
    'footer.tagline': 'Vivienda compartida segura para estudiantes.', 'footer.legal': 'Enlaces legales',
    'footer.terms': 'Términos de Servicio', 'footer.privacy': 'Política de privacidad', 'footer.contact': 'Contacto',
    'footer.lang': 'Idioma', 'footer.home': 'Volver al inicio',
  },
  en: {
    'a11y.saltar': 'Skip to content',
    'nav.menu': 'Open menu', 'nav.cerrar': 'Close menu', 'nav.principal': 'Main',
    'nav.how': 'How it works', 'nav.safety': 'Safety', 'nav.testimonials': 'Testimonials',
    'nav.login': 'Log in', 'nav.signup': 'Sign up',
    'hero.tag': 'For university students in Lima',
    'hero.title': 'Find a verified roommate in Lima',
    'hero.subtitle': 'WasiMasi connects students looking for a room with hosts verified by institutional email or national ID. Search by district and budget, chat and schedule your visit in one place.',
    'hero.search': 'Find a room', 'hero.publish': 'List my room', 'hero.badge': 'Verified host',
    'hero.art': 'Illustration: a house with two roommates and a verified host badge',
    'stats.label': 'Market figures',
    'stats.n1': '578,000', 'stats.students': 'university students in Metropolitan Lima',
    'stats.n2': '25.6%', 'stats.migrate': 'of students move from other regions to study',
    'stats.n3': 'S/ 300 – 1,200', 'stats.rent': 'monthly rent range for a room',
    'how.title': 'How it works',
    'how.s1.title': '1. Sign up and get verified',
    'how.s1.text': 'Create your account as a seeker or host and verify your identity with your institutional email or national ID.',
    'how.s2.title': '2. Search by district and budget',
    'how.s2.text': 'Filter active rooms in a district close to you and within what you can pay.',
    'how.s3.title': '3. Chat and book a visit',
    'how.s3.text': 'Talk to the host, clear up your questions and schedule a visit before deciding.',
    'safety.title': 'Your safety first',
    'safety.text': 'In our interviews, the biggest fear was not knowing who is on the other side. That is why on WasiMasi everyone gets verified before listing a room or booking a visit.',
    'safety.c1.title': 'Verified student',
    'safety.c1.text': 'Seekers are verified with their institutional email and student code.',
    'safety.c2.title': 'Verified identity',
    'safety.c2.text': 'Hosts are verified with their national ID before listing rooms.',
    'testi.title': 'What we heard in our interviews',
    'testi.q1': '“It’s like a national ID profile, but for roommates.”', 'testi.a1': 'Alejandro · Seeker',
    'testi.q2': '“You never really know the person, so I would ask for an ID first.”', 'testi.a2': 'Alfonso · Host',
    'cta.title': 'Ready to find your wasi?', 'cta.text': 'Create your free account and start searching today.', 'cta.button': 'Create free account',
    'footer.tagline': 'Safe shared housing for students.', 'footer.legal': 'Legal links',
    'footer.terms': 'Terms of Service', 'footer.privacy': 'Privacy policy', 'footer.contact': 'Contact',
    'footer.lang': 'Language', 'footer.home': 'Back to home',
  },
};

const STORAGE_KEY = 'wasimasi-lang';

function setLang(lang) {
  const textos = I18N[lang] ?? I18N.es;
  document.documentElement.lang = lang;
  document.querySelectorAll('[data-i18n]').forEach((el) => {
    const texto = textos[el.dataset.i18n];
    if (texto) el.textContent = texto;
  });
  document.querySelectorAll('[data-i18n-aria]').forEach((el) => {
    const texto = textos[el.dataset.i18nAria];
    if (texto) el.setAttribute('aria-label', texto);
  });
  document.querySelectorAll('[data-lang]').forEach((btn) => {
    btn.setAttribute('aria-pressed', String(btn.dataset.lang === lang));
  });
  try {
    localStorage.setItem(STORAGE_KEY, lang);
  } catch {
    /* sin almacenamiento (modo privado): el idioma dura solo esta visita */
  }
}

document.querySelectorAll('[data-lang]').forEach((btn) => {
  btn.addEventListener('click', () => setLang(btn.dataset.lang));
});

let guardado = 'es';
try {
  guardado = localStorage.getItem(STORAGE_KEY) === 'en' ? 'en' : 'es';
} catch {
  /* sin almacenamiento */
}
setLang(guardado);

// Menú mobile (ícono de hamburguesa)
const botonMenu = document.querySelector('.menu-toggle');
const menu = document.getElementById('menu-principal');
if (botonMenu && menu) {
  const cerrar = () => {
    botonMenu.setAttribute('aria-expanded', 'false');
    botonMenu.dataset.i18nAria = 'nav.menu';
    menu.classList.remove('abierto');
    setLang(document.documentElement.lang);
  };
  botonMenu.addEventListener('click', () => {
    const abierto = botonMenu.getAttribute('aria-expanded') === 'true';
    if (abierto) {
      cerrar();
      return;
    }
    botonMenu.setAttribute('aria-expanded', 'true');
    botonMenu.dataset.i18nAria = 'nav.cerrar';
    menu.classList.add('abierto');
    setLang(document.documentElement.lang);
  });
  // Al elegir una sección o presionar Escape, el menú se cierra
  menu.querySelectorAll('a').forEach((a) => a.addEventListener('click', cerrar));
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && botonMenu.getAttribute('aria-expanded') === 'true') {
      cerrar();
      botonMenu.focus();
    }
  });
}
