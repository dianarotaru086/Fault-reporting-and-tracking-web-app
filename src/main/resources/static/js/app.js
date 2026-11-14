/* ============================================================
 * app.js : fonctions communes à toutes les pages
 *   - api()          : appelle le serveur et gère les erreurs
 *   - exigerConnexion() : renvoie vers login.html si non connecté
 *   - afficherMiseEnPage() : dessine la barre du haut et le menu
 *   - echapper()     : protège le HTML contre l'injection
 * ============================================================ */

const LIBELLES_ROLE = {
    UTILISATEUR: "Utilisateur",
    RESPONSABLE: "Responsable technique"
};

/** Appelle l'API. En cas d'erreur, lève un objet {statut, message, erreurs}. */
async function api(url, options = {}) {
    const config = {
        method: options.method || "GET",
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin"
    };
    if (options.corps !== undefined) {
        config.body = JSON.stringify(options.corps);
    }

    const reponse = await fetch(url, config);
    let donnees = null;
    const texte = await reponse.text();
    if (texte) {
        try { donnees = JSON.parse(texte); } catch (e) { donnees = null; }
    }

    if (!reponse.ok) {
        throw {
            statut: reponse.status,
            message: (donnees && donnees.message) || "Une erreur est survenue.",
            erreurs: (donnees && donnees.erreurs) || {}
        };
    }
    return donnees;
}

/** Échappe les caractères spéciaux avant d'insérer du texte dans du HTML. */
function echapper(texte) {
    const div = document.createElement("div");
    div.textContent = texte == null ? "" : String(texte);
    return div.innerHTML;
}

/** Vérifie la connexion. Si personne n'est connecté, on va sur login.html. */
async function exigerConnexion() {
    try {
        return await api("/api/auth/me");
    } catch (e) {
        window.location.href = "login.html";
        return null;
    }
}

/** Dessine la barre du haut + le menu de gauche autour du contenu de la page. */
function afficherMiseEnPage(utilisateur, pageActive) {
    const liens = [
        { id: "accueil", texte: "Accueil", url: "index.html" },
        { id: "signaler", texte: "Signaler une panne", url: "signaler.html" },
        { id: "liste", texte: "Liste des pannes", url: "liste.html" },
        { id: "mes-pannes", texte: "Mes pannes", url: "mes-pannes.html" }
    ];

    const menu = liens.map(l =>
        `<a href="${l.url}" class="${l.id === pageActive ? "actif" : ""}">${l.texte}</a>`
    ).join("");

    const contenu = document.getElementById("contenu").innerHTML;

    document.body.innerHTML = `
        <header class="barre-haut">
            <span class="logo">🔧 Gestion des pannes</span>
            <span class="utilisateur">
                ${echapper(utilisateur.nomComplet)}
                <span class="role">${echapper(LIBELLES_ROLE[utilisateur.role] || utilisateur.role)}</span>
                <button class="discret" id="bouton-deconnexion">Déconnexion</button>
            </span>
        </header>
        <div class="conteneur">
            <nav class="menu">${menu}</nav>
            <main class="contenu" id="contenu">${contenu}</main>
        </div>`;

    document.getElementById("bouton-deconnexion").addEventListener("click", async () => {
        await api("/api/auth/logout", { method: "POST" });
        window.location.href = "login.html";
    });
}
