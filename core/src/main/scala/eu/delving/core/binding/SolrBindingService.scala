package eu.delving.core.binding

import scala.collection.JavaConversions._
import scala.reflect.BeanProperty
import org.apache.solr.common.SolrDocumentList
import java.util. {Date, ArrayList, List => JList}
import java.lang.{Boolean => JBoolean}
import eu.europeana.core.querymodel.query._
import org.apache.solr.client.solrj.response. {FacetField, QueryResponse}
import java.net.URL
import xml. {MetaData, NodeSeq, Elem, XML}
import collection.mutable. {ListBuffer, Map}
import collection.immutable. {HashMap, Map => ImMap}

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 10 /18/10 9:01 PM
 */

object SolrBindingService {

  def getFullDocFromOaiPmh(response : QueryResponse) : FullDocItem = {
    val fullDoc = getFullDoc(response)
    val pmhId = fullDoc.getFieldValue("delving_pmhId")
    getRecordFromOaiPmh(pmhId.getFirst)
  }

  private[binding] def getRecordFromOaiPmh(recordId : String, metadataPrefix: String = "abm") : FullDocItem = {
    val baseUrl = "http://localhost:8983/services/oai-pmh"
    val record = XML.load(new URL(baseUrl + "?verb=GetRecords&metadataPrefix=" + metadataPrefix + "&identifier=" + recordId))
    parseSolrDocumentFromGetRecordResponse(record)
  }

  def parseSolrDocumentFromGetRecordResponse(pmhResponse: Elem): FullDocItem = {
    val metadataElements = pmhResponse \\ "metadata"
    val recordElements: NodeSeq = metadataElements \\ "record"
    val solrDoc = SolrDocument()
    recordElements.foreach{
      recordNode =>
        val cleanNodes = recordNode.nonEmptyChildren.filterNot(node => node.label == "#PCDATA")
        val cleanNodeList = for {
          cleanNode <- cleanNodes
          val fieldName: String = if (!cleanNode.prefix.isEmpty) cleanNode.prefix + "_" + cleanNode.label else cleanNode.label
        } yield (FieldValueNode(fieldName, cleanNode.text, cleanNode.attributes.asAttrMap))
        val fieldNames = for (cleanNode <- cleanNodeList) yield cleanNode.fieldName
        fieldNames.toSet[String].foreach{
          fieldName =>
            solrDoc.add(fieldName, cleanNodeList.filter(node => node.fieldName == fieldName).toList)
        }
    }
    FullDocItem(solrDoc)
  }

  def getSolrDocumentList(solrDocumentList : SolrDocumentList) : List[SolrDocument] = {
    def addFieldNodes(key : String, values: List[Any]) : List[FieldValueNode] =
      for (value <- values) yield (FieldValueNode(key, value.toString))

    val docs = new ListBuffer[SolrDocument]
    val ArrayListObject = classOf[ArrayList[Any]]
    val StringObject = classOf[String]
    val DateObject = classOf[Date]
    // check for required fields else check exception
    solrDocumentList.foreach{
        doc =>
          val solrDoc = SolrDocument()
          doc.entrySet.foreach{
            field =>
              val FieldValueClass: Class[_] = field.getValue.getClass
               FieldValueClass match {
                case ArrayListObject => solrDoc.add(field.getKey, addFieldNodes(field.getKey, field.getValue.asInstanceOf[ArrayList[Any]].toList))
                case StringObject => solrDoc.add(field.getKey, List(FieldValueNode(field.getKey, field.getValue.toString)))
                case DateObject => solrDoc.add(field.getKey, List(FieldValueNode(field.getKey, field.getValue.toString)))
                case _ => println("unknown class " + field.getKey)
              }
          }
      docs add solrDoc
    }
    docs.toList
  }

  def getSolrDocumentList(queryResponse : QueryResponse) : List[SolrDocument] = getSolrDocumentList(queryResponse.getResults)

  def getDocIds(queryResponse: QueryResponse): JList[SolrDocId] = {
    val docIds = new ListBuffer[SolrDocId]
    getSolrDocumentList(queryResponse).foreach{
      doc =>
        docIds add (SolrDocId(doc))
    }
    asJavaList(docIds)
  }

  def getBriefDocs(queryResponse: QueryResponse): JList[BriefDocItem] = getBriefDocs(queryResponse.getResults)

  def getBriefDocs(resultList: SolrDocumentList): JList[BriefDocItem] = {
    val briefDocs = new ListBuffer[BriefDocItem]
    getSolrDocumentList(resultList).foreach{
      doc =>
        briefDocs add (BriefDocItem(doc))
    }
    asJavaList(briefDocs)
  }

  def getFullDoc(queryResponse: QueryResponse): FullDocItem = {
    val results = getFullDocs(queryResponse.getResults)
    if (results.isEmpty) throw new EuropeanaQueryException("Full Doc not found")
    results.head
  }

  def getFullDocs(queryResponse: QueryResponse): JList[FullDocItem] = getFullDocs(queryResponse.getResults)

  def getFullDocs(matchDoc: SolrDocumentList): JList[FullDocItem] = {
    val fullDocs = new ListBuffer[FullDocItem]
    getSolrDocumentList(matchDoc).foreach{
      doc =>
        fullDocs add (FullDocItem(doc))
    }
    asJavaList(fullDocs)
  }

  def createFacetMap(links : JList[FacetQueryLinks]) = FacetMap(links.toList)

  def createFacetStatistics(facets: JList[FacetField]) = FacetStatisticsMap(facets.toList)
}

case class FacetMap(private val links : List[FacetQueryLinks]) {

  val facetMap = Map[String, FacetQueryLinks]()
  links.foreach{
    facet =>
      facetMap put (facet.getType, facet)
  }

  def getFacetList = links

  def getFacet(key: String) : FacetQueryLinks = facetMap.getOrElse(key, new FacetQueryLinks("unknown"))
}

case class FacetStatisticsMap(private val facets: List[FacetField]) {

  val facetsMap = Map[String, JList[FacetField.Count]]()
  facets.foreach{
    facet =>
      if (facet.getValueCount != 0) facetsMap put (facet.getName, facet.getValues)
  }

  def facetExists(key: String): Boolean = facetsMap.containsKey(key)

  def availableFacets : JList[String] = facetsMap.keys.toList

  private def getDummyFacetField : FacetField = {
    val facetField = new FacetField("unknown")
    facetField.add("nothing", 0)
    facetField
  }

  def getFacet(key: String) : JList[FacetField.Count] = facetsMap.getOrElse(key, getDummyFacetField.getValues)

}

case class SolrDocument(fieldMap : Map[String, List[FieldValueNode]] = Map[String, List[FieldValueNode]]()) {

  def get(field: String) : List[String] = for(node: FieldValueNode <- fieldMap.getOrElse(field, List[FieldValueNode]())) yield node.fieldValue

  def getFieldValueNode(field: String) : List[FieldValueNode] = fieldMap.getOrElse(field, List[FieldValueNode]())

  def getFieldValueNodeGroupedByLanguage(field: String) : ImMap[String, List[FieldValueNode]] = fieldMap.getOrElse(field, List[FieldValueNode]()).groupBy(fvn => fvn.getLanguage)

  def getFirst(field: String) : String = fieldMap.getOrElse(field, List[FieldValueNode]()).headOption.getOrElse(FieldValueNode("", "")).fieldValue

  private[binding] def add(field: String, value : List[FieldValueNode]) = fieldMap.put(field, value)

  private[binding] def getFieldNames = fieldMap.keys

  def getFieldValueList : List[FieldValue] = for (key <- fieldMap.keys.toList.filter(_.matches(".*_.*"))) yield FieldValue(key, this)

  def getFieldValuesFiltered(include: Boolean, fields : List[String]) : List[FieldValue] = getFieldValueList.filter((fv => fields.contains(fv.getKey) == include))
}

case class FieldValue (key: String, solrDocument: SolrDocument) {

  private val fieldValues = solrDocument.get(key)

  /**
   * This gives back the key that was used to retrieve the fields from the SolrDocument
   */
  def getKey = key

  /**
   * This gives back the key that was used to retrieve the fields from the SolrDocument, but now replacing the "_" convention
   * used by solr to ":" so that it can be used in xml tags or to represented the fieldnames as they were before being indexed
   * by Apache Solr
   */
  def getKeyAsXml = key.replaceFirst("_", ":")

  /**
   * Only give back the first item from the fieldMap retrieved with 'key' in the SolrDocument as a String. When the key
   * is not found an empty String is returned.
   */
  def getFirst : String = solrDocument.getFirst(key)

  /**
   * Give back all values found in the fieldMap retrieved with 'key' in the SolrDocument as a String Array. When the
   * key is not found an empty String Array is returned.
   */
  def getValueAsArray : Array[String] = fieldValues.asInstanceOf[List[String]].toArray

  /**
   * This function gives back a boolean to say if the results returned from the fieldMap in the SolrDocument will be empty or not
   */
  def isNotEmpty = fieldValues.length != 0

}

case class FieldValueNode (fieldName : String, fieldValue: String, attributes: ImMap[String, String] = new HashMap[String, String]())  {

  def getFieldName = fieldName

  def getFieldValue = fieldValue

  def getAttribute(key : String) = attributes.getOrElse(key, "")

  def getLanguage = attributes.getOrElse("xml:lang", "unknown")

  def hasLanguageAttribute = attributes.contains("xml:lang")

  def hasAttributes = !attributes.isEmpty

  def getAttributeKeys = attributes.keys
}

case class SolrDocId(solrDocument : SolrDocument) extends DocId {
  def getEuropeanaUri : String = solrDocument.getFirst("europeana_uri")
}

case class BriefDocItem(solrDocument : SolrDocument) extends BriefDoc {
    private def assign(key: String) = solrDocument.getFirst(key)

    override def getFieldValue(key : String) : FieldValue = FieldValue(key, solrDocument)

    def getFieldValuesFiltered(include: Boolean, fields: List[String]) : JList[FieldValue] = solrDocument.getFieldValuesFiltered(include, fields)

    def getFieldValueList : JList[FieldValue] = solrDocument.getFieldValueList

    def getId : String = assign("europeana_uri")
    def getTitle : String = assign("title")
    def getThumbnail : String = assign("europeana_object")
    def getCreator : String = assign("creator")
    def getYear : String = assign("YEAR")
    def getProvider : String = assign("PROVIDER")
    def getDataProvider : String = assign("DATAPROVIDER")
    def getLanguage : String = assign("LANGUAGE")
    def getType : DocType = DocType.get(assign("TYPE"))

    @BeanProperty var index : Int = _
    @BeanProperty var fullDocUrl: String = _

    // debug and scoring information
    @BeanProperty var score : Int = _
    @BeanProperty var debugQuery : String = _
}

case class FullDocItem(solrDocument : SolrDocument) extends FullDoc {

    private def assign(key: String) = solrDocument.get(key).asInstanceOf[List[String]].toArray
    private def assignFirst(key: String) = solrDocument.getFirst(key)

    override def getAsArray(key: String) : Array[String] = assign(key)

    override def getAsString(key: String) : String = assignFirst(key)

    override def getFieldValue(key : String) : FieldValue = FieldValue(key, solrDocument)

    override def getFieldValueList() : JList[FieldValue] = solrDocument.getFieldValueList

    override def getFieldValuesFiltered(include: Boolean, fields: Array[String]) : JList[FieldValue] = solrDocument.getFieldValuesFiltered(include, fields.toList)

    // Europeana elements
    override def getId : String = assignFirst("europeana_uri")

    override def getThumbnails : Array[String] = assign("europeana_object") // this is europeanaObject

    override def getEuropeanaIsShownAt : Array[String] = assign("europeana_isShownAt")

    override def getEuropeanaIsShownBy: Array[String] = assign("europeana_isShownBy")

    override def getEuropeanaUserTag: Array[String] = assign("europeana_userTag")

    override def getEuropeanaHasObject : JBoolean = if (assign("europeana_object").isEmpty) false else true

    override def getEuropeanaCountry: Array[String] = assign("europeana_county")

    override def getEuropeanaProvider: Array[String] = assign("europeana_provider")

    override def getEuropeanaDataProvider: Array[String] = assign("europeana_dataProvider")

    override def getEuropeanaSource: Array[String] = assign("europeana_source")

    override def getEuropeanaType: DocType = DocType.get(assignFirst("europeana_type"))

    override def getEuropeanaLanguage: Array[String] = assign("europeana_language") // used to be Language

    override def getEuropeanaYear: Array[String] = assign("europeana_year")

    override def getEuropeanaCollectionName: String = assignFirst("europeana_collectionName")

    override def getEuropeanaCollectionTitle: String = assignFirst("europeana_collectionTitle")

    // here the dcterms namespaces starts
    override def getDcTermsAlternative: Array[String] = assign("dcterms_alternative")

    override def getDcTermsConformsTo: Array[String] = assign("dcterms_conformsTo")

    override def getDcTermsCreated: Array[String] = assign("dcterms_created")

    override def getDcTermsExtent: Array[String] = assign("dcterms_extent")

    override def getDcTermsHasFormat: Array[String] = assign("dcterms_hasFormat")

    override def getDcTermsHasPart: Array[String] = assign("dcterms_hasPart")

    override def getDcTermsHasVersion: Array[String] = assign("dcterms_hasVersion")

    override def getDcTermsIsFormatOf: Array[String] = assign("dcterms_isFormatOf")

    override def getDcTermsIsPartOf: Array[String] = assign("dcterms_isPartOf")

    override def getDcTermsIsReferencedBy: Array[String] = assign("dcterms_isReferencedBy")

    override def getDcTermsIsReplacedBy: Array[String] = assign("dcterms_isReplacedBy")

    override def getDcTermsIsRequiredBy: Array[String] = assign("dcterms_isRequiredBy")

    override def getDcTermsIssued: Array[String] = assign("dcterms_issued")

    override def getDcTermsIsVersionOf: Array[String] = assign("dcterms_isVersionOf")

    override def getDcTermsMedium: Array[String] = assign("dcterms_medium")

    override def getDcTermsProvenance: Array[String] = assign("dcterms_provenance")

    override def getDcTermsReferences: Array[String] = assign("dcterms_references")

    override def getDcTermsReplaces: Array[String] = assign("dcterms_replaces")

    override def getDcTermsRequires: Array[String] = assign("dcterms_requires")

    override def getDcTermsSpatial: Array[String] = assign("dcterms_spatial")

    override def getDcTermsTableOfContents: Array[String] = assign("dcterms_tableOfContents")

    override def getDcTermsTemporal: Array[String] = assign("dcterms_temporal")

    // here the dc namespace starts
    override def getDcContributor: Array[String] = assign("dc_contributor")

    override def getDcCoverage: Array[String] = assign("dc_coverage")

    override def getDcCreator: Array[String] = assign("dc_creator")

    override def getDcDate: Array[String] = assign("dc_date")

    override def getDcDescription: Array[String] = assign("dc_description")

    override def getDcFormat: Array[String] = assign("dc_format")

    override def getDcIdentifier: Array[String] = assign("dc_identifier")

    override def getDcLanguage: Array[String] = assign("dc_language")

    override def getDcPublisher: Array[String] = assign("dc_publisher")

    override def getDcRelation: Array[String] = assign("dc_relation")

    override def getDcRights: Array[String] = assign("dc_rights")

    override def getDcSource: Array[String] = assign("dc_source")

    override def getDcSubject: Array[String] = assign("dc_subject")

    override def getDcTitle: Array[String] = assign("dc_title")

    override def getDcType: Array[String] = assign("dc_type")

    override def getBriefDoc : BriefDoc = BriefDocItem(solrDocument)
}
