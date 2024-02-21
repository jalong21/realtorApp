package models

import play.api.libs.json.{Json, Reads, Writes}

import java.util.UUID

case class ApartmentDTO(title: String, description: String, area: Int, roomsNo: Int, monthlyPrice: Int)
object ApartmentDTO {
  implicit val jsonReads: Reads[ApartmentDTO] = Json.reads[ApartmentDTO]
  implicit val jsonWrites: Writes[ApartmentDTO] = Json.writes[ApartmentDTO]
  def getDTOFromAppartment(apartment: Apartment) = new ApartmentDTO(
    apartment.title,
    apartment.description,
    apartment.area,
    apartment.roomsNo,
    apartment.monthlyPrice)
}

case class Apartment(appId: String, realtorId: String, title: String, description: String, area: Int, roomsNo: Int, monthlyPrice: Int, dateAdded: Long)
object Apartment {
  implicit val jsonReads: Reads[Apartment] = Json.reads[Apartment]
  implicit val jsonWrites: Writes[Apartment] = Json.writes[Apartment]
  def getStorageId(apartmentId: String): String = s"apartment-$apartmentId"
  def getStorageId(apartment: Apartment):String = getStorageId(apartment.appId)
  def createNewApartmentFromDTO(apartmentDTO: ApartmentDTO, realtorId: String) = new Apartment(
    UUID.randomUUID().toString,
    realtorId,
    apartmentDTO.title,
    apartmentDTO.description,
    apartmentDTO.area,
    apartmentDTO.roomsNo,
    apartmentDTO.monthlyPrice,
    System.currentTimeMillis())
}

case class RealtorApartmentList(realtorId: String, ApartmentIdList: List[String])
object RealtorApartmentList {
  implicit val jsonReads: Reads[RealtorApartmentList] = Json.reads[RealtorApartmentList]
  implicit val jsonWrites: Writes[RealtorApartmentList] = Json.writes[RealtorApartmentList]
  def getStorageId(realtorId: String) = s"apartment-$realtorId"
}

case class ApartmentList(ApartmentIdList: List[String])
object ApartmentList {
  implicit val jsonReads: Reads[ApartmentList] = Json.reads[ApartmentList]
  implicit val jsonWrites: Writes[ApartmentList] = Json.writes[ApartmentList]
  def getStorageId = "allApartments"
}