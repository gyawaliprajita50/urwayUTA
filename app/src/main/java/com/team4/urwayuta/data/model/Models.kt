package com.team4.urwayuta.data.model

// ─── SRA 5.3 Data Entities ────────────────────────────────────────────────

/** Student: studentID, name, email, major, password hash */
data class Student(
    val studentID: String,
    val firstName: String,
    val middleName: String = "",
    val lastName: String,
    val email: String,
    val major: String,
    val role: String = "student", // "student" | "faculty"
    var passwordHash: String      // plain-text for prototype; hash in production
) {
    val fullName get() = "$firstName $lastName".trim()
    val initials get() = "${firstName.firstOrNull()?.uppercase()}${lastName.firstOrNull()?.uppercase()}"
}

/** SpotType enum — SRA 4.3 GetSpotType() */
enum class SpotType(val label: String) {
    MICROWAVE("Microwave"),
    SEATING("Seating Area"),
    VENDING("Vending Machine"),
    CAFE("Café"),
    LIBRARY("Library"),
    PHOTO("Photo Spot"),
    STUDY("Study/Bughouse"),
    CLICK_SPOT("Click Spot")
}

/** CampusSpot: spotID, spotName, spotType, buildingName, floor, roomNumber, available */
data class CampusSpot(
    val spotID: Int,
    val spotName: String,
    val spotType: SpotType,
    val buildingName: String,
    val floor: String,
    val roomNumber: String? = null,
    val available: Boolean = true,
    val description: String = "",
    val seats: Int = 0,
    val isRecommended: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    /** SRA 4.3 GetSpotType() */
    fun getSpotType(): String = spotType.label

    /** SRA 4.3 GetAddress() */
    fun getAddress(): String = "$buildingName, $floor${if (roomNumber != null) ", Rm $roomNumber" else ""}"

    /** SRA 4.3 GetSpotDetails() */
    fun getSpotDetails(): CampusSpot = this

    /** SRA 4.3 CheckAvailability() */
    fun checkAvailability(): Boolean = available

    /** SRA 4.3 GetLocation() */
    fun getLocation(): Map<String, String?> = mapOf(
        "building" to buildingName,
        "floor" to floor,
        "room" to roomNumber
    )
}

/** FavoriteSpots: links studentID to a list of spotIDs */
data class FavoriteSpot(
    val studentID: String,
    val spotID: Int
)

/** Custom pin placed by user on the map */
data class CustomPin(
    val pinID: Int,
    val studentID: String,
    val name: String,
    val description: String = "",
    val xPercent: Float = 0f, // position on campus map image
    val yPercent: Float = 0f,
    val isPublic: Boolean = false
)

/** Club: clubID, clubName, interests, meetingInfo */
data class Club(
    val clubID: Int,
    val clubName: String,
    val category: String,
    val interests: String = "",        // SRA: interests
    val meetingInfo: String = "",      // SRA: meetingInfo
    val description: String,
    val location: String,
    val contactEmail: String,
    val howToJoin: String,
    val formUrl: String? = null,
    val formLabel: String? = null,
    val discordUrl: String? = null,
    val discordLabel: String? = null,
    val logoInitials: String = "",
    val logoColor: Int = 0xFF003594.toInt(),
    val isUserCreated: Boolean = false
) {
    /** SRA ClubInfo.ClubActivities() */
    fun getClubActivities(): List<String> = listOf(
        "General Meeting — check Discord or email for schedule",
        "Workshop / Social — managed by faculty and club officers"
    )
}

/** Membership status states — SRA 3.5 STD */
enum class MembershipStatus {
    NOT_A_MEMBER,
    PENDING,
    ACTIVE,
    WITHDRAWAL,
    REJECTED
}

/** Membership: studentID, clubID, enrollmentStatus, membershipDetails */
data class Membership(
    val studentID: String,
    val clubID: Int,
    var enrollmentStatus: MembershipStatus = MembershipStatus.NOT_A_MEMBER,
    val membershipDetails: String = ""
)

/** Route: routeID, startLocation, destinationLocation, distance, duration, steps */
data class Route(
    val routeID: Int,
    val startLocation: String,
    val destinationLocation: String,
    val distance: String,   // SRA GetDistance()
    val duration: String,   // SRA GetDuration() — walking time
    val steps: List<String> // SRA GetSteps()
)
