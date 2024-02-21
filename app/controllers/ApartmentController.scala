package controllers

import models.{Apartment, ApartmentDTO}
import play.api.libs.json.Json
import play.api.mvc._
import services.{ApartmentService, AuthenticationService, RedisService}

import javax.inject.Inject
import scala.util.{Failure, Success}


class ApartmentController @Inject()(cc: ControllerComponents) extends AuthenticatiedController(cc) {

  def getAllApartments(page: Option[Int], pageSize: Option[Int], search: Option[String]) = UserAuthenticatedAction {
    _ => {
      val apartments = ApartmentService.getAllApartments(page.getOrElse(0), pageSize.getOrElse(25), search.getOrElse(null))
        .map(apartment => new ApartmentDTO(apartment.title, apartment.description, apartment.area, apartment.roomsNo, apartment.monthlyPrice))
      Ok(Json.toJson(apartments))
    }
  }

  def getRealtorApartments(page: Option[Int], pageSize: Option[Int], search: Option[String]) = RealtorAuthenticatedAction {
    request => {
      val sessionString: String = request.headers.get("sessionString").getOrElse("")
      AuthenticationService.getUserForSession(sessionString)
        .map(user => {
          val apartments = ApartmentService.getApartmentsByRealtor(user, page.getOrElse(0), pageSize.getOrElse(25))
          Ok(Json.toJson(apartments))
        })
        .getOrElse(BadRequest("Something went wrong"))
    }
  }

  def postApartment() = RealtorAuthenticatedAction {
    request => {
      val sessionString: String = request.headers.get("sessionString").getOrElse("")
      AuthenticationService.getUserForSession(sessionString)
        .map(user => {
          request.body.asJson
            .map(apartmentJson => {
              log.warn(s"adding apartment: $apartmentJson")
              Json.fromJson[ApartmentDTO](apartmentJson).asOpt
                .map(apartmentDTO => {
                  log.warn(s"adding apartment: $apartmentDTO")
                  ApartmentService.addApartment(apartmentDTO, user) match {
                    case Success(_) => Ok(apartmentJson)
                    case Failure(_) => BadRequest("apartment not saved!")
                  }
                }).getOrElse(BadRequest("Apartment not parsed"))
            }).getOrElse(BadRequest("payload not json"))
        })
        .getOrElse(BadRequest("User Not Found"))
    }
  }

  def updateApartment() = RealtorAuthenticatedAction {
    request => {
      val sessionString: String = request.headers.get("sessionString").getOrElse("")
      AuthenticationService.getUserForSession(sessionString)
        .map(user => {
          request.body.asJson
            .map(apartmentJson => {
              Json.fromJson[Apartment](apartmentJson).asOpt
                .map(apartment => {
                  ApartmentService.updateApartment(apartment, user) match {
                    case Success(_) => Ok(apartmentJson)
                    case Failure(_) => BadRequest("apartment not saved!")
                  }
                }).getOrElse(BadRequest("Apartment not parsed"))
            }).getOrElse(BadRequest("payload not json"))
        })
        .getOrElse(BadRequest("User Not Found"))
    }
  }

  def deleteApartment(apartmentId: String) = RealtorAuthenticatedAction {
    request => {
      val sessionString: String = request.headers.get("sessionString").getOrElse("")
      AuthenticationService.getUserForSession(sessionString)
        .map(user => {
          ApartmentService.deleteApartment(apartmentId, user)
          Ok("Success")
        })
        .getOrElse(BadRequest("User Not Found"))
    }
  }
}