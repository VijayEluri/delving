package eu.delving.metarepo.harvesting

import javax.servlet.http.{HttpServletRequest}

import java.lang.String
import java.util.{Map, Date}
import scala.collection.JavaConversions._
import eu.delving.metarepo.core.MetaRepo
import collection.mutable.HashMap
import java.util.Map.Entry
import eu.delving.metarepo.core.MetaRepo.{PmhVerb, HarvestStep, Record}
import org.apache.log4j.Logger
import eu.delving.metarepo.exceptions.{BadArgumentException, BadResumptionTokenException, CannotDisseminateFormatException, NoRecordsMatchException}
import java.text.{SimpleDateFormat, ParseException, DateFormat}
import xml._

/**
 *  This class is used to parse an OAI-PMH instruction from an HttpServletRequest and return the proper XML response
 *
 *  This implementation is based on the v.2.0 specification that can be found here: http://www.openarchives.org/OAI/openarchivesprotocol.html
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 16, 2010 12:06:56 AM
 */

class OaiPmhService(request: HttpServletRequest, metaRepo: MetaRepo) {

  private val log = Logger.getLogger(getClass());

  private val VERB = "verb"
  private val legalParameterKeys = List("verb", "identifier", "metadataPrefix", "set", "from", "until", "resumptionToken")
  private val dateFormat = new SimpleDateFormat("dd/MM/yyyy")

  /**
   * receive an HttpServletRequest with the OAI-PMH parameters and return the correctly formatted xml as a string.
   */

  def parseRequest() : String = {

    val requestParams = getRequestParams(request)

    if (!isLegalPmhRequest(requestParams)) return createErrorResponse("badArgument").toString

    val params = asSingleValueMap(requestParams)

    def pmhRequest(verb: PmhVerb) : PmhRequestEntry = createPmhRequest(params, verb)

    val response = try {
      params.get(VERB).get match {
        case "Identify" => processIdentify( pmhRequest(PmhVerb.IDENTIFY) )
        case "ListMetadataFormats" => processListMetadataFormats( pmhRequest(PmhVerb.List_METADATA_FORMATS) )
        case "ListSets" => processListSets( pmhRequest(PmhVerb.LIST_SETS) )
        case "ListRecords" => processListRecords( pmhRequest(PmhVerb.LIST_RECORDS) )
        case "ListIdentifiers" => processListIdentifiers( pmhRequest(PmhVerb.LIST_IDENTIFIERS) )
        case "GetRecord" => processGetRecord( pmhRequest(PmhVerb.GET_RECORD) )
        case _ => createErrorResponse("badVerb")
      }
    } catch {
      case nrm: NoRecordsMatchException => createErrorResponse("noRecordsMatch")
      case cdf: CannotDisseminateFormatException => createErrorResponse("cannotDisseminateFormat")
      case brt: BadResumptionTokenException => createErrorResponse("badResumptionToken")
      // not caught explicitly MappingException, XMLStreamException todo what to do with these
      case e: Exception =>
        log.error(e.getMessage + e.getStackTraceString)
        createErrorResponse("badArgument")
    }
    response.toString
  }

  def isLegalPmhRequest(params: Map[String, Array[String]]) : Boolean = {
    val isValid = true
    // request must contain the verb parameter
    if (!params.containsKey(VERB)) return false

    // no repeat queryParameters are allowed
    if (params.values.exists(value => value.length > 1)) return false

    // check for illegal queryParameter keys
    if (!(params.keys filterNot (legalParameterKeys contains)).isEmpty) return false

    isValid
  }

  /**
   */
  // TODO: add values from a message.properties file and complete oai identifier block
  def processIdentify(pmhRequestEntry: PmhRequestEntry) : Elem = {
    val config = metaRepo.getMetaRepoConfig
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
      <responseDate>{new Date}</responseDate>
      <request verb="Identify">{request.getRequestURL}</request>
      <Identify>
        <repositoryName>{config.getRepositoryName}</repositoryName>
        <baseURL>{request.getRequestURL}</baseURL>
        <protocolVersion>2.0</protocolVersion>
        <adminEmail>{config.getAdminEmail}</adminEmail>
        <earliestDatestamp>{config.getEarliestDateStamp}</earliestDatestamp>
        <deletedRecord>persistent</deletedRecord>
        <granularity>YYYY-MM-DDThh:mm:ssZ</granularity>
        <compression>deflate</compression>
        <description>
          <oai-identifier
            xmlns="http://www.openarchives.org/OAI/2.0/oai-identifier"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation=
                "http://www.openarchives.org/OAI/2.0/oai-identifier
            http://www.openarchives.org/OAI/2.0/oai-identifier.xsd">
            <scheme>oai</scheme>
            <repositoryIdentifier>{config.getRepositoryIdentifier}</repositoryIdentifier>
            <delimiter>:</delimiter>
            <sampleIdentifier>{config.getSampleIdentifier}</sampleIdentifier>
          </oai-identifier>
        </description>
     </Identify>
</OAI-PMH>
  }

  def processListSets(pmhRequestEntry: PmhRequestEntry) : Elem = {
    val dataSets = metaRepo.getDataSets

    // when there are no collections throw "noSetHierarchy" ErrorResponse
    if (dataSets.size == 0) return createErrorResponse("noSetHierarchy")

    <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
     <responseDate>{new Date}</responseDate>
     <request verb="ListSets">{request.getRequestURL}</request>
      <ListSets>
        { for (set <- dataSets.values) yield
          <set>
            <setSpec>{set.setSpec}</setSpec>
            <setName>{set.setName}</setName>
          </set>
        }
      </ListSets>
    </OAI-PMH>
  }

  /**
   * This method can give back the following Error and Exception conditions: idDoesNotExist, noMetadataFormats.
   */
  def processListMetadataFormats(pmhRequestEntry: PmhRequestEntry) : Elem = {

    // if no identifier present list all formats
    val identifier = pmhRequestEntry.pmhRequestItem.identifier.split(":").last

    // otherwise only list the formats available for the identifier
    val metadataFormats = if (identifier.isEmpty) metaRepo.getMetadataFormats else metaRepo.getMetadataFormats(identifier)

    def formatRequest() : Elem = if (!identifier.isEmpty) <request verb="ListMetadataFormats" identifier={identifier}>{request.getRequestURL}</request>
                                    else <request verb="ListMetadataFormats">{request.getRequestURL}</request>

    val elem =
    <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
      <responseDate>{new Date}</responseDate>
      {formatRequest}
      <ListMetadataFormats>
       {for (format <- metadataFormats) yield
        <metadataFormat>
          <metadataPrefix>{format.prefix}</metadataPrefix>
          <schema>{format.schema}</schema>
          <metadataNamespace>{format.namespace}</metadataNamespace>
       </metadataFormat>
        }
      </ListMetadataFormats>
    </OAI-PMH>
    elem
  }

  /**
   * This method can give back the following Error and Exception conditions: BadResumptionToken, cannotDisseminateFormat, noRecordsMatch, noSetHierachy
   */
  def processListIdentifiers(pmhRequestEntry: PmhRequestEntry) = {
        // parse all the params from map
    val harvestStep: HarvestStep = getHarvestStep(pmhRequestEntry)
    val setSpec = harvestStep.pmhRequest.getSet

      <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
      <responseDate>{new Date}</responseDate>
      <request verb="ListIdentifiers" from={harvestStep.pmhRequest.getFrom.toString} until={harvestStep.pmhRequest.getUntil.toString}
               metadataPrefix={harvestStep.pmhRequest.getMetadataPrefix}
               set={setSpec}>{request.getRequestURL}</request>
      <ListIdentifiers>
        { for (record <- harvestStep.records) yield
        <header status={recordStatus(record)}>
          <identifier>{setSpec}:{record.identifier}</identifier>
          <datestamp>{record.modified}</datestamp>
          <setSpec>{setSpec}</setSpec>
       </header>
        }
        {renderResumptionToken(harvestStep)}
     </ListIdentifiers>
    </OAI-PMH>
  }


  def processListRecords(pmhRequestEntry: PmhRequestEntry) : Elem = {
    val harvestStep: HarvestStep = getHarvestStep(pmhRequestEntry)
    val pmhObject = harvestStep.pmhRequest

    var elem : Elem =
    <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
     <responseDate>{new Date}</responseDate>
     <request verb="ListRecords" from={pmhObject.getFrom.toString}
              until={pmhObject.getUntil.toString} metadataPrefix={pmhObject.getMetadataPrefix}>{request.getRequestURL}</request>
     <ListRecords>
          <metadata>
            {for (record <- harvestStep.records) yield
              renderRecord(record, pmhObject.getMetadataPrefix, pmhObject.getSet)
            }
          </metadata>
       {renderResumptionToken(harvestStep)}
     </ListRecords>
    </OAI-PMH>
    for (entry <- harvestStep.namespaces.toMap.entrySet) {
      elem = elem % new UnprefixedAttribute( "xmlns:"+entry.getKey.toString, entry.getValue.toString, Null )
    }
    elem
  }

  def processGetRecord(pmhRequestEntry: PmhRequestEntry) : Elem = {
    val pmhRequest = pmhRequestEntry.pmhRequestItem
    // get identifier and format from map else throw BadArgument Error
    if (pmhRequest.identifier.isEmpty || pmhRequest.metadataPrefix.isEmpty) return createErrorResponse("badArgument")

    val identifier = pmhRequest.identifier
    val metadataFormat = pmhRequest.metadataPrefix

    val record = metaRepo.getRecord(identifier, metadataFormat)
    if (record == null) return createErrorResponse("idDoesNotExist")

    var elem : Elem =
    <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
      <responseDate>{new Date}</responseDate>
      <request verb="GetRecord" identifier={identifier}
               metadataPrefix={metadataFormat}>{request.getRequestURL}</request>
      <GetRecord>
        {renderRecord(record, metadataFormat, identifier.split(":").head)}
     </GetRecord>
    </OAI-PMH>
    for (entry <- record.namespaces.toMap.entrySet) {
      elem = elem % new UnprefixedAttribute( "xmlns:"+entry.getKey.toString , entry.getValue.toString, Null )
    }
    elem
  }

  private def getHarvestStep(pmhRequestEntry: PmhRequestEntry) : HarvestStep = {
    if (!pmhRequestEntry.resumptionToken.isEmpty)
      metaRepo.getHarvestStep(pmhRequestEntry.resumptionToken)
    else
      createFirstHarvestStep(pmhRequestEntry.pmhRequestItem)
  }

  private def createFirstHarvestStep(item: PmhRequestItem) : HarvestStep = {
    // todo implement proper date parsing
    val from = new Date() //getDate(item.from)
    val until = new Date() //getDate(item.until)
    metaRepo.getFirstHarvestStep(item.verb, item.set, from, until, item.metadataPrefix)
  }

//  private def getDate(dateString: String): Date = {
//    if (dateString.isEmpty) return null
//    val date = try {
//      return dateFormat.parse(dateString)
//    } catch {
//      case e: ParseException => throw new BadArgumentException("Unable to parse date: " + dateString)
//    }
//    date
//  }

  // todo find a way to not show status namespace when not deleted
  private def recordStatus(record: Record) : String = if (record.deleted) "deleted" else ""

  private def renderRecord(record: Record, metadataPrefix: String, set: String) : Elem = {

    val recordAsString = record.xml(metadataPrefix).replaceAll("<[/]{0,1}(br|BR)>", "<br/>").replaceAll("&((?!amp;))","&amp;$1")
    // todo get the record separator for rendering from somewhere
    val response = try {
      val elem = XML.loadString("<record>\n" + {recordAsString} + "</record>")
      <record>
        <header>
          <identifier>{set}:{record.identifier}</identifier>
          <datestamp>{record.modified}</datestamp>
          <setSpec>{set}</setSpec>
        </header>
        <metadata>
{elem}
        </metadata>
      </record>
    } catch {
      case e: Exception =>
          println (e.getMessage)
          <record/>
    }
    response
  }

  private def renderResumptionToken(step: HarvestStep) = {
    if (step.hasNext)
      <resumptionToken expirationDate={step.expiration.toString} completeListSize={step.listSize.toString}
                       cursor={step.cursor.toString}>{step.nextResumptionToken.toString}</resumptionToken>
    else
      <resumptionToken/>
  }

  def createPmhRequest(params: HashMap[String, String], verb: PmhVerb): PmhRequestEntry = {
    def getParam(key: String) = params.getOrElse(key, "")
    val pmh = PmhRequestItem(verb,
      getParam("set"), getParam("from"), getParam("until"),
      getParam("metadataPrefix"), getParam("identifier"))
    PmhRequestEntry(pmh, getParam("resumptionToken"))
  }

  def getRequestParams(request: HttpServletRequest): Map[String, Array[String]] = request.getParameterMap.asInstanceOf[Map[String, Array[String]]]

  def asSingleValueMap(requestParams: Map[String, Array[String]]): HashMap[String, String] = {
    val params = HashMap[String, String]()
    requestParams.entrySet.foreach {entry: Entry[String, Array[String]] => params.put(entry.getKey, entry.getValue.head)}
    params
  }

  /**
   * This method is used to create all the OAI-PMH error responses to a given OAI-PMH request. The error descriptions have
   * been taken directly from the specifications document for v.2.0.
   */
  def createErrorResponse(errorCode: String): Elem = {
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
      <responseDate>{new Date}</responseDate>
      <request>{request.getRequestURL}</request>
      {errorCode match {
      case "badArgument" => <error code="badArgument">The request includes illegal arguments, is missing required arguments, includes a repeated argument, or values for arguments have an illegal syntax.</error>
      case "badResumptionToken" => <error code="badResumptionToken">The value of the resumptionToken argument is invalid or expired.</error>
      case "badVerb" => <error code="badVerb">Value of the verb argument is not a legal OAI-PMH verb, the verb argument is missing, or the verb argument is repeated.</error>
      case "cannotDisseminateFormat" => <error code="cannotDisseminateFormat">The metadata format identified by the value given for the metadataPrefix argument is not supported by the item or by the repository.</error>
      case "idDoesNotExist" => <error code="idDoesNotExist">The value of the identifier argument is unknown or illegal in this repository.</error>
      case "noMetadataFormats" => <error code="noMetadataFormats">There are no metadata formats available for the specified item.</error>
      case "noRecordsMatch" => <error code="noRecordsMatch">The combination of the values of the from, until, set and metadataPrefix arguments results in an empty list.</error>
      case "noSetHierarchy" => <error code="noSetHierarchy">This repository does not support sets</error> // Should never be used. We only use sets
      case _ => <error code="unknown">Unknown Error Corde</error> // should never happen.
    }}
</OAI-PMH>
  }

  case class PmhRequestItem(verb: PmhVerb, set: String, from: String, until: String, metadataPrefix: String, identifier: String)
  case class PmhRequestEntry(pmhRequestItem: PmhRequestItem, resumptionToken: String)

}
object OaiPmhService {
   def parseHttpServletRequest(request: HttpServletRequest, metaRepo: MetaRepo) : String = {
     val service = new OaiPmhService(request, metaRepo)
     service parseRequest
   }
 }

