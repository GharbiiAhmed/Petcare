package tn.petcare_android.data.model.map

import com.mapbox.geojson.Point
import tn.petcare_android.data.model.auth.AppUser


data class VetLocation(
    val id: String,
    val userId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isAvailable: Boolean,
    val appUser: AppUser
) {
    val coordinate: Point
        get() = Point.fromLngLat(longitude, latitude)
}

data class SitterLocation(
    val id: String,
    val userId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isAvailable: Boolean,
    val appUser: AppUser
) {
    val coordinate: Point
        get() = Point.fromLngLat(longitude, latitude)
}

data class TrainerLocation(
    val id: String,
    val userId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isAvailable: Boolean,
    val specialization: String?,
    val hourlyRate: Double?
) {
    val coordinate: Point
        get() = Point.fromLngLat(longitude, latitude)
}

data class SalonLocation(
    val id: String,
    val userId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isAvailable: Boolean,
    val appUser: AppUser
) {
    val coordinate: Point
        get() = Point.fromLngLat(longitude, latitude)
}

sealed class MapLocation {
    abstract val id: String
    abstract val name: String
    abstract val address: String
    abstract val coordinate: Point
    abstract val isAvailable: Boolean
    abstract val userId: String

    data class Vet(val location: VetLocation) : MapLocation() {
        override val id: String get() = location.id
        override val name: String get() = location.name
        override val address: String get() = location.address
        override val coordinate: Point get() = location.coordinate
        override val isAvailable: Boolean get() = location.isAvailable
        override val userId: String get() = location.userId
    }

    data class Sitter(val location: SitterLocation) : MapLocation() {
        override val id: String get() = location.id
        override val name: String get() = location.name
        override val address: String get() = location.address
        override val coordinate: Point get() = location.coordinate
        override val isAvailable: Boolean get() = location.isAvailable
        override val userId: String get() = location.userId
    }

    data class Trainer(val location: TrainerLocation) : MapLocation() {
        override val id: String get() = location.id
        override val name: String get() = location.name
        override val address: String get() = location.address
        override val coordinate: Point get() = location.coordinate
        override val isAvailable: Boolean get() = location.isAvailable
        override val userId: String get() = location.userId
    }

    data class Salon(val location: SalonLocation) : MapLocation() {
        override val id: String get() = location.id
        override val name: String get() = location.name
        override val address: String get() = location.address
        override val coordinate: Point get() = location.coordinate
        override val isAvailable: Boolean get() = location.isAvailable
        override val userId: String get() = location.userId
    }
}
