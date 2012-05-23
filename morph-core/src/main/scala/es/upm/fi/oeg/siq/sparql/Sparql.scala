package es.upm.fi.oeg.siq.sparql
import com.hp.hpl.jena.graph.Node
import com.hp.hpl.jena.graph.Triple
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock
import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.sparql.syntax.ElementOptional
import collection.JavaConversions._
import com.hp.hpl.jena.sparql.syntax.Element
import com.hp.hpl.jena.sparql.syntax.ElementGroup
import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.rdf.model.Literal
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype._
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import com.hp.hpl.jena.sparql.syntax.ElementUnion

trait Sparql{
  implicit def str2Node(s:String)=Node.createVariable(s)
  implicit def res2Node(r:Resource)=r.asNode
  implicit def prop2Node(p:Property)=p.asNode
  //implicit def tg2etb(e:TripleGraph)=e.tblock
  implicit def query2sp(q:Query)=new QueryPlus(q)
  implicit def tgl2etb(tg:List[Tgp])=new TripleBlock(tg).block
  implicit def tgp2etb(tgp:Tgp)=tgp.tblock
  implicit def optg2etb(tg:OpTgp)=tg.opBlock
  implicit def literal2string(l:Literal)=if (l!=null) l.getString else null
  implicit def sol2extsol(qs:QuerySolution)=ExtendedSolution(qs)
  case class Group(e:Element*){
    val groupelement={
      val g= new ElementGroup
      e.foreach(g.addElement(_))
      g
    }
  }
  
  case class Union(e:Element*){
    val unionElement={
      val u=new ElementUnion
      e.foreach(u.addElement(_))
      u
    }
  }
  
  case class TripleBlock(tg:List[Tgp]){
    lazy val block={
      val tb=new ElementTriplesBlock    
      tg.map(t=>t.tblock.getPattern.getList).flatten.foreach(tb.addTriple(_))
      tb
    }
  }
  
  case class Tgp(s:Node,po:(Node,Node)*){
    lazy val tblock={
      val tb=new ElementTriplesBlock    
      po.map(a=>new Triple(s,a._1,a._2)).foreach(tb.addTriple(_))
      tb
    }
  }  
  case class OpTgp(s:Node,po:(Node,Node)*){    
    lazy val opBlock={
      val e=new ElementTriplesBlock
      po.map(a=>new Triple(s,a._1,a._2)).foreach(e.addTriple(_))
      new ElementOptional(e) 
    }
  }
  
  case class Optional(tg:Tgp*){
    lazy val block={
      val e=new ElementTriplesBlock
      tg.map(t=>t.tblock.getPattern.getList).flatten.foreach(e.addTriple(_))
      new ElementOptional(e)
    }
  }
  /*
  class TriplePattern(val e:ElementTriplesBlock){
    def add(tg:TripleGraph)=tg.list.foreach(e.addTriple(_))
  } */ 
  case class QueryPlus(query:Query){
    //def add(tg:TripleGraph)=Unit
    def addResultVars(vars:String*)=vars.foreach(query.addResultVar(_))
    def addResultVars(vars:Array[String])=vars.foreach(query.addResultVar(_))
  }
  abstract class SparqlQuery(val body:Element)
  
  case class SelectSparqlQuery(override val body:Element,vars:Array[String]) extends SparqlQuery(body){
    private val query=new Query
    query.setQuerySelectType
    private val sp=new QueryPlus(query)      
    sp.addResultVars(vars)
    query.setQueryPattern(body)
    
    def serialize=query.serialize
  }
  /*
  object SelectSparqlQuery {
    def apply(body:Element,vars:Array[String])={
      val query=new Query
      query.setQuerySelectType
      val sp=new SparqlQuery(query)      
      sp.addResultVars(vars)
      query.setQueryPattern(body)
      query
    }
  }*/
  
  case class ExtendedSolution(s:QuerySolution){
  def lit(atr:String)=s.getLiteral(atr)
  def res(atr:String)=s.getResource(atr)
}

}

trait XSDtypes{
   val types=Array(XSDanyURI,XSDstring,XSDpositiveInteger,XSDint,XSDdecimal,XSDdouble,XSDdateTime)
   def toDatatype(uri:String)={
      val dtype=types.find(t=>t.getURI.equals(uri))
      dtype.getOrElse(XSDanyURI)
   }
   def toDatatype(res:Resource):XSDDatatype={
     if (res==null) null
     else toDatatype(res.getURI)
   }
}