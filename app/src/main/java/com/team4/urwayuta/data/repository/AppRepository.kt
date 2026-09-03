package com.team4.urwayuta.data.repository

import android.graphics.Color
import com.team4.urwayuta.data.model.*

/**
 * AppRepository — single source of truth for all in-memory data.
 * In production, replace with Room + SQLite (SRA 5.1).
 *
 * Implements SRA 4.3 functions:
 *   SearchSpots, FilterByType, GetNearbySpots, CheckAvailability
 *   CalculateRoute, GetDirections, GetWalkingTime
 *   SaveFavorite, ViewMap, ShowAllSpots, FilterByCategory, ShowSpotOnMap
 */
object AppRepository {

    // ── Auth ──────────────────────────────────────────────────────────────

    val students = mutableListOf(
        Student("cse3310", "cse3310", "", "001",
            "cx00000@mavs.uta.edu", "Computer Science", "student", "team4")
    )

    fun findStudent(idOrEmail: String, password: String): Student? =
        students.find { (it.studentID == idOrEmail || it.email == idOrEmail) && it.passwordHash == "team4" }

    fun registerStudent(student: Student): Boolean {
        if (students.any { it.email == student.email }) return false
        students.add(student)
        return true
    }

    fun generateEmail(firstName: String, middleName: String, lastName: String, lastFour: String): String {
        val f = firstName.firstOrNull()?.lowercaseChar() ?: 'x'
        val m = if (middleName.isNotEmpty()) middleName.first().lowercaseChar() else 'x'
        val l = lastName.firstOrNull()?.lowercaseChar() ?: 'x'
        return "$f$m$l${lastFour.takeLast(4).padStart(4,'0')}@mavs.uta.edu"
    }

    // ── Walking times (Google Maps estimates) ─────────────────────────────

    val walkingTimes = mapOf(
        "ERB" to mapOf("NH" to "3 min", "UH" to "7 min", "SEIR" to "8 min"),
        "NH"  to mapOf("ERB" to "3 min", "UH" to "5 min", "SEIR" to "6 min"),
        "UH"  to mapOf("ERB" to "7 min", "NH" to "5 min", "SEIR" to "2 min"),
        "SEIR" to mapOf("ERB" to "8 min", "NH" to "6 min", "UH" to "2 min")
    )

    val buildingCoords = mapOf(
        "ERB"  to Pair(32.7322, -97.1149),
        "NH"   to Pair(32.7316, -97.1141),
        "UH"   to Pair(32.7295, -97.1148),
        "SEIR" to Pair(32.7289, -97.1148)
    )

    val buildingFullNames = mapOf(
        "ERB"  to "Engineering Research Building",
        "NH"   to "Nedderman Hall",
        "UH"   to "University Hall",
        "SEIR" to "Science & Engineering Innovation & Research"
    )

    /** SRA 4.3 GetWalkingTime() */
    fun getWalkingTime(from: String, to: String): String =
        walkingTimes[from]?.get(to) ?: "N/A"

    /** SRA 4.3 CalculateRoute() */
    fun calculateRoute(from: String, to: String): Route {
        val duration = getWalkingTime(from, to)
        return Route(
            routeID = "${from}_${to}".hashCode(),
            startLocation = buildingFullNames[from] ?: from,
            destinationLocation = buildingFullNames[to] ?: to,
            distance = when(duration) { "2 min" -> "0.1 mi"; "3 min" -> "0.2 mi";
                "5 min" -> "0.25 mi"; "6 min" -> "0.3 mi"; "7 min" -> "0.35 mi"; else -> "0.4 mi" },
            duration = duration,
            steps = listOf("Exit $from", "Head toward $to", "Arrive at ${buildingFullNames[to] ?: to}")
        )
    }

    // ── Campus Spots ──────────────────────────────────────────────────────

    val allSpots = mutableListOf(
        // SEIR
        CampusSpot(1, "Vending Machine", SpotType.VENDING, "SEIR", "2nd Floor", description="By elevator"),
        CampusSpot(2, "Seating Area", SpotType.SEATING, "SEIR", "1st Floor", description="23 seats", seats=23),
        CampusSpot(3, "Seating Area", SpotType.SEATING, "SEIR", "2nd Floor", description="23 seats", seats=23),
        CampusSpot(4, "Inclusive Café", SpotType.CAFE, "SEIR", "1st Floor",
            description="Inclusive Café — campus café on the 1st floor of SEIR", isRecommended=true,
            latitude=32.7282463, longitude=-97.1131905),
        CampusSpot(28, "Photo Spot", SpotType.PHOTO, "SEIR", "Exterior",
            description="North entrance", isRecommended=true, latitude=32.7290, longitude=-97.1147),
        // UH
        CampusSpot(5, "Vending (Snacks)", SpotType.VENDING, "UH", "1st Floor", description="2 snack machines"),
        CampusSpot(6, "Vending (Drinks)", SpotType.VENDING, "UH", "1st Floor", description="6 drink machines"),
        CampusSpot(7, "Seating Area", SpotType.SEATING, "UH", "1st Floor",
            description="46 seats — quiet area near vending", seats=46, isRecommended=true,
            latitude=32.7295, longitude=-97.1148),
        CampusSpot(8, "Seating Area", SpotType.SEATING, "UH", "2nd Floor", description="5 seats", seats=5),
        CampusSpot(9, "Seating Area", SpotType.SEATING, "UH", "Basement", description="29 seats", seats=29),
        // NH
        CampusSpot(10, "Vending (Snacks)", SpotType.VENDING, "NH", "1st Floor", description="1 snack machine"),
        CampusSpot(11, "Vending (Drinks)", SpotType.VENDING, "NH", "1st Floor", description="2 drink machines"),
        CampusSpot(12, "Seating Area", SpotType.SEATING, "NH", "1st Floor", description="30 seats", seats=30),
        CampusSpot(13, "Seating Area", SpotType.SEATING, "NH", "2nd Floor", description="25 seats", seats=25),
        CampusSpot(14, "Library", SpotType.LIBRARY, "NH", "Basement", description="Full library access"),
        // ERB
        CampusSpot(15, "Vending (Snacks)", SpotType.VENDING, "ERB", "1st Floor", description="Left wing"),
        CampusSpot(16, "Vending (Drinks)", SpotType.VENDING, "ERB", "1st Floor", description="Left wing — 2 machines"),
        CampusSpot(17, "Vending (Snacks)", SpotType.VENDING, "ERB", "2nd Floor", description="Right wing"),
        CampusSpot(18, "Vending (Drinks)", SpotType.VENDING, "ERB", "2nd Floor", description="Right wing — 2 machines"),
        CampusSpot(19, "Seating Area", SpotType.SEATING, "ERB", "1st Floor", description="Left: 18 seats, Right: 1 seat", seats=19),
        CampusSpot(20, "Seating Area", SpotType.SEATING, "ERB", "2nd Floor", description="Left: 12, Right: 7 seats", seats=19),
        CampusSpot(21, "Seating Area", SpotType.SEATING, "ERB", "3rd Floor", description="14 seats — no balcony", seats=14),
        CampusSpot(22, "Seating Area", SpotType.SEATING, "ERB", "4th Floor",
            description="29 seats — balcony access", seats=29, isRecommended=true,
            latitude=32.7322, longitude=-97.1149),
        CampusSpot(23, "Seating Area", SpotType.SEATING, "ERB", "5th Floor", description="14 seats — balcony access", seats=14),
        CampusSpot(24, "Seating Area", SpotType.SEATING, "ERB", "6th Floor", description="17 seats — no balcony", seats=17),
        CampusSpot(25, "Bughouse (Study)", SpotType.STUDY, "ERB", "5th Floor", description="Collaborative study space"),
        CampusSpot(26, "Microwave", SpotType.MICROWAVE, "ERB", "5th Floor", roomNumber="548",
            description="Room 548 — 4 seats and sink", seats=4, isRecommended=true,
            latitude=32.7322, longitude=-97.1149),
        CampusSpot(27, "Photo Spot", SpotType.PHOTO, "ERB", "Exterior",
            description="West courtyard", isRecommended=true, latitude=32.7323, longitude=-97.1150),
        // Click Spots
        CampusSpot(30, "Maximus Eventus", SpotType.CLICK_SPOT, "Central Campus", "Exterior",
            description="UTA Spirit Horse by Garrison Calcote — iconic campus landmark", isRecommended=true,
            latitude=32.7276159, longitude=-97.1128931),
        CampusSpot(31, "UH to SEIR Path", SpotType.CLICK_SPOT, "Central Campus", "Outdoor",
            description="Scenic walkway connecting University Hall and SEIR", isRecommended=true,
            latitude=32.7292, longitude=-97.1148),
        CampusSpot(32, "Library Mall", SpotType.CLICK_SPOT, "Central Campus", "Outdoor",
            description="Open mall in front of the Central Library", isRecommended=true,
            latitude=32.7299215, longitude=-97.1135963),
        CampusSpot(33, "Water Fountain", SpotType.CLICK_SPOT, "Central Campus", "Outdoor",
            description="Between University Hall and the Library", isRecommended=true,
            latitude=32.72936, longitude=-97.113471),
        CampusSpot(34, "Brazos Park", SpotType.CLICK_SPOT, "Off Campus", "Outdoor",
            description="Nearby park for relaxation", isRecommended=true,
            latitude=32.7308879, longitude=-97.1108258),
        CampusSpot(35, "Cooper Chase", SpotType.CLICK_SPOT, "Off Campus", "Outdoor",
            description="Near Cooper Street", isRecommended=true,
            latitude=32.7332667, longitude=-97.1149926),
        CampusSpot(36, "UTA Tower", SpotType.CLICK_SPOT, "Central Campus", "Exterior",
            description="Bell tower — signature UTA landmark", isRecommended=true,
            latitude=32.7335648, longitude=-97.114283),
        CampusSpot(37, "Doug Russell Park", SpotType.CLICK_SPOT, "Off Campus", "Outdoor",
            description="Green park adjacent to UTA campus", isRecommended=true,
            latitude=32.726748, longitude=-97.116972),
        CampusSpot(38, "Engineering Research Building", SpotType.CLICK_SPOT, "ERB", "Exterior",
            description="Main entrance of ERB — engineering hub of UTA campus", isRecommended=true,
            latitude=32.7334346, longitude=-97.1132302),
        CampusSpot(39, "Janet & Mike Greene Research Quadrangle", SpotType.CLICK_SPOT, "Central Campus", "Outdoor",
            description="Research quadrangle — scenic outdoor study space", isRecommended=true,
            latitude=32.732685, longitude=-97.1129719),
        CampusSpot(40, "CAPPA Courtyard", SpotType.CLICK_SPOT, "Central Campus", "Outdoor",
            description="College of Architecture courtyard — great outdoor space", isRecommended=true,
            latitude=32.7313643, longitude=-97.1157959)
    )

    /** SRA 4.3 SearchSpots() */
    fun searchSpots(query: String): List<CampusSpot> =
        allSpots.filter { it.spotName.contains(query, true) || it.buildingName.contains(query, true) }

    /** SRA 4.3 FilterByType() */
    fun filterByType(type: SpotType?): List<CampusSpot> =
        if (type == null) allSpots else allSpots.filter { it.spotType == type }

    /** SRA 4.3 GetNearbySpots() */
    fun getNearbySpots(building: String): List<CampusSpot> =
        allSpots.filter { it.buildingName == building }

    /** SRA 4.3 CheckAvailability() */
    fun checkAvailability(spotID: Int): Boolean =
        allSpots.find { it.spotID == spotID }?.available ?: false

    /** SRA 4.3 GetSpotDetails() */
    fun getSpotDetails(spotID: Int): CampusSpot? =
        allSpots.find { it.spotID == spotID }

    /** Recommended spots for "For You" tab */
    fun getRecommendedSpots(): List<CampusSpot> = allSpots.filter { it.isRecommended }

    // ── Favorites ─────────────────────────────────────────────────────────

    val favoriteSpotIDs = mutableSetOf<Int>()
    val customPins = mutableListOf<CustomPin>()
    private var nextPinID = 1

    /** SRA SaveFavorite() */
    fun saveFavorite(studentID: String, spotID: Int) { favoriteSpotIDs.add(spotID) }
    fun removeFavorite(spotID: Int) { favoriteSpotIDs.remove(spotID) }
    fun isFavorite(spotID: Int) = favoriteSpotIDs.contains(spotID)
    fun getFavoriteSpots(): List<CampusSpot> = allSpots.filter { favoriteSpotIDs.contains(it.spotID) }

    fun addCustomPin(pin: CustomPin): CustomPin {
        val p = pin.copy(pinID = nextPinID++)
        customPins.add(p)
        return p
    }
    fun removeCustomPin(pinID: Int) { customPins.removeAll { it.pinID == pinID } }

    // ── Clubs ─────────────────────────────────────────────────────────────

    val clubs = mutableListOf(
        Club(1, "ACM UTA", "Technology",
            description="Largest CS/CE org at UTA. Hosts HackUTA, workshops, and software projects.",
            location="500 UTA Blvd",
            contactEmail="contact@uta.acm.org",
            howToJoin="Fill out the MavOrgs membership form and join the Discord.",
            formUrl="https://mavorgs.campuslabs.com/engage/submitter/form/start/623436",
            formLabel="MavOrgs Membership Form",
            discordUrl="https://discord.gg/nwUCt6tfCK",
            discordLabel="discord.gg/nwUCt6tfCK",
            logoInitials="AC", logoColor=Color.parseColor("#0D1117")),
        Club(2, "IEEE UTA", "Engineering",
            description="IEEE student branch. Technical workshops, networking, and industry connections.",
            location="Nedderman Hall #132",
            contactEmail="utarlington.ieee@gmail.com",
            howToJoin="Email the chapter or visit Nedderman Hall #132 during office hours.",
            logoInitials="IE", logoColor=Color.parseColor("#006699")),
        Club(3, "Girls Who Code", "Technology",
            description="Community for women and non-binary students in tech. Workshops and mentorship.",
            location="UTA Campus",
            contactEmail="utagwc@gmail.com",
            howToJoin="Email the team to get involved. No experience needed.",
            logoInitials="GW", logoColor=Color.parseColor("#D81B60")),
        Club(4, "Cybersecurity Club", "Security",
            description="Est. 2011. Pen testing, cryptography, red/blue team tactics. Fridays 6–8 PM. No dues.",
            location="ERB 436",
            contactEmail="rxr8786@mavs.uta.edu",
            howToJoin="Step 1: Join Discord. Step 2: Complete the Sign-Up Form on MavOrgs.",
            formUrl="https://mavorgs.campuslabs.com/engage/submitter/form/start/604431",
            formLabel="CSEC Sign-Up Form",
            discordUrl="https://discord.gg/tdrfjgqndm",
            discordLabel="discord.gg/tdrfjgqndm",
            logoInitials="CS", logoColor=Color.parseColor("#1B5E20")),
        Club(5, "Aero Mavs", "Aerospace",
            description="UTA's largest aerospace org. Solid Rocketry, Hybrid, and Fixed-Wing divisions.",
            location="Woolf Hall Rm 113",
            contactEmail="aeromavs@gmail.com",
            howToJoin="Email the club for meeting info. Open to all majors.",
            logoInitials="AM", logoColor=Color.parseColor("#E65100")),
        Club(6, "Engineers Without Borders", "Engineering",
            description="Engineering solutions for communities. Water system in Bolivia 2025.",
            location="UTA Campus",
            contactEmail="mavs.ewb@gmail.com",
            howToJoin="Visit the Linktree for details and contact officers.",
            formUrl="https://linktr.ee/ewbuta",
            formLabel="linktr.ee/ewbuta",
            logoInitials="EW", logoColor=Color.parseColor("#4E342E")),
        Club(7, "IISE UTA", "Industrial Eng.",
            description="Industrial & systems engineering society. Connects students with professionals.",
            location="Woolf Hall Rm 420S",
            contactEmail="iise-officers@mavs.uta.edu",
            howToJoin="Come to a General Body Meeting or fill out the interest form.",
            formUrl="https://forms.office.com/r/hbZjEcmxYW",
            formLabel="Microsoft Forms Interest Form",
            logoInitials="II", logoColor=Color.parseColor("#283593")),
        Club(8, "VGDO", "Game Dev",
            description="Video Game Developers Org. Game jams, workshops, art, music. No dues.",
            location="ERB 420",
            contactEmail="vgdo.uta@gmail.com",
            howToJoin="Join Discord, agree to server rules, set your nickname. An officer will grant access.",
            discordUrl="https://discord.gg/k5cgBEStQj",
            discordLabel="discord.gg/k5cgBEStQj",
            logoInitials="VG", logoColor=Color.parseColor("#4A148C")),
        Club(9, "LUGTNUTS", "Open Source",
            description="Linux User Group at UTA. Open source, systems, terminal. All levels welcome.",
            location="500 UTA Blvd",
            contactEmail="lugnutsclub@proton.me",
            howToJoin="Join the Discord community. No formal process needed.",
            formUrl="https://lugtnuts.org",
            formLabel="lugtnuts.org",
            discordUrl="https://discord.gg/SJvNTGmrD5",
            discordLabel="discord.gg/SJvNTGmrD5",
            logoInitials="LG", logoColor=Color.parseColor("#263238")),
        Club(10, "Maverick Chess Club", "Strategy",
            description="All skill levels welcome. No dues. Walk-in meetings open to all students and staff.",
            location="Arlington, TX",
            contactEmail="mavchess@proton.me",
            howToJoin="Walk in to any meeting. For email updates fill the MavEngage form.",
            formUrl="https://mavengage.uta.edu/submitter/form/start/714027",
            formLabel="MavEngage Membership Form",
            logoInitials="MC", logoColor=Color.parseColor("#311B92"))
    )

    // ── Memberships ───────────────────────────────────────────────────────

    val memberships = mutableMapOf<Int, MembershipStatus>() // clubID -> status
    val savedClubIDs = mutableSetOf<Int>()

    fun getMembershipStatus(clubID: Int): MembershipStatus =
        memberships[clubID] ?: MembershipStatus.NOT_A_MEMBER

    fun setMembershipStatus(clubID: Int, status: MembershipStatus) {
        memberships[clubID] = status
    }

    fun toggleSavedClub(clubID: Int) {
        if (savedClubIDs.contains(clubID)) savedClubIDs.remove(clubID)
        else savedClubIDs.add(clubID)
    }

    fun getAllClubs(): List<Club> = clubs

    fun getClub(clubID: Int): Club? = clubs.find { it.clubID == clubID }
}