package services

import models.{Apartment, ApartmentDTO, ApartmentList, RealtorApartmentList, User}
import play.api.Logger
import play.api.libs.json.{JsResult, JsValue, Json}

import scala.util.{Failure, Success}

object ApartmentService {

  val log = Logger(this.getClass.getName)

  def getAllApartments(page: Int, pageSize: Int = 25, search: String = null) = {
    getApartmentList(ApartmentList.getStorageId)
      .flatMap(apartmentId => getApartmentById(apartmentId))
      .filter(apartment => {
        if (search != null && !apartment.title.toLowerCase.contains(search.toLowerCase) && !apartment.description.toLowerCase.contains(search.toLowerCase)) {
          false
        } else {
          true
        }
      })
      .grouped(pageSize)
      .drop(page)
      .next()
  }

  def getApartmentById(apartmentId: String) = RedisService.getValue(Apartment.getStorageId(apartmentId)) match {
    case Success(apartmentString) => Json.fromJson[Apartment](Json.parse(apartmentString)).asOpt
    case Failure(_) => None
  }

  def getApartmentsByRealtor(realtor: User, page: Int, pageSize: Int = 25) = {
    getApartmentList(RealtorApartmentList.getStorageId(User.getUserId(realtor)))
      .flatMap(apartmentId => getApartmentById(apartmentId))
      .grouped(pageSize)
      .drop(page)
      .next()
  }

  def updateApartment(updatedApartment: Apartment, realtor: User) = {
    if(updatedApartment.realtorId.equals(User.getUserId(realtor))) {
      getApartmentById(Apartment.getStorageId(updatedApartment))
        .map(_ => RedisService.setValue(Apartment.getStorageId(updatedApartment), Json.toJson[Apartment](updatedApartment).toString()))
    }
  }

  def deleteApartment(apartmentId: String, realtor: User): Option[Unit] = getApartmentById(apartmentId)
      .map(apartment => deleteApartment(apartment, realtor))

  def deleteApartment(apartment: Apartment, realtor: User): Unit = {
    if(apartment.realtorId.equals(User.getUserId(realtor))) {
      RedisService.removeValue(Apartment.getStorageId(apartment))
      deleteApartmentFromApartmentList(apartment, ApartmentList.getStorageId)
      deleteApartmentFromApartmentList(apartment, RealtorApartmentList.getStorageId(User.getUserId(realtor)))
    }
  }

  private def deleteApartmentFromApartmentList(apartment: Apartment, storageId: String): Unit = {
    val oldApartmentSeq: Seq[String] = getApartmentList(storageId)
    val newApartmentSeqJson = Json
      .toJson[Seq[String]](oldApartmentSeq.filterNot(_.equals(apartment.appId)))
      .toString()
    RedisService.setValue(storageId, newApartmentSeqJson)
  }

  def addApartment(apartmentDTO: ApartmentDTO, realtor: User) = {
    // because we're not using a standard relational DB, we need to maintain multiple lists
    val apartment = Apartment.createNewApartmentFromDTO(apartmentDTO, User.getUserId(realtor))
    log.warn(s"add apartment: $apartment")
    RedisService.setValue(Apartment.getStorageId(apartment), Json.toJson[Apartment](apartment).toString())

    updateOrCreateAllApartmentList(apartment)
    updateOrCreateRealtorApartmentList(apartment, realtor)
  }

  private def getApartmentList(storageId: String) = {
    var resultString: Seq[String] = Seq.empty[String]
    try {
      resultString = RedisService.getValue(storageId) match {
        case Success(apartmentList) => Json.fromJson[Seq[String]](Json.parse(apartmentList)).get
        case Failure(_) => Seq.empty[String]
      }
    } catch {
      case t: Throwable => resultString = Seq.empty[String]
    }
    resultString
  }

  private def updateOrCreateRealtorApartmentList(apartment: Apartment, realtor: User) = {
    log.warn(s"add apartment realtor List")
    val storageId = RealtorApartmentList.getStorageId(User.getUserId(realtor))
    updateOrCreateApartmentList(apartment, storageId)
  }

  private def updateOrCreateAllApartmentList(apartment: Apartment) = {
    log.warn(s"add apartment all List")
    val storageId = ApartmentList.getStorageId
    updateOrCreateApartmentList(apartment, storageId)
  }

  private def updateOrCreateApartmentList(apartment: Apartment, storageId: String) = {
    log.warn(s"add apartment List")
    val oldApartmentSeq: Seq[String] = getApartmentList(storageId)
    val newApartmentSeqJson = Json
      .toJson[Seq[String]](oldApartmentSeq.appended(apartment.appId))
      .toString()
    RedisService.setValue(storageId, newApartmentSeqJson)
  }

}
