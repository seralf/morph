package es.upm.fi.oeg.morph.rdb
import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.junit.JUnitSuite
import org.scalatest.prop.Checkers
import org.junit.Before
import org.junit.Test
import com.hp.hpl.jena.rdf.model.ModelFactory
import es.upm.fi.dia.oeg.morph.R2RProcessor
import java.io.InputStream
import java.util.Properties
import es.upm.fi.oeg.morph.execute.RdfGenerator
import es.upm.fi.oeg.morph.voc.RDFFormat
import es.upm.fi.oeg.morph.r2rml.R2rmlReader
import java.net.URI

class BikeGenerationTest extends JUnitSuite with ShouldMatchersForJUnit with Checkers {
  @Before def initialize() {}
  private def load(fis:InputStream)={
    val props = new Properties
    props.load(fis)    
    fis.close
    props
  }  
  @Test def testGenerate{
    val output=ModelFactory.createDefaultModel()
    //println("output: "+tc.output)
    val props=load(getClass.getClassLoader().getResourceAsStream("config/morph.properties"))

    val r2r=new R2RProcessor
    props.setProperty(R2RProcessor.R2R_MAPPING_URL,"mappings/bikes.ttl");
    r2r.configure(props);
    val reader=new R2rmlReader
    reader.read(new URI("mappings/bikes.ttl"))
    val ds=new RdfGenerator(reader,r2r.relational).generate
    ds.getDefaultModel.write(System.out,RDFFormat.N3)
  }

}