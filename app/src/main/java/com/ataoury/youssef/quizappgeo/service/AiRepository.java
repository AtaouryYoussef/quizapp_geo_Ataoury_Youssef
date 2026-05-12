package com.ataoury.youssef.quizappgeo.service;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ataoury.youssef.quizappgeo.model.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AiRepository {

        private final GeminiApiService geminiApiService;
        private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

        public AiRepository() {
                this.geminiApiService = new GeminiApiService();
        }

        public LiveData<List<Question>> generateQuizQuestions(@NonNull String cityName) {
                MutableLiveData<List<Question>> questionsLiveData = new MutableLiveData<>();

                geminiApiService.generateQuizQuestions(cityName, new GeminiApiService.GeminiCallback() {
                        @Override
                        public void onSuccess(List<Question> questions) {
                                List<Question> prepared = normalizeAndShuffleQuestions(questions, cityName);
                                if (prepared.isEmpty()) {
                                        List<Question> fallbackQuestions = normalizeAndShuffleQuestions(
                                                        buildFallbackQuestions(cityName),
                                                        cityName);
                                        questionsLiveData.postValue(fallbackQuestions);
                                        errorMessage.postValue("Questions IA invalides, quiz local chargé.");
                                        return;
                                }
                                questionsLiveData.postValue(prepared);
                        }

                        @Override
                        public void onError(String message) {
                                List<Question> fallbackQuestions = normalizeAndShuffleQuestions(
                                                buildFallbackQuestions(cityName),
                                                cityName);
                                if (!fallbackQuestions.isEmpty()) {
                                        questionsLiveData.postValue(fallbackQuestions);
                                        errorMessage.postValue("Connexion IA indisponible, quiz local chargé.");
                                        return;
                                }
                                errorMessage.postValue(message);
                        }
                });

                return questionsLiveData;
        }

        public LiveData<String> getErrorMessage() {
                return errorMessage;
        }

        private List<Question> buildFallbackQuestions(@NonNull String cityName) {
                List<Question> items = new ArrayList<>();
                String city = cityName.trim().isEmpty() ? "votre ville" : cityName.trim();
                String cityLower = city.toLowerCase(Locale.ROOT);

                if (cityLower.contains("casablanca") || cityLower.equals("casa")) {
                        items.add(new Question(
                                        "Casablanca est surtout connue pour quel monument emblématique au bord de l'Atlantique ?",
                                        Arrays.asList("Mosquée Hassan II", "Tour Eiffel", "Pyramides de Gizeh",
                                                        "Statue de la Liberté"),
                                        0));
                        items.add(new Question(
                                        "La population de la métropole de Casablanca est approximativement de combien d'habitants ?",
                                        Arrays.asList("Environ 4 millions", "Environ 500 000", "Environ 1 million",
                                                        "Environ 10 millions"),
                                        0));
                        items.add(new Question(
                                        "Quel rôle économique majeur joue Casablanca au Maroc ?",
                                        Arrays.asList("Principal pôle financier et portuaire", "Capitale politique",
                                                        "Ville minière principale", "Centre administratif royal"),
                                        0));
                        items.add(new Question(
                                        "Quel quartier est historiquement connu pour son architecture Art déco à Casablanca ?",
                                        Arrays.asList("Le centre-ville", "La médina de Fès", "La Kasbah des Oudayas",
                                                        "Guéliz"),
                                        0));
                        items.add(new Question(
                                        "Quel club de football est basé à Casablanca ?",
                                        Arrays.asList("Wydad Athletic Club", "Paris Saint-Germain", "FC Barcelone",
                                                        "Bayern Munich"),
                                        0));
                        items.add(new Question(
                                        "Quel autre grand club est également de Casablanca ?",
                                        Arrays.asList("Raja Club Athletic", "Olympique Lyonnais", "Inter Milan",
                                                        "Arsenal"),
                                        0));
                        items.add(new Question(
                                        "Quel littoral borde Casablanca ?",
                                        Arrays.asList("Océan Atlantique", "Mer Rouge", "Golfe Persique", "Mer Noire"),
                                        0));
                        items.add(new Question(
                                        "Quel mode de transport urbain moderne dessert Casablanca ?",
                                        Arrays.asList("Tramway", "Métro aérien", "Téléphérique inter-ville",
                                                        "Monorail régional"),
                                        0));
                        items.add(new Question(
                                        "Le port de Casablanca est surtout important pour quoi ?",
                                        Arrays.asList("Le commerce et la logistique", "Le ski alpin",
                                                        "La production pétrolière offshore",
                                                        "La culture du riz"),
                                        0));
                        items.add(new Question(
                                        "Quelle est la langue fréquemment utilisée dans la vie quotidienne à Casablanca ?",
                                        Arrays.asList("Darija marocaine", "Japonais", "Suédois", "Coréen"),
                                        0));
                        return items;
                }

                if (cityLower.contains("rabat")) {
                        items.add(new Question(
                                        "Quel monument historique domine l'estuaire du Bouregreg a Rabat ?",
                                        Arrays.asList("Kasbah des Oudayas", "Mosquee Hassan II", "Bab Boujloud",
                                                        "Koutoubia"),
                                        0));
                        items.add(new Question(
                                        "Quel site inacheve celebre est un symbole de Rabat ?",
                                        Arrays.asList("Tour Hassan", "Minaret de la Koutoubia", "Volubilis",
                                                        "Borj Nord"),
                                        0));
                        items.add(new Question(
                                        "Quel fleuve separe Rabat de Sale ?",
                                        Arrays.asList("Bouregreg", "Sebou", "Moulouya", "Draa"),
                                        0));
                        items.add(new Question(
                                        "Rabat est surtout connue comme quelle ville du Maroc ?",
                                        Arrays.asList("Capitale administrative et politique", "Capitale economique",
                                                        "Capitale miniere",
                                                        "Capitale petroliere"),
                                        0));
                        items.add(new Question(
                                        "Quel jardin emblématique attire beaucoup de visiteurs a Rabat ?",
                                        Arrays.asList("Jardin d'Essais Botaniques", "Jardin Majorelle", "Menara",
                                                        "Parc Sindibad"),
                                        0));
                        items.add(new Question(
                                        "Quel moyen de transport relie Rabat et Sale sur le reseau moderne ?",
                                        Arrays.asList("Tramway Rabat-Sale", "Metro souterrain", "Monorail",
                                                        "TGV urbain"),
                                        0));
                        items.add(new Question(
                                        "Quel ocean borde Rabat ?",
                                        Arrays.asList("Ocean Atlantique", "Ocean Indien", "Mer Rouge", "Mer Caspienne"),
                                        0));
                        items.add(new Question(
                                        "Quel quartier de Rabat est reconnu pour son ambiance cotiere historique ?",
                                        Arrays.asList("Oudayas", "Gueliz", "Maarif", "Batha"),
                                        0));
                        items.add(new Question(
                                        "Quel role joue Rabat avec Casablanca dans l'axe Kenitra-Casablanca ?",
                                        Arrays.asList("Pole institutionnel majeur", "Zone uniquement agricole",
                                                        "Station de ski", "Port de peche principal"),
                                        0));
                        items.add(new Question(
                                        "Le complexe historique Tour Hassan et Mausolee Mohammed V se situe dans quelle ville ?",
                                        Arrays.asList("Rabat", "Agadir", "Oujda", "Tetouan"),
                                        0));
                        return items;
                }

                if (cityLower.contains("marrakech") || cityLower.contains("marrakesh")) {
                        items.add(new Question(
                                        "Quelle grande place animee est le coeur touristique de Marrakech ?",
                                        Arrays.asList("Jemaa el-Fna", "Place des Nations Unies", "Place Al Atlas",
                                                        "Bab El Had"),
                                        0));
                        items.add(new Question(
                                        "Quel monument religieux est parmi les plus connus de Marrakech ?",
                                        Arrays.asList("Mosquee Koutoubia", "Mosquee Hassan II", "Mosquee Tinmel",
                                                        "Grande Mosquee d'Oujda"),
                                        0));
                        items.add(new Question(
                                        "Quel quartier moderne est celebre a Marrakech ?",
                                        Arrays.asList("Gueliz", "Habous", "Anfa", "Mers Sultan"),
                                        0));
                        items.add(new Question(
                                        "Quel jardin iconique est associe a Marrakech ?",
                                        Arrays.asList("Jardin Majorelle", "Jardin d'Essais de Rabat",
                                                        "Parc de la Ligue Arabe", "Parc Perdicaris"),
                                        0));
                        items.add(new Question(
                                        "Marrakech est surnommee comment en raison de ses murs ?",
                                        Arrays.asList("Ville rouge", "Ville bleue", "Ville blanche", "Ville verte"),
                                        0));
                        items.add(new Question(
                                        "A proximite de Marrakech, quelle chaine de montagnes est visible ?",
                                        Arrays.asList("Haut Atlas", "Rif", "Anti-Liban", "Taurus"),
                                        0));
                        items.add(new Question(
                                        "Quel palais historique est tres visite a Marrakech ?",
                                        Arrays.asList("Palais Bahia", "Palais Royal de Rabat", "Palais El Badi de Fes",
                                                        "Dar El Makhzen de Tanger"),
                                        0));
                        items.add(new Question(
                                        "Quel type d'activite economique est tres fort a Marrakech ?",
                                        Arrays.asList("Tourisme et hotellerie", "Industrie petroliere offshore",
                                                        "Charbon", "Construction navale lourde"),
                                        0));
                        items.add(new Question(
                                        "Quel evenement international est souvent organise a Marrakech ?",
                                        Arrays.asList("Conferences et festivals internationaux",
                                                        "Jeux olympiques d'hiver", "Salon aerospatial de Farnborough",
                                                        "Grand prix F1 permanent"),
                                        0));
                        items.add(new Question(
                                        "Dans quelle ville marocaine se trouve la Menara celebre pour son bassin ?",
                                        Arrays.asList("Marrakech", "Rabat", "Meknes", "Nador"),
                                        0));
                        return items;
                }

                if (cityLower.contains("fes") || cityLower.contains("fès")) {
                        items.add(new Question(
                                        "Quel quartier historique de Fes est classe au patrimoine mondial de l'UNESCO ?",
                                        Arrays.asList("Fes el-Bali", "Anfa", "Gueliz", "Bourgogne"),
                                        0));
                        items.add(new Question(
                                        "Quelle universite historique de Fes est souvent citee parmi les plus anciennes ?",
                                        Arrays.asList("Universite Al Quaraouiyine",
                                                        "Universite Mohammed VI Polytechnique", "Universite Cadi Ayyad",
                                                        "Universite Abdelmalek Essaadi"),
                                        0));
                        items.add(new Question(
                                        "Quel artisanat traditionnel est particulierement celebre a Fes ?",
                                        Arrays.asList("Tanneries et travail du cuir", "Construction navale",
                                                        "Extraction de phosphate", "Textile denim industriel"),
                                        0));
                        items.add(new Question(
                                        "Comment s'appelle la celebre porte bleue de Fes ?",
                                        Arrays.asList("Bab Boujloud", "Bab Agnaou", "Bab El Had", "Bab Marrakech"),
                                        0));
                        items.add(new Question(
                                        "Quel style architectural est fortement present dans l'ancienne medina de Fes ?",
                                        Arrays.asList("Arabo-andalou", "Gothique nordique", "Brutalisme sovietique",
                                                        "Art nouveau parisien"),
                                        0));
                        items.add(new Question(
                                        "Fes est situee globalement dans quelle region geographique du Maroc ?",
                                        Arrays.asList("Nord-centre interieur", "Extreme sud saharien",
                                                        "Cote atlantique sud", "Region rifaine extreme nord-ouest"),
                                        0));
                        items.add(new Question(
                                        "Quelle medersa renommee se trouve a Fes ?",
                                        Arrays.asList("Bou Inania", "Ben Youssef", "Sahrij de Taza",
                                                        "Ibn Khaldoun d'Agadir"),
                                        0));
                        items.add(new Question(
                                        "Quel est un atout majeur de Fes pour les visiteurs ?",
                                        Arrays.asList("Patrimoine historique dense", "Stations de ski alpines",
                                                        "Iles tropicales", "Volcan actif"),
                                        0));
                        items.add(new Question(
                                        "Quel type de ruelles caracterise Fes el-Bali ?",
                                        Arrays.asList("Ruelles etroites pietonnes", "Boulevards autoroutiers",
                                                        "Avenues maritimes", "Canaux navigables"),
                                        0));
                        items.add(new Question(
                                        "Dans quelle ville trouve-t-on les tanneries Chouara ?",
                                        Arrays.asList("Fes", "Casablanca", "Rabat", "Tanger"),
                                        0));
                        return items;
                }

                if (cityLower.contains("tanger") || cityLower.contains("tangier")) {
                        items.add(new Question(
                                        "Tanger est connue pour sa position entre quelle mer et quel ocean ?",
                                        Arrays.asList("Mediterranee et Atlantique", "Mer Rouge et Atlantique",
                                                        "Mer Noire et Atlantique", "Caspienne et Atlantique"),
                                        0));
                        items.add(new Question(
                                        "Quel cap celebre est proche de Tanger ?",
                                        Arrays.asList("Cap Spartel", "Cap Ghir", "Cap Dra", "Cap Carbon"),
                                        0));
                        items.add(new Question(
                                        "Quel port strategique moderne se trouve dans la region de Tanger ?",
                                        Arrays.asList("Tanger Med", "Port de Nador West Med", "Port d'Essaouira",
                                                        "Port de Safi"),
                                        0));
                        items.add(new Question(
                                        "Quelle grotte touristique est associee a Tanger ?",
                                        Arrays.asList("Grottes d'Hercule", "Grotte Friouato", "Grotte Win-Timdouine",
                                                        "Grotte Ifri n'Ammar"),
                                        0));
                        items.add(new Question(
                                        "Quel role economique majeur joue Tanger aujourd'hui ?",
                                        Arrays.asList("Logistique et industrie exportatrice", "Capitale administrative",
                                                        "Zone miniere principale", "Production cerealiere dominante"),
                                        0));
                        items.add(new Question(
                                        "Quelle influence historique est tres visible dans l'architecture de Tanger ?",
                                        Arrays.asList("Mediterraneenne et internationale", "Andine",
                                                        "Scandinave medievale", "Steppe asiatique"),
                                        0));
                        items.add(new Question(
                                        "Quel moyen de transport rapide relie Tanger a d'autres grandes villes marocaines ?",
                                        Arrays.asList("Train a grande vitesse", "Metro interurbain", "Tram marin",
                                                        "Telecabine nationale"),
                                        0));
                        items.add(new Question(
                                        "Quelle medina est connue pour ses points de vue sur le detroit ?",
                                        Arrays.asList("Medina de Tanger", "Medina de Tetouan", "Medina d'Asilah",
                                                        "Medina d'Oujda"),
                                        0));
                        items.add(new Question(
                                        "Quel detroit se situe face a Tanger ?",
                                        Arrays.asList("Detroit de Gibraltar", "Detroit d'Ormuz", "Detroit de Bering",
                                                        "Detroit de Malacca"),
                                        0));
                        items.add(new Question(
                                        "Dans quelle ville marocaine peut-on visiter Cap Spartel ?",
                                        Arrays.asList("Tanger", "Marrakech", "Rabat", "Fes"),
                                        0));
                        return items;
                }

                if (cityLower.contains("agadir")) {
                        items.add(new Question(
                                        "Agadir est surtout connue pour quel atout naturel majeur ?",
                                        Arrays.asList("Sa baie atlantique et ses plages", "Un grand fleuve interieur",
                                                        "Une chaine alpine enneigee", "Un volcan actif"),
                                        0));
                        items.add(new Question(
                                        "Quel quartier en hauteur offre une vue panoramique sur Agadir ?",
                                        Arrays.asList("La Kasbah d'Agadir Oufella", "Gueliz", "Habous", "Bab Doukkala"),
                                        0));
                        items.add(new Question(
                                        "Quel secteur economique est fort a Agadir ?",
                                        Arrays.asList("Tourisme balneaire", "Industrie siderurgique lourde",
                                                        "Extraction de charbon", "Production automobile de masse"),
                                        0));
                        items.add(new Question(
                                        "Agadir est situee sur quelle facade maritime du Maroc ?",
                                        Arrays.asList("Atlantique", "Mediterraneenne", "Mer Rouge", "Mer d'Arabie"),
                                        0));
                        items.add(new Question(
                                        "Quel grand souk est celebre a Agadir ?",
                                        Arrays.asList("Souk El Had", "Souk Sebat", "Kissariat Al Kifah",
                                                        "Souk El Attarine"),
                                        0));
                        items.add(new Question(
                                        "Quelle vallee est souvent visitee pres d'Agadir pour ses palmeraies ?",
                                        Arrays.asList("Vallee du Paradis (Paradise Valley)", "Vallee du Ziz",
                                                        "Vallee du Dades", "Vallee de l'Ourika"),
                                        0));
                        items.add(new Question(
                                        "Quel style touristique est tres associe a Agadir ?",
                                        Arrays.asList("Sejour plage et soleil", "Tourisme polaire", "Safari de steppe",
                                                        "Alpinisme glaciaire"),
                                        0));
                        items.add(new Question(
                                        "Quel type de climat domine a Agadir ?",
                                        Arrays.asList("Doux et oceanique semi-aride", "Continental froid",
                                                        "Tropical humide", "Subarctique"),
                                        0));
                        items.add(new Question(
                                        "Agadir se trouve dans quelle grande region marocaine ?",
                                        Arrays.asList("Souss-Massa", "Fes-Meknes", "Draa-Tafilalet", "L'Oriental"),
                                        0));
                        items.add(new Question(
                                        "Dans quelle ville marocaine trouve-t-on le Souk El Had ?",
                                        Arrays.asList("Agadir", "Fes", "Tetouan", "Rabat"),
                                        0));
                        return items;
                }

                items.add(new Question(
                                "Quel élément est souvent au centre historique de " + city + " ?",
                                Arrays.asList("Une médina ou vieille ville", "Un parc national", "Un aéroport",
                                                "Une mine"),
                                0));
                items.add(new Question(
                                "Pour découvrir " + city + ", quel moyen favorise l'exploration locale ?",
                                Arrays.asList("Marcher dans les quartiers", "Rester uniquement à l'hôtel",
                                                "Éviter les habitants",
                                                "Ne pas utiliser de carte"),
                                0));
                items.add(new Question(
                                "Quel type de cuisine est le plus pertinent à tester à " + city + " ?",
                                Arrays.asList("Spécialités locales", "Uniquement fast-food international",
                                                "Aucun repas",
                                                "Plats sans lien culturel"),
                                0));
                items.add(new Question(
                                "Quand visiter les lieux emblématiques de " + city + " pour éviter la foule ?",
                                Arrays.asList("Tôt le matin", "Toujours en heure de pointe",
                                                "Seulement la nuit sans sécurité",
                                                "Jamais"),
                                0));
                items.add(new Question(
                                "Quel comportement est recommandé pendant un quiz géo à " + city + " ?",
                                Arrays.asList("Respecter les lieux et habitants", "Ignorer les règles",
                                                "Jeter des déchets",
                                                "Bloquer la circulation"),
                                0));
                items.add(new Question(
                                "Pour mieux comprendre l'histoire de " + city + ", quoi privilégier ?",
                                Arrays.asList("Musées et guides locaux", "Rumeurs non vérifiées", "Rien lire du tout",
                                                "Éviter tout monument"),
                                0));
                items.add(new Question(
                                "Quel indice géographique aide à se repérer dans " + city + " ?",
                                Arrays.asList("Les repères urbains majeurs", "Fermer la carte",
                                                "Désactiver toute localisation",
                                                "Éviter les panneaux"),
                                0));
                items.add(new Question(
                                "Quel est un bon réflexe sécurité en explorant " + city + " ?",
                                Arrays.asList("Partager son itinéraire", "Partir sans téléphone", "Ignorer la météo",
                                                "Refuser toute information"),
                                0));
                items.add(new Question(
                                "Pour un déplacement durable dans " + city + ", que choisir ?",
                                Arrays.asList("Transports publics", "Trajets inutiles en voiture",
                                                "Moteur au ralenti permanent",
                                                "Aucun déplacement"),
                                0));
                items.add(new Question(
                                "Quel est l'objectif principal de ce quiz sur " + city + " ?",
                                Arrays.asList("Apprendre en explorant", "Punir l'utilisateur", "Bloquer l'application",
                                                "Supprimer l'historique"),
                                0));

                return items;
        }

        private List<Question> normalizeAndShuffleQuestions(List<Question> source, @NonNull String cityName) {
                List<Question> prepared = new ArrayList<>();
                if (source == null) {
                        return prepared;
                }

                Random random = new Random(System.nanoTime());
                String city = cityName.trim().isEmpty() ? "cette ville" : cityName.trim();

                for (Question item : source) {
                        if (item == null || item.getQuestionText() == null || item.getQuestionText().trim().isEmpty()) {
                                continue;
                        }

                        List<String> choices = new ArrayList<>();
                        if (item.getChoices() != null) {
                                for (String choice : item.getChoices()) {
                                        if (choice != null && !choice.trim().isEmpty()
                                                        && !choices.contains(choice.trim())) {
                                                choices.add(choice.trim());
                                        }
                                }
                        }

                        if (choices.size() < 2) {
                                continue;
                        }

                        while (choices.size() < 4) {
                                choices.add("Aucune des autres propositions sur " + city);
                        }
                        while (choices.size() > 4) {
                                choices.remove(choices.size() - 1);
                        }

                        int originalCorrectIndex = item.getCorrectAnswerIndex();
                        if (originalCorrectIndex < 0 || originalCorrectIndex >= choices.size()) {
                                originalCorrectIndex = 0;
                        }
                        String correctAnswer = choices.get(originalCorrectIndex);

                        Collections.shuffle(choices, random);
                        int shuffledCorrectIndex = choices.indexOf(correctAnswer);
                        if (shuffledCorrectIndex < 0) {
                                shuffledCorrectIndex = random.nextInt(choices.size());
                        }

                        prepared.add(new Question(item.getQuestionText().trim(), choices, shuffledCorrectIndex));
                        if (prepared.size() == 10) {
                                break;
                        }
                }

                return prepared;
        }
}
