package es.upm.fi.oeg.morph.tc
import org.junit.Before
import org.junit.Test
import org.junit.Ignore

class D019Test extends R2RMLTest("D019-1table1primarykey3columns3rows") {

  @Before def initialize() {}
   
  @Test def testTC019a{
	val tc=suit.getTc("R2RMLTC0019a")	
	val ds=suit.testTc(tc.get)
	val dg=ds.asDatasetGraph
	dg.getDefaultGraph.size should be (2)
  }
  @Test def testTC019b{
	val tc=suit.getTc("R2RMLTC0019b")	
	val ds=suit.testTc(tc.get)
	val dg=ds.asDatasetGraph
	dg.getDefaultGraph.size should be (3)
  }


}