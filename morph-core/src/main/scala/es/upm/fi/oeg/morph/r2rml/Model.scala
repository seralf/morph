package es.upm.fi.oeg.morph.r2rml

import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.datatypes.RDFDatatype
import es.upm.fi.oeg.morph.r2rml.R2rmlUtils._

case class TriplesMap(uri:String,logicalTable:LogicalTable,
    subjectMap:SubjectMap,poMaps:Array[PredicateObjectMap])

case class TermType(tt:Resource){
  def isBlank=tt.equals(R2RML.BlankNode)  
}
object IRIType extends TermType(R2RML.IRI)
object BlankNodeType extends TermType(R2RML.BlankNode)
object LiteralType extends TermType(R2RML.Literal)

abstract class TermMap(val constant:RDFNode,val column:String,
    val template:String,val termType:TermType){
    def allnull(a:Any*)=a.forall(_==null)
}
    
case class SubjectMap(const:RDFNode,col:String,temp:String,term:TermType,
  rdfsClass:Resource,graphMap:GraphMap) extends TermMap(const,col,temp,term){
  override val termType=if (term.tt==null) IRIType else term
  if (termType==LiteralType) 
    throw new R2rmlModelException("Invalid Term Type sor SubjectMap: "+termType)
  if (allnull(constant,column,template))
    throw new R2rmlModelException("SubjectMap template, column or constant must be defined ")
  
  def this(constant:RDFNode,rdfsClass:Resource,graphMap:GraphMap)=
    this(constant,null,null,IRIType,rdfsClass,graphMap)
  def this(template:String,termType:TermType,rdfsClass:Resource,graphMap:GraphMap)=
    this(null,null,template,termType,rdfsClass,graphMap)  
}

case class GraphMap(const:RDFNode,col:String,temp:String) 
  extends TermMap(const,col,temp,IRIType){
  def this(constant:RDFNode)=this(constant,null,null)
  def this(template:String)=this(null,null,template)
}

case class ObjectMap(const:RDFNode,col:String,temp:String,term:TermType,
    lang:String,dtype:RDFDatatype,inv:String)
  extends TermMap(const,col,temp,term)

case class RefObjectMap(parentTriplesMap:String,joinCondition:String) 
  //extends ObjectMap(null,null,null,null,null,null,null)
case class PredicateMap(const:RDFNode,col:String,temp:String)
  extends TermMap(const,col,temp,IRIType){
  if (allnull(const,col,temp)) throw new R2rmlModelException("PredicateObjectMap requires constant, column or template")
  def this(constant:RDFNode)=this(constant,null,null)

}

case class PredicateObjectMap(predicateMap:PredicateMap,objectMap:ObjectMap,
    refObjectMap:RefObjectMap,graphMap:GraphMap){
  private val objId= if (objectMap!=null) extractColumn(objectMap)
    else refObjectMap.parentTriplesMap
  val id=predicateMap.const.asResource.getLocalName+ (if (objId!=null) objId else "")
  
  def this(predicateMap:PredicateMap,objectMap:ObjectMap)=this(predicateMap,objectMap,null,null)
  def this(predicateMap:PredicateMap,refObjectMap:RefObjectMap)=this(predicateMap,null,refObjectMap,null)
}

case class LogicalTable(tableName:String,sqlQuery:String,sqlVersion:RDFNode){
  if (sqlVersion!=null && !exists(sqlVersion.asResource))
    throw new R2rmlModelException("Unsupported SQL Version: "+sqlVersion,null)
  def this(tableName:String)=this(tableName,null,null)
  def exists(version:Resource)= {
   val vers =R2RML.SqlVersion.versions
   !vers.filter( _.getURI.equals(version.getURI))
      .isEmpty 
  }
}


class R2rmlModelException(msg:String,e:Throwable) extends Exception(msg,e){
  def this(msg:String)=this(msg,null)
}
  