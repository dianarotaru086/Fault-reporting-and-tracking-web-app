/* app.js : fonctions communes à toutes les pages
 *   api()               appelle le serveur et normalise les erreurs
 *   exigerConnexion()   renvoie vers login.html si personne n'est connecté
 *   afficherMiseEnPage() dessine la barre du haut et le menu
 *   echapper()          protège le HTML contre l'injection de code
 */

const LIBELLES_ROLE = {
    UTILISATEUR: "Utilisateur",
    RESPONSABLE: "Responsable technique"
};

/** Appelle l'API. En cas d'erreur, lève {statut, message, erreurs}. */
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

/** Vérifie la connexion ; sinon on va sur la page de connexion. */
async function exigerConnexion() {
    try {
        return await api("/api/auth/me");
    } catch (e) {
        window.location.href = "login.html";
        return null;
    }
}

/** Dessine la barre du haut et le menu autour du contenu de la page. */
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
    const role = LIBELLES_ROLE[utilisateur.role] || utilisateur.role;

    document.body.innerHTML = `
        <header class="barre-haut">
            <span class="nom"><span class="marque"></span>Gestion des <em>pannes</em></span>
            <span class="compte">
                ${echapper(utilisateur.nomComplet)} <span class="role">· ${echapper(role)}</span>
                <button id="bouton-deconnexion">Déconnexion</button>
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

/* ---------- aides pour les formulaires ---------- */

/** Efface tous les messages d'erreur d'un formulaire. */
function effacerErreurs(racine) {
    racine.querySelectorAll(".champ-erreur").forEach(e => e.classList.remove("champ-erreur"));
    racine.querySelectorAll(".message-champ").forEach(e => e.textContent = "");
}

/**
 * Affiche sous chaque champ le message envoyé par le serveur.
 * Chaque champ est un <div class="champ" data-champ="nom"> contenant un .message-champ.
 * Renvoie le nombre de champs trouvés.
 */
function afficherErreursChamps(racine, erreurs) {
    let trouves = 0;
    for (const [nom, message] of Object.entries(erreurs || {})) {
        const champ = racine.querySelector(`[data-champ="${nom}"]`);
        if (!champ) continue;
        champ.classList.add("champ-erreur");
        champ.querySelector(".message-champ").textContent = message;
        trouves++;
    }
    return trouves;
}

/** "2026-11-14T16:38:00" devient "14/11/2026". */
function formaterDate(iso) {
    return iso ? new Date(iso).toLocaleDateString("fr-FR") : "";
}

/** "IN_PROGRESS" devient "statut-in-progress" : la classe CSS du point de couleur. */
function classeStatut(statut) {
    return "statut-" + String(statut).toLowerCase().replace(/_/g, "-");
}
/** "2026-11-14T16:38:00" devient "14/11/2026 16:38". */
function formaterDateHeure(iso) {
    return iso ? new Date(iso).toLocaleString("fr-FR", { dateStyle: "short", timeStyle: "short" }) : "";
}
