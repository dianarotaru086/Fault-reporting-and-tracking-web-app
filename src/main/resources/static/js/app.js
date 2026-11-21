/* app.js : fonctions communes à toutes les pages
 *   api()               appelle le serveur et normalise les erreurs
 *   exigerConnexion()   renvoie vers login.html si personne n'est connecté
 *   afficherMiseEnPage() dessine la barre du haut et le menu
 *   echapper()          protège le HTML contre l'injection de code
 */

const LIBELLES_ROLE = {
    UTILISATEUR: "Utilisateur",
    RESPONSABLE: "Responsable technique",
    ADMIN: "Administrateur"
};


async function api(url, options = {}) {
    const config = {
        method: options.method || "GET",
        headers: {},
        credentials: "same-origin"
    };
    if (options.formulaire) {
        config.body = options.formulaire;
    } else {
        config.headers["Content-Type"] = "application/json";
        if (options.corps !== undefined) {
            config.body = JSON.stringify(options.corps);
        }
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

function echapper(texte) {
    const div = document.createElement("div");
    div.textContent = texte == null ? "" : String(texte);
    return div.innerHTML;
}

function initiales(nomComplet) {
    const mots = String(nomComplet || "").trim().split(/\s+/).filter(Boolean);
    if (mots.length === 0) return "?";
    const premiere = mots[0][0];
    const derniere = mots.length > 1 ? mots[mots.length - 1][0] : "";
    return (premiere + derniere).toUpperCase();
}
async function exigerConnexion() {
    try {
        return await api("/api/auth/me");
    } catch (e) {
        window.location.href = "login.html";
        return null;
    }
}

function afficherMiseEnPage(utilisateur, pageActive) {
    const liens = [
        { id: "accueil", texte: "Accueil", url: "index.html" },
        { id: "signaler", texte: "Signaler une panne", url: "signaler.html" },
        { id: "liste", texte: "Liste des pannes", url: "liste.html" },
        { id: "mes-pannes", texte: "Mes pannes", url: "mes-pannes.html" },
        { id: "profil", texte: "Mon profil", url: "profil.html" }
    ];
    if (utilisateur.role === "ADMIN") {
        liens.push({ id: "admin", texte: "Administration", url: "admin.html" });
    }

    const menu = liens.map(l =>
        `<a href="${l.url}" class="${l.id === pageActive ? "actif" : ""}">${l.texte}</a>`
    ).join("");

    const contenu = document.getElementById("contenu").innerHTML;
    const role = LIBELLES_ROLE[utilisateur.role] || utilisateur.role;

    const avatar = utilisateur.photoUrl
        ? `<img class="avatar-petit" src="${echapper(utilisateur.photoUrl)}" alt="">`
        : `<span class="avatar-petit">${echapper(initiales(utilisateur.nomComplet))}</span>`;

    document.body.innerHTML = `
        <header class="barre-haut">
            <span class="nom"><span class="marque"></span>Gestion des <em>pannes</em></span>
            <span class="compte">
                <a href="profil.html">${avatar}${echapper(utilisateur.nomComplet)}</a>
                <span class="role">· ${echapper(role)}</span>
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

function effacerErreurs(racine) {
    racine.querySelectorAll(".champ-erreur").forEach(e => e.classList.remove("champ-erreur"));
    racine.querySelectorAll(".message-champ").forEach(e => e.textContent = "");
}

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

function formaterDate(iso) {
    return iso ? new Date(iso).toLocaleDateString("fr-FR") : "";
}

function classeStatut(statut) {
    return "statut-" + String(statut).toLowerCase().replace(/_/g, "-");
}

function formaterDateHeure(iso) {
    return iso ? new Date(iso).toLocaleString("fr-FR", { dateStyle: "short", timeStyle: "short" }) : "";
}

function campusCourt(nom) {
    return nom ? nom.replace(/\s*\(.*?\)/, "") : "";
}

function tableauPannes(pannes, messageVide) {
    if (pannes.length === 0) {
        return `<p class="vide">${echapper(messageVide || "Aucune panne.")}</p>`;
    }
    const lignes = pannes.map(p => `
        <tr>
            <td>${p.id}</td>
            <td><a href="detail.html?id=${p.id}">${echapper(p.titre)}</a></td>
            <td>${p.unite ? `<strong>${echapper(p.unite)}</strong><br>` : ""}${p.campus ? echapper(campusCourt(p.campus)) + " · " : ""}${echapper(p.lieu)}</td>
            <td>${echapper(p.categorieLibelle)}</td>
            <td class="${p.priorite === "HAUTE" ? "priorite-haute" : ""}">${echapper(p.prioriteLibelle)}</td>
            <td><span class="statut ${classeStatut(p.statut)}">${echapper(p.statutLibelle)}</span></td>
            <td>${formaterDate(p.dateSignalement)}</td>
        </tr>`).join("");

    return `<table>
        <thead><tr><th>ID</th><th>Titre</th><th>Lieu</th><th>Catégorie</th>
                   <th>Priorité</th><th>Statut</th><th>Date</th></tr></thead>
        <tbody>${lignes}</tbody>
    </table>`;
}
